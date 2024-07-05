package joserodpt.realregions.plugin.gui;

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

import joserodpt.realregions.api.RealRegionsAPI;
import joserodpt.realregions.api.regions.RWorld;
import joserodpt.realregions.api.utils.Itens;
import joserodpt.realregions.api.utils.Pagination;
import joserodpt.realregions.api.utils.PlayerInput;
import joserodpt.realregions.api.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WorldsListGUI {

    private static Map<UUID, WorldsListGUI> inventories = new HashMap<>();
    private Inventory inv;

    private final ItemStack placeholder = Itens.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    private final ItemStack next = Itens.createItem(Material.GREEN_STAINED_GLASS, 1, "&aNext",
            Collections.singletonList("&fClick here to go to the next page."));
    private final ItemStack back = Itens.createItem(Material.YELLOW_STAINED_GLASS, 1, "&6Back",
            Collections.singletonList("&fClick here to go back to the next page."));
    private final ItemStack close = Itens.createItem(Material.ACACIA_DOOR, 1, "&cGo Back",
            Collections.singletonList("&fClick here to close this menu."));

    private UUID uuid;
    private HashMap<Integer, RWorld> display = new HashMap<>();

    int pageNumber = 0;
    Pagination<RWorld> p;

    public enum WorldSort { SIZE, REGISTRATION_DATE}
    private WorldSort ws;
    private RealRegionsAPI rr;

    public WorldsListGUI(Player pl, WorldSort ws, RealRegionsAPI rr) {
        this.rr = rr;
        this.ws = ws;
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color("&8Real&eRegions &8v" + rr.getPlugin().getDescription().getVersion() + " | Worlds"));
        this.uuid = pl.getUniqueId();

        load();

        this.register();
    }

    public void load() {
        List<RWorld> worlds = new ArrayList<>(rr.getWorldManagerAPI().getWorldsAndPossibleImports());

        switch (ws) {
            case REGISTRATION_DATE:
                worlds.sort(Comparator.comparingInt(RWorld::getRegistrationDate));
                break;
            case SIZE:
                worlds.sort(Comparator.comparingDouble(RWorld::getWorldSizeMB));
                break;
        }

        this.p = new Pagination<>(28, worlds);
        fillChest(p.getPage(this.pageNumber), ws);
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
            case REGISTRATION_DATE:
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
                        WorldsListGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        Player p = (Player) clicker;

                        e.setCancelled(true);

                        switch (e.getRawSlot())
                        {
                            case 47:
                                switch (current.ws) {
                                    case REGISTRATION_DATE:
                                        current.ws = WorldSort.SIZE;
                                        break;
                                    case SIZE:
                                        current.ws = WorldSort.REGISTRATION_DATE;
                                        break;
                                }
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
                                current.load();
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
                            case 51:
                                new PlayerInput(p, input -> current.rr.getWorldManagerAPI().createWorld(p, input, RWorld.WorldType.NORMAL), input -> {
                                    WorldsListGUI wv = new WorldsListGUI(p, current.ws, current.rr);
                                    wv.openInventory(p);
                                });
                        }

                        if (current.display.containsKey(e.getRawSlot())) {
                            RWorld a = current.display.get(e.getRawSlot());

                            if (a.getWorldType() == RWorld.WorldType.UNKNOWN_TO_BE_IMPORTED) {
                                if (e.getClick() == ClickType.DROP) {
                                    current.rr.getWorldManagerAPI().removeWorldFiles(p, a);
                                } else {
                                    current.rr.getWorldManagerAPI().importWorld(p, a.getRWorldName(), RWorld.WorldType.NORMAL);
                                }
                                current.load();
                            } else {
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
                                                MaterialPickerGUI mp = new MaterialPickerGUI(a, p, MaterialPickerGUI.PickType.ICON_WORLD, current.rr);
                                                mp.openInventory(p);
                                            }
                                        }.runTaskLater(current.rr.getPlugin(), 2);
                                        break;
                                    case DROP:
                                        current.rr.getWorldManagerAPI().unregisterWorld(p, a);
                                        current.load();
                                        break;
                                    default:
                                        p.closeInventory();
                                        new BukkitRunnable()
                                        {
                                            public void run()
                                            {
                                                RegionsListGUI v = new RegionsListGUI(p, a, current.rr);
                                                v.openInventory(p);
                                            }
                                        }.runTaskLater(current.rr.getPlugin(), 2);
                                        break;
                                }
                            }
                        }
                    }
                }
            }

            private void backPage(WorldsListGUI asd) {
                if (asd.p.exists(asd.pageNumber - 1)) {
                    asd.pageNumber--;
                }

                asd.fillChest(asd.p.getPage(asd.pageNumber), asd.ws);
            }

            private void nextPage(WorldsListGUI asd) {
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