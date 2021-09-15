package josegamerpt.realregions.utils;

import josegamerpt.realregions.regions.Region;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CubeVisualizer {

    private Region mp;
    private double dist = 0.5;

    public CubeVisualizer(Region mp) {
        this.mp = mp;
    }

    public List<Location> getCube() {
        if (mp.getMaster().getPOS(1) != null && mp.getMaster().getPOS(2) != null && mp.getView()) {
            List<Location> result = new ArrayList<>();
            World world = mp.getMaster().getPOS(1).getWorld();
            double minX = Math.min(mp.getMaster().getPOS(1).getX(), mp.getMaster().getPOS(2).getX());
            double minY = Math.min(mp.getMaster().getPOS(1).getY(), mp.getMaster().getPOS(2).getY());
            double minZ = Math.min(mp.getMaster().getPOS(1).getZ(), mp.getMaster().getPOS(2).getZ());
            double maxX = Math.max(mp.getMaster().getPOS(1).getX() + 1, mp.getMaster().getPOS(2).getX() + 1);
            double maxY = Math.max(mp.getMaster().getPOS(1).getY() + 1, mp.getMaster().getPOS(2).getY() + 1);
            double maxZ = Math.max(mp.getMaster().getPOS(1).getZ() + 1, mp.getMaster().getPOS(2).getZ() + 1);

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
