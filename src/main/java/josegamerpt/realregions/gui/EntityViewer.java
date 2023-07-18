package josegamerpt.realregions.gui;

import josegamerpt.realregions.classes.*;
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

import java.util.*;

public class EntityViewer {

    private static Map<UUID, EntityViewer> inventories = new HashMap<>();
    private Inventory inv;

    static ItemStack placeholder = Itens.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    static ItemStack next = Itens.createItem(Material.GREEN_STAINED_GLASS, 1, "&aNext",
            Collections.singletonList("&fClick here to go to the next page."));
    static ItemStack back = Itens.createItem(Material.YELLOW_STAINED_GLASS, 1, "&6Back",
            Collections.singletonList("&fClick here to go back to the next page."));
    static ItemStack close = Itens.createItem(Material.ACACIA_DOOR, 1, "&cGo Back",
            Collections.singletonList("&fClick here to go back."));
    static ItemStack search = Itens.createItem(Material.OAK_SIGN, 1, "&9Search",
            Collections.singletonList("&fClick here to search for a entity."));

    private UUID uuid;
    private ArrayList<EntityIcon> eicon;
    private HashMap<Integer, EntityIcon> display = new HashMap<>();

    int pageNumber = 0;
    Pagination<EntityIcon> p;
    private RWorld r;

    public EntityViewer(Player pl, RWorld r) {
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color(r.getName() + " &8| Entities"));
        this.uuid = pl.getUniqueId();
        this.r = r;
        this.eicon = getEnts();

        this.p = new Pagination<>(28, this.eicon);
        fillChest(this.p.getPage(this.pageNumber));

        this.register();
    }

    public EntityViewer(Player pl, RWorld r, String search) {
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color(r.getName() + " &8| Search for " + search));
        this.uuid = pl.getUniqueId();
        this.r = r;
        this.eicon = searchEntity(search);

        this.p = new Pagination<>(28, this.eicon);
        fillChest(p.getPage(this.pageNumber));

        this.register();
    }

    private ArrayList<EntityIcon> getEnts() {
        ArrayList<EntityIcon> ms = new ArrayList<>();
        this.r.getWorld().getEntities().forEach(entity -> ms.add(new EntityIcon(entity)));
        return ms;
    }

    private ArrayList<EntityIcon> searchEntity(String s) {
        ArrayList<EntityIcon> ms = new ArrayList<>();
        for (EntityIcon e : getEnts()) {
            if (e.getEntityName().toLowerCase().contains(s.toLowerCase())) {
                ms.add(e);
            }
        }
        return ms;
    }

    public void fillChest(List<EntityIcon> items) {
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
        for (ItemStack i : this.inv.getContents()) {
            if (i == null) {
                if (items.size() != 0) {
                    EntityIcon e = items.get(0);
                    this.inv.setItem(slot, e.getIcon());
                    this.display.put(slot, e);
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
                        EntityViewer current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        Player p = (Player) clicker;

                        e.setCancelled(true);

                        switch (e.getRawSlot())
                        {
                            case 4:
                                new PlayerInput(p, input -> {
                                    if (current.searchEntity(input).size() == 0) {
                                        Text.send(p, "&fNothing found for your search terms.");

                                        current.exit(p);
                                        return;
                                    }
                                    EntityViewer df = new EntityViewer(p, current.r, input);
                                    df.openInventory(p);
                                }, input -> {
                                    p.closeInventory();
                                    WorldViewer wv = new WorldViewer(p);
                                    wv.openInventory(p);
                                });
                                break;
                            case 49:
                                current.exit(p);
                                break;
                            case 26:
                            case 35:
                                nextPage(current);
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
                                break;
                            case 18:
                            case 27:
                                backPage(current);
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
                                break;
                        }

                        if (current.display.containsKey(e.getRawSlot())) {
                            EntityIcon a = current.display.get(e.getRawSlot());
                            p.closeInventory();
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
                            p.teleport(a.getEntity().getLocation());
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

    private boolean lastPage() {
        return pageNumber == (p.totalPages() - 1);
    }

    private boolean firstPage() {
        return pageNumber == 0;
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