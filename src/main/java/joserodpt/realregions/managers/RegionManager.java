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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RegionManager {

    private final RealRegionsPlugin rr;
    private List<Region> view = new ArrayList<>();

    public List<Region> getView() {
        return view;
    }

    public RegionManager(RealRegionsPlugin rr) {
        this.rr = rr;
    }

    public List<Region> getRegions() {
        return rr.getWorldManager().getWorldList()
                .stream()
                .flatMap(rWorld -> rWorld.getRegionList().stream())
                .collect(Collectors.toList());
    }

    public void deleteRegion(CommandSender p, Region a) {
        if (a.getType() == Region.RegionType.INFINITE)
        {
            Text.send(p, Language.file().getString("Region.Cant-Delete-Infinite").replace("%name%", a.getDisplayName()));
            return;
        }

        if (a.getOrigin() != Region.RegionOrigin.REALREGIONS) {
            Text.send(p, "&fThis region was imported from " + a.getOrigin().getDisplayName() + "&r&7. &cDelete it there.");
            return;
        }

        //remove permissions from RealPermissions
        if (rr.getRealPermissionsAPI() != null) {
            rr.getRealPermissionsAPI().getHookupAPI().removePermissionFromHookup(rr.getDescription().getName(), a.getRegionBypassPermissions());
        }

        deleteRegion(a);

        Text.send(p, Language.file().getString("Region.Deleted").replace("%name%", a.getDisplayName()));
    }

    public void deleteRegion(Region a) {
        a.setBeingVisualized(false);
        a.getRWorld().removeRegion(a);
        a.getRWorld().getConfig().set("Regions." + a.getRegionName(), null);
        a.getRWorld().saveConfig();
    }

    public Region getRegionPlusName(String name) {
        try {
            String[] split = name.split("@");
            String world = split[1];
            String reg = split[0];

            RWorld w = rr.getWorldManager().getWorld(world);
            return (w != null) ? rr.getRegionManager().getRegions().stream()
                    .filter(region -> region.getRegionName().equals(reg))
                    .findFirst()
                    .orElse(null) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public Region getFirstPriorityRegionContainingLocation(Location l) {
        return rr.getWorldManager().getWorld(l.getWorld()).getRegionList().stream()
                .sorted(Comparator.comparingInt(Region::getPriority).reversed())
                .filter(region -> region.isLocationInRegion(l))
                .findFirst()
                .orElse(null);
    }

    public void createCubeRegion(String name, Location min, Location max, RWorld r) {
        CuboidRegion crg = new CuboidRegion(min, max, ChatColor.stripColor(Text.color(name)), name, r, Material.LIGHT_BLUE_STAINED_GLASS, 100);
        r.addRegion(crg);

        //save region
        crg.saveData(Region.RegionData.ALL);

        //send region permissions to RealPermissions
        if (rr.getRealPermissionsAPI() != null) {
            rr.getRealPermissionsAPI().getHookupAPI().addPermissionToHookup(rr.getDescription().getName(), crg.getRegionBypassPermissions());
        }
    }

    public void createCubeRegionRealMines(RMine mine, RWorld rw) {
        CuboidRegion crg = new CuboidRegion(mine.getPOS1(), mine.getPOS2(), ChatColor.stripColor(mine.getName()), mine.getDisplayName(), rw, mine.getIcon(), 101);
        rw.addRegion(crg);
        crg.setOrigin(Region.RegionOrigin.REALMINES);

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
        if (reg.getOrigin() != Region.RegionOrigin.REALREGIONS) {
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

    public void checkRealMinesRegions(Map<String, RMine> mines) {
        for (String mineName : mines.keySet()) {
            RMine mine = mines.get(mineName);
            RWorld rw = rr.getWorldManager().getWorld(mine.getWorld());

            if (!rw.hasRegion(mineName)) {
                //create new region
                rr.getRegionManager().createCubeRegionRealMines(mine, rw);
            } else {
                //update region location
                CuboidRegion r = (CuboidRegion) rr.getRegionManager().getRegionPlusName(mineName + "@" + rw.getRWorldName());
                if (r != null) {
                    if (r.getCube().getPOS1() != mine.getPOS1()) {
                        r.setCube(new Cube(mine.getPOS1(), mine.getPOS2()));
                        continue;
                    }

                    if (r.getCube().getPOS2() != mine.getPOS2()) {
                        r.setCube(new Cube(mine.getPOS1(), mine.getPOS2()));
                    }
                }
            }
        }
    }
}
