package josegamerpt.realregions.listeners;

import josegamerpt.realregions.classes.RRParticle;
import josegamerpt.realregions.classes.Region;
import josegamerpt.realregions.managers.WorldManager;
import josegamerpt.realregions.utils.Particles;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class WorldListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Region r = WorldManager.isLocationInRegion(event.getLocation());
        if (!r.entityspawning && event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CUSTOM)) {
            event.getEntity().remove();
            cancel(event.getLocation());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.NORMAL)
    public void explodeHeight(EntityExplodeEvent e) {
        if (e.getEntityType() == EntityType.PRIMED_TNT) {
            Region r = WorldManager.isLocationInRegion(e.getEntity().getLocation());
            if (!r.explosions) {
                e.blockList().clear();
                cancel(e.getEntity().getLocation());
                e.setCancelled(true);
            }
        }
    }

    private void cancel(Location l) {
        Particles.spawnParticle(RRParticle.FLAME_CANCEL, l.getBlock().getLocation());
        l.getWorld().playSound(l, Sound.BLOCK_ANVIL_BREAK, 1, 50);
    }
}
