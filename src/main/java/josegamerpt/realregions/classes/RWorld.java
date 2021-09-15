package josegamerpt.realregions.classes;

import josegamerpt.realregions.RealRegions;
import josegamerpt.realregions.regions.CuboidRegion;
import josegamerpt.realregions.regions.Region;
import josegamerpt.realregions.utils.IO;
import josegamerpt.realregions.utils.Itens;
import josegamerpt.realregions.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.logging.Level;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

//RealRegions World
public class RWorld {

    public enum Data { ICON, REGIONS }

    private World world;
    private ArrayList<Region> regions = new ArrayList<>();
    private File file;
    private FileConfiguration config;
    private Material icon;

    public RWorld(World w) {
        this.world = w;
        checkConfig();
        loadRegions();
        this.icon = Material.valueOf(config.getString("Settings.Icon"));
    }

    private void loadRegions() {
        for (String r : config.getConfigurationSection("Regions").getKeys(false)) {
            Region.Type rt = Region.Type.valueOf(config.getString("Regions." + r + ".Type"));
            String n = config.getString("Regions." + r + ".Display-Name");
            CuboidRegion reg = null;
            if (config.getBoolean("Regions." + r + ".isGlobal")) {
                reg = new CuboidRegion(r, n, this, Material.valueOf(config.getString("Regions." + r + ".Icon")));
            } else {
                switch (rt)
                {
                    case CUBOID:
                        reg = new CuboidRegion(Text.textToLoc(config.getString("Regions." + r + ".POS.1"), this.world),
                                Text.textToLoc(config.getString("Regions." + r + ".POS.2"), this.world),
                                ChatColor.stripColor(r), config.getString("Regions." + r + ".Display-Name"), this,
                                Material.valueOf(config.getString("Regions." + r + ".Icon")), config.getInt("Regions." + r + ".Priority"));
                        break;
                }
            }

            if (reg != null) {
                reg.blockinteract = config.getBoolean("Regions." + r + ".Block.Interact");
                reg.containerinteract = config.getBoolean("Regions." + r + ".Container.Interact");
                reg.blockbreak = config.getBoolean("Regions." + r + ".Block.Break");
                reg.blockplace = config.getBoolean("Regions." + r + ".Block.Place");
                reg.pvp = config.getBoolean("Regions." + r + ".PVP");
                reg.pve = config.getBoolean("Regions." + r + ".PVE");
                reg.hunger = config.getBoolean("Regions." + r + ".Hunger");
                reg.takedamage = config.getBoolean("Regions." + r + ".Damage");
                reg.explosions = config.getBoolean("Regions." + r + ".Explosions");
                reg.itemdrop = config.getBoolean("Regions." + r + ".Item.Drop");
                reg.itempickup = config.getBoolean("Regions." + r + ".Item.Pickup");
                reg.entityspawning = config.getBoolean("Regions." + r + ".Entity-Spawning");
                reg.enter = config.getBoolean("Regions." + r + ".Enter");
                reg.accesscrafting = config.getBoolean("Regions." + r + ".Acess.Crafting-Table");
                reg.accesschests = config.getBoolean("Regions." + r + ".Acess.Chests");
                reg.accesshoppers = config.getBoolean("Regions." + r + ".Acess.Hoppers");
                this.regions.add(reg);
            }
        }
    }

    private void checkConfig() {
        this.file = new File(RealRegions.getPL().getDataFolder() + "/worlds/", world.getName() + ".yml");
        if (!this.file.exists()) {
            this.file.getParentFile().mkdirs();
            try {
                this.file.createNewFile();
                setupDefaultConfig();
            } catch (IOException e) {
                RealRegions.log(Level.SEVERE, "RealRegions threw an error while creating world config for " + world.getName());
                e.printStackTrace();
            }
        }

        if (this.config == null) {
            this.config = YamlConfiguration.loadConfiguration(file);
        }
    }

