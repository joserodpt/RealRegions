package joserodpt.realregions.api;

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

import joserodpt.realregions.api.config.TranslatableLine;
import joserodpt.realregions.api.regions.CuboidRegion;
import joserodpt.realregions.api.regions.Region;
import joserodpt.realregions.api.utils.ItemStackSpringer;
import joserodpt.realregions.api.utils.Text;
import joserodpt.realregions.api.utils.IO;
import joserodpt.realregions.api.utils.Itens;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RWorld implements Listener {

    public enum WorldType {NORMAL, NETHER, THE_END, VOID, UNKNOWN_TO_BE_IMPORTED}

    public enum Data {ICON, LOAD, TP_JOIN, WORLD_INVENTORIES, ALL}

    private final String worldName;
    private World world;
    private final WorldType wt;
    private File file;
    private FileConfiguration config;
    private Material icon;
    private double worldSizeMB;
    private boolean loaded = true;
    private boolean tpOnJoin = false;
    private boolean worldInventories = false;
    private final Map<String, Region> regions = new LinkedHashMap<>();
    private int resetWorldEverySeconds = 0;
    private BukkitTask resetTask;

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

    public void setTPJoin(boolean b) {
        this.tpOnJoin = b;
        saveData(Data.TP_JOIN, true);
    }

    public boolean isTPJoinON() {
        return this.tpOnJoin;
    }

    public void setWorldInventories(boolean worldInventories) {
        this.worldInventories = worldInventories;
        saveData(Data.WORLD_INVENTORIES, true);
    }

    public void setResetEverySeconds(int time) {
        if (time <= 2) { return; }

        this.resetWorldEverySeconds = time;
        config.set("Settings.Reset-Every-Seconds", this.resetWorldEverySeconds);
        saveConfig();

        if (this.resetTask != null) {
            this.resetTask.cancel();
        }

        RWorld ref = this;

        Bukkit.getScheduler().runTaskLater(RealRegionsAPI.getInstance().getPlugin(), () -> this.resetTask = new BukkitRunnable() {
            @Override
            public void run() {
                RealRegionsAPI.getInstance().getWorldManagerAPI().resetWorld(ref);
            }
        }.runTaskTimer(RealRegionsAPI.getInstance().getPlugin(), 0, this.resetWorldEverySeconds * 20L), 20L * this.resetWorldEverySeconds);
    }

    public boolean hasWorldInventories() {
        return this.worldInventories;
    }

    public Location getTPJoinLocation() {
        return this.world.getSpawnLocation();
    }

    private void loadRegions() {
        for (String regionName : this.getConfig().getConfigurationSection("Regions").getKeys(false)) {
            Region.RegionType rt = Region.RegionType.valueOf(this.getConfig().getString("Regions." + regionName + ".Type"));
            String regionDisplayName = this.getConfig().getString("Regions." + regionName + ".Display-Name");
            boolean announceEnterTitle = this.getConfig().getBoolean("Regions." + regionName + ".Announce-Enter.Title", false);
            boolean announceEnterActionbar = this.getConfig().getBoolean("Regions." + regionName + ".Announce-Enter.Actionbar", false);
            Region reg = null;

            switch (rt) {
                case INFINITE:
                    reg = new Region(regionName, regionDisplayName, this, Material.valueOf(this.getConfig().getString("Regions." + regionName + ".Icon")), this.getConfig().getInt("Regions." + regionName + ".Priority"), Region.RegionType.INFINITE, announceEnterTitle, announceEnterActionbar);
                    break;
                case CUBOID:
                    reg = new CuboidRegion(Text.textToLoc(this.getConfig().getString("Regions." + regionName + ".POS.1"), this.getWorld()),
                            Text.textToLoc(this.getConfig().getString("Regions." + regionName + ".POS.2"), this.getWorld()),
                            ChatColor.stripColor(regionName), this.getConfig().getString("Regions." + regionName + ".Display-Name"), this,
                            Material.valueOf(this.getConfig().getString("Regions." + regionName + ".Icon")), this.getConfig().getInt("Regions." + regionName + ".Priority"), announceEnterTitle, announceEnterActionbar);
                    break;
            }

            String orig = this.getConfig().getString("Regions." + regionName + ".Origin", "-");
            assert orig != null;
            if (!orig.equals("-")) {
                reg.setOrigin(Region.RegionOrigin.valueOf(orig));
            }

            if (reg != null) {
                //load region flags
                reg.blockInteract = this.getConfig().getBoolean("Regions." + regionName + ".Block.Interact");
                reg.containerInteract = this.getConfig().getBoolean("Regions." + regionName + ".Container.Interact");
                reg.blockBreak = this.getConfig().getBoolean("Regions." + regionName + ".Block.Break");
                reg.blockPlace = this.getConfig().getBoolean("Regions." + regionName + ".Block.Place");
                reg.pvp = this.getConfig().getBoolean("Regions." + regionName + ".PVP");
                reg.pve = this.getConfig().getBoolean("Regions." + regionName + ".PVE");
                reg.hunger = this.getConfig().getBoolean("Regions." + regionName + ".Hunger");
                reg.takeDamage = this.getConfig().getBoolean("Regions." + regionName + ".Damage");
                reg.explosions = this.getConfig().getBoolean("Regions." + regionName + ".Explosions");
                reg.itemDrop = this.getConfig().getBoolean("Regions." + regionName + ".Item.Drop");
                reg.itemPickup = this.getConfig().getBoolean("Regions." + regionName + ".Item.Pickup");
                reg.entitySpawning = this.getConfig().getBoolean("Regions." + regionName + ".Entity-Spawning");
                reg.enter = this.getConfig().getBoolean("Regions." + regionName + ".Enter");
                reg.accessCrafting = this.getConfig().getBoolean("Regions." + regionName + ".Access.Crafting-Table");
                reg.accessChests = this.getConfig().getBoolean("Regions." + regionName + ".Access.Chests");
                reg.accessHoppers = this.getConfig().getBoolean("Regions." + regionName + ".Access.Hoppers");
                //failsafe if it doesn't exist, they're new entries
                reg.noChat = this.getConfig().getBoolean("Regions." + regionName + ".No-Chat", false);
                reg.noConsumables = this.getConfig().getBoolean("Regions." + regionName + ".No-Consumables", false);
                reg.disabledNetherPortal = this.getConfig().getBoolean("Regions." + regionName + ".Disabled-Nether-Portal", false);
                reg.disabledEndPortal = this.getConfig().getBoolean("Regions." + regionName + ".Disabled-End-Portal", false);
                reg.noFireSpreading = this.getConfig().getBoolean("Regions." + regionName + ".No-Fire-Spreading", false);

                reg.saveData(Region.RegionData.FLAGS);
                this.getRegions().put(regionName, reg);
            }
        }
    }

    private Map<String, Region> getRegions() {
        return this.regions;
    }

    public Collection<Region> getRegionList() {
        return this.getRegions().values();
    }

    public void removeRegion(Region a) {
        this.regions.remove(a.getRegionName());
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
        task.runTaskLaterAsynchronously(RealRegionsAPI.getInstance().getPlugin(), 0);
    }

    private void checkConfig() {
        this.file = new File(RealRegionsAPI.getInstance().getPlugin().getDataFolder() + "/worlds/", this.getRWorldName() + ".yml");
        if (!this.file.exists()) {
            this.file.getParentFile().mkdirs();
            try {
                this.file.createNewFile();
                setupDefaultConfig();
            } catch (IOException e) {
                RealRegionsAPI.getInstance().getLogger().severe("RealRegions threw an error while creating world config for " + this.getRWorldName());
                e.printStackTrace();
            }
        }

        if (this.config == null) {
            this.config = YamlConfiguration.loadConfiguration(file);
            this.icon = Material.valueOf(config.getString("Settings.Icon"));
            this.tpOnJoin = config.getBoolean("Settings.TP-On-Join");
            this.worldInventories = config.getBoolean("Settings.World-Inventories");
            this.setResetEverySeconds(config.getInt("Settings.Reset-Every-Seconds"));
        }
    }

    public void deleteConfig() {
        if (this.resetTask != null) {
            this.resetTask.cancel();
        }

        File fileToDelete = new File(RealRegionsAPI.getInstance().getPlugin().getDataFolder() + "/worlds/", this.getRWorldName() + ".yml");

        if (fileToDelete.exists()) {
            if (!fileToDelete.delete()) {
                RealRegionsAPI.getInstance().getLogger().severe("Failed to delete Configuration file for " + this.getRWorldName() + ".");
            }
        } else {
            RealRegionsAPI.getInstance().getLogger().severe("Configuration file for " + this.getRWorldName() + " doesn't exist.");
        }

    }

    public void reloadConfig() {
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveConfig() {
        try {
            this.config.save(file);
        } catch (IOException e) {
            RealRegionsAPI.getInstance().getLogger().severe("RealRegions threw an error while saving world config for " + this.getRWorldName());
        }
    }

    public void setWorldSpawn(Location location) {
        this.world.setSpawnLocation(location);
        this.world.save();
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
        this.config.set("Settings.TP-On-Join", false);
        this.config.set("Settings.World-Inventories", false);
        this.config.set("Settings.Unix-Register", System.currentTimeMillis() / 1000L);

        //default global region
        this.config.set("Regions.Global.Type", Region.RegionType.INFINITE.name());
        this.config.set("Regions.Global.Display-Name", "&f&lGlobal");
        this.config.set("Regions.Global.Priority", 10);
        this.config.set("Regions.Global.Icon", Material.BEDROCK.name());
        this.config.set("Regions.Global.Announce-Enter.Title", false);
        this.config.set("Regions.Global.Announce-Enter.Actionbar", false);
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
            TranslatableLine.WORLD_TP_UNLOADED.send(p);
            return;
        }

        p.teleport(this.world.getSpawnLocation());
        if (!silent) {
            TranslatableLine.WORLD_TP.setV1(TranslatableLine.ReplacableVar.WORLD.eq(this.getRWorldName())).send(p);
        }
    }

    public boolean setGameRule(String gameRule, String val) {
        try {
            Integer op = Integer.valueOf(val);
            switch (gameRule) {
                case "randomTickSpeed":
                    this.getWorld().setGameRule(GameRule.RANDOM_TICK_SPEED, op);
                    break;
                case "spawnRadius":
                    this.getWorld().setGameRule(GameRule.SPAWN_RADIUS, op);
                    break;
                case "maxEntityCramming":
                    this.getWorld().setGameRule(GameRule.MAX_ENTITY_CRAMMING, op);
                    break;
                case "maxCommandChainLength":
                    this.getWorld().setGameRule(GameRule.MAX_COMMAND_CHAIN_LENGTH, op);
                    break;
                default:
                    return false;
            }

            return true;
        } catch (NumberFormatException e) {
            try {
                Boolean op = Boolean.valueOf(val);
                switch (gameRule) {
                    case "announceAdvancements":
                        this.getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, op);
                        break;
                    case "commandBlockOutput":
                        this.getWorld().setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, op);
                        break;
                    case "disableElytraMovementCheck":
                        this.getWorld().setGameRule(GameRule.DISABLE_ELYTRA_MOVEMENT_CHECK, op);
                        break;
                    case "doDaylightCycle":
                        this.getWorld().setGameRule(GameRule.DO_DAYLIGHT_CYCLE, op);
                        break;
                    case "doEntityDrops":
                        this.getWorld().setGameRule(GameRule.DO_ENTITY_DROPS, op);
                        break;
                    case "doFireTick":
                        this.getWorld().setGameRule(GameRule.DO_FIRE_TICK, op);
                        break;
                    case "doLimitedCrafting":
                        this.getWorld().setGameRule(GameRule.DO_LIMITED_CRAFTING, op);
                        break;
                    case "doMobLoot":
                        this.getWorld().setGameRule(GameRule.DO_MOB_LOOT, op);
                        break;
                    case "doMobSpawning":
                        this.getWorld().setGameRule(GameRule.DO_MOB_SPAWNING, op);
                        break;
                    case "doTileDrops":
                        this.getWorld().setGameRule(GameRule.DO_TILE_DROPS, op);
                        break;
                    case "doWeatherCycle":
                        this.getWorld().setGameRule(GameRule.DO_WEATHER_CYCLE, op);
                        break;
                    case "keepInventory":
                        this.getWorld().setGameRule(GameRule.KEEP_INVENTORY, op);
                        break;
                    case "logAdminCommands":
                        this.getWorld().setGameRule(GameRule.LOG_ADMIN_COMMANDS, op);
                        break;
                    case "mobGriefing":
                        this.getWorld().setGameRule(GameRule.MOB_GRIEFING, op);
                        break;
                    case "naturalRegeneration":
                        this.getWorld().setGameRule(GameRule.NATURAL_REGENERATION, op);
                        break;
                    case "reducedDebugInfo":
                        this.getWorld().setGameRule(GameRule.REDUCED_DEBUG_INFO, op);
                        break;
                    case "sendCommandFeedback":
                        this.getWorld().setGameRule(GameRule.SEND_COMMAND_FEEDBACK, op);
                        break;
                    case "showDeathMessages":
                        this.getWorld().setGameRule(GameRule.SHOW_DEATH_MESSAGES, op);
                        break;
                    case "spectatorsGenerateChunks":
                        this.getWorld().setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, op);
                        break;
                    default:
                        return false;
                }
            } catch (Exception ex) {
                return false;
            }
        }

        return true;
    }

    public String getRWorldName() {
        return this.worldName;
    }

    public ItemStack getItem() {
        return this.getWorldType() == WorldType.UNKNOWN_TO_BE_IMPORTED ?
                Itens.createItem(getIcon(), 1, "&f" + this.getRWorldName() + " &7[&e&lUNIMPORTED&7]", Arrays.asList("&f", "&7Click to import this world.", "&cQ (Drop)&7 to &cdelete &7this world."))
                : Itens.createItem(getIcon(), 1, "&f" + this.getRWorldName() + " &7[&b" + (this.getWorld() == null ? "&e&lUNLOADED" : this.getWorldSizeMB() + "mb") + "&7]", Arrays.asList("&5", " &6On this world:", "  &b" + (this.getWorld() == null ? "?" : this.getWorld().getPlayers().size()) + " &fplayers.", "  &b" + (this.getWorld() == null ? "?" : this.getWorld().getEntities().size()) + " &fentities.", "  &b" + (this.getWorld() == null ? "?" : this.getWorld().getLoadedChunks().length) + " &floaded chunks.", "", "&fRegistered on: &b" + Text.convertUnixTimeToDate(this.getRegistrationDate()), "&f", "&7Left Click to inspect this world.", "&7Middle click to change the world icon.", "&7Right Click to teleport to this world.", "&cQ (Drop)&7 to unregister this world."));
    }

    private Material getIcon() {
        return this.icon;
    }

    public void setIcon(Material a) {
        this.icon = a;
    }

    public void setLoaded(boolean b) {
        this.loaded = b;

        this.saveData(Data.LOAD, true);

        if (b) {
            postWorldLoad();
        }
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public void saveData(Data dw, boolean saveConfig) {
        switch (dw) {
            case ALL:
                saveData(Data.ICON, false);
                saveData(Data.LOAD, false);
                saveData(Data.WORLD_INVENTORIES, false);
                break;
            case ICON:
                config.set("Settings.Icon", this.icon.name());
                break;
            case LOAD:
                config.set("Settings.Load", this.loaded);
                break;
            case TP_JOIN:
                config.set("Settings.TP-On-Join", this.tpOnJoin);
            case WORLD_INVENTORIES:
                config.set("Settings.World-Inventories", this.worldInventories);
                break;
        }

        if (saveConfig) {
            saveConfig();
        }
    }

    public double getWorldSizeMB() {
        return this.worldSizeMB;
    }

    public void giveWorldInventory(Player player) {
        if (this.hasWorldInventories()) {
            if (config.contains("Inventories." + player.getUniqueId() + ".Inventory")) {
                if (Objects.requireNonNull(config.getList("Inventories." + player.getUniqueId() + ".Inventory")).isEmpty()) {
                    return;
                }

                ItemStack[] items = ItemStackSpringer.getItemsDeSerialized((List<Map<String, Object>>) Objects.requireNonNull(config.getList("Inventories." + player.getUniqueId() + ".Inventory")));
                if (items != null) {
                    player.getInventory().setContents(items);
                }
            }
        }
    }

    public void saveWorldInventory(Player player) {
        config.set("Inventories." + player.getUniqueId() + ".Name", player.getName());
        config.set("Inventories." + player.getUniqueId() + ".Inventory", ItemStackSpringer.getItemsSerialized(player.getInventory().getContents()));
        saveConfig();
    }
}