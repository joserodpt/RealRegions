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

import joserodpt.realregions.RealRegionsPlugin;
import joserodpt.realregions.utils.IO;
import joserodpt.realregions.utils.Itens;
import joserodpt.realregions.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RWorld implements Listener {

    public enum WorldType { NORMAL, NETHER, THE_END, VOID, UNKNOWN_TO_BE_IMPORTED }

    public enum Data { ICON, LOAD }
    private final String worldName;
    private World world;
    private final WorldType wt;
    private File file;
    private FileConfiguration config;
    private Material icon;
    private double worldSizeMB;
    private boolean loaded = true;
    private Map<String, Region> regions = new LinkedHashMap<>();

    public RWorld(String worldNameImported) {
        this.worldName = worldNameImported;
        this.wt = WorldType.UNKNOWN_TO_BE_IMPORTED;
        this.icon = Material.GRAY_STAINED_GLASS_PANE;
        this.loaded = false;
    }

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

        //load regions
        this.loadRegions();

        this.setLoaded(true);
    }

    public boolean hasRegion(String mineName) {
        return this.getRegions().containsKey(mineName);
    }

    public void addRegion(Region r) {
        this.getRegions().put(r.getRegionName(), r);
    }

    private void loadRegions() {
        for (String regionName : this.getConfig().getConfigurationSection("Regions").getKeys(false)) {
            Region.RegionType rt = Region.RegionType.valueOf(this.getConfig().getString("Regions." + regionName + ".Type"));
            String regionDisplayName = this.getConfig().getString("Regions." + regionName + ".Display-Name");
            Region reg = null;

            //load region flags
            boolean blockInteract = this.getConfig().getBoolean("Regions." + regionName + ".Block.Interact");
            boolean containerInteract = this.getConfig().getBoolean("Regions." + regionName + ".Container.Interact");
            boolean blockBreak = this.getConfig().getBoolean("Regions." + regionName + ".Block.Break");
            boolean blockPlace = this.getConfig().getBoolean("Regions." + regionName + ".Block.Place");
            boolean pvp = this.getConfig().getBoolean("Regions." + regionName + ".PVP");
            boolean pve = this.getConfig().getBoolean("Regions." + regionName + ".PVE");
            boolean hunger = this.getConfig().getBoolean("Regions." + regionName + ".Hunger");
            boolean takeDamage = this.getConfig().getBoolean("Regions." + regionName + ".Damage");
            boolean explosions = this.getConfig().getBoolean("Regions." + regionName + ".Explosions");
            boolean itemDrop = this.getConfig().getBoolean("Regions." + regionName + ".Item.Drop");
            boolean itemPickup = this.getConfig().getBoolean("Regions." + regionName + ".Item.Pickup");
            boolean entitySpawning = this.getConfig().getBoolean("Regions." + regionName + ".Entity-Spawning");
            boolean enter = this.getConfig().getBoolean("Regions." + regionName + ".Enter");
            boolean accessCrafting = this.getConfig().getBoolean("Regions." + regionName + ".Access.Crafting-Table");
            boolean accessChests = this.getConfig().getBoolean("Regions." + regionName + ".Access.Chests");
            boolean accessHoppers = this.getConfig().getBoolean("Regions." + regionName + ".Access.Hoppers");

            switch (rt)
            {
                case INFINITE:
                    reg = new Region(regionName, regionDisplayName, this, Material.valueOf(this.getConfig().getString("Regions." + regionName + ".Icon")), this.getConfig().getInt("Regions." + regionName + ".Priority"), Region.RegionType.INFINITE);
                    break;
                case CUBOID:
                    reg = new CuboidRegion(Text.textToLoc(this.getConfig().getString("Regions." + regionName + ".POS.1"), this.getWorld()),
                            Text.textToLoc(this.getConfig().getString("Regions." + regionName + ".POS.2"), this.getWorld()),
                            ChatColor.stripColor(regionName), this.getConfig().getString("Regions." + regionName + ".Display-Name"), this,
                            Material.valueOf(this.getConfig().getString("Regions." + regionName + ".Icon")), this.getConfig().getInt("Regions." + regionName + ".Priority"));
                    break;
            }

            String orig = this.getConfig().getString("Regions." + regionName + ".Origin", "-");
            assert orig != null;
            if (!orig.equals("-")) {
                reg.setOrigin(Region.RegionOrigin.valueOf(orig));
            }

            if (reg != null) {
                reg.blockinteract = blockInteract;
                reg.containerinteract = containerInteract;
                reg.blockbreak = blockBreak;
                reg.blockplace = blockPlace;
                reg.pvp = pvp;
                reg.pve = pve;
                reg.hunger = hunger;
                reg.takedamage = takeDamage;
                reg.explosions = explosions;
                reg.itemdrop = itemDrop;
                reg.itempickup = itemPickup;
                reg.entityspawning = entitySpawning;
                reg.enter = enter;
                reg.accesscrafting = accessCrafting;
                reg.accesschests = accessChests;
                reg.accesshoppers = accessHoppers;
                reg.saveData(Region.RegionData.FLAGS);
                this.getRegions().put(regionName, reg);
            }
        }
    }

    private Map<String, Region> getRegions() {
        return this.regions;
    }

    public List<Region> getRegionList() {
        return new ArrayList<>(this.getRegions().values());
    }

    public void removeRegion(Region a) {
        this.regions.remove(a);
    }

    public int getRegistrationDate() {
        return this.getWorldType() == WorldType.UNKNOWN_TO_BE_IMPORTED ? (int) (System.currentTimeMillis() / 1000L) : this.getConfig().getInt("Settings.Unix-Register");
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
        task.runTaskLaterAsynchronously(RealRegionsPlugin.getPlugin(), 0);
    }

    private void checkConfig() {
        this.file = new File(RealRegionsPlugin.getPlugin().getDataFolder() + "/worlds/", this.getRWorldName() + ".yml");
        if (!this.file.exists()) {
            this.file.getParentFile().mkdirs();
            try {
                this.file.createNewFile();
                setupDefaultConfig();
            } catch (IOException e) {
                RealRegionsPlugin.getPlugin().getLogger().severe("RealRegions threw an error while creating world config for " + this.getRWorldName());
                e.printStackTrace();
            }
        }

        if (this.config == null) {
            this.config = YamlConfiguration.loadConfiguration(file);
            this.icon = Material.valueOf(config.getString("Settings.Icon"));
        }
    }

    public void deleteConfig() {
        File fileToDelete = new File(RealRegionsPlugin.getPlugin().getDataFolder() + "/worlds/", this.getRWorldName() + ".yml");

        if (fileToDelete.exists()) {
            if (!fileToDelete.delete()) {
                RealRegionsPlugin.getPlugin().getLogger().severe("Failed to delete Configuration file for " + this.getRWorldName() + ".");
            }
        } else {
            RealRegionsPlugin.getPlugin().getLogger().severe("Configuration file for " + this.getRWorldName() + " doesn't exist.");
        }

    }

    public void reloadConfig() {
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveConfig() {
        try {
            this.config.save(file);
        } catch (IOException e) {
            RealRegionsPlugin.getPlugin().getLogger().severe("RealRegions threw an error while saving world config for " + this.getRWorldName());
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
        return this.getWorldType() == WorldType.UNKNOWN_TO_BE_IMPORTED ?
                Itens.createItem(getIcon(), 1, "&f" + this.getRWorldName() + " &7[&e&lUNIMPORTED&7]", Arrays.asList("&f", "&7Click to import this world.", "&cQ (Drop)&7 to &cdelete &7this world."))
                : Itens.createItem(getIcon(), 1, "&f" + this.getRWorldName() + " &7[&b" + (this.getWorld() == null ? "&e&lUNLOADED" : this.getWorldSizeMB() + "mb") + "&7]", Arrays.asList("&5", " &6On this world:", "  &b" + (this.getWorld() == null ? "?" : this.getWorld().getPlayers().size()) + " &fplayers.", "  &b" + (this.getWorld() == null ? "?" : this.getWorld().getEntities().size()) + " &fentities.", "  &b" + (this.getWorld() == null ? "?" : this.getWorld().getLoadedChunks().length) + " &floaded chunks.","", "&fRegistered on: &b" + Text.convertUnixTimeToDate(this.getRegistrationDate()), "&f", "&7Left Click to inspect this world.", "&7Middle click to change the world icon.", "&7Right Click to teleport to this world.", "&cQ (Drop)&7 to unregister this world."));
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