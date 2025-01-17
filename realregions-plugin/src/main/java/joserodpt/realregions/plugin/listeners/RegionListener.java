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
import joserodpt.realregions.api.config.RRConfig;
import joserodpt.realregions.api.config.TranslatableLine;
import joserodpt.realregions.plugin.RealRegionsPlugin;
import joserodpt.realregions.api.regions.Region;
import joserodpt.realregions.api.regions.RegionFlags;
import joserodpt.realregions.api.utils.Particles;
import joserodpt.realregions.api.utils.Text;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class RegionListener implements Listener {
    private final RealRegionsAPI rr;

    public RegionListener(RealRegionsAPI rr) {
        this.rr = rr;
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent e) {
        if (e.getPlayer().isOp() && rr.hasNewUpdate()) {
            Text.send(e.getPlayer(), "&6&LWARNING! &r&fThere is a new update available for RealRegions! https://www.spigotmc.org/resources/111629/");
        }
    }

    //world listeners
    @EventHandler(priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Region selected = rr.getRegionManagerAPI().getFirstPriorityRegionContainingLocation(event.getLocation());

        if (selected != null && !selected.entitySpawning) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLeafDecay(LeavesDecayEvent event) {
        Region selected = rr.getRegionManagerAPI().getFirstPriorityRegionContainingLocation(event.getBlock().getLocation());

        if (selected != null && !selected.leafDecay) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent e) {
        Location explodeLocation = e.getLocation();
        Region selected = rr.getRegionManagerAPI().getFirstPriorityRegionContainingLocation(explodeLocation);

        if (selected != null && !selected.explosions) {
            e.setCancelled(true);
        }
    }

    //player listeners

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location blockLocation = event.getBlock().getLocation();
        Region selected = rr.getRegionManagerAPI().getFirstPriorityRegionContainingLocation(blockLocation);

        if (selected != null) {
            Player p = event.getPlayer();

            if (p.isOp() || p.hasPermission(RegionFlags.BLOCK_BREAK.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName()))) {
                return;
            }

            if (!selected.blockBreak) {
                event.setCancelled(true);
                cancelEvent(blockLocation, p, TranslatableLine.REGION_CANT_BREAK_BLOCK.get());
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Location blockLocation = event.getBlock().getLocation();
        Region selected = rr.getRegionManagerAPI().getFirstPriorityRegionContainingLocation(blockLocation);

        if (selected != null) {
            Player p = event.getPlayer();

            if (p.isOp() || p.hasPermission(RegionFlags.BLOCK_PLACE.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName()))) {
                return;
            }

            if (!selected.blockPlace) {
                event.setCancelled(true);
                cancelEvent(blockLocation, p, TranslatableLine.REGION_CANT_PLACE_BLOCK.get());
            }
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        Location entityLocation = event.getEntity().getLocation();
        Region selected = rr.getRegionManagerAPI().getFirstPriorityRegionContainingLocation(entityLocation);

        if (selected != null) {
            if (!(event.getEntity() instanceof Player))
                return;

            Player p = (Player) event.getEntity();

            if (p.isOp() || p.hasPermission(RegionFlags.HUNGER.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName()))) {
                return;
            }

            if (!selected.hunger) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        Location itemLocation = e.getItemDrop().getLocation();
        Region selected = rr.getRegionManagerAPI().getFirstPriorityRegionContainingLocation(itemLocation);

        if (selected != null) {
            Player p = e.getPlayer();

            if (p.isOp() || p.hasPermission(RegionFlags.ITEM_DROP.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName()))) {
                return;
            }

            if (!selected.itemDrop) {
                e.setCancelled(true);
                cancelEvent(itemLocation, p, TranslatableLine.REGION_CANT_DROP_ITEMS.get());
                return;
            }

            //verificar onde é que o item aterra
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (e.getItemDrop().isOnGround()) {
                        Region landed = rr.getRegionManagerAPI().getFirstPriorityRegionContainingLocation(e.getItemDrop().getLocation());
                        if (landed != null && !landed.itemDrop) {
                            ItemStack tmp = e.getItemDrop().getItemStack();
                            e.getItemDrop().remove();
                            p.getInventory().addItem(tmp);
                            cancelEvent(itemLocation, p, TranslatableLine.REGION_CANT_DROP_ITEMS.get());
                        }
                        cancel();
                    }
                }
            }.runTaskTimer(RealRegionsPlugin.getPlugin(), 1, 1);

            if (!e.isCancelled() && e.getItemDrop() != null && e.getItemDrop().getItemStack() != null && selected.itemPickupOnlyOwner) {
                Item item = e.getItemDrop();
                item.setMetadata("owner", new FixedMetadataValue(rr.getPlugin(), p.getUniqueId()));
            }
        }
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent e) {
        Location entityLocation = e.getEntity().getLocation();
        Region selected = rr.getRegionManagerAPI().getFirstPriorityRegionContainingLocation(entityLocation);

        if (selected != null) {
            if (!(e.getEntity() instanceof Player))
                return;

            Player p = (Player) e.getEntity();

            if (p.isOp() || p.hasPermission(RegionFlags.ITEM_PICKUP.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName()))) {
                return;
            }

            if (!selected.itemPickup) {
                e.setCancelled(true);
                cancelEvent(entityLocation, p, TranslatableLine.REGION_CANT_PICKUP_ITEMS.get());
                return;
            }

            if (!e.isCancelled() && e.getItem() != null && e.getItem().getItemStack() != null && selected.itemPickupOnlyOwner) {
                Item item = e.getItem();
                UUID owner = UUID.fromString(item.getMetadata("owner").get(0).asString());
                if (!owner.equals(p.getUniqueId())) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        commonDamageHandler(e);
    }

    @EventHandler
    public void onDamageByBlock(EntityDamageByBlockEvent e) {
        commonDamageHandler(e);
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        commonDamageHandler(e);
    }

    private void commonDamageHandler(EntityDamageEvent e) {
        Location entityLocation = e.getEntity().getLocation();
        Region selected = rr.getRegionManagerAPI().getFirstPriorityRegionContainingLocation(entityLocation);

        if (selected != null) {
            if (e.getEntity() instanceof Player) {
                Player p = (Player) e.getEntity();

                if (p.isOp() || p.hasPermission(RegionFlags.TAKE_DAMAGE.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName()))) {
                    return;
                }

                if (!selected.takeDamage) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void tp(PlayerTeleportEvent e) {
        Location toLocation = e.getTo();
        Region selected = rr.getRegionManagerAPI().getFirstPriorityRegionContainingLocation(toLocation);

        if (selected != null) {
            Player p = e.getPlayer();

            if (p.isOp() || p.hasPermission(RegionFlags.ENTER.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName()))) {
                return;
            }

            switch (e.getCause()) {
                case CHORUS_FRUIT:
                    if (!selected.enter) {
                        e.setCancelled(true);

                        cancelEvent(e.getTo(), p, TranslatableLine.REGION_CANT_ENTER_HERE.get());

                        p.getInventory().addItem(new ItemStack(Material.CHORUS_FRUIT));
                    }
                    break;
                case ENDER_PEARL:
                    p.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
                case END_PORTAL:
                    if (selected.disabledEndPortal) {
                        e.setCancelled(true);
                        TranslatableLine.REGION_DISABLED_END_PORTAL.send(p);
                    }
                    break;
                case NETHER_PORTAL:
                case END_GATEWAY:
                case SPECTATE:
                    if (!selected.enter) {
                        e.setCancelled(true);

                        cancelEvent(e.getTo(), p, TranslatableLine.REGION_CANT_ENTER_HERE.get());
                    }
                    break;
            }

            notifyRegionChange(p, selected);
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
        Region selected = rr.getRegionManagerAPI().getFirstPriorityRegionContainingLocation(toLocation);

        if (selected != null) {
            Player p = e.getPlayer();

            if (p.isOp() || p.hasPermission(RegionFlags.ENTER.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName()))) {
                return;
            }

            notifyRegionChange(p, selected);

            if (!selected.enter) {
                Particles.spawnParticle(Particles.RRParticle.LAVA, e.getTo());
                //OLD: p.setVelocity(p.getEyeLocation().getDirection().setY(-0.7D).multiply(-0.7D));

                p.teleport(e.getFrom());  //TODO: better player knockback?

                cancelEvent(e.getTo(), p, TranslatableLine.REGION_CANT_ENTER_HERE.get());
            }
        }
    }

    private void notifyRegionChange(Player player, Region r) {
        if (r != null) {
            if (rr.getRegionManagerAPI().getLastRegions().containsKey(player.getUniqueId())) {
                if (rr.getRegionManagerAPI().getLastRegions().get(player.getUniqueId()) != r) {
                    // announce region change via titles
                    if (r.announceEnterTitle)
                        player.sendTitle(TranslatableLine.REGION_ENTERING_TITLE.setV1(TranslatableLine.ReplacableVar.NAME.eq(r.getDisplayName())).get(),
                                TranslatableLine.REGION_ENTERING_SUBTITLE.setV1(TranslatableLine.ReplacableVar.NAME.eq(r.getDisplayName())).get(), 10, 40, 10);

                    if (r.announceEnterActionbar)
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(TranslatableLine.REGION_ENTERING_SUBTITLE.setV1(TranslatableLine.ReplacableVar.NAME.eq(r.getDisplayName())).get()));
                }
                rr.getRegionManagerAPI().getLastRegions().put(player.getUniqueId(), r);
            } else {
                rr.getRegionManagerAPI().getLastRegions().put(player.getUniqueId(), r);
            }
        }
    }

    @EventHandler
    public void onPaintingBreak(HangingBreakByEntityEvent e) {
        Location entityLocation = e.getEntity().getLocation();
        Region selected = rr.getRegionManagerAPI().getFirstPriorityRegionContainingLocation(entityLocation);

        if (selected != null) {
            if (!(e.getRemover() instanceof Player))
                return;

            Player p = (Player) e.getRemover();

            if (p.isOp() || p.hasPermission(RegionFlags.BLOCK_BREAK.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName())) ||
                    p.hasPermission(RegionFlags.BLOCK_INTERACTIONS.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName()))) {
                return;
            }

            if (!selected.blockBreak || !selected.blockInteract) {
                e.setCancelled(true);
                cancelEvent(entityLocation, p, TranslatableLine.REGION_CANT_BREAK_BLOCK.get());
            }
        }
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) {
            return;
        }
        Location clickedBlockLocation = e.getClickedBlock().getLocation();
        Region selected = rr.getRegionManagerAPI().getFirstPriorityRegionContainingLocation(clickedBlockLocation);

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
            if (e.getHand() != null && e.getHand().equals(EquipmentSlot.HAND)) {
                if (!selected.blockInteract) {
                    e.setCancelled(true);
                    cancelEvent(clickedBlockLocation, p, TranslatableLine.REGION_CANT_INTERACT_BLOCKS.get());
                }

                Block b = e.getClickedBlock();
                //container

                if (b.getType().name().contains("SHULKER_BOX")) {
                    if (!selected.containerInteract) {
                        e.setCancelled(true);
                        cancelEvent(clickedBlockLocation, p, TranslatableLine.REGION_CANT_INTERACT_CONTAINER.get());
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
                        if (!selected.containerInteract) {
                            e.setCancelled(true);
                            cancelEvent(clickedBlockLocation, p, TranslatableLine.REGION_CANT_INTERACT_CONTAINER.get());
                        }
                        break;
                }

                //individual
                switch (b.getType()) {
                    case CRAFTING_TABLE:
                        if (!selected.accessCrafting) {
                            e.setCancelled(true);
                            cancelEvent(clickedBlockLocation, p, TranslatableLine.REGION_CANT_INTERACT_CRAFTING_TABLES.get());
                        }
                        break;
                    case HOPPER:
                        if (!selected.accessHoppers) {
                            e.setCancelled(true);
                            cancelEvent(clickedBlockLocation, p, TranslatableLine.REGION_CANT_INTERACT_HOPPER.get());
                        }
                        break;
                    case CHEST:
                        if (!selected.accessChests) {
                            e.setCancelled(true);
                            cancelEvent(clickedBlockLocation, p, TranslatableLine.REGION_CANT_OPEN_CHEST.get());
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
        Region selected = rr.getRegionManagerAPI().getFirstPriorityRegionContainingLocation(entityLocation);

        if (selected != null) {
            Player damager = (Player) event.getDamager();

            if (damager.isOp() || damager.hasPermission(RegionFlags.PVP.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName()))) {
                return;
            }

            //pvp
            if (!selected.pvp) {
                event.setCancelled(true);
                if (!RRConfig.file().getBoolean("RealRegions.Disable-Alert-Messages")) {
                    TranslatableLine.REGION_CANT_PVP.send(damager);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onChat(AsyncPlayerChatEvent e) {
        Region selected = rr.getRegionManagerAPI().getFirstPriorityRegionContainingLocation(e.getPlayer().getLocation());

        if (selected != null) {
            Player p = e.getPlayer();

            if (p.isOp() || p.hasPermission(RegionFlags.NO_CHAT.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName()))) {
                return;
            }

            if (selected.noChat) {
                e.setCancelled(true);
                if (!RRConfig.file().getBoolean("RealRegions.Disable-Alert-Messages")) {
                    TranslatableLine.REGION_CANT_CHAT.send(p);
                }
            }
        }
    }

    @EventHandler
    public void onConsumeItem(PlayerItemConsumeEvent e) {
        Region selected = rr.getRegionManagerAPI().getFirstPriorityRegionContainingLocation(e.getPlayer().getLocation());

        if (selected != null) {
            Player p = e.getPlayer();

            if (p.isOp() || p.hasPermission(RegionFlags.NO_CONSUMABLES.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName()))) {
                return;
            }

            if (selected.noConsumables) {
                e.setCancelled(true);
                if (!RRConfig.file().getBoolean("RealRegions.Disable-Alert-Messages")) {
                    TranslatableLine.REGION_CANT_CONSUME.send(p);
                }
            }
        }
    }

    @EventHandler
    public void onNetherPortalEnter(PlayerPortalEvent e) {
        Region selected = rr.getRegionManagerAPI().getFirstPriorityRegionContainingLocation(e.getPlayer().getLocation());

        if (selected != null) {
            Player p = e.getPlayer();

            if (p.isOp() || p.hasPermission(RegionFlags.DISABLED_NETHER_PORTAL.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName()))) {
                return;
            }

            if (e.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL && selected.disabledNetherPortal) {
                e.setCancelled(true);

                if (!RRConfig.file().getBoolean("RealRegions.Disable-Alert-Messages")) {
                    TranslatableLine.REGION_DISABLED_NETHER_PORTAL.send(p);
                }
            }
        }
    }

    @EventHandler
    public void blockSpreadEvent(BlockSpreadEvent e) {
        Region selected = rr.getRegionManagerAPI().getFirstPriorityRegionContainingLocation(e.getSource().getLocation());
        if (selected != null) {
            if (selected.noFireSpreading) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void onDamageEntity(EntityDamageByEntityEvent event) {
        Location damagerLocation = event.getDamager().getLocation();
        Region selected = rr.getRegionManagerAPI().getFirstPriorityRegionContainingLocation(damagerLocation);

        if (selected != null) {
            if (!(event.getDamager() instanceof Player))
                return;

            Player damager = (Player) event.getDamager();
            if (damager.isOp() || damager.hasPermission(RegionFlags.PVE.getBypassPermission(selected.getRWorld().getRWorldName(), selected.getRegionName()))) {
                return;
            }

            //pve
            if (!selected.pve) {
                event.setCancelled(true);
                if (!RRConfig.file().getBoolean("RealRegions.Disable-Alert-Messages")) {
                    TranslatableLine.REGION_CANT_PVE.send(damager);
                }
            }
        }
    }


    private void cancelEvent(Location l, Player p, String s) {
        if (!RRConfig.file().getBoolean("RealRegions.Disable-Alert-Messages")) {
            Text.send(p, s);
        }

        Particles.spawnParticle(Particles.RRParticle.FLAME_CANCEL, l.getBlock().getLocation());

        if (RRConfig.file().getBoolean("RealRegions.Effects.Sounds")) {
            l.getWorld().playSound(l, Sound.BLOCK_ANVIL_BREAK, 1, 50);
        }
    }
}
