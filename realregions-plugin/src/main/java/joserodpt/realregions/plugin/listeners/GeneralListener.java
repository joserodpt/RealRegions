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
 * @author José Rodrigues © 2020-2024
 * @link https://github.com/joserodpt/RealRegions
 */

import joserodpt.realregions.api.RealRegionsAPI;
import joserodpt.realregions.api.RWorld;
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
        tpJoinLogic(e.getPlayer(), null);
        rra.getRegionManagerAPI().getLastRegions().put(e.getPlayer().getUniqueId(), rra.getRegionManagerAPI().getFirstPriorityRegionContainingLocation(e.getPlayer().getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void changeWorld(PlayerChangedWorldEvent e) {
        tpJoinLogic(e.getPlayer(), e.getFrom());
    }

    private void tpJoinLogic(Player player, World previousWorld) {
        World w = player.getWorld();

        RWorld rw = rra.getWorldManagerAPI().getWorld(w);
        if (rw == null) {
            return;
        }

        if (rw.isTPJoinON()) {
            Location loc = rw.getTPJoinLocation();
            if (loc != null) {
                player.teleport(loc);
            }
        }

        if (previousWorld == null) {
            return;
        }
        if (!previousWorld.equals(w)) {
            RWorld previousRWorld = rra.getWorldManagerAPI().getWorld(previousWorld);
            if (previousWorld == null) {
                if (previousWorld != null && previousRWorld.hasWorldInventories()) {
                    previousRWorld.saveWorldInventory(player);
                    player.getInventory().clear();
                }
                rw.giveWorldInventory(player);
            }
        }
    }
}
