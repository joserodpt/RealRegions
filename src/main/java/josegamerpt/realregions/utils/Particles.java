package josegamerpt.realregions.utils;

import josegamerpt.realregions.enums.RRParticle;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;

public class Particles {

    public static void spawnParticle(RRParticle p, Location l)
    {
        switch (p)
        {
            case FLAME_CANCEL:
                l.getWorld().spawnParticle(Particle.REDSTONE, l.getX()+ 0.5D, l.getY() + 1.5D, l.getZ()+ 0.5D, 0, 0.001, 1, 0, 1, new Particle.DustOptions(Color.BLACK, 1));
                break;
            case BARRIER:
                l.getWorld().spawnParticle(Particle.BARRIER, l.getX()+ 0.5D, l.getY() + 1.5D, l.getZ()+ 0.5D, 0, 0.001, 1, 0);
                break;
        }
    }

}
