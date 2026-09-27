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
 * @author José Rodrigues © 2020-2025
 * @link https://github.com/joserodpt/RealRegions
 */

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realregions.api.utils.Format;
import joserodpt.realregions.api.RealRegionsAPI;
import joserodpt.realregions.api.config.TranslatableLine;
import joserodpt.realregions.api.managers.RegionManagerAPI;
import joserodpt.realregions.api.regions.CuboidRegion;
import joserodpt.realregions.api.RWorld;
import joserodpt.realregions.api.regions.Region;
import joserodpt.realregions.api.utils.Cube;
import joserodpt.realregions.api.utils.CubeVisualizer;
import joserodpt.realutils.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static joserodpt.realregions.api.config.TranslatableLine.TranslatableLinePlaceholder.INPUT;
import static joserodpt.realregions.api.config.TranslatableLine.TranslatableLinePlaceholder.NAME;

public class RegionManager extends RegionManagerAPI {

    private final RealRegionsAPI rra;
    private List<Region> viewing = new ArrayList<>();
    private final Map<UUID, Region> lastRegion = new HashMap<>();

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
        if (a.getType() == Region.RegionType.INFINITE) {
            TranslatableLine.REGION_CANT_DELETE_INFINITE.with(NAME, a.getDisplayName()).send(p);
            return;
        }

        if (a.getOrigin() != Region.RegionOrigin.REALREGIONS) {
            TranslatableLine.REGION_IMPORTED_FROM_EXTERNAL.with(NAME, a.getOrigin().getDisplayName()).send(p);
            return;
        }

        //remove permissions from RealPermissions
        if (rra.getRealPermissionsAPI() != null) {
            rra.getRealPermissionsAPI().getHooksAPI().removePermissionFromHook(rra.getPlugin().getDescription().getName(), a.getRegionBypassPermissions());
        }

        deleteRegion(a);

        TranslatableLine.REGION_DELETED.with(NAME, a.getDisplayName()).send(p);
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
        CuboidRegion crg = new CuboidRegion(ChatColor.stripColor(Text.color(name)), r, Material.LIGHT_BLUE_STAINED_GLASS, min, max);
        crg.setPriority(100);
        crg.setupDefaultConfig();
        r.addRegion(crg);


        //save region
        crg.saveData(Region.RegionData.ALL);

        //send region permissions to RealPermissions
        if (rra.getRealPermissionsAPI() != null) {
            rra.getRealPermissionsAPI().getHooksAPI().addPermissionToHook(rra.getPlugin().getDescription().getName(), crg.getRegionBypassPermissions());
        }
    }

    @Override
    public void createCubeRegionRealMines(RMine mine, RWorld rw) {
        final Location[] bounds = mineBounds(mine);
        if (bounds == null || rw == null) {
            skipMine(mine, rw);
            return;
        }
        CuboidRegion crg = new CuboidRegion(ChatColor.stripColor(mine.getName()), rw, mine.getIcon(), bounds[0], bounds[1]);
        crg.setDisplayName(mine.getDisplayName());
        crg.setupDefaultConfig();
        crg.setOrigin(Region.RegionOrigin.REALMINES);
        rw.addRegion(crg);

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
            TranslatableLine.REGION_REDEFINE_EXTERNAL_PLUGIN.with(NAME, reg.getDisplayName()).send(p);
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
        mines.values().forEach(this::syncRealMinesRegion);
    }

    @Override
    public void syncRealMinesRegion(RMine mine) {
        final RWorld rw = mine.getWorld() == null ? null : rra.getWorldManagerAPI().getWorld(mine.getWorld());
        final Location[] bounds = mineBounds(mine);
        if (bounds == null || rw == null) {
            skipMine(mine, rw);
            return;
        }

        //the region is named after the mine without its colours, as createCubeRegionRealMines names it
        final Region existing = this.getRegionPlusName(ChatColor.stripColor(mine.getName()) + "@" + rw.getRWorldName());
        if (existing == null) {
            this.createCubeRegionRealMines(mine, rw);
        } else if (existing instanceof CuboidRegion) {
            final CuboidRegion r = (CuboidRegion) existing;
            if (!sameArea(r.getCube(), bounds[0], bounds[1])) {
                r.setCube(new Cube(bounds[0], bounds[1]));
                r.saveData(Region.RegionData.BOUNDS);
            }
        }
    }

    /**
     * The two corners of a mine's area, or null while it has none. A schematic mine only keeps
     * where it is pasted, so its area is the one RealMines worked out when pasting it; the other
     * types have both corners of their own.
     */
    private static Location[] mineBounds(RMine mine) {
        if (mine.getMineCuboid() != null && mine.getMineCuboid().getPOS1() != null && mine.getMineCuboid().getPOS2() != null) {
            return new Location[]{mine.getMineCuboid().getPOS1(), mine.getMineCuboid().getPOS2()};
        }
        if (mine.getPOS1() != null && mine.getPOS2() != null) {
            return new Location[]{mine.getPOS1(), mine.getPOS2()};
        }
        return null;
    }

    private void skipMine(RMine mine, RWorld rw) {
        rra.getLogger().warning("Skipped the region for RealMines' mine " + ChatColor.stripColor(mine.getName()) + ": "
                + (rw == null ? "its world isn't registered in RealRegions." : "it has no area yet (a schematic mine that hasn't been pasted)."));
    }

    /** Whether a cube covers the blocks between these two corners, whichever order they come in. */
    private static boolean sameArea(Cube cube, Location a, Location b) {
        final Location c1 = cube.getPOS1(), c2 = cube.getPOS2();
        if (c1 == null || c2 == null || c1.getWorld() == null || !c1.getWorld().equals(a.getWorld())) {
            return false;
        }
        return Math.min(c1.getBlockX(), c2.getBlockX()) == Math.min(a.getBlockX(), b.getBlockX())
                && Math.min(c1.getBlockY(), c2.getBlockY()) == Math.min(a.getBlockY(), b.getBlockY())
                && Math.min(c1.getBlockZ(), c2.getBlockZ()) == Math.min(a.getBlockZ(), b.getBlockZ())
                && Math.max(c1.getBlockX(), c2.getBlockX()) == Math.max(a.getBlockX(), b.getBlockX())
                && Math.max(c1.getBlockY(), c2.getBlockY()) == Math.max(a.getBlockY(), b.getBlockY())
                && Math.max(c1.getBlockZ(), c2.getBlockZ()) == Math.max(a.getBlockZ(), b.getBlockZ());
    }

    @Override
    public void toggleRegionView(CommandSender commandSender, Region a) {
        if (a instanceof CuboidRegion) {
            if (this.getViewing().contains(a)) {
                getViewing().remove(a);
            } else {
                this.getViewing().add(a);
            }

            TranslatableLine.REGION_VIEW_REGION.with(NAME, a.getDisplayName()).with(INPUT, Format.styleBoolean(this.getViewing().contains(a))).send(commandSender);
        } else {
            TranslatableLine.REGION_CANT_VIEW_INFINITE_REGION.send(commandSender);
        }
    }

    @Override
    public Map<UUID, Region> getLastRegions() {
        return lastRegion;
    }
}
