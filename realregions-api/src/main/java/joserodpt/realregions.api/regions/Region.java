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
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealRegions
 */

import joserodpt.realpermissions.api.pluginhookup.ExternalPluginPermission;
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

    public int priority;

    public Region(String name, String displayname, RWorld w, Material m, int priority, RegionType rt) {
        //Infinite Region
        this.name = name;
        this.displayname = displayname;
        this.icon = m;
        this.rw = w;
        this.priority = priority;

        if (rt == RegionType.INFINITE) {
            //save region
            this.saveData(RegionData.ALL);
        }
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
                break;
            case SETTINGS:
                cfg.set("Regions." + this.name + ".Type", this.getType().name());
                cfg.set("Regions." + this.name + ".Display-Name", this.displayname);
                cfg.set("Regions." + this.name + ".Priority", this.priority);
                cfg.set("Regions." + this.name + ".Icon", this.icon.name());
                if (this.getOrigin() != RegionOrigin.REALREGIONS)
                    cfg.set("Regions." + this.name + ".Origin", this.getOrigin().name());
                break;
            case ALL:
                this.saveData(RegionData.ICON);
                this.saveData(RegionData.FLAGS);
                this.saveData(RegionData.SETTINGS);
                break;
        }

        rw.saveConfig();
    }

    public RegionType getType()
    {
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
        desc.addAll(flagsList(Text.styleBoolean(this.accessChests),
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
                Text.styleBoolean(this.leafDecay)));

        return Itens.createItem(getIcon(), 1, "&f" + getDisplayName() + " &7[&b" + (getType() == RegionType.INFINITE ? "INFINITE" : this.getType().name()) + "&7]", desc);
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
    public int getPriority()
    {
        return this.priority;
    }
    public boolean isLocationInRegion(Location l) {
        return this.getRWorld().getWorld() == l.getWorld();
    }
}
