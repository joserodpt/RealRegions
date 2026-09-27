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
import org.bukkit.ChatColor;
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
                    rra.getRegionManagerAPI().syncRealMinesRegion(e.getMine());
                break;
            case REMOVED:
                if (RRConfig.file().getBoolean("RealRegions.Hooks.RealMines.Import-Mines") && e.getMine().getWorld() != null) {
                    //named without the mine's colours, as the region was created; skipped mines have none to delete
                    final Region r = rra.getRegionManagerAPI().getRegionPlusName(ChatColor.stripColor(e.getMine().getName()) + "@" + e.getMine().getWorld().getName());
                    if (r != null) {
                        rra.getRegionManagerAPI().deleteRegion(r);
                    }
                }
                break;
            case BOUNDS_UPDATED:
                if (RRConfig.file().getBoolean("RealRegions.Hooks.RealMines.Import-Mines")) {
                    //also creates the region of a mine that had no area when it was first seen
                    rra.getRegionManagerAPI().syncRealMinesRegion(e.getMine());
                }
                break;
        }
    }
}
