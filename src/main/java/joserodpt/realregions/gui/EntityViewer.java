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
import joserodpt.realregions.utils.Itens;
import joserodpt.realregions.utils.Pagination;
import joserodpt.realregions.utils.PlayerInput;
import joserodpt.realregions.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class EntityViewer {

    private static Map<UUID, EntityViewer> inventories = new HashMap<>();
    private Inventory inv;

    private ItemStack placeholder = Itens.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    private ItemStack next = Itens.createItem(Material.GREEN_STAINED_GLASS, 1, "&aNext",
            Collections.singletonList("&fClick here to go to the next page."));
    private ItemStack back = Itens.createItem(Material.YELLOW_STAINED_GLASS, 1, "&6Back",
            Collections.singletonList("&fClick here to go back to the next page."));
    private ItemStack close = Itens.createItem(Material.ACACIA_DOOR, 1, "&cGo Back",
            Collections.singletonList("&fClick here to go back."));
    private ItemStack search = Itens.createItem(Material.OAK_SIGN, 1, "&9Search",
            Collections.singletonList("&fClick here to search for a entity."));

    private UUID uuid;
    private ArrayList<EntityIcon> eicon;
    private HashMap<Integer, EntityIcon> display = new HashMap<>();

    int pageNumber = 0;
    private Pagination<EntityIcon> p;
    private RWorld r;
    private RealRegions rr;

    public EntityViewer(Player pl, RWorld r, RealRegions rr) {
        this.rr = rr;
        this.r = r;

        if (!r.isLoaded()) {
            return;
        }

        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color(r.getRWorldName() + " &8| Entities"));
        this.uuid = pl.getUniqueId();
        this.eicon = getEnts(pl);

        this.p = new Pagination<>(28, this.eicon);
        fillChest(this.p.getPage(this.pageNumber));

        this.register();
    }

    public EntityViewer(Player pl, RWorld r, String search, RealRegions rr) {
        this.rr = rr;
        this.r = r;

        if (!r.isLoaded()) {
            return;
        }

        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color(r.getRWorldName() + " &8| Search for " + search));
        this.uuid = pl.getUniqueId();
        this.eicon = searchEntity(pl, search);

        this.p = new Pagination<>(28, this.eicon);
        fillChest(p.getPage(this.pageNumber));

        this.register();
    }

    public EntityViewer(Player pl, RWorld r, EntityType e, RealRegions rr) {
        this.rr = rr;
        this.r = r;

        if (!r.isLoaded()) {
            return;
        }

        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color(r.getRWorldName() + " &8| Players"));
        this.uuid = pl.getUniqueId();
        this.eicon = searchEntity(pl, e);

        this.p = new Pagination<>(28, this.eicon);
        fillChest(p.getPage(this.pageNumber));

        this.register();
    }

    private ArrayList<EntityIcon> getEnts(Player p) {
        return this.r.getWorld().getEntities().stream()
                .map(entity -> new EntityIcon(p, entity))
                .sorted(Comparator.comparingDouble(EntityIcon::getDistanceRelativeToPlayer))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private ArrayList<EntityIcon> searchEntity(Player p, String s) {
        return getEnts(p).stream()
                .filter(e -> e.getEntityName().toLowerCase().contains(s.toLowerCase()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private ArrayList<EntityIcon> searchEntity(Player p, EntityType search) {
        return getEnts(p).stream()
                .filter(e -> e.getEntity().getType() == search)
                .collect(Collectors.toCollection(ArrayList::new));
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
                if (!items.isEmpty()) {
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
        if (!r.isLoaded()) {
            Text.send(target, Language.file().getString("Menu.Unloaded-World"));
            return;
        }

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
                                    if (current.searchEntity(p, input).isEmpty()) {
                                        Text.send(p, Language.file().getString("Search.No-Results"));

                                        current.exit(p, current.rr);
                                        return;
                                    }
                                    EntityViewer df = new EntityViewer(p, current.r, input, current.rr);
                                    df.openInventory(p);
                                }, input -> {
                                    p.closeInventory();
                                    WorldViewer wv = new WorldViewer(p, WorldViewer.WorldSort.TIME, current.rr);
                                    wv.openInventory(p);
                                });
                                break;
                            case 49:
                                current.exit(p, current.rr);
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

    protected void exit(Player p, RealRegions rr) {
        p.closeInventory();
        WorldGUI v = new WorldGUI(p, this.r, rr);
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