package josegamerpt.realregions.regions;

import com.google.gson.internal.bind.JsonTreeWriter;
import josegamerpt.realregions.classes.Cube;
import josegamerpt.realregions.classes.RWorld;
import josegamerpt.realregions.utils.CubeVisualizer;
import josegamerpt.realregions.utils.Itens;
import josegamerpt.realregions.utils.Text;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CuboidRegion implements Region {

    private Material icon;
    private String name;
    private String displayname;
    private Cube cube;
    private CubeVisualizer cv;
    private RWorld rw;
    private boolean global, view = false;

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
    public int priority = 10;

    public CuboidRegion(String name, String displayname, RWorld w, Material m) {
        //Global Region
        this.name = name;
        this.displayname = displayname;
        this.global = true;
        this.icon = m;
        this.rw = w;
    }

    @Override
    public Boolean getView()
    {
        return this.view;
    }

    @Override
    public Boolean hasEntitySpawning() {
        return this.entityspawning;
    }

    @Override
    public Boolean hasExplosions() {
        return this.explosions;
    }

    public CuboidRegion(Location pos1, Location pos2, String name, String displayname, RWorld w, Material m, int pri) {
        this.cube = new Cube(pos1, pos2);
        this.displayname = displayname;
        this.rw = w;
        this.icon = m;
        this.priority = pri;
        this.name = name;
        this.cv = new CubeVisualizer(this);
    }

    @Override
    public ItemStack getItem() {
        List<String> desc = new ArrayList<>();
        desc.add("&fPriority: &b" + this.priority);

        if (!this.global) {desc.add("&fBlocks: &b" + getBlockCount() + " &7(&b " + this.cube.getSizeX() + " &fx &b" + this.cube.getSizeY() + " &fx &b" + this.cube.getSizeZ() + " &7)");}

        desc.addAll(Arrays.asList("",
                "&6Flags:",
                "  &fBlock Break: " + boolStyle(this.blockbreak),
                "  &fBlock Place: " + boolStyle(this.blockplace),
                "  &fBlock Interactions: " + boolStyle(this.blockinteract),
                "  &fContainer Interactions: " + boolStyle(this.containerinteract),
                "  &fPVP: " + boolStyle(this.pvp),
                "  &fPVE: " + boolStyle(this.pve),
                "  &fHunger: " + boolStyle(this.hunger),
                "  &fTake Damage: " + boolStyle(this.takedamage),
                "  &fExplosions: " + boolStyle(this.explosions),
                "  &fItem Pickup: " + boolStyle(this.itempickup),
                "  &fItem Drop: " + boolStyle(this.itemdrop),
                "  &fEntity Spawning: " + boolStyle(this.entityspawning),
                "  &fEnter: " + boolStyle(this.enter),
                "  &fAcess Crafting Tables: " + boolStyle(this.accesscrafting),
                "  &fAcess Chests: " + boolStyle(this.accesschests),
                "  &fAcess Hoppers: " + boolStyle(this.accesshoppers),
                "&f",
                "&7Left Click to edit this region.",
                "&7Shift + Left Click to change this region icon.",
                "&7Right Click to visualize this region.",
                "&7Shift + Right Click to change this regions displayname."
        ));

            return Itens.createItem(icon, 1, "&f" + this.displayname + " &7[&b" + getStringType() + "&7]", desc);
    }

    private String boolStyle(boolean a) {
        return a ? "&a✔ enabled" : "&c❌ disabled";
    }

    private int getBlockCount() {
        return this.cube.getVolume();
    }

    private String getStringType() {
        return this.global ? "INFINITE" : this.getType().name();
    }

    @Override
    public Region.Type getType()
    {
        return Type.CUBOID;
    }

    @Override
    public void teleport(Player p) {
        if (this.global) {
            p.teleport(this.rw.getWorld().getSpawnLocation());
        } else {
            p.teleport(this.cube.getCenter());
        }
        Text.send(p, "&fYou teleported to region &b" + this.displayname + "&r &fon &a" + this.rw.getName());
    }

    @Override
    public String getDisplayName() {
        return displayname;
    }

    @Override
    public void setIcon(Material a) {
        this.icon = a;
    }

    @Override
    public void saveData(Region.Data dr) {
        FileConfiguration cfg = rw.getConfig();
        switch (dr) {
            case ICON:
                cfg.set("Regions." + this.name + ".Icon", this.icon.name());
                rw.saveConfig();
                break;
            case REGION:
                cfg.set("Regions." + this.name + ".Type", this.getType().name());
                cfg.set("Regions." + this.name + ".POS.1", Text.locToTex(this.cube.getPOS(1)));
                cfg.set("Regions." + this.name + ".POS.2", Text.locToTex(this.cube.getPOS(2)));
                cfg.set("Regions." + this.name + ".Display-Name", this.displayname);
                cfg.set("Regions." + this.name + ".isGlobal", this.global);
                cfg.set("Regions." + this.name + ".Priority", this.priority);
                cfg.set("Regions." + this.name + ".Icon", this.icon.name());
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
                cfg.set("Regions." + this.name + ".Acess.Crafting-Table", this.accesscrafting);
                cfg.set("Regions." + this.name + ".Acess.Chests", this.accesschests);
                cfg.set("Regions." + this.name + ".Acess.Hoppers", this.accesshoppers);
                rw.saveConfig();
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
                cfg.set("Regions." + this.name + ".Acess.Crafting-Table", this.accesscrafting);
                cfg.set("Regions." + this.name + ".Acess.Chests", this.accesschests);
                cfg.set("Regions." + this.name + ".Acess.Hoppers", this.accesshoppers);
                rw.saveConfig();
                break;
            case SETTINGS:
                cfg.set("Regions." + this.name + ".Type", this.getType().name());
                cfg.set("Regions." + this.name + ".Display-Name", this.displayname);
                cfg.set("Regions." + this.name + ".isGlobal", this.global);
                cfg.set("Regions." + this.name + ".Priority", this.priority);
                cfg.set("Regions." + this.name + ".Icon", this.icon.name());
                rw.saveConfig();
                break;
        }
    }

    @Override
    public RWorld getWorld() {
        return this.rw;
    }

    @Override
    public void toggleVisual(Player p) {
        if (this.global)
        {
            Text.send(p, "&fYou &ccant &fvisualize this region because its a global region.");
            return;
        }

        this.view = !this.view;
        Text.send(p, "&fVisualizing " + this.displayname + ": " + boolStyle(this.view));
    }

    @Override
    public Cube getMaster() {
        return this.cube;
    }

    @Override
    public CubeVisualizer getViewingMaster() {
        return this.cv;
    }

    @Override
    public boolean canVisualize() {
        return !this.global;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isGlobal() {
        return this.global;
    }

    @Override
    public void setDisplayName(String s) {
        this.displayname = s;
    }

    @Override
    public void setPriority(Integer a) {
        this.priority = a;
    }

    @Override
    public int getPriority()
    {
        return this.priority;
    }

    @Override
    public boolean isLocationInRegion(Location l) {
        if (this.global)
        {
            return this.getWorld().getWorld() == l.getWorld();
        } else {
            return this.cube.contains(l);
        }
    }

    @Override
    public boolean hasBlockBreak() {
        return this.blockbreak;
    }

    @Override
    public boolean hasBlockPlace() {
        return this.blockplace;
    }

    @Override
    public boolean hasHunger() {
        return this.hunger;
    }

    @Override
    public boolean hasItemDrop() {
        return this.itemdrop;
    }

    @Override
    public boolean hasItemPickup() {
        return this.itempickup;
    }

    @Override
    public boolean hasTakeDamage() {
        return this.takedamage;
    }

    @Override
    public boolean hasEnter() {
        return this.enter;
    }

    @Override
    public boolean hasBlockInteract() {
        return this.blockinteract;
    }

    @Override
    public boolean hasContainerInteract() {
        return this.containerinteract;
    }

    @Override
    public boolean hasAccessCrafting() {
        return this.accesscrafting;
    }

    @Override
    public boolean hasAccessHoppers() {
        return this.accesshoppers;
    }

    @Override
    public boolean hasAccessChests() {
        return this.accesschests;
    }

    @Override
    public boolean hasPVP() {
        return this.pvp;
    }

    @Override
    public boolean hasPVE() {
        return this.pve;
    }

    @Override
    public void setBlockBreak(boolean b) {
        this.blockbreak = b;
    }

    @Override
    public void setBlockPlace(boolean b) {
        this.blockplace = b;
    }

    @Override
    public void setHunger(boolean b) {
        this.hunger = b;
    }

    @Override
    public void setItemDrop(boolean b) {
        this.itemdrop = b;
    }

    @Override
    public void setItemPickup(boolean b) {
        this.itempickup = b;
    }

    @Override
    public void setTakeDamage(boolean b) {
        this.takedamage = b;
    }

    @Override
    public void setEnter(boolean b) {
        this.enter = b;
    }

    @Override
    public void setBlockInteract(boolean b) {
        this.blockinteract = b;
    }

    @Override
    public void setContainerInteract(boolean b) {
        this.containerinteract = b;
    }

    @Override
    public void setAccessCrafting(boolean b) {
        this.accesscrafting = b;
    }

    @Override
    public void setAccessHoppers(boolean b) {
        this.accesshoppers = b;
    }

    @Override
    public void setAccessChests(boolean b) {
        this.accesschests = b;
    }

    @Override
    public void setPVP(boolean b) {
        this.pvp = b;
    }

    @Override
    public void setPVE(boolean b) {
        this.pve = b;
    }

    @Override
    public void setEntitySpawning(boolean b) {
        this.entityspawning = b;
    }

    @Override
    public void setExplosions(boolean b) {
        this.explosions = b;
    }
}
