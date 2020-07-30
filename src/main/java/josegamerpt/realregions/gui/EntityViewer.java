package josegamerpt.realregions.gui;

import josegamerpt.realregions.RealRegions;
import josegamerpt.realregions.classes.*;
import josegamerpt.realregions.utils.Itens;
import josegamerpt.realregions.utils.Pagination;
import josegamerpt.realregions.utils.PlayerInput;
import josegamerpt.realregions.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
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

import java.util.*;

public class EntityViewer {

    private static Map<UUID, EntityViewer> inventories = new HashMap<>();
    private Inventory inv;

    static ItemStack placeholder = Itens.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    static ItemStack next = Itens.createItem(Material.GREEN_STAINED_GLASS, 1, "&aNext",
            Arrays.asList("&fClick here to go to the next page."));
    static ItemStack back = Itens.createItem(Material.YELLOW_STAINED_GLASS, 1, "&6Back",
            Arrays.asList("&fClick here to go back to the next page."));
    static ItemStack close = Itens.createItem(Material.ACACIA_DOOR, 1, "&cGo Back",
            Arrays.asList("&fClick here to go back."));
    static ItemStack search = Itens.createItem(Material.SIGN, 1, "&9Search",
            Arrays.asList("&fClick here to search for a entity."));

    private UUID uuid;
    private ArrayList<EntityIcon> eicon;
    private HashMap<Integer, EntityIcon> display = new HashMap<Integer, EntityIcon>();

    int pageNumber = 0;
    Pagination<EntityIcon> p;
    private RWorld r;

    public EntityViewer(Player pl, RWorld r) {
        inv = Bukkit.getServer().createInventory(null, 54, Text.color(r.getName() + " &8| Entities"));
        this.uuid = pl.getUniqueId();
        this.r = r;
        eicon = getEnts();

        p = new Pagination<EntityIcon>(28, eicon);
        fillChest(p.getPage(pageNumber));

        this.register();
    }

    public EntityViewer(Player pl, RWorld r, String search) {
        inv = Bukkit.getServer().createInventory(null, 54, Text.color(r.getName() + " &8| Search for " + search));
        this.uuid = pl.getUniqueId();
        this.r = r;
        eicon = searchEntity(search);

        p = new Pagination<EntityIcon>(28, eicon);
        fillChest(p.getPage(pageNumber));

        this.register();
    }

    private ArrayList<EntityIcon> getEnts() {
        ArrayList<EntityIcon> ms = new ArrayList<EntityIcon>();
        this.r.getWorld().getEntities().forEach(entity -> ms.add(new EntityIcon(entity)));
        return ms;
    }

    private ArrayList<EntityIcon> searchEntity(String s) {
        ArrayList<EntityIcon> ms = new ArrayList<EntityIcon>();
        for (EntityIcon e : getEnts()) {
            if (e.getEntityName().toLowerCase().contains(s.toLowerCase())) {
                ms.add(e);
            }
        }
        return ms;
    }

    public void fillChest(List<EntityIcon> items) {
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
                    EntityIcon e = items.get(0);
                    inv.setItem(slot, e.getIcon());
                    display.put(slot, e);
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
                        EntityViewer current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        Player gp = (Player) clicker;

                        e.setCancelled(true);

                        if (e.getRawSlot() == 4) {
                            new PlayerInput(gp, new PlayerInput.InputRunnable() {
                                @Override
                                public void run(String input) {
                                    if (current.searchEntity(input).size() == 0) {
                                        Text.send(gp, "&fNothing found for your search terms.");

                                        current.exit(gp);
                                        return;
                                    }
                                    EntityViewer df = new EntityViewer(gp, current.r, input);
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
                            EntityIcon a = current.display.get(e.getRawSlot());
                            gp.closeInventory();
                            gp.playSound(gp.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
                            gp.teleport(a.getEntity().getLocation());
                        }
                    }
                }
            }

            private void backPage(EntityViewer asd) {
                if (asd.p.exists(asd.pageNumber - 1)) {
                    asd.pageNumber--;
                }

                asd.fillChest(asd.p.getPage(asd.pageNumber));
            }

            private void nextPage(EntityViewer asd) {
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
        p.closeInventory();
        WorldGUI v = new WorldGUI(p, this.r);
        v.openInventory(p);
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