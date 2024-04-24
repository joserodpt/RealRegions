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
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealRegions
 */

import joserodpt.realregions.api.RealRegionsAPI;
import joserodpt.realregions.api.regions.RWorld;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class GeneralListener implements Listener {
    private final RealRegionsAPI rra;

    public GeneralListener(RealRegionsAPI rra) {
        this.rra = rra;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void join(PlayerJoinEvent e) {
        tpJoinLogic(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void changeWorld(PlayerChangedWorldEvent e) {
        tpJoinLogic(e.getPlayer());
    }

    private void tpJoinLogic(Player player) {
        World w = player.getWorld();

        RWorld rw = rra.getWorldManagerAPI().getWorld(w);
        if (rw != null && rw.isTPJoinON()) {
            Location loc = rw.getTPJoinLocation();
            if (loc != null) {
                player.teleport(loc);
            }
        }
    }
}
