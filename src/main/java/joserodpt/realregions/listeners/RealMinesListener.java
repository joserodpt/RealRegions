package joserodpt.realregions.listeners;

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

import joserodpt.realmines.api.event.RealMinesMineChangeEvent;
import joserodpt.realregions.RealRegions;
import joserodpt.realregions.config.Config;
import joserodpt.realregions.regions.CuboidRegion;
import joserodpt.realregions.regions.Region;
import joserodpt.realregions.utils.Cube;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RealMinesListener implements Listener {

    private final RealRegions rr;
    public RealMinesListener(RealRegions realRegions) {
        this.rr = realRegions;
    }

    @EventHandler
    public void mineChangeEvent(RealMinesMineChangeEvent e) {
        switch (e.getChangeOperation()) {
            case ADDED:
                if (Config.file().getBoolean("RealRegions.Hooks.RealMines.Import-Mines"))
                    rr.getWorldManager().getRegionManager().createCubeRegionRealMines(e.getMine(), rr.getWorldManager().getWorld(e.getMine().getWorld()));
                break;
            case REMOVED:
                if (Config.file().getBoolean("RealRegions.Hooks.RealMines.Import-Mines"))
                    rr.getWorldManager().getRegionManager().deleteRegion(rr.getWorldManager().getRegionManager().getRegionPlusName(e.getMine().getName() + "@" + e.getMine().getWorld().getName()));
                break;
            case BOUNDS_UPDATED:
                if (Config.file().getBoolean("RealRegions.Hooks.RealMines.Import-Mines")) {
                    CuboidRegion r = (CuboidRegion) rr.getWorldManager().getRegionManager().getRegionPlusName((e.getMine().getName() + "@" + e.getMine().getWorld().getName()));
                    r.setCube(new Cube(e.getMine().getPOS1(), e.getMine().getPOS2()));
                    r.saveData(Region.RegionData.BOUNDS);
                }
                break;
        }
    }
}
