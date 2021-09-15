package josegamerpt.realregions.listeners;

import josegamerpt.realregions.RealRegions;
import josegamerpt.realregions.enums.RRParticle;
import josegamerpt.realregions.regions.Region;
import josegamerpt.realregions.utils.Particles;
import josegamerpt.realregions.utils.Text;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();

        if (p.isOp()) {
            return;
        }

        Region r = RealRegions.getWorldManager().isLocationInRegion(event.getBlock().getLocation());
        if (r.hasBlockBreak()) {
            if (p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Block-Breaking.Disallow")) {
                event.setCancelled(true);
                cancel(event.getBlock().getLocation(), p, "&cYou cant break that block here.");
            }
        } else {
            if (!p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Block-Breaking.Allow")) {
                event.setCancelled(true);
                cancel(event.getBlock().getLocation(), p, "&cYou cant break that block here.");
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player p = event.getPlayer();
        if (p.isOp()) {
            return;
        }

        Region r = RealRegions.getWorldManager().isLocationInRegion(event.getBlock().getLocation());
        if (r.hasBlockPlace()) {
            if (p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Block-Placing.Disallow")) {
                event.setCancelled(true);
                cancel(event.getBlock().getLocation(), p, "&cYou cant place blocks here.");
            }
        } else {
            if (!p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Block-Placing.Allow")) {
                event.setCancelled(true);
                cancel(event.getBlock().getLocation(), p, "&cYou cant place blocks here.");
            }
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;

        Player p = (Player) event.getEntity();
        if (p.isOp()) {
            return;
        }

        Region r = RealRegions.getWorldManager().isLocationInRegion(event.getEntity().getLocation());
        if (r.hasHunger()) {
            if (p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Hunger.Disallow")) {
                event.setCancelled(true);
            }
        } else {
            if (!p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Hunger.Allow")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (p.isOp()) {
            return;
        }

        Region r = RealRegions.getWorldManager().isLocationInRegion(p.getLocation());
        if (r.hasItemDrop()) {
            if (p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Item-Drop.Disallow")) {
                e.setCancelled(true);
                cancel(p.getLocation(), p, "&cYou cant drop items here.");
            }
        } else {
            if (!p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Item-Drop.Allow")) {
                e.setCancelled(true);
                cancel(p.getLocation(), p, "&cYou cant drop items here.");
            }
        }
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player))
            return;

        Player p = (Player) e.getEntity();
        if (p.isOp()) {
            return;
        }

        Region r = RealRegions.getWorldManager().isLocationInRegion(p.getLocation());
        if (r.hasItemPickup()) {
            if (p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Item-Pickup.Disallow")) {
                e.setCancelled(true);
                cancel(e.getItem().getLocation(), p, "&cYou cant pick up items here.");
            }
        } else {
            if (!p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Item-Pickup.Allow")) {
                e.setCancelled(true);
                cancel(e.getItem().getLocation(), p, "&cYou cant pick up items here.");
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (p.isOp()) {
                return;
            }

            Region r = RealRegions.getWorldManager().isLocationInRegion(p.getLocation());
            if (r.hasTakeDamage()) {
                if (p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Damage.Disallow")) {
                    e.setCancelled(true);
                }
            } else {
                if (!p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Damage.Allow")) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void tp(PlayerTeleportEvent event) {
        Player p = event.getPlayer();
        Region r = RealRegions.getWorldManager().isLocationInRegion(event.getTo());
        switch (event.getCause()) {
            case ENDER_PEARL:
            case CHORUS_FRUIT:
                if (r.hasEnter()) {
                    if (p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Enter.Disallow")) {
                        cancelMovement(p, event);
                    }
                } else {
                    if (!p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Enter.Allow")) {
                        cancelMovement(p, event);
                    }
                }
                break;
        }
    }

    @EventHandler
    public void move(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        if (p.isOp()) {
            return;
        }

        Region r = RealRegions.getWorldManager().isLocationInRegion(event.getTo());
        if (r.hasEnter()) {
            if (p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Enter.Disallow")) {
                cancelMovement(p, event);
            }
        } else {
            if (!p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Enter.Allow")) {
                cancelMovement(p, event);
            }
        }
    }

    private void cancelMovement(Player p, PlayerMoveEvent event) {
        cancel(event.getTo(), p, "&cYou cant enter here.");
        Particles.spawnParticle(RRParticle.BARRIER, event.getTo());
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (p.isOp()) {
            return;
        }

        if (event.getClickedBlock() == null) {
            return;
        }
        Region r = RealRegions.getWorldManager().isLocationInRegion(event.getClickedBlock().getLocation());
        if (event.getHand().equals(EquipmentSlot.HAND)) {
            if (r.hasBlockInteract()) {
                if (p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Block-Interaction.Disallow")) {
                    event.setCancelled(true);
                    cancel(event.getClickedBlock().getLocation(), p, "&cYou cant interact with blocks here.");
                }
            } else {
                if (!p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Block-Interaction.Allow")) {
                    event.setCancelled(true);
                    cancel(event.getClickedBlock().getLocation(), p, "&cYou cant interact with blocks here.");
                }
            }

            Block b = event.getClickedBlock();
            //container
            if (b.getType().name().contains("SHULKER_BOX"))
            {
                if (r.hasContainerInteract()) {
                    if (p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Containers.Disallow")) {
                        event.setCancelled(true);
                        cancel(event.getClickedBlock().getLocation(), p, "&cYou cant interact with this container.");
                    }
                } else {
                    if (!p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Containers.Allow")) {
                        event.setCancelled(true);
                        cancel(event.getClickedBlock().getLocation(), p, "&cYou cant interact with this container.");
                    }
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
                    if (r.hasContainerInteract()) {
                        if (p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Containers.Disallow")) {
                            event.setCancelled(true);
                            cancel(event.getClickedBlock().getLocation(), p, "&cYou cant interact with this container.");
                        }
                    } else {
                        if (!p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Containers.Allow")) {
                            event.setCancelled(true);
                            cancel(event.getClickedBlock().getLocation(), p, "&cYou cant interact with this container.");
                        }
                    }
                    break;
            }


            //individual
            switch (b.getType()) {
                case CRAFTING_TABLE:
                    if (r.hasAccessCrafting()) {
                        if (p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Crafting.Disallow")) {
                            event.setCancelled(true);
                            cancel(event.getClickedBlock().getLocation(), p, "&cYou cant interact with crafting tables here.");
                        }
                    } else {
                        if (!p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Crafting.Allow")) {
                            event.setCancelled(true);
                            cancel(event.getClickedBlock().getLocation(), p, "&cYou cant interact with crafting tables here.");
                        }
                    }
                    break;
                case HOPPER:
                    if (r.hasAccessHoppers()) {
                        if (p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Hoppers.Disallow")) {
                            event.setCancelled(true);
                            cancel(event.getClickedBlock().getLocation(), p, "&cYou cant interact with hoppers here.");
                        }
                    } else {
                        if (!p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Hoppers.Allow")) {
                            event.setCancelled(true);
                            cancel(event.getClickedBlock().getLocation(), p, "&cYou cant interact with hoppers here.");
                        }
                    }
                    break;
                case CHEST:
                    if (r.hasAccessChests()) {
                        if (p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Chests.Disallow")) {
                            event.setCancelled(true);
                            cancel(event.getClickedBlock().getLocation(), p, "&cYou cant interact with chest here.");
                        }
                    } else {
                        if (!p.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Chests.Allow")) {
                            event.setCancelled(true);
                            cancel(event.getClickedBlock().getLocation(), p, "&cYou cant interact with chest here.");
                        }
                    }
                    break;
            }
        }

    }

    @EventHandler
    private void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player))
            return;
        if (!(event.getEntity() instanceof Player))
            return;

        Player damager = (Player) event.getDamager();
        if (damager.isOp()) {
            return;
        }

        //pvp
        Player damagee = (Player) event.getEntity();
        Region r = RealRegions.getWorldManager().isLocationInRegion(damagee.getLocation());
        if (r.hasPVP()) {
            if (damager.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".PVP.Disallow")) {
                event.setCancelled(true);
                Text.send(damager, "&cYou cant PVP here.");
            }
        } else {
            if (!damager.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".PVP.Allow")) {
                event.setCancelled(true);
                Text.send(damager, "&cYou cant PVP here.");
            }
        }
    }

    @EventHandler
    private void onDamageEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player))
            return;

        Player damager = (Player) event.getDamager();
        if (damager.isOp()) {
            return;
        }

        //pve
        Region r = RealRegions.getWorldManager().isLocationInRegion(damager.getLocation());
        if (r.hasPVE()) {
            if (damager.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".PVE.Disallow")) {
                event.setCancelled(true);
                Text.send(damager, "&cYou cant PVE here.");
            }
        } else {
            if (!damager.hasPermission("RealRegions." + r.getWorld().getName() + "." + r.getName() + ".PVE.Allow")) {
                event.setCancelled(true);
                Text.send(damager, "&cYou cant PVE here.");
            }
        }
    }

    private void cancel(Location l, Player p, String s) {
        Text.send(p, s);
        Particles.spawnParticle(RRParticle.FLAME_CANCEL, l.getBlock().getLocation());
        l.getWorld().playSound(l, Sound.BLOCK_ANVIL_BREAK, 1, 50);
    }
}
