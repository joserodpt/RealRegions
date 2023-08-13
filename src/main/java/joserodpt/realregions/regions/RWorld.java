package joserodpt.realregions.regions;

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

import joserodpt.realregions.RealRegions;
import joserodpt.realregions.utils.IO;
import joserodpt.realregions.utils.Itens;
import joserodpt.realregions.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class RWorld implements Listener {

    public enum WorldType { NORMAL, NETHER, THE_END, VOID }

    public enum Data { ICON, LOAD }

    private final String worldName;
    private World world;
    private final WorldType wt;
    private File file;
    private FileConfiguration config;
    private Material icon;
    private double worldSizeMB;
    private boolean loaded = true;

    public RWorld(String worldName, WorldType wt) {
        this.worldName = worldName;
        this.wt = wt;

        //object is loaded but world isn't
        //this.world = w;
        this.checkConfig();

        this.setLoaded(false);
    }

    public RWorld(String worldName, World w, WorldType wt) {
        this.worldName = worldName;
        this.wt = wt;

        //load RWorld from Bukkit World
        this.world = w;
        this.checkConfig();

        this.setLoaded(true);
    }

    public void postWorldLoad() {
        //calculate size for world
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                File folder = new File(Bukkit.getWorldContainer() + "/" + getRWorldName());
                worldSizeMB = IO.toMB(IO.folderSize(folder));
            }
        };
        task.runTaskLaterAsynchronously(RealRegions.getPlugin(), 0);
    }

    private void checkConfig() {
        this.file = new File(RealRegions.getPlugin().getDataFolder() + "/worlds/", this.getRWorldName() + ".yml");
        if (!this.file.exists()) {
            this.file.getParentFile().mkdirs();
            try {
                this.file.createNewFile();
                setupDefaultConfig();
            } catch (IOException e) {
                RealRegions.getPlugin().getLogger().severe("RealRegions threw an error while creating world config for " + this.getRWorldName());
                e.printStackTrace();
            }
        }

        if (this.config == null) {
            this.config = YamlConfiguration.loadConfiguration(file);
            this.icon = Material.valueOf(config.getString("Settings.Icon"));
        }
    }

    public void deleteConfig() {
        File fileToDelete = new File(RealRegions.getPlugin().getDataFolder() + "/worlds/", this.getRWorldName() + ".yml");

        if (fileToDelete.exists()) {
            if (!fileToDelete.delete()) {
                RealRegions.getPlugin().getLogger().severe("Failed to delete Configuration file for " + this.getRWorldName() + ".");
            }
        } else {
            RealRegions.getPlugin().getLogger().severe("Configuration file for " + this.getRWorldName() + " doesn't exist.");
        }

    }

    public void reloadConfig() {
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveConfig() {
        try {
            this.config.save(file);
        } catch (IOException e) {
            RealRegions.getPlugin().getLogger().severe("RealRegions threw an error while saving world config for " + this.getRWorldName());
        }
    }

    private void setupDefaultConfig() {
        this.config = YamlConfiguration.loadConfiguration(file);

        Material m;
        switch (wt) {
            case NORMAL:
                m = Material.GRASS_BLOCK;
                break;
            case NETHER:
                m = Material.NETHERRACK;
                break;
            case THE_END:
                m = Material.END_STONE;
                break;
            case VOID:
                m = Material.FEATHER;
                break;
            default:
                throw new IllegalStateException("Unexpected value in World Type (is this a bug?): " + world.getEnvironment());
        }

        this.icon = m;
        this.config.set("Settings.Icon", this.icon.name());
        this.config.set("Settings.Type", this.getWorldType().name());
        this.config.set("Settings.Load", true);
        this.config.set("Settings.Unix-Register", System.currentTimeMillis() / 1000L);

        //default global region
        this.config.set("Regions.Global.Type", Region.RegionType.INFINITE.name());
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

    public void setWorld(World w) {
        this.world = w;
    }

    public WorldType getWorldType() {
        return this.wt;
    }

    public void teleport(Player p, boolean silent) {
        if (!isLoaded()) {
            Text.send(p, "&cYou can't teleport to this world because it is unloaded.");
            return;
        }

        p.teleport(this.world.getSpawnLocation());
        if (!silent) {
            Text.send(p, "Teleported to world: &b" + this.getRWorldName());
        }
    }

    public String getRWorldName() {
        return this.worldName;
    }

    public ItemStack getItem() {
        return Itens.createItem(getIcon(), 1, "&f" + this.getRWorldName() + " &7[&b" + (this.getWorld() == null ? "&e&lUNLOADED" : this.getWorldSizeMB() + "mb") + "&7]", Arrays.asList("&5", " &6On this world:", "  &b" + (this.getWorld() == null ? "?" : this.getWorld().getPlayers().size()) + " &fplayers.", "  &b" + (this.getWorld() == null ? "?" : this.getWorld().getEntities().size()) + " &fentities.", "  &b" + (this.getWorld() == null ? "?" : this.getWorld().getLoadedChunks().length) + " &floaded chunks.","", "&fRegistered on: &b" + Text.convertUnixTimeToDate(this.config.getInt("Settings.Unix-Register")), "&f", "&7Left Click to inspect this world.", "&7Middle click to change the world icon.", "&7Right Click to teleport to this world."));
    }

    private Material getIcon() {
        return this.icon;
    }

    public void setIcon(Material a) {
        this.icon = a;
    }

    public void setLoaded(boolean b) {
        this.loaded = b;

        this.saveData(Data.LOAD);

        if (b) { postWorldLoad(); }
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public void saveData(Data dw) {
        switch (dw)
        {
            case ICON:
                config.set("Settings.Icon", this.icon.name());
                saveConfig();
                break;
            case LOAD:
                config.set("Settings.Load", this.loaded);
                saveConfig();
                break;
        }
    }

    public double getWorldSizeMB() {
        return worldSizeMB;
    }
}