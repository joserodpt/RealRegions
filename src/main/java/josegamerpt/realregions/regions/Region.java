package josegamerpt.realregions.regions;

import josegamerpt.realregions.RealRegions;
import josegamerpt.realregions.utils.Itens;
import josegamerpt.realregions.utils.Text;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Region {

    public enum RegionType {CUBOID, INFINITE}
    public enum RegionData {ALL, ICON, SETTINGS, FLAGS}

    private Material icon;
    private final String name;
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
    public Boolean isBeingVisualized()
    {
        return this.isBeingVisualized;
    }

    public void setBeingVisualized(boolean beingVisualized) {
        this.isBeingVisualized = beingVisualized;

        if (this.isBeingVisualized) {
            RealRegions.getPlugin().getWorldManager().getRegionManager().getView().add(this);
        } else {
            RealRegions.getPlugin().getWorldManager().getRegionManager().getView().remove(this);
        }
    }

    public void saveData(Region.RegionData dr) {
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

    public void flagsList(List<String> desc, String s, String s2, String s3, String s4, String s5, String s6, String s7, String s8, String s9, String s10, String s11, String s12, String s13, String s14, String s15, String s16) {
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

    public void teleport(Player p, boolean silent) {
        if (!this.getRWorld().isLoaded()) {
            Text.send(p, "&cYou can't teleport to this region because it belongs to a world that is unloaded.");
            return;
        }

        p.teleport(this.rw.getWorld().getSpawnLocation());
        if (!silent) {
            Text.send(p, "&fYou teleported to region &b" + this.displayname + "&r &fon &a" + this.rw.getRWorldName());
        }
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
        Text.send(p, "&fYou &ccan't &fvisualize this region because its an infinite region.");
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

}
