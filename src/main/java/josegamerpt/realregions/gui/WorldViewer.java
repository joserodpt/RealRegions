package josegamerpt.realregions.gui;

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

import josegamerpt.realregions.RealRegions;
import josegamerpt.realregions.regions.RWorld;
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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WorldViewer {

    private static Map<UUID, WorldViewer> inventories = new HashMap<>();
    private Inventory inv;

    private ItemStack placeholder = Itens.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    private ItemStack next = Itens.createItem(Material.GREEN_STAINED_GLASS, 1, "&aNext",
            Collections.singletonList("&fClick here to go to the next page."));
    private ItemStack back = Itens.createItem(Material.YELLOW_STAINED_GLASS, 1, "&6Back",
            Collections.singletonList("&fClick here to go back to the next page."));
    private ItemStack close = Itens.createItem(Material.ACACIA_DOOR, 1, "&cGo Back",
            Collections.singletonList("&fClick here to close this menu."));

    private UUID uuid;
    private HashMap<Integer, RWorld> display = new HashMap<>();

    int pageNumber = 0;
    Pagination<RWorld> p;

    public enum WorldSort { SIZE, TIME }
    private WorldSort ws;
    private RealRegions rr;

    public WorldViewer(Player pl, WorldSort ws, RealRegions rr) {
        this.rr = rr;
        this.ws = ws;
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color("&8Real&eRegions &8| Worlds"));
        this.uuid = pl.getUniqueId();
        List<RWorld> worlds = rr.getWorldManager().getWorlds();

        switch (ws) {
            case TIME:
                worlds.sort(Comparator.comparingInt(world -> world.getConfig().getInt("Settings.Unix-Register")));
                break;
            case SIZE:
                worlds.sort(Comparator.comparingDouble(RWorld::getWorldSizeMB));
                break;
        }

        this.p = new Pagination<>(28, worlds);
        fillChest(p.getPage(this.pageNumber), ws);

        this.register();
    }

    public void fillChest(List<RWorld> items, WorldSort ws) {
        this.inv.clear();
        this.display.clear();

        for (int i = 0; i < 9; ++i) {
            this.inv.setItem(i, placeholder);
        }

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
                    RWorld e = items.get(0);
                    this.inv.setItem(slot, e.getItem());
                    this.display.put(slot, e);
                    items.remove(0);
                }
            }
            slot++;
        }

        switch (ws)
        {
            case SIZE:
                this.inv.setItem(47, Itens.createItem(Material.CHEST, 1, "&fSorted by &aSize", Collections.singletonList("&fClick here to sort by &bRegistration Date")));
                break;
            case TIME:
                this.inv.setItem(47, Itens.createItem(Material.CLOCK, 1, "&fSorted by &aRegistration Date", Collections.singletonList("&fClick here to sort by &bSize")));
                break;
        }

        this.inv.setItem(49, close);

        this.inv.setItem(51, Itens.createItem(Material.CRAFTING_TABLE, 1, "&fCreate a New World"));
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
                        WorldViewer current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        Player p = (Player) clicker;

                        e.setCancelled(true);

                        switch (e.getRawSlot())
                        {
                            case 47:
                                p.closeInventory();
                                switch (current.ws) {
                                    case TIME:
                                        new BukkitRunnable()
                                        {
                                            public void run()
                                            {
                                                WorldViewer v = new WorldViewer(p, WorldSort.SIZE, current.rr);
                                                v.openInventory(p);
                                            }
                                        }.runTaskLater(current.rr, 2);
                                        break;
                                    case SIZE:
                                        new BukkitRunnable()
                                        {
                                            public void run()
                                            {
                                                WorldViewer v = new WorldViewer(p, WorldSort.TIME, current.rr);
                                                v.openInventory(p);
                                            }
                                        }.runTaskLater(current.rr, 2);
                                        break;
                                }
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
                            case 51:
                                new PlayerInput(p, input -> current.rr.getWorldManager().createWorld(p, input, RWorld.WorldType.NORMAL), input -> {
                                    WorldViewer wv = new WorldViewer(p, current.ws, current.rr);
                                    wv.openInventory(p);
                                });
                        }

                        if (current.display.containsKey(e.getRawSlot())) {
                            RWorld a = current.display.get(e.getRawSlot());
                            switch (e.getClick())
                            {
                                case RIGHT:
                                    p.closeInventory();
                                    a.teleport(p, false);
                                    break;
                                case MIDDLE:
                                    p.closeInventory();
                                    new BukkitRunnable()
                                    {
                                        public void run()
                                        {
                                            MaterialPicker mp = new MaterialPicker(a, p, MaterialPicker.PickType.ICON_WORLD, current.rr);
                                            mp.openInventory(p);
                                        }
                                    }.runTaskLater(current.rr, 2);
                                    break;
                                default:
                                    p.closeInventory();
                                    new BukkitRunnable()
                                    {
                                        public void run()
                                        {
                                            WorldGUI v = new WorldGUI(p, a, current.rr);
                                            v.openInventory(p);
                                        }
                                    }.runTaskLater(current.rr, 2);
                                    break;
                            }
                        }
                    }
                }
            }

            private void backPage(WorldViewer asd) {
                if (asd.p.exists(asd.pageNumber - 1)) {
                    asd.pageNumber--;
                }

                asd.fillChest(asd.p.getPage(asd.pageNumber), asd.ws);
            }

            private void nextPage(WorldViewer asd) {
                if (asd.p.exists(asd.pageNumber + 1)) {
                    asd.pageNumber++;
                }

                asd.fillChest(asd.p.getPage(asd.pageNumber), asd.ws);
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