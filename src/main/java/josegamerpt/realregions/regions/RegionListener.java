package josegamerpt.realregions.regions;

import josegamerpt.realregions.RealRegions;
import josegamerpt.realregions.config.Config;
import josegamerpt.realregions.utils.Particles;
import josegamerpt.realregions.utils.Text;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class RegionListener implements Listener {

    //world listeners
    @EventHandler(priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Region selected = RealRegions.getPlugin().getWorldManager().getRegionManager().getFirstPriorityRegionContainingLocation(event.getLocation());

        if (selected != null && !selected.hasEntitySpawning()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent e) {
        Location explodeLocation = e.getLocation();
        Region selected = RealRegions.getPlugin().getWorldManager().getRegionManager().getFirstPriorityRegionContainingLocation(explodeLocation);

        if (selected != null && !selected.hasExplosions()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location blockLocation = event.getBlock().getLocation();
        Region selected = RealRegions.getPlugin().getWorldManager().getRegionManager().getFirstPriorityRegionContainingLocation(blockLocation);

        if (selected != null) {
            Player p = event.getPlayer();

            if (p.isOp() || p.hasPermission(RegionFlags.BLOCK_BREAK.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName()))) {
                return;
            }

            if (!selected.hasBlockBreak()) {
                event.setCancelled(true);
                cancel(blockLocation, p, "&cYou can't break that block here.");
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Location blockLocation = event.getBlock().getLocation();
        Region selected = RealRegions.getPlugin().getWorldManager().getRegionManager().getFirstPriorityRegionContainingLocation(blockLocation);

        if (selected != null) {
            Player p = event.getPlayer();

            if (p.isOp() || p.hasPermission(RegionFlags.BLOCK_PLACE.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName()))) {
                return;
            }

            if (!selected.hasBlockPlace()) {
                event.setCancelled(true);
                cancel(blockLocation, p, "&cYou can't place blocks here.");
            }
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        Location entityLocation = event.getEntity().getLocation();
        Region selected = RealRegions.getPlugin().getWorldManager().getRegionManager().getFirstPriorityRegionContainingLocation(entityLocation);

        if (selected != null) {
            if (!(event.getEntity() instanceof Player))
                return;

            Player p = (Player) event.getEntity();

            if (p.isOp() || p.hasPermission(RegionFlags.HUNGER.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName()))) {
                return;
            }

            if (!selected.hasHunger()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        Location itemLocation = e.getItemDrop().getLocation();
        Region selected = RealRegions.getPlugin().getWorldManager().getRegionManager().getFirstPriorityRegionContainingLocation(itemLocation);

        if (selected != null) {
            Player p = e.getPlayer();

            if (p.isOp() || p.hasPermission(RegionFlags.ITEM_DROP.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName()))) {
                return;
            }

            if (!selected.hasItemDrop()) {
                e.setCancelled(true);
                cancel(itemLocation, p, "&cYou can't drop items here.");
            }
        }
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent e) {
        Location entityLocation = e.getEntity().getLocation();
        Region selected = RealRegions.getPlugin().getWorldManager().getRegionManager().getFirstPriorityRegionContainingLocation(entityLocation);

        if (selected != null) {
            if (!(e.getEntity() instanceof Player))
                return;

            Player p = (Player) e.getEntity();

            if (p.isOp() || p.hasPermission(RegionFlags.ITEM_PICKUP.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName()))) {
                return;
            }

            if (!selected.hasItemPickup()) {
                e.setCancelled(true);
                cancel(entityLocation, p, "&cYou can't pick up items here.");
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        Location entityLocation = e.getEntity().getLocation();
        Region selected = RealRegions.getPlugin().getWorldManager().getRegionManager().getFirstPriorityRegionContainingLocation(entityLocation);

        if (selected != null) {
            if (e.getEntity() instanceof Player) {
                Player p = (Player) e.getEntity();

                if (p.isOp() || p.hasPermission(RegionFlags.TAKE_DAMAGE.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName()))) {
                    return;
                }

                if (!selected.hasTakeDamage()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void tp(PlayerTeleportEvent e) {
        Location toLocation = e.getTo();
        Region selected = RealRegions.getPlugin().getWorldManager().getRegionManager().getFirstPriorityRegionContainingLocation(toLocation);

        if (selected != null) {
            Player p = e.getPlayer();

            if (p.isOp() || p.hasPermission(RegionFlags.ENTER.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName()))) {
                return;
            }

            switch (e.getCause()) {
                case CHORUS_FRUIT:
                    if (!selected.hasEnter()) {
                        e.setCancelled(true);

                        cancel(e.getTo(), p, "&cYou can't enter here.");

                        p.getInventory().addItem(new ItemStack(Material.CHORUS_FRUIT));
                    }
                    break;
                case ENDER_PEARL:
                    p.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
                case END_PORTAL:
                case NETHER_PORTAL:
                case END_GATEWAY:
                case SPECTATE:
                    if (!selected.hasEnter()) {
                        e.setCancelled(true);

                        cancel(e.getTo(), p, "&cYou can't enter here.");
                    }
                    break;
            }
        }
    }

    @EventHandler
    public void move(PlayerMoveEvent e) {
        if (e.getFrom().getBlockX() == e.getTo().getBlockX()
                && e.getFrom().getBlockY() == e.getTo().getBlockY()
                && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) {
            return;
        }

        Location toLocation = e.getTo();
        Region selected = RealRegions.getPlugin().getWorldManager().getRegionManager().getFirstPriorityRegionContainingLocation(toLocation);

        if (selected != null) {
            Player p = e.getPlayer();

            if (p.isOp() || p.hasPermission(RegionFlags.ENTER.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName()))) {
                return;
            }

            if (!selected.hasEnter()) {
                Particles.spawnParticle(Particles.RRParticle.LAVA, e.getTo());
                //OLD: p.setVelocity(p.getEyeLocation().getDirection().setY(-0.7D).multiply(-0.7D));

                p.teleport(e.getFrom());  //TODO: better player knockback?

                cancel(e.getTo(), p, "&cYou can't enter here.");
            }
        }
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) {
            return;
        }
        Location clickedBlockLocation = e.getClickedBlock().getLocation();
        Region selected = RealRegions.getPlugin().getWorldManager().getRegionManager().getFirstPriorityRegionContainingLocation(clickedBlockLocation);

        if (selected != null) {
            Player p = e.getPlayer();

            if (p.isOp() || p.hasPermission(RegionFlags.BLOCK_INTERACTIONS.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName())) ||
                    p.hasPermission(RegionFlags.CONTAINER_INTERACTIONS.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName())) ||
                    p.hasPermission(RegionFlags.ACCESS_CRAFTING_TABLES.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName())) ||
                    p.hasPermission(RegionFlags.ACCESS_HOPPERS.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName())) ||
                    p.hasPermission(RegionFlags.ACCESS_CHESTS.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName()))) {
                return;
            }

            if (e.getClickedBlock() == null) {
                return;
            }
            if (e.getHand().equals(EquipmentSlot.HAND)) {
                if (!selected.hasBlockInteract()) {
                    e.setCancelled(true);
                    cancel(clickedBlockLocation, p, "&cYou can't interact with blocks here.");
                }

                Block b = e.getClickedBlock();
                //container

                if (b.getType().name().contains("SHULKER_BOX")) {
                    if (!selected.hasContainerInteract()) {
                        e.setCancelled(true);
                        cancel(clickedBlockLocation, p, "&cYou can't interact with this container.");
                    }
                    return;
                }
                switch (b.getType()) {
                    case ENDER_CHEST:
                    case DROPPER:
                    case DISPENSER:
                    case HOPPER:
                    case BARREL:
                    case CHEST:
                        if (!selected.hasContainerInteract()) {
                            e.setCancelled(true);
                            cancel(clickedBlockLocation, p, "&cYou can't interact with this container.");
                        }
                        break;
                }

                //individual
                switch (b.getType()) {
                    case CRAFTING_TABLE:
                        if (!selected.hasAccessCrafting()) {
                            e.setCancelled(true);
                            cancel(clickedBlockLocation, p, "&cYou can't interact with crafting tables here.");
                        }
                        break;
                    case HOPPER:
                        if (!selected.hasAccessHoppers()) {
                            e.setCancelled(true);
                            cancel(clickedBlockLocation, p, "&cYou can't interact with hoppers here.");
                        }
                        break;
                    case CHEST:
                        if (!selected.hasAccessChests()) {
                            e.setCancelled(true);
                            cancel(clickedBlockLocation, p, "&cYou can't open chests here.");
                        }
                        break;
                }
            }
        }
    }

    @EventHandler
    private void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player))
            return;
        if (!(event.getEntity() instanceof Player))
            return;

        Location entityLocation = event.getEntity().getLocation();
        Region selected = RealRegions.getPlugin().getWorldManager().getRegionManager().getFirstPriorityRegionContainingLocation(entityLocation);

        if (selected != null) {
            Player damager = (Player) event.getDamager();

            if (damager.isOp() || damager.hasPermission(RegionFlags.PVP.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName()))) {
                return;
            }

            //pvp
            if (!selected.hasPVP()) {
                event.setCancelled(true);
                Text.send(damager, "&cYou can't PVP here.");
            }
        }
    }

    @EventHandler
    private void onDamageEntity(EntityDamageByEntityEvent event) {
        Location damagerLocation = event.getDamager().getLocation();
        Region selected = RealRegions.getPlugin().getWorldManager().getRegionManager().getFirstPriorityRegionContainingLocation(damagerLocation);

        if (selected != null) {
            if (!(event.getDamager() instanceof Player))
                return;

            Player damager = (Player) event.getDamager();
            if (damager.isOp() || damager.hasPermission(RegionFlags.PVE.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName()))) {
                return;
            }

            //pve
            if (!selected.hasPVE()) {
                event.setCancelled(true);
                Text.send(damager, "&cYou can't PVE here.");
            }
        }
    }


    private void cancel(Location l, Player p, String s) {
        Text.send(p, s);
        Particles.spawnParticle(Particles.RRParticle.FLAME_CANCEL, l.getBlock().getLocation());

        if (Config.getConfig().getBoolean("RealRegions.Effects.Sounds")) {
            l.getWorld().playSound(l, Sound.BLOCK_ANVIL_BREAK, 1, 50);
        }
    }
}
