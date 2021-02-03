package josegamerpt.realregions.utils;

import org.bukkit.util.Vector;

public class PlayerMovement {

    public static MOVType movementType(Vector moving, Vector looking, Boolean sprint) {
        Vector diff = moving.subtract(looking);

        if (diff.length() > 0 && diff.length() < 1.2) {
            return MOVType.BACK;
        }
        if (diff.length() > 1.3 && diff.length() < 1.5) {
            return MOVType.SIDE;
        } else if (diff.length() > 1.8 && diff.length() <= 2) {
            return sprint ? MOVType.SPRINT_FRONT : MOVType.FRONT;
        }
        if (Double.isNaN(diff.getX()) || Double.isNaN(diff.getY()) || Double.isNaN(diff.getZ())) {
            return MOVType.ROTATION;
        }
        return null;
    }

    public enum MOVType {BACK, SIDE, SPRINT_FRONT, FRONT, ROTATION}

}
