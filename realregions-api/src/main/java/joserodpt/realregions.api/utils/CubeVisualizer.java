package joserodpt.realregions.api.utils;

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

import joserodpt.realregions.api.regions.CuboidRegion;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CubeVisualizer {

    private final CuboidRegion mp;

    public CubeVisualizer(CuboidRegion mp) {
        this.mp = mp;
    }

    public List<Location> getCube() {
        if (mp.getCube().getPOS1() != null && mp.getCube().getPOS2() != null) {
            List<Location> result = new ArrayList<>();
            World world = mp.getCube().getPOS1().getWorld();
            double minX = Math.min(mp.getCube().getPOS1().getX(), mp.getCube().getPOS2().getX());
            double minY = Math.min(mp.getCube().getPOS1().getY(), mp.getCube().getPOS2().getY());
            double minZ = Math.min(mp.getCube().getPOS1().getZ(), mp.getCube().getPOS2().getZ());
            double maxX = Math.max(mp.getCube().getPOS1().getX() + 1, mp.getCube().getPOS2().getX() + 1);
            double maxY = Math.max(mp.getCube().getPOS1().getY() + 1, mp.getCube().getPOS2().getY() + 1);
            double maxZ = Math.max(mp.getCube().getPOS1().getZ() + 1, mp.getCube().getPOS2().getZ() + 1);

            double dist = 0.5;
            for (double x = minX; x <= maxX; x += dist) {
                for (double y = minY; y <= maxY; y += dist) {
                    for (double z = minZ; z <= maxZ; z += dist) {
                        int components = 0;
                        if (x == minX || x == maxX) components++;
                        if (y == minY || y == maxY) components++;
                        if (z == minZ || z == maxZ) components++;
                        if (components >= 2) {
                            result.add(new Location(world, x, y, z));
                        }
                    }
                }
            }
            return result;
        } else {
            return Collections.emptyList();
        }
    }

    public void spawnParticle(Location location) {
        location.getWorld().spawnParticle(Particle.REDSTONE, location.getX(), location.getY(), location.getZ(), 0, 0.001, 1, 0, 1, new Particle.DustOptions(Color.BLUE, 1));
    }
}
