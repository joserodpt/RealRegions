package josegamerpt.realregions.utils;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class DistanceCalculator {

    public static double calculateDistance(Entity entity1, Entity entity2) {
        if (entity1.getLocation().getWorld() != entity2.getLocation().getWorld()) {
            return -1;
        }

        // Get the locations of the two entities
        Location location1 = entity1.getLocation();
        Location location2 = entity2.getLocation();

        // Use the distance formula to calculate the distance
        double dx = location2.getX() - location1.getX();
        double dy = location2.getY() - location1.getY();
        double dz = location2.getZ() - location1.getZ();

        double distanceSquared = dx * dx + dy * dy + dz * dz;

        return Math.round(Math.sqrt(distanceSquared) * 100.0) / 100.0;
    }
}
