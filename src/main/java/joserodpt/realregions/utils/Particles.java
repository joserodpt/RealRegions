package joserodpt.realregions.utils;

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

import joserodpt.realregions.Config;
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
