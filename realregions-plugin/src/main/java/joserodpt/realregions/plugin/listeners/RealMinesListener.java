package joserodpt.realregions.plugin.listeners;

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

import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.event.RealMinesMineChangeEvent;
import joserodpt.realmines.api.event.RealMinesPluginLoadedEvent;
import joserodpt.realregions.api.RealRegionsAPI;
import joserodpt.realregions.api.config.RRConfig;
import joserodpt.realregions.api.regions.Region;
import joserodpt.realregions.api.utils.Cube;
import joserodpt.realregions.api.regions.CuboidRegion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RealMinesListener implements Listener {

    private final RealRegionsAPI rra;
    public RealMinesListener(RealRegionsAPI rra) {
        this.rra = rra;
    }

    @EventHandler
    public void realMinesLoadedEvent(RealMinesPluginLoadedEvent e) {
        rra.setRealMinesAPI(RealMinesAPI.getInstance());
        rra.getLogger().info("Hooked onto RealMines! Version: " + rra.getRealMinesAPI().getVersion());
        if (RRConfig.file().getBoolean("RealRegions.Hooks.RealMines.Import-Mines")) {
            rra.getRegionManagerAPI().checkRealMinesRegions(rra.getRealMinesAPI().getMineManager().getMines());
            rra.getLogger().info("Loaded " + rra.getRealMinesAPI().getMineManager().getRegisteredMines().size() + " mine regions from RealMines.");
        }
    }

    @EventHandler
    public void mineChangeEvent(RealMinesMineChangeEvent e) {
        switch (e.getChangeOperation()) {
            case ADDED:
                if (RRConfig.file().getBoolean("RealRegions.Hooks.RealMines.Import-Mines"))
                    rra.getRegionManagerAPI().createCubeRegionRealMines(e.getMine(), rra.getWorldManagerAPI().getWorld(e.getMine().getWorld()));
                break;
            case REMOVED:
                if (RRConfig.file().getBoolean("RealRegions.Hooks.RealMines.Import-Mines"))
                    rra.getRegionManagerAPI().deleteRegion(rra.getRegionManagerAPI().getRegionPlusName(e.getMine().getName() + "@" + e.getMine().getWorld().getName()));
                break;
            case BOUNDS_UPDATED:
                if (RRConfig.file().getBoolean("RealRegions.Hooks.RealMines.Import-Mines")) {
                    CuboidRegion r = (CuboidRegion) rra.getRegionManagerAPI().getRegionPlusName((e.getMine().getName() + "@" + e.getMine().getWorld().getName()));
                    r.setCube(new Cube(e.getMine().getPOS1(), e.getMine().getPOS2()));
                    r.saveData(Region.RegionData.BOUNDS);
                }
                break;
        }
    }
}
