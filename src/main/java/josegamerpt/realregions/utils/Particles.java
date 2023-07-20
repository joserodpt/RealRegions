package josegamerpt.realregions.utils;

import josegamerpt.realregions.config.Config;
import org.bukkit.Location;
import org.bukkit.Particle;

public class Particles {
    public enum RRParticle { FLAME_CANCEL, LAVA }

    public static void spawnParticle(RRParticle p, Location l)
    {
        if (Config.getConfig().getBoolean("RealRegions.Effects.Particles")) {
            switch (p)
            {
                case FLAME_CANCEL:
                    l.getWorld().spawnParticle(Particle.FLAME, l.getX()+ 0.5D, l.getY() + 1D, l.getZ()+ 0.5D, 0, 0.001, 1, 0);
                    break;
                case LAVA:
                    l.getWorld().spawnParticle(Particle.LAVA, l.getX()+ 0.5D, l.getY() + 1D, l.getZ()+ 0.5D, 0, 0.001, 1, 0);
                    break;
            }
        }
    }
}
