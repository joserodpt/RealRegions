package josegamerpt.realregions.regions;

import josegamerpt.realregions.RealRegions;
import josegamerpt.realregions.classes.RWorld;
import josegamerpt.realregions.config.Config;
import josegamerpt.realregions.enums.RRParticle;
import josegamerpt.realregions.utils.Itens;
import josegamerpt.realregions.utils.Particles;
import josegamerpt.realregions.utils.Text;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RRegion {

    public enum RegionType {CUBOID, INFINITE}
    public enum RegionData {ALL, ICON, SETTINGS, FLAGS}

    private Material icon;
    private String name;
    private String displayname;
    private final RWorld rw;
    private boolean isBeingVisualized = false;

    //flags
    public boolean blockbreak = true;
    public boolean blockplace = true;
    public boolean blockinteract = true;
    public boolean containerinteract = true;
    public boolean pvp = true;
    public boolean pve = true;
    public boolean hunger = true;
    public boolean takedamage = true;
    public boolean explosions = true;
    public boolean itempickup = true;
    public boolean itemdrop = true;
    public boolean entityspawning = true;
    public boolean enter = true;
    public boolean accesscrafting = true;
    public boolean accesschests = true;
    public boolean accesshoppers = true;
    public int priority;

    public RRegion(String name, String displayname, RWorld w, Material m, int priority, RegionType rt) {
        //Infinite Region
        this.name = name;
        this.displayname = displayname;
        this.icon = m;
        this.rw = w;
        this.priority = priority;

        //register region listener
        RealRegions.getInstance().getPluginManager().registerEvents(getRegionListener(), RealRegions.getInstance());

        if (rt == RegionType.INFINITE) {
            //save region
            this.saveData(RegionData.ALL);
        }
    }
    public Boolean isBeingVisualized()
    {
        return this.isBeingVisualized;
    }

    public void setBeingVisualized(boolean beingVisualized) {
        this.isBeingVisualized = beingVisualized;
    }

    public void saveData(RRegion.RegionData dr) {
        FileConfiguration cfg = rw.getConfig();
        switch (dr) {
            case ICON:
                cfg.set("Regions." + this.name + ".Icon", this.icon.name());
                break;
            case FLAGS:
                cfg.set("Regions." + this.name + ".Block.Interact", this.blockinteract);
                cfg.set("Regions." + this.name + ".Block.Break", this.blockbreak);
                cfg.set("Regions." + this.name + ".Block.Place", this.blockplace);
                cfg.set("Regions." + this.name + ".Container.Interact", this.containerinteract);
                cfg.set("Regions." + this.name + ".PVP", this.pvp);
                cfg.set("Regions." + this.name + ".PVE", this.pve);
                cfg.set("Regions." + this.name + ".Hunger", this.hunger);
                cfg.set("Regions." + this.name + ".Damage", this.takedamage);
                cfg.set("Regions." + this.name + ".Explosions", this.explosions);
                cfg.set("Regions." + this.name + ".Item.Drop", this.itemdrop);
                cfg.set("Regions." + this.name + ".Item.Pickup", this.itempickup);
                cfg.set("Regions." + this.name + ".Entity-Spawning", this.entityspawning);
                cfg.set("Regions." + this.name + ".Enter", this.enter);
                cfg.set("Regions." + this.name + ".Access.Crafting-Table", this.accesscrafting);
                cfg.set("Regions." + this.name + ".Access.Chests", this.accesschests);
                cfg.set("Regions." + this.name + ".Access.Hoppers", this.accesshoppers);
                break;
            case SETTINGS:
                cfg.set("Regions." + this.name + ".Type", this.getType().name());
                cfg.set("Regions." + this.name + ".Display-Name", this.displayname);
                cfg.set("Regions." + this.name + ".Priority", this.priority);
                cfg.set("Regions." + this.name + ".Icon", this.icon.name());
                break;
            case ALL:
                this.saveData(RegionData.ICON);
                this.saveData(RegionData.FLAGS);
                this.saveData(RegionData.SETTINGS);
                break;
        }

        rw.saveConfig();
    }

    public Boolean hasEntitySpawning() {
        return this.entityspawning;
    }

    public Boolean hasExplosions() {
        return this.explosions;
    }

    public RegionType getType()
    {
        return RegionType.INFINITE;
    }

    public ItemStack getItem() {
        List<String> desc = new ArrayList<>();
        desc.add("&fPriority: &b" + this.priority);

        flagsList(desc, Text.styleBoolean(this.accesschests), Text.styleBoolean(this.accesscrafting), Text.styleBoolean(this.accesshoppers), Text.styleBoolean(this.blockbreak), Text.styleBoolean(this.blockinteract), Text.styleBoolean(this.blockplace), Text.styleBoolean(this.containerinteract), Text.styleBoolean(this.entityspawning), Text.styleBoolean(this.enter), Text.styleBoolean(this.explosions), Text.styleBoolean(this.hunger), Text.styleBoolean(this.itemdrop), Text.styleBoolean(this.itempickup), Text.styleBoolean(this.pve), Text.styleBoolean(this.pvp), Text.styleBoolean(this.takedamage));

        return Itens.createItem(getIcon(), 1, "&f" + getDisplayName() + " &7[&b" + (getType() == RegionType.INFINITE ? "INFINITE" : this.getType().name()) + "&7]", desc);
    }

    void flagsList(List<String> desc, String s, String s2, String s3, String s4, String s5, String s6, String s7, String s8, String s9, String s10, String s11, String s12, String s13, String s14, String s15, String s16) {
        desc.addAll(Arrays.asList("",
                "&6Flags:",
                " &fAccess Chests: " + s,
                " &fAccess Crafting Tables: " + s2,
                " &fAccess Hoppers: " + s3,
                " &fBlock Break: " + s4,
                " &fBlock Interactions: " + s5,
                " &fBlock Place: " + s6,
                " &fContainer Interactions: " + s7,
                " &fEntity Spawning: " + s8,
                " &fEnter: " + s9,
                " &fExplosions: " + s10,
                " &fHunger: " + s11,
                " &fItem Drop: " + s12,
                " &fItem Pickup: " + s13,
                " &fPVE: " + s14,
                " &fPVP: " + s15,
                " &fTake Damage: " + s16,
                "&f",
                "&7Left Click to edit this region.",
                "&7Shift + Left Click to change this region icon.",
                "&7Right Click to visualize this region.",
                "&7Shift + Right Click to change this regions displayname.",
                "&cQ to delete this region."
        ));
    }

    public void teleport(Player p) {
        p.teleport(this.rw.getWorld().getSpawnLocation());
        Text.send(p, "&fYou teleported to region &b" + this.displayname + "&r &fon &a" + this.rw.getRWorldName());
    }

    public void setIcon(Material a) {
        this.icon = a;
    }

    public RWorld getRWorld() {
        return this.rw;
    }

    public String getDisplayName() {
        return displayname;
    }

    public Material getIcon() {
        return icon;
    }

    public void toggleVisual(Player p) {
        p.closeInventory();
        Text.send(p, "&fYou &ccant &fvisualize this region because its an infinite region.");
    }

    public boolean canVisualize() {
        return false;
    }

    public String getRegionName() {
        return this.name;
    }

    public void setDisplayName(String s) {
        this.displayname = s;
    }
    public void setPriority(Integer a) {
        this.priority = a;
    }

    public int getPriority()
    {
        return this.priority;
    }

    public boolean isLocationInRegion(Location l) {
        return this.getRWorld().getWorld() == l.getWorld();
    }

    public boolean hasBlockBreak() {
        return this.blockbreak;
    }

    public boolean hasBlockPlace() {
        return this.blockplace;
    }

    public boolean hasHunger() {
        return this.hunger;
    }

    public boolean hasItemDrop() {
        return this.itemdrop;
    }

    public boolean hasItemPickup() {
        return this.itempickup;
    }

    public boolean hasTakeDamage() {
        return this.takedamage;
    }

    public boolean hasEnter() {
        return this.enter;
    }

    public boolean hasBlockInteract() {
        return this.blockinteract;
    }

    public boolean hasContainerInteract() {
        return this.containerinteract;
    }

    public boolean hasAccessCrafting() {
        return this.accesscrafting;
    }

    public boolean hasAccessHoppers() {
        return this.accesshoppers;
    }

    public boolean hasAccessChests() {
        return this.accesschests;
    }

    public boolean hasPVP() {
        return this.pvp;
    }

    public boolean hasPVE() {
        return this.pve;
    }

    public void setBlockBreak(boolean b) {
        this.blockbreak = b;
    }

    public void setBlockPlace(boolean b) {
        this.blockplace = b;
    }

    public void setHunger(boolean b) {
        this.hunger = b;
    }

    public void setItemDrop(boolean b) {
        this.itemdrop = b;
    }

    public void setItemPickup(boolean b) {
        this.itempickup = b;
    }

    public void setTakeDamage(boolean b) {
        this.takedamage = b;
    }

    public void setEnter(boolean b) {
        this.enter = b;
    }

    public void setBlockInteract(boolean b) {
        this.blockinteract = b;
    }

    public void setContainerInteract(boolean b) {
        this.containerinteract = b;
    }

    public void setAccessCrafting(boolean b) {
        this.accesscrafting = b;
    }

    public void setAccessHoppers(boolean b) {
        this.accesshoppers = b;
    }

    public void setAccessChests(boolean b) {
        this.accesschests = b;
    }

    public void setPVP(boolean b) {
        this.pvp = b;
    }

    public void setPVE(boolean b) {
        this.pve = b;
    }

    public void setEntitySpawning(boolean b) {
        this.entityspawning = b;
    }

    public void setExplosions(boolean b) {
        this.explosions = b;
    }

    public Listener getRegionListener() {
        return new Listener() {
            //world listeners
            @EventHandler(priority = EventPriority.HIGH)
            public void onCreatureSpawn(CreatureSpawnEvent event) {
                if (isLocationInRegion(event.getLocation())) {
                    if (hasEntitySpawning()) {
                        event.setCancelled(true);
                    }
                }
            }

            @EventHandler(priority=EventPriority.HIGH)
            public void onEntityExplode(EntityExplodeEvent e) {
                if (isLocationInRegion(e.getLocation())) {
                    if (hasExplosions()) {
                        e.setCancelled(true);
                    }
                }
            }

            //player listeners
            @EventHandler
            public void onBlockBreak(BlockBreakEvent event) {
                if (isLocationInRegion(event.getBlock().getLocation())) {
                    Player p = event.getPlayer();

                    //ignore if the player is OP
                    if (p.isOp()) {
                        return;
                    }

                    if (hasBlockBreak()) {
                        if (p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Block-Breaking.Disallow")) {
                            event.setCancelled(true);
                            cancel(event.getBlock().getLocation(), p, "&cYou cant break that block here.");
                        }
                    } else {
                        if (!p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Block-Breaking.Allow")) {
                            event.setCancelled(true);
                            cancel(event.getBlock().getLocation(), p, "&cYou cant break that block here.");
                        }
                    }
                }
            }

            @EventHandler
            public void onBlockPlace(BlockPlaceEvent event) {
                if (isLocationInRegion(event.getBlock().getLocation())) {
                    Player p = event.getPlayer();
                    if (p.isOp()) {
                        return;
                    }

                    if (hasBlockPlace()) {
                        if (p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Block-Placing.Disallow")) {
                            event.setCancelled(true);
                            cancel(event.getBlock().getLocation(), p, "&cYou cant place blocks here.");
                        }
                    } else {
                        if (!p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Block-Placing.Allow")) {
                            event.setCancelled(true);
                            cancel(event.getBlock().getLocation(), p, "&cYou cant place blocks here.");
                        }
                    }
                }
            }

            @EventHandler
            public void onHunger(FoodLevelChangeEvent event) {
                if (isLocationInRegion(event.getEntity().getLocation())) {

                    if (!(event.getEntity() instanceof Player))
                        return;

                    Player p = (Player) event.getEntity();
                    if (p.isOp()) {
                        return;
                    }

                    if (hasHunger()) {
                        if (p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Hunger.Disallow")) {
                            event.setCancelled(true);
                        }
                    } else {
                        if (!p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Hunger.Allow")) {
                            event.setCancelled(true);
                        }
                    }
                }
            }

            @EventHandler
            public void onItemDrop(PlayerDropItemEvent e) {
                if (isLocationInRegion(e.getItemDrop().getLocation())) {

                    Player p = e.getPlayer();
                    if (p.isOp()) {
                        return;
                    }

                    if (hasItemDrop()) {
                        if (p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Item-Drop.Disallow")) {
                            e.setCancelled(true);
                            cancel(p.getLocation(), p, "&cYou cant drop items here.");
                        }
                    } else {
                        if (!p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Item-Drop.Allow")) {
                            e.setCancelled(true);
                            cancel(p.getLocation(), p, "&cYou cant drop items here.");
                        }
                    }
                }
            }

            @EventHandler
            public void onItemPickup(EntityPickupItemEvent e) {
                if (isLocationInRegion(e.getEntity().getLocation())) {

                    if (!(e.getEntity() instanceof Player))
                        return;

                    Player p = (Player) e.getEntity();
                    if (p.isOp()) {
                        return;
                    }

                    if (hasItemPickup()) {
                        if (p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Item-Pickup.Disallow")) {
                            e.setCancelled(true);
                            cancel(e.getItem().getLocation(), p, "&cYou cant pick up items here.");
                        }
                    } else {
                        if (!p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Item-Pickup.Allow")) {
                            e.setCancelled(true);
                            cancel(e.getItem().getLocation(), p, "&cYou cant pick up items here.");
                        }
                    }
                }
            }

            @EventHandler
            public void onDamage(EntityDamageEvent e) {
                if (isLocationInRegion(e.getEntity().getLocation())) {
                    if (e.getEntity() instanceof Player) {
                        Player p = (Player) e.getEntity();
                        if (p.isOp()) {
                            return;
                        }

                        if (hasTakeDamage()) {
                            if (p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Damage.Disallow")) {
                                e.setCancelled(true);
                            }
                        } else {
                            if (!p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Damage.Allow")) {
                                e.setCancelled(true);
                            }
                        }
                    }
                }
            }

            @EventHandler
            public void tp(PlayerTeleportEvent e) {
                if (isLocationInRegion(e.getTo())) {

                    Player p = e.getPlayer();
                    switch (e.getCause()) {
                        case END_PORTAL:
                        case NETHER_PORTAL:
                        case ENDER_PEARL:
                        case CHORUS_FRUIT:
                            if (hasEnter()) {
                                if (p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Enter.Disallow")) {
                                    cancelMovement(p, e);
                                }
                            } else {
                                if (!p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Enter.Allow")) {
                                    cancelMovement(p, e);
                                }
                            }
                            break;
                    }
                }
            }

            @EventHandler
            public void move(PlayerMoveEvent e) {
                if (isLocationInRegion(e.getTo())) {
                    Player p = e.getPlayer();
                    if (p.isOp()) {
                        return;
                    }

                    if (hasEnter()) {
                        if (p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Enter.Disallow")) {
                            cancelMovement(p, e);
                        }
                    } else {
                        if (!p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Enter.Allow")) {
                            cancelMovement(p, e);
                        }
                    }
                }
            }

            private void cancelMovement(Player p, PlayerMoveEvent event) {
                Particles.spawnParticle(RRParticle.BARRIER, event.getTo());
                p.setVelocity(p.getEyeLocation().getDirection().setY(Config.file().getDouble("RealRegions.Pushback-Movement.Y-Component")).multiply(Config.file().getDouble("RealRegions.Pushback-Movement.Multiplier")));

                cancel(event.getTo(), p, "&cYou cant enter here.");
            }

            @EventHandler
            public void onBlockInteract(PlayerInteractEvent event) {
                if (event.getClickedBlock() == null) {
                    return;
                }
                if (isLocationInRegion(event.getClickedBlock().getLocation())) {

                    Player p = event.getPlayer();
                    if (p.isOp()) {
                        return;
                    }

                    if (event.getClickedBlock() == null) {
                        return;
                    }
                    if (event.getHand().equals(EquipmentSlot.HAND)) {
                        if (hasBlockInteract()) {
                            if (p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Block-Interaction.Disallow")) {
                                event.setCancelled(true);
                                cancel(event.getClickedBlock().getLocation(), p, "&cYou cant interact with blocks here.");
                            }
                        } else {
                            if (!p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Block-Interaction.Allow")) {
                                event.setCancelled(true);
                                cancel(event.getClickedBlock().getLocation(), p, "&cYou cant interact with blocks here.");
                            }
                        }

                        Block b = event.getClickedBlock();
                        //container
                        if (b.getType().name().contains("SHULKER_BOX")) {
                            if (hasContainerInteract()) {
                                if (p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Containers.Disallow")) {
                                    event.setCancelled(true);
                                    cancel(event.getClickedBlock().getLocation(), p, "&cYou cant interact with this container.");
                                }
                            } else {
                                if (!p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Containers.Allow")) {
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
                                if (hasContainerInteract()) {
                                    if (p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Containers.Disallow")) {
                                        event.setCancelled(true);
                                        cancel(event.getClickedBlock().getLocation(), p, "&cYou cant interact with this container.");
                                    }
                                } else {
                                    if (!p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Containers.Allow")) {
                                        event.setCancelled(true);
                                        cancel(event.getClickedBlock().getLocation(), p, "&cYou cant interact with this container.");
                                    }
                                }
                                break;
                        }


                        //individual
                        switch (b.getType()) {
                            case CRAFTING_TABLE:
                                if (hasAccessCrafting()) {
                                    if (p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Crafting.Disallow")) {
                                        event.setCancelled(true);
                                        cancel(event.getClickedBlock().getLocation(), p, "&cYou cant interact with crafting tables here.");
                                    }
                                } else {
                                    if (!p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Crafting.Allow")) {
                                        event.setCancelled(true);
                                        cancel(event.getClickedBlock().getLocation(), p, "&cYou cant interact with crafting tables here.");
                                    }
                                }
                                break;
                            case HOPPER:
                                if (hasAccessHoppers()) {
                                    if (p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Hoppers.Disallow")) {
                                        event.setCancelled(true);
                                        cancel(event.getClickedBlock().getLocation(), p, "&cYou cant interact with hoppers here.");
                                    }
                                } else {
                                    if (!p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Hoppers.Allow")) {
                                        event.setCancelled(true);
                                        cancel(event.getClickedBlock().getLocation(), p, "&cYou cant interact with hoppers here.");
                                    }
                                }
                                break;
                            case CHEST:
                                if (hasAccessChests()) {
                                    if (p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Chests.Disallow")) {
                                        event.setCancelled(true);
                                        cancel(event.getClickedBlock().getLocation(), p, "&cYou cant interact with chest here.");
                                    }
                                } else {
                                    if (!p.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".Chests.Allow")) {
                                        event.setCancelled(true);
                                        cancel(event.getClickedBlock().getLocation(), p, "&cYou cant interact with chest here.");
                                    }
                                }
                                break;
                        }
                    }
                }
            }

            @EventHandler
            private void onDamage(EntityDamageByEntityEvent event) {
                if (isLocationInRegion(event.getEntity().getLocation())) {
                    if (!(event.getDamager() instanceof Player))
                        return;
                    if (!(event.getEntity() instanceof Player))
                        return;

                    Player damager = (Player) event.getDamager();
                    if (damager.isOp()) {
                        return;
                    }

                    //pvp
                   if (hasPVP()) {
                        if (damager.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".PVP.Disallow")) {
                            event.setCancelled(true);
                            Text.send(damager, "&cYou cant PVP here.");
                        }
                    } else {
                        if (!damager.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".PVP.Allow")) {
                            event.setCancelled(true);
                            Text.send(damager, "&cYou cant PVP here.");
                        }
                    }
                }
            }

            @EventHandler
            private void onDamageEntity(EntityDamageByEntityEvent event) {
                if (isLocationInRegion(event.getDamager().getLocation())) {

                    if (!(event.getDamager() instanceof Player))
                        return;

                    Player damager = (Player) event.getDamager();
                    if (damager.isOp()) {
                        return;
                    }

                    //pve
                    if (hasPVE()) {
                        if (damager.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".PVE.Disallow")) {
                            event.setCancelled(true);
                            Text.send(damager, "&cYou cant PVE here.");
                        }
                    } else {
                        if (!damager.hasPermission("RealRegions." + getRWorld().getRWorldName() + "." + getRegionName() + ".PVE.Allow")) {
                            event.setCancelled(true);
                            Text.send(damager, "&cYou cant PVE here.");
                        }
                    }
                }
            }

            private void cancel(Location l, Player p, String s) {
                Text.send(p, s);
                Particles.spawnParticle(RRParticle.FLAME_CANCEL, l.getBlock().getLocation());
                l.getWorld().playSound(l, Sound.BLOCK_ANVIL_BREAK, 1, 50);
            }
        };
    }
}
