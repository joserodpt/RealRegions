package josegamerpt.realregions.classes;

import josegamerpt.realregions.RealRegions;
import josegamerpt.realregions.utils.CubeVisualizer;
import josegamerpt.realregions.utils.Itens;
import josegamerpt.realregions.utils.Text;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class Region {

    private Material icon = Material.BARRIER;
    private String name;
    private String displayname;
    private Cube cube;
    private CubeVisualizer cv;
    private RWorld rw;
    private boolean global = false;
    public boolean view = false;

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
    public boolean acesscrafting = true;
    public boolean acesschests = true;
    public boolean acesshoppers = true;
    public int priority = 10;


    public Region(String name, String displayname, RWorld w, Material m) {
        //Global Region
        this.name = name;
        this.displayname = displayname;
        this.global = true;
        this.icon = m;
        this.rw = w;
    }

    public Region(Location pos1, Location pos2, String name, String displayname, RWorld w, Material m, int pri) {
        cube = new Cube(pos1, pos2);
        this.displayname = displayname;
        this.rw = w;
        this.icon = m;
        this.priority = pri;
        this.name = name;
        this.cv = new CubeVisualizer(this);
    }

    public ItemStack getItem() {
        if (global) {
            return Itens.createItem(icon, 1, "&f" + this.displayname + " &7[&b" + getType() + "&7]", Arrays.asList(
                    "&fPriority: &b" + this.priority,
                    "&fFlags:",
                    "  &fBlock Break: " + boolStyle(blockbreak),
                    "  &fBlock Place: " + boolStyle(blockplace),
                    "  &fBlock Interactions: " + boolStyle(blockinteract),
                    "  &fContainer Interactions: " + boolStyle(containerinteract),
                    "  &fPVP: " + boolStyle(pvp),
                    "  &fPVE: " + boolStyle(pve),
                    "  &fHunger: " + boolStyle(hunger),
                    "  &fTake Damage: " + boolStyle(takedamage),
                    "  &fExplosions: " + boolStyle(explosions),
                    "  &fItem Pickup: " + boolStyle(itempickup),
                    "  &fItem Drop: " + boolStyle(itemdrop),
                    "  &fEntity Spawning: " + boolStyle(entityspawning),
                    "  &fEnter: " + boolStyle(enter),
                    "  &fAcess Crafting Tables: " + boolStyle(acesscrafting),
                    "  &fAcess Chests: " + boolStyle(acesschests),
                    "  &fAcess Hoppers: " + boolStyle(acesshoppers),
                    "&f",
                    "&7Left Click to edit this region.",
                    "&7Shift + Left Click to change this region icon.",
                    "&7Right Click to teleport to this region.",
                    "&7Shift + Right Click to change this regions displayname.",
                    "&7Middle Click to visualize this region."));
        } else {
            return Itens.createItem(icon, 1, "&f" + this.displayname + " &7[&b" + getType() + "&7]", Arrays.asList(
                    "&fPriority: &b" + this.priority,
                    "&fBlocks: &b" + getBlockCount() + " &7(&b " + this.cube.getSizeX() + " &fx &b" + this.cube.getSizeY() + " &fx &b" + this.cube.getSizeZ() + " &7)",
                    "&fFlags:",
                    "  &fBlock Break: " + boolStyle(blockbreak),
                    "  &fBlock Place: " + boolStyle(blockplace),
                    "  &fBlock Interactions: " + boolStyle(blockinteract),
                    "  &fContainer Interactions: " + boolStyle(containerinteract),
                    "  &fPVP: " + boolStyle(pvp),
                    "  &fPVE: " + boolStyle(pve),
                    "  &fHunger: " + boolStyle(hunger),
                    "  &fTake Damage: " + boolStyle(takedamage),
                    "  &fExplosions: " + boolStyle(explosions),
                    "  &fItem Pickup: " + boolStyle(itempickup),
                    "  &fItem Drop: " + boolStyle(itemdrop),
                    "  &fEntity Spawning: " + boolStyle(entityspawning),
                    "  &fEnter: " + boolStyle(enter),
                    "  &fAcess Crafting Tables: " + boolStyle(acesscrafting),
                    "  &fAcess Chests: " + boolStyle(acesschests),
                    "  &fAcess Hoppers: " + boolStyle(acesshoppers),
                    "&f",
                    "&7Left Click to edit this region.",
                    "&7Shift + Left Click to change this region icon.",
                    "&7Right Click to teleport to this region.",
                    "&7Shift + Right Click to change this regions displayname.",
                    "&7Middle Click to visualize this region.",
                    "&7Press Q to delete this region."));
        }
    }

    private String boolStyle(boolean bdsadsadasdasawdadw) {
        if (bdsadsadasdasawdadw) {
            return "&a✔ enabled";
        } else {
            return "&c❌ disabled";
        }
    }

    private int getBlockCount() {
        return this.cube.getVolume();
    }

    private String getType() {
        if (global) {
            return "Infinite";
        } else {
            return "Cube";
        }
    }

    public void teleport(Player p) {
        if (global) {
            p.teleport(rw.getWorld().getSpawnLocation());
        } else {
            p.teleport(this.cube.getCenter());
        }
        Text.send(p, "&fYou teleported to region &b" + displayname + "&r &fon &a" + rw.getName());
    }

    public String getDisplayName() {
        return displayname;
    }

    public void setIcon(Material a) {
        this.icon = a;
    }

    public void saveData(Data.Region dr) {
        FileConfiguration cfg = rw.getConfig();
        switch (dr) {
            case ICON:
                cfg.set("Regions." + this.name + ".Icon", this.icon.name());
                rw.saveConfig();
                break;
            case REGION:
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
                cfg.set("Regions." + this.name + ".Acess.Crafting-Table", this.acesscrafting);
                cfg.set("Regions." + this.name + ".Acess.Chests", this.acesschests);
                cfg.set("Regions." + this.name + ".Acess.Hoppers", this.acesshoppers);
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
                cfg.set("Regions." + this.name + ".Acess.Crafting-Table", this.acesscrafting);
                cfg.set("Regions." + this.name + ".Acess.Chests", this.acesschests);
                cfg.set("Regions." + this.name + ".Acess.Hoppers", this.acesshoppers);
                rw.saveConfig();
                break;
            case SETTINGS:
                cfg.set("Regions." + this.name + ".Display-Name", this.displayname);
                cfg.set("Regions." + this.name + ".isGlobal", this.global);
                cfg.set("Regions." + this.name + ".Priority", this.priority);
                cfg.set("Regions." + this.name + ".Icon", this.icon.name());
                rw.saveConfig();
                break;
        }
    }

    public RWorld getWorld() {
        return this.rw;
    }

    public void toggleVisual(Player p) {
        if (global)
        {
            Text.send(p, "&fYou &ccant &fvisualize this region because its a global region.");
            return;
        }

        this.view = !this.view;
        Text.send(p, "&fVisualizing " + this.displayname + ": " + boolStyle(this.view));
    }

    public Cube getMaster() {
        return this.cube;
    }

    public CubeVisualizer getViewingMaster() {
        return this.cv;
    }

    public boolean canVisualize() {
        return !this.global;
    }

    public String getName() {
        return this.name;
    }

    public boolean isGlobal() {
        return this.global;
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
        if (this.global)
        {
            if (this.getWorld().getWorld() == l.getWorld())
            {
                return true;
            } else {
                return false;
            }
        } else {
            return this.cube.contains(l);
        }
    }
}
