package joserodpt.realregions.plugin.managers;

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
import joserodpt.realregions.api.RealRegionsAPI;
import joserodpt.realregions.api.config.TranslatableLine;
import joserodpt.realregions.api.managers.RegionManagerAPI;
import joserodpt.realregions.api.regions.CuboidRegion;
import joserodpt.realregions.api.regions.RWorld;
import joserodpt.realregions.api.regions.Region;
import joserodpt.realregions.api.utils.Cube;
import joserodpt.realregions.api.utils.CubeVisualizer;
import joserodpt.realregions.api.utils.Text;
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

public class RegionManager extends RegionManagerAPI {

    private final RealRegionsAPI rra;
    private List<Region> viewing = new ArrayList<>();

    @Override
    public List<Region> getViewing() {
        return viewing;
    }

    public RegionManager(RealRegionsAPI rra) {
        this.rra = rra;
    }

    @Override
    public List<Region> getRegions() {
        return rra.getWorldManagerAPI().getWorldList()
                .stream()
                .flatMap(rWorld -> rWorld.getRegionList().stream())
                .collect(Collectors.toList());
    }

    @Override
    public void deleteRegion(CommandSender p, Region a) {
//        if (a.getType() == Region.RegionType.INFINITE) {
//            TranslatableLine.REGION_CANT_DELETE_INFINITE.setV1(TranslatableLine.ReplacableVar.NAME.eq(a.getDisplayName())).send(p);
//            return;
//        }
//
//        if (a.getOrigin() != Region.RegionOrigin.REALREGIONS) {
//            TranslatableLine.REGION_IMPORTED_FROM_EXTERNAL.setV1(TranslatableLine.ReplacableVar.NAME.eq(a.getOrigin().getDisplayName())).send(p);
//            return;
//        }

        //remove permissions from RealPermissions
        if (rra.getRealPermissionsAPI() != null) {
            rra.getRealPermissionsAPI().getHookupAPI().removePermissionFromHookup(rra.getPlugin().getDescription().getName(), a.getRegionBypassPermissions());
        }

        deleteRegion(a);

        TranslatableLine.REGION_DELETED.setV1(TranslatableLine.ReplacableVar.NAME.eq(a.getDisplayName())).send(p);
    }

    @Override
    public void deleteRegion(Region a) {
        if (a != null) {
            this.getViewing().remove(a);
            a.getRWorld().removeRegion(a);
            a.getRWorld().getConfig().set("Regions." + a.getRegionName(), null);
            a.getRWorld().saveConfig();
        }
    }

    @Override
    public Region getRegionPlusName(String name) {
        try {
            String[] split = name.split("@");
            String world = split[1];
            String reg = split[0];

            RWorld w = rra.getWorldManagerAPI().getWorld(world);
            return (w != null) ? rra.getRegionManagerAPI().getRegions().stream()
                    .filter(region -> region.getRegionName().equals(reg))
                    .findFirst()
                    .orElse(null) : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Region getFirstPriorityRegionContainingLocation(Location l) {
        return rra.getWorldManagerAPI().getWorld(l.getWorld()) == null ? null : rra.getWorldManagerAPI().getWorld(l.getWorld()).getRegionList().stream()
                .sorted(Comparator.comparingInt(Region::getPriority).reversed())
                .filter(region -> region.isLocationInRegion(l))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void createCubeRegion(String name, Location min, Location max, RWorld r) {
        CuboidRegion crg = new CuboidRegion(min, max, ChatColor.stripColor(Text.color(name)), name, r, Material.LIGHT_BLUE_STAINED_GLASS, 100);
        r.addRegion(crg);

        //save region
        crg.saveData(Region.RegionData.ALL);

        //send region permissions to RealPermissions
        if (rra.getRealPermissionsAPI() != null) {
            rra.getRealPermissionsAPI().getHookupAPI().addPermissionToHookup(rra.getPlugin().getDescription().getName(), crg.getRegionBypassPermissions());
        }
    }

    @Override
    public void createCubeRegionRealMines(RMine mine, RWorld rw) {
        CuboidRegion crg = new CuboidRegion(mine.getPOS1(), mine.getPOS2(), ChatColor.stripColor(mine.getName()), mine.getDisplayName(), rw, mine.getIcon(), 101);
        rw.addRegion(crg);
        crg.setOrigin(Region.RegionOrigin.REALMINES);

        //save region
        crg.saveData(Region.RegionData.ALL);
    }

    @Override
    public void startVisualizer() {
        //visualizer loop
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Region region : getViewing()) {
                    if (region.canVisualize() && region.getRWorld().isLoaded()) {
                        CubeVisualizer v = ((CuboidRegion) region).getCubeVisualizer();
                        v.getCube().forEach(v::spawnParticle);
                    }
                }
            }
        }.runTaskTimer(rra.getPlugin(),0, 10);
    }

    @Override
    public void setRegionBounds(Region reg, Player p) {
        if (reg.getOrigin() != Region.RegionOrigin.REALREGIONS) {
            TranslatableLine.REGION_REDEFINE_EXTERNAL_PLUGIN.setV1(TranslatableLine.ReplacableVar.NAME.eq(reg.getDisplayName())).send(p);
            return;
        }

        final WorldEditPlugin w = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        try {
            final com.sk89q.worldedit.regions.Region r = w.getSession(p.getPlayer()).getSelection(w.getSession(p.getPlayer()).getSelectionWorld());

            if (r != null) {
                final Location pos1 = new Location(p.getWorld(), r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ());
                final Location pos2 = new Location(p.getWorld(), r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ());

                ((CuboidRegion) reg).setCube(new Cube(pos1, pos2));
                reg.saveData(Region.RegionData.BOUNDS);
                TranslatableLine.REGION_SET_BOUNDS.send(p);
            }
        } catch (final Exception e) {
            TranslatableLine.SELECTION_NONE.send(p);
        }
    }

    @Override
    public void checkRealMinesRegions(Map<String, RMine> mines) {
        for (String mineName : mines.keySet()) {
            RMine mine = mines.get(mineName);
            RWorld rw = rra.getWorldManagerAPI().getWorld(mine.getWorld());

            if (!rw.hasRegion(mineName)) {
                //create new region
                rra.getRegionManagerAPI().createCubeRegionRealMines(mine, rw);
            } else {
                //update region location
                CuboidRegion r = (CuboidRegion) rra.getRegionManagerAPI().getRegionPlusName(mineName + "@" + rw.getRWorldName());
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

    @Override
    public void toggleRegionView(CommandSender commandSender, Region a) {
        if (a instanceof CuboidRegion) {
            if (this.getViewing().contains(a)) {
                getViewing().remove(a);
            } else {
                this.getViewing().add(a);
            }

            TranslatableLine.REGION_VIEW_REGION.setV1(TranslatableLine.ReplacableVar.NAME.eq(a.getDisplayName())).setV2(TranslatableLine.ReplacableVar.INPUT.eq(Text.styleBoolean(this.getViewing().contains(a)))).send(commandSender);
        } else {
            TranslatableLine.REGION_CANT_VIEW_INFINITE_REGION.send(commandSender);
        }
    }
}
