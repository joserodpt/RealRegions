package josegamerpt.realregions.gui;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import josegamerpt.realregions.RealRegions;
import josegamerpt.realregions.regions.RWorld;
import josegamerpt.realregions.regions.Region;
import josegamerpt.realregions.utils.Itens;
import josegamerpt.realregions.utils.Pagination;
import josegamerpt.realregions.utils.PlayerInput;
import josegamerpt.realregions.utils.Text;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class WorldGUI {

    private static Map<UUID, WorldGUI> inventories = new HashMap<>();
    private Inventory inv;

    private ItemStack placeholder = Itens.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "&7Regions");
    private ItemStack newr = Itens.createItem(Material.CRAFTING_TABLE, 1, "&b&lNew Region", Collections.singletonList("&FClick to create a new region."));

    private ItemStack close = Itens.createItem(Material.OAK_DOOR, 1, "&cClose",
            Collections.singletonList("&fClick here to close this menu."));

    private final UUID uuid;
    private HashMap<Integer, Region> display = new HashMap<>();
    private RWorld r;

    int pageNumber = 0;
    private Pagination<Region> p;
    private RealRegions rr;

    public WorldGUI(Player as, RWorld r, RealRegions rr) {
        this.rr = rr;
        this.uuid = as.getUniqueId();
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color("&8Real&eRegions &8| &9" + r.getRWorldName()));

        this.r = r;
        load();

        this.register();
    }

    public void load() {
        p = new Pagination<>(15, rr.getWorldManager().getRegionManager().getRegions(r));
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


        this.inv.setItem(16, this.r.isLoaded() ? Itens.createItem(Material.DISPENSER, 1, "&6Unload", Collections.singletonList("&FClick to unload this world.")) :  Itens.createItem(Material.COMMAND_BLOCK, 1, "&aLoad", Collections.singletonList("&FClick to load this world.")));
        this.inv.setItem(25,  Itens.createItem(Material.SPAWNER, 1, "&aEntities", Arrays.asList("&9On this world: &b" + (r.getWorld() == null ? "?" : r.getWorld().getEntities().size()),"","&FClick to manage this worlds entities.")));
        this.inv.setItem(34, Itens.createItem(Material.PLAYER_HEAD, 1, "&9Players on this world", Collections.singletonList("&b" + (r.getWorld() == null ? "?" : r.getWorld().getPlayers().size()) + " &fplayers")));

        this.inv.setItem(41, newr);

        ItemStack next = Itens.createItem(Material.GREEN_STAINED_GLASS, 1, "&aNext",
                Arrays.asList("&fCurrent Page: &b" + (pageNumber + 1), "&fClick here to go to the next page."));
        ItemStack back = Itens.createItem(Material.YELLOW_STAINED_GLASS, 1, "&6Back",
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
                        WorldGUI current = inventories.get(uuid);
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
                                }.runTaskLater(current.rr, 2);
                                break;
                            case 34:
                                p.closeInventory();
                                new BukkitRunnable() {
                                    public void run() {
                                        EntityViewer v = new EntityViewer(p, current.r, EntityType.PLAYER);
                                        v.openInventory(p);
                                    }
                                }.runTaskLater(current.rr, 2);
                                break;
                            case 16:
                                p.closeInventory();

                                if (!current.r.isLoaded()) {
                                    current.rr.getWorldManager().loadWorld(p, current.r.getRWorldName());
                                } else {
                                    current.rr.getWorldManager().unloadWorld(p, current.r);
                                }

                                break;
                            case 41:
                                if (!current.r.getWorld().getName().equals(p.getWorld().getName()))
                                {
                                    Text.send(p, "&fYou have to be on " + current.r.getWorld().getName() + " to create a region.");
                                    return;
                                }

                                WorldEditPlugin w = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
                                try {
                                    com.sk89q.worldedit.regions.Region r = w.getSession(p).getSelection(w.getSession(p).getSelectionWorld());

                                    if (r != null) {
                                        Location min = new Location(p.getWorld(), r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ());
                                        Location max = new Location(p.getWorld(), r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ());

                                        p.closeInventory();
                                        new PlayerInput(p, input -> {
                                            //continue
                                            current.rr.getWorldManager().getRegionManager().createCubeRegion(input, min, max, current.r);
                                            Text.send(p, "&aRegion created.");
                                            new BukkitRunnable() {
                                                public void run() {
                                                    WorldGUI g = new WorldGUI(p, current.r, current.rr);
                                                    g.openInventory(p);
                                                }
                                            }.runTaskLater(current.rr, 2);
                                        }, input -> {
                                            WorldGUI wv = new WorldGUI(p, current.r, current.rr);
                                            wv.openInventory(p);
                                        });
                                    } else {
                                        Text.send(p, "nada");
                                    }
                                } catch (Exception exception) {
                                    Text.send(p, "You don't have any selection.");
                                    p.closeInventory();
                                }

                                break;
                            case 43:
                                p.closeInventory();
                                WorldViewer asd = new WorldViewer(p, WorldViewer.WorldSort.TIME, current.rr);
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
                                    a.toggleVisual(p);
                                    break;
                                case LEFT:
                                    p.closeInventory();
                                    new BukkitRunnable() {
                                        public void run() {
                                            RegionGUI fg = new RegionGUI(p, a, current.rr);
                                            fg.openInventory(p);
                                        }
                                    }.runTaskLater(current.rr, 2);
                                    break;
                                case SHIFT_LEFT:
                                    p.closeInventory();
                                    new BukkitRunnable() {
                                        public void run() {
                                            MaterialPicker mp = new MaterialPicker(a, p, MaterialPicker.PickType.ICON_REG, current.rr);
                                            mp.openInventory(p);
                                        }
                                    }.runTaskLater(current.rr, 2);
                                    break;
                                case DROP:
                                    p.closeInventory();
                                    current.rr.getWorldManager().getRegionManager().deleteRegion(p, a);
                                    break;
                                case SHIFT_RIGHT:
                                    new PlayerInput(p, input -> {
                                        //continue
                                        a.setDisplayName(input);
                                        a.saveData(Region.RegionData.SETTINGS);
                                        Text.send(p, "&fRegion displayname changed to " + Text.color(input));
                                        new BukkitRunnable() {
                                            public void run() {
                                                WorldGUI g = new WorldGUI(p, current.r, current.rr);
                                                g.openInventory(p);
                                            }
                                        }.runTaskLater(current.rr, 2);
                                    }, input -> {
                                        WorldGUI wv = new WorldGUI(p, current.r, current.rr);
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

            private void backPage(WorldGUI asd) {
                if (asd.p.exists(asd.pageNumber - 1)) {
                    asd.pageNumber--;
                }

                asd.fillChest(asd.p.getPage(asd.pageNumber));
            }

            private void nextPage(WorldGUI asd) {
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