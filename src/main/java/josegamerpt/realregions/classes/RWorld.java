package josegamerpt.realregions.classes;

import josegamerpt.realregions.RealRegions;
import josegamerpt.realregions.regions.RRegion;
import josegamerpt.realregions.utils.IO;
import josegamerpt.realregions.utils.Itens;
import josegamerpt.realregions.utils.Text;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

//RealRegions World
public class RWorld implements Listener {

    public enum Data { ICON, REGIONS }

    private World world;
    private File file;
    private FileConfiguration config;
    private Material icon;
    private double worldSizeMB;

    public RWorld(World w) {
        this.world = w;
        checkConfig();
        this.icon = Material.valueOf(config.getString("Settings.Icon"));

        //register listener for this rworld


        //calculate size for world
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                File folder = new File(Bukkit.getWorldContainer() + "/" + getRWorldName());
                worldSizeMB = IO.toMB(IO.folderSize(folder));
            }
        };

        // Start the task
        task.runTaskLaterAsynchronously(RealRegions.getInstance(), 0);
    }

    private void checkConfig() {
        this.file = new File(RealRegions.getInstance().getDataFolder() + "/worlds/", world.getName() + ".yml");
        if (!this.file.exists()) {
            this.file.getParentFile().mkdirs();
            try {
                this.file.createNewFile();
                setupDefaultConfig();
            } catch (IOException e) {
                RealRegions.getInstance().log(Level.SEVERE, "RealRegions threw an error while creating world config for " + world.getName());
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
            RealRegions.getInstance().log(Level.OFF,"RealRegions threw an error while saving world config for " + world.getName());
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
        this.config.set("Regions.Global.Type", RRegion.RegionType.INFINITE.name());
        this.config.set("Regions.Global.Display-Name", "&f&lGlobal");
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
        this.config.set("Regions.Global.Access.Crafting-Table", true);
        this.config.set("Regions.Global.Access.Chests", true);
        this.config.set("Regions.Global.Access.Hoppers", true);
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

    public String getRWorldName() {
        return world.getName();
    }

    public ItemStack getItem() {
        return Itens.createItem(getIcon(), 1, "&f" + this.getRWorldName() + " &7[&b" + worldSizeMB + "mb&7]", Arrays.asList("&5", " &6On this world:", "  &b" + world.getPlayers().size() + " &fplayers.", "  &b" + world.getEntities().size() + " &fentities.", "  &b" + world.getLoadedChunks().length + " &floaded chunks.", "&f", "&7Left Click to inspect this world.", "&7Middle click to change the world icon.", "&7Right Click to teleport to this world."));
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
                RealRegions.getInstance().getWorldManager().getRegionManager().saveRegions(this);
                break;
        }
    }
}
