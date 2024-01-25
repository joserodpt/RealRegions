package joserodpt.realregions.managers;

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

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realregions.RealRegionsPlugin;
import joserodpt.realregions.config.Language;
import joserodpt.realregions.regions.RWorld;
import joserodpt.realregions.regions.CuboidRegion;
import joserodpt.realregions.regions.Region;
import joserodpt.realregions.regions.RegionOrigin;
import joserodpt.realregions.utils.Cube;
import joserodpt.realregions.utils.CubeVisualizer;
import joserodpt.realregions.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RegionManager {

    private final WorldManager wm;
    private List<Region> view = new ArrayList<>();

    public List<Region> getView() {
        return view;
    }

    public RegionManager(WorldManager wm) {
        this.wm = wm;
    }

    public void saveRegions(RWorld w) {
        getRegions(w).forEach(rRegion -> rRegion.saveData(Region.RegionData.ALL));
    }

    public List<Region> loadRegions(RWorld w) {
        List<Region> loaded_regions = new ArrayList<>();
        for (String r : w.getConfig().getConfigurationSection("Regions").getKeys(false)) {
            Region.RegionType rt = Region.RegionType.valueOf(w.getConfig().getString("Regions." + r + ".Type"));
            String n = w.getConfig().getString("Regions." + r + ".Display-Name");
            Region reg = null;

            //load region flags
            boolean blockInteract = w.getConfig().getBoolean("Regions." + r + ".Block.Interact");
            boolean containerInteract = w.getConfig().getBoolean("Regions." + r + ".Container.Interact");
            boolean blockBreak = w.getConfig().getBoolean("Regions." + r + ".Block.Break");
            boolean blockPlace = w.getConfig().getBoolean("Regions." + r + ".Block.Place");
            boolean pvp = w.getConfig().getBoolean("Regions." + r + ".PVP");
            boolean pve = w.getConfig().getBoolean("Regions." + r + ".PVE");
            boolean hunger = w.getConfig().getBoolean("Regions." + r + ".Hunger");
            boolean takeDamage = w.getConfig().getBoolean("Regions." + r + ".Damage");
            boolean explosions = w.getConfig().getBoolean("Regions." + r + ".Explosions");
            boolean itemDrop = w.getConfig().getBoolean("Regions." + r + ".Item.Drop");
            boolean itemPickup = w.getConfig().getBoolean("Regions." + r + ".Item.Pickup");
            boolean entitySpawning = w.getConfig().getBoolean("Regions." + r + ".Entity-Spawning");
            boolean enter = w.getConfig().getBoolean("Regions." + r + ".Enter");
            boolean accessCrafting = w.getConfig().getBoolean("Regions." + r + ".Access.Crafting-Table");
            boolean accessChests = w.getConfig().getBoolean("Regions." + r + ".Access.Chests");
            boolean accessHoppers = w.getConfig().getBoolean("Regions." + r + ".Access.Hoppers");

            switch (rt)
            {
                case INFINITE:
                    reg = new Region(r, n, w, Material.valueOf(w.getConfig().getString("Regions." + r + ".Icon")), w.getConfig().getInt("Regions." + r + ".Priority"), Region.RegionType.INFINITE);
                    break;
                case CUBOID:
                    reg = new CuboidRegion(Text.textToLoc(w.getConfig().getString("Regions." + r + ".POS.1"), w.getWorld()),
                            Text.textToLoc(w.getConfig().getString("Regions." + r + ".POS.2"),  w.getWorld()),
                            ChatColor.stripColor(r), w.getConfig().getString("Regions." + r + ".Display-Name"), w,
                            Material.valueOf(w.getConfig().getString("Regions." + r + ".Icon")), w.getConfig().getInt("Regions." + r + ".Priority"));
                    break;
            }

            String orig = w.getConfig().getString("Regions." + r + ".Origin", "-");
            assert orig != null;
            if (!orig.equals("-")) {
                reg.setOrigin(RegionOrigin.valueOf(orig));
            }

            if (reg != null) {
                reg.blockinteract = blockInteract;
                reg.containerinteract = containerInteract;
                reg.blockbreak = blockBreak;
                reg.blockplace = blockPlace;
                reg.pvp = pvp;
                reg.pve = pve;
                reg.hunger = hunger;
                reg.takedamage = takeDamage;
                reg.explosions = explosions;
                reg.itemdrop = itemDrop;
                reg.itempickup = itemPickup;
                reg.entityspawning = entitySpawning;
                reg.enter = enter;
                reg.accesscrafting = accessCrafting;
                reg.accesschests = accessChests;
                reg.accesshoppers = accessHoppers;
                reg.saveData(Region.RegionData.FLAGS);
                loaded_regions.add(reg);
            }
        }

        return loaded_regions;
    }

    public void deleteRegion(CommandSender p, Region a) {
        if (a.getType() == Region.RegionType.INFINITE)
        {
            Text.send(p, Language.file().getString("Region.Cant-Delete-Infinite").replace("%name%", a.getDisplayName()));
            return;
        }

        if (a.getOrigin() != RegionOrigin.REALREGIONS) {
            Text.send(p, "&fThis region was imported from " + a.getOrigin().getDisplayName() + "&r&7. &cDelete it there.");
            return;
        }

        //remove permissions from RealPermissions
        if (wm.getPlugin().getRealPermissionsAPI() != null) {
            wm.getPlugin().getRealPermissionsAPI().getHookupAPI().removePermissionFromHookup(wm.getPlugin().getDescription().getName(), a.getRegionBypassPermissions());
        }

        deleteRegion(a);

        Text.send(p, Language.file().getString("Region.Deleted").replace("%name%", a.getDisplayName()));
    }

    public void deleteRegion(Region a) {

        a.setBeingVisualized(false);
        wm.getWorldsAndRegions().get(a.getRWorld()).remove(a);
        a.getRWorld().getConfig().set("Regions." + a.getRegionName(), null);
        a.getRWorld().saveConfig();
    }

    public Region getRegionPlusName(String name) {
        try {
            String[] split = name.split("@");
            String world = split[1];
            String reg = split[0];

            RWorld w = wm.getWorld(world);
            return (w != null) ? wm.getWorldsAndRegions().getOrDefault(w, Collections.emptyList()).stream()
                    .filter(region -> region.getRegionName().equals(reg))
                    .findFirst()
                    .orElse(null) : null;
        } catch (Exception e) {
            return null;
        }
    }


    public boolean hasRegion(RWorld w, String name) {
        return wm.getWorldsAndRegions().getOrDefault(w, new ArrayList<>())
                .stream()
                .anyMatch(region -> region.getRegionName().equals(name));
    }

    public ArrayList<Region> getAllRegions() {
        return wm.getWorldsAndRegions().values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public Region getFirstPriorityRegionContainingLocation(Location l) {
        return wm.getWorldsAndRegions().values().stream()
                .flatMap(Collection::stream)
                .sorted(Comparator.comparingInt(Region::getPriority).reversed())
                .filter(region -> region.isLocationInRegion(l))
                .findFirst()
                .orElse(null);

    }

    public List<Region> getRegions(RWorld w) {
        return wm.getWorldsAndRegions().get(w);
    }

    public void createCubeRegion(String name, Location min, Location max, RWorld r) {
        CuboidRegion crg = new CuboidRegion(min, max, ChatColor.stripColor(Text.color(name)), name, r, Material.LIGHT_BLUE_STAINED_GLASS, 100);
        wm.getWorldsAndRegions().get(r).add(crg);

        //save region
        crg.saveData(Region.RegionData.ALL);

        //send region permissions to RealPermissions
        if (wm.getPlugin().getRealPermissionsAPI() != null) {
            wm.getPlugin().getRealPermissionsAPI().getHookupAPI().addPermissionToHookup(wm.getPlugin().getDescription().getName(), crg.getRegionBypassPermissions());
        }
    }

    public void createCubeRegionRealMines(RMine mine, RWorld rw) {
        CuboidRegion crg = new CuboidRegion(mine.getPOS1(), mine.getPOS2(), ChatColor.stripColor(mine.getName()), mine.getDisplayName(), rw, mine.getIcon(), 101);
        wm.getWorldsAndRegions().get(rw).add(crg);
        crg.setOrigin(RegionOrigin.REALMINES);

        //save region
        crg.saveData(Region.RegionData.ALL);
    }

    public void startVisualizer() {
        //visualizer loop
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Region region : view) {
                    if (region.canVisualize() && region.getRWorld().isLoaded()) {
                        CubeVisualizer v = ((CuboidRegion) region).getCubeVisualizer();
                        v.getCube().forEach(v::spawnParticle);
                    }
                }
            }
        }.runTaskTimer(RealRegionsPlugin.getPlugin(),0, 10);
    }

    public void setRegionBounds(Region reg, Player p) {
        if (reg.getOrigin() != RegionOrigin.REALREGIONS) {
            Text.send(p, "&fYou &ccan't redefine &fthe bounds of a region imported by " + reg.getOrigin().getDisplayName());
            return;
        }

        final WorldEditPlugin w = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        try {
            final com.sk89q.worldedit.regions.Region r = w.getSession(p.getPlayer()).getSelection(w.getSession(p.getPlayer()).getSelectionWorld());

            if (r != null) {
                final Location pos1 = new Location(p.getWorld(), r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ());
                final Location pos2 = new Location(p.getWorld(), r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ());

                ((CuboidRegion) reg).setCube(new Cube(pos1, pos2));
                ((CuboidRegion) reg).saveData(Region.RegionData.BOUNDS);
                Text.send(p, Language.file().getString("Region.Region-Set-Bounds"));
            }
        } catch (final Exception e) {
            Text.send(p, Language.file().getString("Selection.None"));
        }
    }
}
