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
 * @author José Rodrigues © 2020-2025
 * @link https://github.com/joserodpt/RealRegions
 */

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import joserodpt.realregions.api.RealRegionsAPI;
import joserodpt.realregions.api.config.TranslatableLine;
import joserodpt.realregions.api.RWorld;
import joserodpt.realregions.api.regions.Region;
import joserodpt.realutils.item.Items;
import joserodpt.realutils.gui.Pagination;
import joserodpt.realutils.gui.MaterialPickerGUI;
import joserodpt.realutils.input.PlayerInput;
import joserodpt.realutils.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static joserodpt.realregions.api.config.TranslatableLine.TranslatableLinePlaceholder.INPUT;
import static joserodpt.realregions.api.config.TranslatableLine.TranslatableLinePlaceholder.WORLD;

public class RegionsListGUI {

    private static Map<UUID, RegionsListGUI> inventories = new HashMap<>();
    private Inventory inv;

    private final ItemStack placeholder = Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "&7Regions");
    private final ItemStack newr = Items.createItem(Material.CRAFTING_TABLE, 1, "&b&lNew Region", Collections.singletonList("&FClick to create a new region."));

    private final ItemStack close = Items.createItem(Material.OAK_DOOR, 1, "&cClose",
            Collections.singletonList("&fClick here to close this menu."));

    private final UUID uuid;
    private HashMap<Integer, Region> display = new HashMap<>();
    private final RWorld r;

    int pageNumber = 0;
    private Pagination<Region> p;
    private RealRegionsAPI rr;

    public RegionsListGUI(Player as, RWorld r, RealRegionsAPI rr) {
        this.rr = rr;
        this.uuid = as.getUniqueId();
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color("&8Real&eRegions &8| &9" + r.getRWorldName()));

        this.r = r;
        load();

        this.register();
    }

    public void load() {
        p = new Pagination<>(15, new ArrayList<>(r.getRegionList()));
        fillChest(p.getPage(pageNumber));
    }

    public void fillChest(List<Region> items) {
        this.inv.clear();
        this.display.clear();

        for (int i = 10; i < 33; ++i) {
            switch (i)
            {
                case 18:
                case 24:
                case 25:
                case 26:
                case 27:
                case 15:
                case 16:
                case 17:
                    break;
                default:
                    if (!items.isEmpty()) {
                        Region wi = items.get(0);
                        this.inv.setItem(i, wi.getItem());
                        this.display.put(i, wi);
                        items.remove(0);
                    } else {
                        this.inv.setItem(i, placeholder);
                    }
                    break;
            }
        }


        this.inv.setItem(16, this.r.isLoaded() ? Items.createItem(Material.DISPENSER, 1, "&6Unload", Collections.singletonList("&FClick to unload this world.")) :  Items.createItem(Material.COMMAND_BLOCK, 1, "&aLoad", Collections.singletonList("&FClick to load this world.")));
        this.inv.setItem(25,  Items.createItem(Material.SPAWNER, 1, "&aEntities", Arrays.asList("&9On this world: &b" + (r.getWorld() == null ? "?" : r.getWorld().getEntities().size()),"","&FClick to manage this worlds entities.")));
        this.inv.setItem(34, Items.createItem(Material.PLAYER_HEAD, 1, "&9Players on this world", Collections.singletonList("&b" + (r.getWorld() == null ? "?" : r.getWorld().getPlayers().size()) + " &fplayers")));

        this.inv.setItem(41, newr);

        ItemStack next = Items.createItem(Material.GREEN_STAINED_GLASS, 1, "&aNext",
                Arrays.asList("&fCurrent Page: &b" + (pageNumber + 1), "&fClick here to go to the next page."));
        ItemStack back = Items.createItem(Material.YELLOW_STAINED_GLASS, 1, "&6Back",
                Arrays.asList("&fCurrent Page: &b" + (pageNumber + 1), "&fClick here to go back to the next page."));

        this.inv.setItem(38, next);
        this.inv.setItem(37, back);

        this.inv.setItem(43, close);
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
                    Player p = (Player) clicker;
                    if (e.getCurrentItem() == null) {
                        return;
                    }
                    UUID uuid = clicker.getUniqueId();
                    if (inventories.containsKey(uuid)) {
                        RegionsListGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        e.setCancelled(true);

                        switch (e.getRawSlot()) {
                            case 25:
                                p.closeInventory();
                                new BukkitRunnable() {
                                    public void run() {
                                        EntityViewer v = new EntityViewer(p, current.r, current.rr);
                                        v.openInventory(p);
                                    }
                                }.runTaskLater(current.rr.getPlugin(), 2);
                                break;
                            case 34:
                                p.closeInventory();
                                new BukkitRunnable() {
                                    public void run() {
                                        EntityViewer v = new EntityViewer(p, current.r, EntityType.PLAYER, current.rr);
                                        v.openInventory(p);
                                    }
                                }.runTaskLater(current.rr.getPlugin(), 2);
                                break;
                            case 16:
                                p.closeInventory();

                                if (!current.r.isLoaded()) {
                                    current.rr.getWorldManagerAPI().loadWorld(p, current.r.getRWorldName());
                                } else {
                                    final Runnable unload = () -> current.rr.getWorldManagerAPI().unloadWorld(p, current.r);
                                    //asked first where the server has dialogs; a no brings this menu back
                                    if (!Confirmations.unloadWorld(p, current.r, unload, () -> new RegionsListGUI(p, current.r, current.rr).openInventory(p))) {
                                        unload.run();
                                    }
                                }

                                break;
                            case 41:
                                if (!current.r.getWorld().getName().equals(p.getWorld().getName()))
                                {
                                    TranslatableLine.REGION_NOT_IN_WORLD.with(WORLD, current.r.getWorld().getName()).send(p);
                                    return;
                                }

                                WorldEditPlugin w = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
                                try {
                                    com.sk89q.worldedit.regions.Region r = w.getSession(p).getSelection(w.getSession(p).getSelectionWorld());

                                    if (r != null) {
                                        Location min = new Location(p.getWorld(), r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ());
                                        Location max = new Location(p.getWorld(), r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ());

                                        p.closeInventory();
                                        new PlayerInput(p, true, input -> {
                                            //continue
                                            current.rr.getRegionManagerAPI().createCubeRegion(input, min, max, current.r);
                                            TranslatableLine.REGION_CREATED.send(p);
                                            new BukkitRunnable() {
                                                public void run() {
                                                    RegionsListGUI g = new RegionsListGUI(p, current.r, current.rr);
                                                    g.openInventory(p);
                                                }
                                            }.runTaskLater(current.rr.getPlugin(), 2);
                                        }, input -> {
                                            RegionsListGUI wv = new RegionsListGUI(p, current.r, current.rr);
                                            wv.openInventory(p);
                                        });
                                    } else {
                                        TranslatableLine.SELECTION_NONE.send(p);
                                    }
                                } catch (Exception exception) {
                                    TranslatableLine.SELECTION_NONE.send(p);
                                    p.closeInventory();
                                }

                                break;
                            case 43:
                                p.closeInventory();
                                WorldsListGUI asd = new WorldsListGUI(p, WorldsListGUI.WorldSort.REGISTRATION_DATE, current.rr);
                                asd.openInventory(p);
                                break;
                            case 38:
                                nextPage(current);
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
                                break;
                            case 37:
                                backPage(current);
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
                                break;

                        }

                        if (current.display.containsKey(e.getRawSlot())) {
                            Region a = current.display.get(e.getRawSlot());
                            switch (e.getClick()) {
                                case RIGHT:
                                    p.closeInventory();
                                    current.rr.getRegionManagerAPI().toggleRegionView(p, a);
                                    break;
                                case LEFT:
                                    p.closeInventory();
                                    new BukkitRunnable() {
                                        public void run() {
                                            RegionSettingsGUI fg = new RegionSettingsGUI(p, a, current.rr);
                                            fg.openInventory(p);
                                        }
                                    }.runTaskLater(current.rr.getPlugin(), 2);
                                    break;
                                case SHIFT_LEFT:
                                    p.closeInventory();
                                    new BukkitRunnable() {
                                        public void run() {
                                            //null when closed without picking: back to this list either way
                                            MaterialPickerGUI mp = new MaterialPickerGUI(p, "Select icon for " + a.getDisplayName(), MaterialPickerGUI.MaterialLists.ONLY_BLOCKS, m -> {
                                                if (m != null) {
                                                    a.setIcon(m);
                                                    a.saveData(Region.RegionData.ICON);
                                                }
                                                new RegionsListGUI(p, a.getRWorld(), current.rr).openInventory(p);
                                            });
                                            mp.openInventory(p);
                                        }
                                    }.runTaskLater(current.rr.getPlugin(), 2);
                                    break;
                                case DROP:
                                    //asked first where the server has dialogs, which replace this menu, so it is reopened after
                                    final Runnable reopen = () -> new RegionsListGUI(p, current.r, current.rr).openInventory(p);
                                    if (!Confirmations.deleteRegion(p, a, () -> {
                                        current.rr.getRegionManagerAPI().deleteRegion(p, a);
                                        reopen.run();
                                    }, reopen)) {
                                        current.rr.getRegionManagerAPI().deleteRegion(p, a);
                                        current.load();
                                    }
                                    break;
                                case SHIFT_RIGHT:
                                    new PlayerInput(p, false, input -> {
                                        a.setDisplayName(input);
                                        a.saveData(Region.RegionData.SETTINGS);
                                        TranslatableLine.REGION_DISPLAY_NAME_CHANGED.with(INPUT, Text.color(input)).send(p);
                                        new BukkitRunnable() {
                                            public void run() {
                                                RegionsListGUI g = new RegionsListGUI(p, current.r, current.rr);
                                                g.openInventory(p);
                                            }
                                        }.runTaskLater(current.rr.getPlugin(), 2);
                                    }, input -> {
                                        RegionsListGUI wv = new RegionsListGUI(p, current.r, current.rr);
                                        wv.openInventory(p);
                                    });
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
            }

            private void backPage(RegionsListGUI asd) {
                if (asd.p.exists(asd.pageNumber - 1)) {
                    asd.pageNumber--;
                }

                asd.fillChest(asd.p.getPage(asd.pageNumber));
            }

            private void nextPage(RegionsListGUI asd) {
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