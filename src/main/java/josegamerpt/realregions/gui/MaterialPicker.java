package josegamerpt.realregions.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import josegamerpt.realregions.RealRegions;
import josegamerpt.realregions.classes.Data;
import josegamerpt.realregions.classes.PickType;
import josegamerpt.realregions.classes.RWorld;
import josegamerpt.realregions.classes.Region;
import josegamerpt.realregions.utils.Itens;
import josegamerpt.realregions.utils.Pagination;
import josegamerpt.realregions.utils.PlayerInput;
import josegamerpt.realregions.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class MaterialPicker {

    private static Map<UUID, MaterialPicker> inventories = new HashMap<>();
    private Inventory inv;

    static ItemStack placeholder = Itens.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    static ItemStack next = Itens.createItem(Material.GREEN_STAINED_GLASS, 1, "&aNext",
            Arrays.asList("&fClick here to go to the next page."));
    static ItemStack back = Itens.createItem(Material.YELLOW_STAINED_GLASS, 1, "&6Back",
            Arrays.asList("&fClick here to go back to the next page."));
    static ItemStack close = Itens.createItem(Material.ACACIA_DOOR, 1, "&cGo Back",
            Arrays.asList("&fClick here to go back."));
    static ItemStack search = Itens.createItem(Material.SIGN, 1, "&9Search",
            Arrays.asList("&fClick here to search for a block."));

    private UUID uuid;
    private ArrayList<Material> items;
    private HashMap<Integer, Material> display = new HashMap<Integer, Material>();

    int pageNumber = 0;
    Pagination<Material> p;
    private Object min;
    private PickType pt;

    public MaterialPicker(Object m, Player pl, PickType block) {
        this.uuid = pl.getUniqueId();
        this.min = m;
        this.pt = block;
        if (block.equals(PickType.ICON_REG)) {
            inv = Bukkit.getServer().createInventory(null, 54, Text.color("Select icon for " + ((Region) m).getDisplayName()));
        }
        if (block.equals(PickType.ICON_WORLD)) {
            inv = Bukkit.getServer().createInventory(null, 54, Text.color("Select icon for " + ((RWorld) m).getName()));
        }

        items = getIcons();

        p = new Pagination<Material>(28, items);
        fillChest(p.getPage(pageNumber));

        this.register();
    }

    public MaterialPicker(Object m, Player pl, PickType block, String search) {
        this.uuid = pl.getUniqueId();
        this.min = m;
        this.pt = block;
        if (block.equals(PickType.ICON_REG)) {
            inv = Bukkit.getServer().createInventory(null, 54, Text.color("Select icon for " + ((Region) m).getDisplayName()));
        }
        if (block.equals(PickType.ICON_WORLD)) {
            inv = Bukkit.getServer().createInventory(null, 54, Text.color("Select icon for " + ((RWorld) m).getName()));
        }

        items = searchMaterial(search);

        p = new Pagination<Material>(28, items);
        fillChest(p.getPage(pageNumber));

        this.register();
    }

    private ArrayList<Material> getIcons() {
        ArrayList<Material> ms = new ArrayList<Material>();
        for (Material m : Material.values()) {
            if (!m.equals(Material.AIR) && m.isSolid() && m.isBlock() && m.isItem()) {
                ms.add(m);
            }
        }
        return ms;
    }

    private ArrayList<Material> searchMaterial(String s) {
        ArrayList<Material> ms = new ArrayList<Material>();
        for (Material m : getIcons()) {
            if (m.name().toLowerCase().contains(s.toLowerCase())) {
                ms.add(m);
            }
        }
        return ms;
    }

    public void fillChest(List<Material> items) {

        inv.clear();
        display.clear();

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, placeholder);
        }

        inv.setItem(4, search);

        inv.setItem(45, placeholder);
        inv.setItem(46, placeholder);
        inv.setItem(47, placeholder);
        inv.setItem(48, placeholder);
        inv.setItem(49, placeholder);
        inv.setItem(50, placeholder);
        inv.setItem(51, placeholder);
        inv.setItem(52, placeholder);
        inv.setItem(53, placeholder);
        inv.setItem(36, placeholder);
        inv.setItem(44, placeholder);
        inv.setItem(9, placeholder);
        inv.setItem(17, placeholder);

        inv.setItem(18, back);
        inv.setItem(27, back);
        inv.setItem(26, next);
        inv.setItem(35, next);

        int slot = 0;
        for (ItemStack i : inv.getContents()) {
            if (i == null) {
                if (items.size() != 0) {
                    Material s = items.get(0);
                    inv.setItem(slot,
                            Itens.createItem(s, 1, "§f" + s.name(), Arrays.asList("&fClick to pick this.")));
                    display.put(slot, s);
                    items.remove(0);
                }
            }
            slot++;
        }

        inv.setItem(49, close);
    }

    public void openInventory(Player target) {
        Inventory inv = getInventory();
        InventoryView openInv = target.getOpenInventory();
        if (openInv != null) {
            Inventory openTop = target.getOpenInventory().getTopInventory();
            if (openTop != null && openTop.getType().name().equalsIgnoreCase(inv.getType().name())) {
                openTop.setContents(inv.getContents());
            } else {
                target.openInventory(inv);
            }
        }
    }

    public static Listener getListener() {
        return new Listener() {
            @EventHandler
            public void onClick(InventoryClickEvent e) {
                HumanEntity clicker = e.getWhoClicked();
                if (clicker instanceof Player) {
                    if (e.getCurrentItem() == null) {
                        return;
                    }
                    UUID uuid = clicker.getUniqueId();
                    if (inventories.containsKey(uuid)) {
                        MaterialPicker current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        Player gp = (Player) clicker;

                        e.setCancelled(true);

                        if (e.getRawSlot() == 4) {
                            new PlayerInput(gp, new PlayerInput.InputRunnable() {
                                @Override
                                public void run(String input) {
                                    if (current.searchMaterial(input).size() == 0) {
                                        Text.send(gp, "&fNothing found for your search terms.");

                                        current.exit(gp);
                                        return;
                                    }
                                    MaterialPicker df = new MaterialPicker(current.min, gp, current.pt, input);
                                    df.openInventory(gp);
                                }
                            }, new PlayerInput.InputRunnable() {
                                @Override
                                public void run(String input) {
                                    gp.closeInventory();
                                    WorldViewer wv = new WorldViewer(gp);
                                    wv.openInventory(gp);
                                }
                            });
                        }

                        if (e.getRawSlot() == 49) {
                            current.exit(gp);
                        }

                        if (e.getRawSlot() == 26 || e.getRawSlot() == 35) {
                            nextPage(current);
                            gp.playSound(gp.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
                        }
                        if (e.getRawSlot() == 18 || e.getRawSlot() == 27) {
                            backPage(current);
                            gp.playSound(gp.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
                        }

                        if (current.display.containsKey(e.getRawSlot())) {
                            Material a = current.display.get(e.getRawSlot());
                            if (current.pt.equals(PickType.ICON_REG)) {
                                gp.closeInventory();
                                Region r = ((Region) current.min);
                                r.setIcon(a);
                                r.saveData(Data.Region.ICON);
                                WorldGUI v = new WorldGUI(gp, r.getWorld());
                                v.openInventory(gp);
                            }
                            if (current.pt.equals(PickType.ICON_WORLD)) {
                                gp.closeInventory();
                                RWorld r = ((RWorld) current.min);
                                r.setIcon(a);
                                r.saveData(Data.World.ICON);
                                WorldViewer v = new WorldViewer(gp);
                                v.openInventory(gp);
                            }
                        }
                    }
                }
            }

            private void backPage(MaterialPicker asd) {
                if (asd.p.exists(asd.pageNumber - 1)) {
                    asd.pageNumber--;
                }

                asd.fillChest(asd.p.getPage(asd.pageNumber));
            }

            private void nextPage(MaterialPicker asd) {
                if (asd.p.exists(asd.pageNumber + 1)) {
                    asd.pageNumber++;
                }

                asd.fillChest(asd.p.getPage(asd.pageNumber));
            }

            @EventHandler
            public void onClose(InventoryCloseEvent e) {
                if (e.getPlayer() instanceof Player) {
                    if (e.getInventory() == null) {
                        return;
                    }
                    Player p = (Player) e.getPlayer();
                    UUID uuid = p.getUniqueId();
                    if (inventories.containsKey(uuid)) {
                        inventories.get(uuid).unregister();
                    }
                }
            }
        };
    }

    protected void exit(Player p) {
        if (this.pt.equals(PickType.ICON_WORLD)) {
            p.closeInventory();
            new BukkitRunnable() {
                public void run() {
                    WorldViewer wv = new WorldViewer(p);
                    wv.openInventory(p);
                }
            }.runTaskLater(RealRegions.getPL(), 2);
        }
        if (this.pt.equals(PickType.ICON_REG)) {
            p.closeInventory();
            new BukkitRunnable() {
                public void run() {
                    WorldGUI v = new WorldGUI(p, ((Region) min).getWorld());
                    v.openInventory(p);
                }
            }.runTaskLater(RealRegions.getPL(), 2);
        }
    }

    public Inventory getInventory() {
        return inv;
    }

    private void register() {
        inventories.put(this.uuid, this);
    }

    private void unregister() {
        inventories.remove(this.uuid);
    }
}