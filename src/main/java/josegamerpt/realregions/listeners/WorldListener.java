package josegamerpt.realregions.listeners;

import josegamerpt.realregions.RealRegions;
import josegamerpt.realregions.enums.RRParticle;
import josegamerpt.realregions.regions.Region;
import josegamerpt.realregions.utils.Particles;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class WorldListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Region r = RealRegions.getWorldManager().isLocationInRegion(event.getLocation());
        if (r != null && !r.hasEntitySpawning()) {
            event.getEntity().remove();
            cancel(event.getLocation());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void explodeHeight(EntityExplodeEvent e) {
        if (e.getEntityType() == EntityType.PRIMED_TNT) {
            Region r = RealRegions.getWorldManager().isLocationInRegion(e.getEntity().getLocation());
            if (r != null && !r.hasExplosions()) {
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
