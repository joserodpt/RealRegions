package joserodpt.realregions.api.regions;

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

import joserodpt.realpermissions.api.pluginhook.ExternalPluginPermission;
import joserodpt.realregions.api.RWorld;
import joserodpt.realregions.api.config.TranslatableLine;
import joserodpt.realregions.api.utils.Itens;
import joserodpt.realregions.api.utils.Text;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Region {

    public enum RegionOrigin {
        REALREGIONS("&fReal&eRegions"),
        REALMINES("&fReal&9Mines");

        private final String s;

        RegionOrigin(String s) {
            this.s = s;
        }

        public String getDisplayName() {
            return s;
        }
    }

    public enum RegionType {CUBOID, INFINITE}

    public enum RegionData {ALL, ICON, SETTINGS, FLAGS, BOUNDS}

    private RegionOrigin regionOrigin = RegionOrigin.REALREGIONS;
    private Material icon;
    private final String name;
    private String displayname;
    private final RWorld rw;
    public boolean announceEnterTitle = false;
    public boolean announceEnterActionbar = false;

    //flags
    public boolean blockBreak = true;
    public boolean blockPlace = true;
    public boolean blockInteract = true;
    public boolean containerInteract = true;
    public boolean pvp = true;
    public boolean pve = true;
    public boolean hunger = true;
    public boolean takeDamage = true;
    public boolean explosions = true;
    public boolean itemPickup = true;
    public boolean itemDrop = true;
    public boolean entitySpawning = true;
    public boolean enter = true;
    public boolean accessCrafting = true;
    public boolean accessChests = true;
    public boolean accessHoppers = true;
    public boolean noChat = false;
    public boolean noConsumables = false;
    public boolean disabledNetherPortal = false;
    public boolean disabledEndPortal = false;
    public boolean noFireSpreading = false;
    public boolean leafDecay = false;
    public boolean itemPickupOnlyOwner = false;

    public int priority;

    //infinite
    public Region(String name, RWorld w, Material m) {
        this(name, w, m, RegionType.INFINITE);
    }

    //custom type main constructor
    public Region(String name, RWorld w, Material m, RegionType rt) {
        this.name = name;
        this.icon = m;
        this.rw = w;
        this.displayname = rw.getConfig().getString("Regions." + name + ".Display-Name", name);
        this.priority = rw.getConfig().getInt("Regions." + name + ".Priority", 0);

        //load region flags
        this.loadRegionFlags();

        if (rt == RegionType.INFINITE) {
            //save region
            this.saveData(RegionData.ALL);
        }
    }

    private void loadRegionFlags() {
        //load region flags
        this.blockInteract = rw.getConfig().getBoolean("Regions." + this.getRegionName() + ".Block.Interact");
        this.containerInteract = rw.getConfig().getBoolean("Regions." + this.getRegionName() + ".Container.Interact");
        this.blockBreak = rw.getConfig().getBoolean("Regions." + this.getRegionName() + ".Block.Break");
        this.blockPlace = rw.getConfig().getBoolean("Regions." + this.getRegionName() + ".Block.Place");
        this.pvp = rw.getConfig().getBoolean("Regions." + this.getRegionName() + ".PVP");
        this.pve = rw.getConfig().getBoolean("Regions." + this.getRegionName() + ".PVE");
        this.hunger = rw.getConfig().getBoolean("Regions." + this.getRegionName() + ".Hunger");
        this.takeDamage = rw.getConfig().getBoolean("Regions." + this.getRegionName() + ".Damage");
        this.explosions = rw.getConfig().getBoolean("Regions." + this.getRegionName() + ".Explosions");
        this.itemDrop = rw.getConfig().getBoolean("Regions." + this.getRegionName() + ".Item.Drop");
        this.itemPickup = rw.getConfig().getBoolean("Regions." + this.getRegionName() + ".Item.Pickup");
        this.entitySpawning = rw.getConfig().getBoolean("Regions." + this.getRegionName() + ".Entity-Spawning");
        this.enter = rw.getConfig().getBoolean("Regions." + this.getRegionName() + ".Enter");
        this.accessCrafting = rw.getConfig().getBoolean("Regions." + this.getRegionName() + ".Access.Crafting-Table");
        this.accessChests = rw.getConfig().getBoolean("Regions." + this.getRegionName() + ".Access.Chests");
        this.accessHoppers = rw.getConfig().getBoolean("Regions." + this.getRegionName() + ".Access.Hoppers");

        //failsafe if it doesn't exist, they're new entries
        this.announceEnterTitle = rw.getConfig().getBoolean("Regions." + this.getRegionName() + ".Announce-Enter.Title", false);
        this.announceEnterActionbar = rw.getConfig().getBoolean("Regions." + this.getRegionName() + ".Announce-Enter.Actionbar", false);
        this.noChat = rw.getConfig().getBoolean("Regions." + this.getRegionName() + ".No-Chat", false);
        this.noConsumables = rw.getConfig().getBoolean("Regions." + this.getRegionName() + ".No-Consumables", false);
        this.disabledNetherPortal = rw.getConfig().getBoolean("Regions." + this.getRegionName() + ".Disabled-Nether-Portal", false);
        this.disabledEndPortal = rw.getConfig().getBoolean("Regions." + this.getRegionName() + ".Disabled-End-Portal", false);
        this.noFireSpreading = rw.getConfig().getBoolean("Regions." + this.getRegionName() + ".No-Fire-Spreading", false);
        this.itemPickupOnlyOwner = rw.getConfig().getBoolean("Regions." + this.getRegionName() + ".Item-Pickup-Only-Owner", false);

        this.saveData(Region.RegionData.FLAGS);
    }

    public void saveData(Region.RegionData dr) {
        FileConfiguration cfg = rw.getConfig();
        switch (dr) {
            case ICON:
                cfg.set("Regions." + this.name + ".Icon", this.icon.name());
                break;
            case FLAGS:
                cfg.set("Regions." + this.name + ".Block.Interact", this.blockInteract);
                cfg.set("Regions." + this.name + ".Block.Break", this.blockBreak);
                cfg.set("Regions." + this.name + ".Block.Place", this.blockPlace);
                cfg.set("Regions." + this.name + ".Container.Interact", this.containerInteract);
                cfg.set("Regions." + this.name + ".PVP", this.pvp);
                cfg.set("Regions." + this.name + ".PVE", this.pve);
                cfg.set("Regions." + this.name + ".Hunger", this.hunger);
                cfg.set("Regions." + this.name + ".Damage", this.takeDamage);
                cfg.set("Regions." + this.name + ".Explosions", this.explosions);
                cfg.set("Regions." + this.name + ".Item.Drop", this.itemDrop);
                cfg.set("Regions." + this.name + ".Item.Pickup", this.itemPickup);
                cfg.set("Regions." + this.name + ".Entity-Spawning", this.entitySpawning);
                cfg.set("Regions." + this.name + ".Enter", this.enter);
                cfg.set("Regions." + this.name + ".Access.Crafting-Table", this.accessCrafting);
                cfg.set("Regions." + this.name + ".Access.Chests", this.accessChests);
                cfg.set("Regions." + this.name + ".Access.Hoppers", this.accessHoppers);
                cfg.set("Regions." + this.name + ".No-Chat", this.noChat);
                cfg.set("Regions." + this.name + ".No-Consumables", this.noConsumables);
                cfg.set("Regions." + this.name + ".Disabled-Nether-Portal", this.disabledNetherPortal);
                cfg.set("Regions." + this.name + ".Disabled-End-Portal", this.disabledEndPortal);
                cfg.set("Regions." + this.name + ".No-Fire-Spreading", this.noFireSpreading);
                cfg.set("Regions." + this.name + ".Item-Pickup-Only-Owner", this.itemPickupOnlyOwner);
                break;
            case SETTINGS:
                cfg.set("Regions." + this.name + ".Type", this.getType().name());
                cfg.set("Regions." + this.name + ".Display-Name", this.displayname);
                cfg.set("Regions." + this.name + ".Priority", this.priority);
                cfg.set("Regions." + this.name + ".Icon", this.icon.name());
                cfg.set("Regions." + this.name + ".Announce-Enter.Title", this.announceEnterTitle);
                cfg.set("Regions." + this.name + ".Announce-Enter.Actionbar", this.announceEnterActionbar);
                if (this.getOrigin() != RegionOrigin.REALREGIONS)
                    cfg.set("Regions." + this.name + ".Origin", this.getOrigin().name());
                break;
            case BOUNDS:
                if (this instanceof CuboidRegion) {
                    CuboidRegion cr = (CuboidRegion) this;
                    if (cr.getCube() == null || cr.getCube().getPOS1() == null || cr.getCube().getPOS2() == null) {
                        return;
                    }
                    cfg.set("Regions." + this.getRegionName() + ".POS.1", Text.locToTex(cr.getCube().getPOS1()));
                    cfg.set("Regions." + this.getRegionName() + ".POS.2", Text.locToTex(cr.getCube().getPOS2()));
                }
                break;
            case ALL:
                this.saveData(RegionData.ICON);
                this.saveData(RegionData.FLAGS);
                this.saveData(RegionData.SETTINGS);
                this.saveData(RegionData.BOUNDS);
                break;
        }

        rw.saveConfig();
    }

    public void setupDefaultConfig() {
        rw.getConfig().set("Regions." + this.getRegionName() + ".Block.Interact", true);
        rw.getConfig().set("Regions." + this.getRegionName() + ".Block.Break", true);
        rw.getConfig().set("Regions." + this.getRegionName() + ".Block.Place", true);
        rw.getConfig().set("Regions." + this.getRegionName() + ".Container.Interact", true);
        rw.getConfig().set("Regions." + this.getRegionName() + ".PVP", true);
        rw.getConfig().set("Regions." + this.getRegionName() + ".PVE", true);
        rw.getConfig().set("Regions." + this.getRegionName() + ".Hunger", true);
        rw.getConfig().set("Regions." + this.getRegionName() + ".Damage", true);
        rw.getConfig().set("Regions." + this.getRegionName() + ".Explosions", true);
        rw.getConfig().set("Regions." + this.getRegionName() + ".Item.Drop", true);
        rw.getConfig().set("Regions." + this.getRegionName() + ".Item.Pickup", true);
        rw.getConfig().set("Regions." + this.getRegionName() + ".Entity-Spawning", true);
        rw.getConfig().set("Regions." + this.getRegionName() + ".Enter", true);
        rw.getConfig().set("Regions." + this.getRegionName() + ".Create-Portal", true);
        rw.getConfig().set("Regions." + this.getRegionName() + ".Access.Crafting-Table", true);
        rw.getConfig().set("Regions." + this.getRegionName() + ".Access.Chests", true);
        rw.getConfig().set("Regions." + this.getRegionName() + ".Access.Hoppers", true);

        loadRegionFlags();
    }

    public RegionType getType() {
        return RegionType.INFINITE;
    }

    public RegionOrigin getOrigin() {
        return regionOrigin;
    }

    public void setOrigin(RegionOrigin regionOrigin) {
        this.regionOrigin = regionOrigin;
    }

    public ItemStack getItem() {
        List<String> desc = new ArrayList<>();
        desc.add("&fPriority: &b" + this.priority);

        // Add dimensions and volume if applicable
        if (this instanceof CuboidRegion) {
            CuboidRegion cr = (CuboidRegion) this;
            desc.add("&fBlocks: &b" + cr.getCube().getVolume() + " &7(&b "
                    + cr.getCube().getSizeX() + " &fx &b"
                    + cr.getCube().getSizeY() + " &fx &b"
                    + cr.getCube().getSizeZ() + " &7)");
        }

        // Add flags
        desc.addAll(flagsList(
                Text.styleBoolean(this.accessChests),
                Text.styleBoolean(this.accessCrafting),
                Text.styleBoolean(this.accessHoppers),
                Text.styleBoolean(this.blockBreak),
                Text.styleBoolean(this.blockInteract),
                Text.styleBoolean(this.blockPlace),
                Text.styleBoolean(this.containerInteract),
                Text.styleBoolean(this.entitySpawning),
                Text.styleBoolean(this.enter),
                Text.styleBoolean(this.explosions),
                Text.styleBoolean(this.hunger),
                Text.styleBoolean(this.itemDrop),
                Text.styleBoolean(this.itemPickup),
                Text.styleBoolean(this.pve),
                Text.styleBoolean(this.pvp),
                Text.styleBoolean(this.takeDamage),
                Text.styleBoolean(this.noChat),
                Text.styleBoolean(this.noConsumables),
                Text.styleBoolean(this.noFireSpreading),
                Text.styleBoolean(this.disabledNetherPortal),
                Text.styleBoolean(this.disabledEndPortal),
                Text.styleBoolean(this.leafDecay)
        ));

        // Add a special message if the origin is REALMINES
        if (Objects.requireNonNull(this.getOrigin()) == RegionOrigin.REALMINES) {
            desc.add("&7This region was imported from " + this.getOrigin().getDisplayName() + "&r&7. Delete it there.");
        }

        // Determine the region type display
        String typeDisplay = this.getType() == RegionType.INFINITE ? "INFINITE" : this.getType().name();

        // Create and return the item
        return Itens.createItem(
                getIcon(),
                1,
                "&f" + getDisplayName() + " &7[&b" + typeDisplay + "&7]",
                desc
        );
    }

    public List<String> flagsList(String s, String s2, String s3, String s4, String s5, String s6, String s7, String s8, String s9, String s10, String s11, String s12, String s13, String s14, String s15, String s16, String s17, String s18, String s19, String s20, String s21, String s22) {
        return Arrays.asList("",
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
                " &fNo Chat: " + s17,
                " &fNo Consumables: " + s18,
                " &fDisable Fire Spreading: " + s19,
                " &fDisable Nether Portal: " + s20,
                " &fDisable End Portal: " + s21,
                " &fLeaf Decay: " + s22,

                "&f",
                "&7Left Click to edit this region.",
                "&7Shift + Left Click to change this region icon.",
                "&7Right Click to visualize this region.",
                "&7Shift + Right Click to change this regions displayname.",
                "&cQ to delete this region."
        );
    }

    public void teleport(Player p, boolean silent) {
        if (!this.getRWorld().isLoaded()) {
            TranslatableLine.REGION_TP_UNLOADED_WORLD.send(p);
            return;
        }

        p.teleport(this.rw.getWorld().getSpawnLocation());
        if (!silent) {
            TranslatableLine.REGION_TP.setV1(TranslatableLine.ReplacableVar.NAME.eq(this.displayname)).setV2(TranslatableLine.ReplacableVar.WORLD.eq(this.getRWorld().getRWorldName())).send(p);
        }
    }

    public List<ExternalPluginPermission> getRegionBypassPermissions() {
        return Arrays.stream(RegionFlags.values())
                .map(value -> new ExternalPluginPermission(
                        value.getBypassPermission(this.getRWorld().getRWorldName(), this.getRegionName()),
                        "Bypass permission for region: " + this.getRegionName() + " in world: " + this.getRWorld().getRWorldName()))
                .collect(Collectors.toList());
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

    public boolean canVisualize() {
        return false;
    }

    public String getRegionName() {
        return this.name;
    }

    public String getRegionNamePlusWorld() {
        return this.name + "@" + this.getRWorld().getRWorldName();
    }

    public void setDisplayName(String s) {
        this.displayname = s;
    }

    public void setPriority(Integer a) {
        this.priority = a;
    }

    public int getPriority() {
        return this.priority;
    }

    public boolean isLocationInRegion(Location l) {
        return this.getRWorld().getWorld() == l.getWorld();
    }
}
