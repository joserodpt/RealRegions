package joserodpt.realregions.gui;

/*
 *  ______           _______           
 *  | ___ \         | | ___ \         (_)
 *  | |_/ /___  __ _| | |_/ /___  __ _ _  ___  _ __  ___
 *  |    // _ \/ _` | |    // _ \/ _` | |/ _ \| '_ \/ __|
 *  | |\ \  __/ (_| | | |\ \  __/ (_| | | (_) | | | \__ \
 *  \_| \_\___|\__,_|_\_| \_\___|\__, |_|\___/|_| |_|___/
 *                                __/ |
 *                               |___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealRegions
 */

import joserodpt.realregions.RealRegions;
import joserodpt.realregions.config.Language;
import joserodpt.realregions.regions.RWorld;
import joserodpt.realregions.regions.CuboidRegion;
import joserodpt.realregions.regions.Region;
import joserodpt.realregions.utils.Itens;
import joserodpt.realregions.utils.Pagination;
import joserodpt.realregions.utils.PlayerInput;
import joserodpt.realregions.utils.Text;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class MaterialPicker {

    public enum PickType {
        ICON_WORLD, ICON_REG
    }

    private static Map<UUID, MaterialPicker> inventories = new HashMap<>();
    private Inventory inv;

    private ItemStack placeholder = Itens.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    private ItemStack next = Itens.createItem(Material.GREEN_STAINED_GLASS, 1, "&aNext",
            Collections.singletonList("&fClick here to go to the next page."));
    private ItemStack back = Itens.createItem(Material.YELLOW_STAINED_GLASS, 1, "&6Back",
            Collections.singletonList("&fClick here to go back to the next page."));
    private ItemStack close = Itens.createItem(Material.ACACIA_DOOR, 1, "&cGo Back",
            Collections.singletonList("&fClick here to go back."));
    private ItemStack search = Itens.createItem(Material.OAK_SIGN, 1, "&9Search",
            Collections.singletonList("&fClick here to search for a block."));

    private UUID uuid;
    private List<Material> items;
    private HashMap<Integer, Material> display = new HashMap<>();

    int pageNumber = 0;
    Pagination<Material> p;
    private Object min;
    private PickType pt;
    private RealRegions rr;

    public MaterialPicker(Object m, Player pl, PickType block, RealRegions rr) {
        this.rr = rr;
        this.uuid = pl.getUniqueId();
        this.min = m;
        this.pt = block;

        switch (block) {
            case ICON_REG:
                inv = Bukkit.getServer().createInventory(null, 54, Text.color("Select icon for " + ((Region) m).getDisplayName()));
                break;
            case ICON_WORLD:
                inv = Bukkit.getServer().createInventory(null, 54, Text.color("Select icon for " + ((RWorld) m).getRWorldName()));
                break;
        }

        this.items = getIcons();

        this.p = new Pagination<>(28, this.items);
        fillChest(this.p.getPage(this.pageNumber));

        this.register();
    }

    public MaterialPicker(Object m, Player pl, PickType block, String search, RealRegions rr) {
        this.rr = rr;
        this.uuid = pl.getUniqueId();
        this.min = m;
        this.pt = block;
        switch (block)
        {
            case ICON_REG:
                inv = Bukkit.getServer().createInventory(null, 54, Text.color("Select icon for " + ((Region) m).getDisplayName()));
                break;
            case ICON_WORLD:
                inv = Bukkit.getServer().createInventory(null, 54, Text.color("Select icon for " + ((RWorld) m).getRWorldName()));
                break;
        }

        this.items = searchMaterial(search);
        this.p = new Pagination<>(28, this.items);
        fillChest(this.p.getPage(this.pageNumber));

        this.register();
    }

    private List<Material> getIcons() {
        return Arrays.stream(Material.values())
                .filter(m -> !m.equals(Material.AIR) && m.isSolid() && m.isBlock() && m.isItem())
                .collect(Collectors.toList());
    }

    private List<Material> searchMaterial(String s) {
        return getIcons().stream()
                .filter(m -> m.name().toLowerCase().contains(s.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void fillChest(List<Material> items) {

        this.inv.clear();
        this.display.clear();

        for (int i = 0; i < 9; ++i) {
            this.inv.setItem(i, placeholder);
        }

        this.inv.setItem(4, search);

        this.inv.setItem(45, placeholder);
        this.inv.setItem(46, placeholder);
        this.inv.setItem(47, placeholder);
        this.inv.setItem(48, placeholder);
        this.inv.setItem(49, placeholder);
        this.inv.setItem(50, placeholder);
        this.inv.setItem(51, placeholder);
        this.inv.setItem(52, placeholder);
        this.inv.setItem(53, placeholder);
        this.inv.setItem(36, placeholder);
        this.inv.setItem(44, placeholder);
        this.inv.setItem(9, placeholder);
        this.inv.setItem(17, placeholder);

        if (firstPage()) {
            this.inv.setItem(18, placeholder);
            this.inv.setItem(27, placeholder);
        } else {
            this.inv.setItem(18, back);
            this.inv.setItem(27, back);
        }

        if (lastPage()) {
            this.inv.setItem(26, placeholder);
            this.inv.setItem(35, placeholder);
        } else {
            this.inv.setItem(26, next);
            this.inv.setItem(35, next);
        }

        int slot = 0;
        for (ItemStack i : inv.getContents()) {
            if (i == null) {
                if (!items.isEmpty()) {
                    Material s = items.get(0);
                    this.inv.setItem(slot,
                            Itens.createItem(s, 1, "§f" + s.name(), Collections.singletonList("&fClick to pick this.")));
                    this.display.put(slot, s);
                    items.remove(0);
                }
            }
            slot++;
        }

        this.inv.setItem(49, close);
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

                        switch (e.getRawSlot())
                        {
                            case 4:
                                new PlayerInput(gp, input -> {
                                    if (current.searchMaterial(input).isEmpty()) {
                                        //Text.send(gp, "&fNothing found for your search terms.");
                                        Text.send(gp, Language.file().getString("Search.No-Results"));
                                        current.exit(gp, current.rr);
                                        return;
                                    }
                                    MaterialPicker df = new MaterialPicker(current.min, gp, current.pt, input, current.rr);
                                    df.openInventory(gp);
                                }, input -> {
                                    gp.closeInventory();
                                    WorldViewer wv = new WorldViewer(gp, WorldViewer.WorldSort.TIME, current.rr);
                                    wv.openInventory(gp);
                                });
                                break;
                            case 49:
                                current.exit(gp, current.rr);
                                break;
                            case 26:
                            case 35:
                                nextPage(current);
                                gp.playSound(gp.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
                                break;
                            case 18:
                            case 27:
                                backPage(current);
                                gp.playSound(gp.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
                                break;
                        }

                        if (current.display.containsKey(e.getRawSlot())) {
                            Material a = current.display.get(e.getRawSlot());
                            if (current.pt.equals(PickType.ICON_REG)) {
                                gp.closeInventory();
                                Region r = ((Region) current.min);
                                r.setIcon(a);
                                r.saveData(Region.RegionData.ICON);
                                WorldGUI v = new WorldGUI(gp, r.getRWorld(), current.rr);
                                v.openInventory(gp);
                            }
                            if (current.pt.equals(PickType.ICON_WORLD)) {
                                gp.closeInventory();
                                RWorld r = ((RWorld) current.min);
                                r.setIcon(a);
                                r.saveData(RWorld.Data.ICON);
                                WorldViewer v = new WorldViewer(gp, WorldViewer.WorldSort.TIME, current.rr);
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

    private boolean lastPage() {
        return pageNumber == (p.totalPages() - 1);
    }

    private boolean firstPage() {
        return pageNumber == 0;
    }

    protected void exit(Player p, RealRegions rr) {
        p.closeInventory();
        switch (this.pt)
        {
            case ICON_WORLD:
                new BukkitRunnable() {
                    public void run() {
                        WorldViewer wv = new WorldViewer(p, WorldViewer.WorldSort.TIME, rr);
                        wv.openInventory(p);
                    }
                }.runTaskLater(RealRegions.getPlugin(), 2);
                break;
            case ICON_REG:
                new BukkitRunnable() {
                    public void run() {
                        WorldGUI v = new WorldGUI(p, ((CuboidRegion) min).getRWorld(), rr);
                        v.openInventory(p);
                    }
                }.runTaskLater(RealRegions.getPlugin(), 2);
                break;
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