    private void reloadConfig() {
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveConfig() {
        try {
            this.config.save(file);
        } catch (IOException e) {
            RealRegions.log(Level.OFF,"RealRegions threw an error while saving world config for " + world.getName());
        }
    }

    private void setupDefaultConfig() {
        this.config = YamlConfiguration.loadConfiguration(file);

        Material m;
        switch (world.getEnvironment()) {
            case NORMAL:
                m = Material.GRASS_BLOCK;
                break;
            case NETHER:
                m = Material.NETHERRACK;
                break;
            case THE_END:
                m = Material.END_STONE;
                break;
            default:
                throw new IllegalStateException("Unexpected value (is this a bug?): " + world.getEnvironment());
        }

        this.icon = m;
        this.config.set("Settings.Icon", this.icon.name());
        this.config.set("Regions.Global.Type", Region.Type.CUBOID.name());
        this.config.set("Regions.Global.Display-Name", "&f&lGlobal");
        this.config.set("Regions.Global.isGlobal", true);
        this.config.set("Regions.Global.Priority", 10);
        this.config.set("Regions.Global.Icon", Material.BEDROCK.name());
        this.config.set("Regions.Global.Block.Interact", true);
        this.config.set("Regions.Global.Block.Break", true);
        this.config.set("Regions.Global.Block.Place", true);
        this.config.set("Regions.Global.Container.Interact", true);
        this.config.set("Regions.Global.PVP", true);
        this.config.set("Regions.Global.PVE", true);
        this.config.set("Regions.Global.Hunger", true);
        this.config.set("Regions.Global.Damage", true);
        this.config.set("Regions.Global.Explosions", true);
        this.config.set("Regions.Global.Item.Drop", true);
        this.config.set("Regions.Global.Item.Pickup", true);
        this.config.set("Regions.Global.Entity-Spawning", true);
        this.config.set("Regions.Global.Enter", true);
        this.config.set("Regions.Global.Create-Portal", true);
        this.config.set("Regions.Global.Acess.Crafting-Table", true);
        this.config.set("Regions.Global.Acess.Chests", true);
        this.config.set("Regions.Global.Acess.Hoppers", true);
        saveConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public World getWorld() {
        return this.world;
    }

    public void teleport(Player p, boolean silent) {
        p.teleport(this.world.getSpawnLocation());
        if (!silent) {
            Text.send(p, "Teleported to &b" + this.world.getName());
        }
    }

    public String getName() {
        return world.getName();
    }

    public ArrayList<Region> getRegions() {
        return regions;
    }

    public void addRegion(Region r) {
        this.regions.add(r);
    }

    public ItemStack getItem() {
        File folder = new File(Bukkit.getWorldContainer() + "/" + world.getName());
        return Itens.createItem(getIcon(), 1, "&f" + world.getName() + " &7[&b" + IO.toMB(IO.folderSize(folder)) + "mb&7]", Arrays.asList("&5", " &6On this world:", "  &b" + world.getPlayers().size() + " &fplayers.", "  &b" + world.getEntities().size() + " &fentities.", "  &b" + world.getLoadedChunks().length + " &floaded chunks.", "&f", "&7Left Click to inspect this world.", "&7Middle click to change the world icon.", "&7Right Click to teleport to this world."));
    }

    private Material getIcon() {
        return this.icon;
    }

    public void setIcon(Material a) {
        this.icon = a;
    }

    public void saveData(Data dw) {
        switch (dw)
        {
            case ICON:
                config.set("Settings.Icon", this.icon.name());
                saveConfig();
                break;
            case REGIONS:
                this.regions.forEach(region -> region.saveData(Region.Data.REGION));
                break;
        }
    }

    public void deleteRegion(Region a) {
        this.regions.remove(a);
        this.config.set("Regions." + a.getName(), null);
        saveConfig();
    }

    public boolean hasRegion(String name) {
        for (Region region : this.regions) {
            if (region.getName().equalsIgnoreCase(name))
            {
                return true;
            }
        }
        return false;
    }
}
