package joserodpt.realregions.managers;

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

import joserodpt.realmines.api.mine.RMine;
import joserodpt.realregions.RealRegionsPlugin;
import joserodpt.realregions.config.Language;
import joserodpt.realregions.regions.CuboidRegion;
import joserodpt.realregions.regions.RWorld;
import joserodpt.realregions.regions.Region;
import joserodpt.realregions.utils.Cube;
import joserodpt.realregions.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WorldManager {
    private final RegionManager rm = new RegionManager(this);
    private final RealRegionsPlugin rr;
    public WorldManager(RealRegionsPlugin rr) {
        this.rr = rr;
    }
    private Map<RWorld, List<Region>> worlds_reg_dic = new HashMap<>();
    public Map<RWorld, List<Region>> getWorldsAndRegions() {
        return worlds_reg_dic;
    }
    public RegionManager getRegionManager() {
        return rm;
    }

    public RealRegionsPlugin getPlugin() {
        return rr;
    }

    public List<RWorld> getWorlds() {
        return new ArrayList<>(worlds_reg_dic.keySet());
    }
    public void loadWorlds() {
        //check if folder "worlds" exists in RealRegions
        File folder = new File(rr.getDataFolder() + "/worlds");

        if (folder.exists() && folder.isDirectory()) {
            for (File world : folder.listFiles()) {
                String worldName = world.getName().replace(".yml", "");

                //temporary load world config file to see if it is to load world
                FileConfiguration worldConfig = YamlConfiguration.loadConfiguration(world);

                RWorld.WorldType wt = RWorld.WorldType.valueOf(worldConfig.getString("Settings.Type"));

                if (worldConfig.getBoolean("Settings.Load")) {
                    //to load world, check if folder exists
                    File worldFolder = new File(Bukkit.getWorldContainer() + "/" + worldName);

                    //if it doesn't exist, display an warning
                    if (!worldFolder.exists() || !worldFolder.isDirectory()) {
                        rr.getLogger().severe(worldName + " folder NOT FOUND in server's directory. This world will not be loaded onto RealRegions. Please verify.");
                    } else {
                        WorldCreator worldCreator = new WorldCreator(worldName);
                        World w = worldCreator.createWorld();
                        if (w != null) {
                            this.initializeRWorld(worldName, w, wt);
                        } else {
                            Bukkit.getLogger().severe("Failed to load world: " + worldName);
                        }
                    }
                } else {
                    //don't load world, but it's registered
                    //load rworld object but don't load the world
                    RWorld rw = new RWorld(worldName, wt);

                    //load regions
                    worlds_reg_dic.put(rw, rm.loadRegions(rw));
                }
            }
        } else {
            //folder doesn't exist, load default worlds from Bukkit
            rr.getLogger().info("First startup, importing default worlds.");
            for (World world : Bukkit.getWorlds()) {
                initializeRWorld(world.getName(), world, RWorld.WorldType.valueOf(world.getEnvironment().name()));
            }
        }
    }
    public void createWorld(CommandSender p, String worldName, RWorld.WorldType wt) {
        Text.send(p, Language.file().getString("World.Being-Created").replace("%name%", worldName));

        WorldCreator worldCreator = new WorldCreator(worldName);

        if (wt == RWorld.WorldType.VOID) {
            worldCreator.environment(World.Environment.NORMAL);
            worldCreator.generateStructures(false);
            worldCreator.generator(new VoidWorld());
        } else {
            try {
                worldCreator.environment(World.Environment.valueOf(wt.name()));
            } catch (Exception e) {
                throw new IllegalStateException("Unexpected value in World Type (is this a bug?): " + wt.name());
            }
        }

        World world = worldCreator.createWorld();
        if (world != null) {
            //registar mundo no real regions
            this.initializeRWorld(worldName, world, wt);

            Text.send(p, Language.file().getString("World.Created").replace("%name%", worldName));
        } else {
            Text.send(p, Language.file().getString("World.Failed-To-Create").replace("%name%", worldName));
        }
    }
    private void initializeRWorld(String worldName, World w, RWorld.WorldType wt) {
        //load rworld
        RWorld rw = new RWorld(worldName, w, wt);

        //load regions
        worlds_reg_dic.put(rw, rm.loadRegions(rw));
    }
    public void loadWorld(CommandSender p, String worldName) {
        RWorld rw = getWorld(worldName);
        if (rw.isLoaded()) {
            Text.send(p, Language.file().getString("World.Already-Loaded"));

        } else {
            WorldCreator worldCreator = new WorldCreator(worldName);
            World w = worldCreator.createWorld();
            if (w != null) {
                rw.setWorld(w);
                rw.setLoaded(true);
            } else {
                Bukkit.getLogger().severe("Failed to load world: " + worldName);
            }

            Text.send(p, Language.file().getString("World.Loaded").replace("%name%", worldName));
        }
    }
    public void unloadWorld(RWorld rw, boolean save) {
        rw.setLoaded(false);
        this.getRegionManager().saveRegions(rw);

        World world = rw.getWorld();

        if (world != null) {
            RWorld tp = getWorld("world");
            for (Player player : world.getPlayers()) {
                tp.teleport(player, false);
            }
        }
        rr.getServer().unloadWorld(world, save);
    }
    public void unloadWorld(CommandSender p, RWorld r) {
        if (r.getRWorldName().equalsIgnoreCase("world") || r.getRWorldName().startsWith("world_"))
        {
            Text.send(p, Language.file().getString("World.Unload-Default-Worlds"));
        } else {
            if (!r.isLoaded()) {
                Text.send(p, Language.file().getString("World.Already-Unloaded"));
            } else {
                Text.send(p, Language.file().getString("World.Being-Unloaded").replace("%name%", r.getRWorldName()));
                unloadWorld(r, true);
                Text.send(p, Language.file().getString("World.Unloaded"));
            }
        }
    }
    public void importWorld(CommandSender p, String worldName, RWorld.WorldType wt) {
        //check if folder exists
        File worldFolder = new File(Bukkit.getWorldContainer() + "/" + worldName);

        //if it doesn't exist, display an warning
        if (!worldFolder.exists() || !worldFolder.isDirectory()) {
            Text.send(p, Language.file().getString("Folder.Not-Found").replace("%name%", worldName));
        } else {
            Text.send(p, Language.file().getString("World.Being-Imported").replace("%name%", worldName));

            WorldCreator worldCreator = new WorldCreator(worldName);

            if (wt == RWorld.WorldType.VOID) {
                worldCreator.environment(World.Environment.NORMAL);
                worldCreator.generateStructures(false);
                worldCreator.generator(new VoidWorld());
            } else {
                worldCreator.environment(World.Environment.valueOf(wt.name()));
            }

            World w = worldCreator.createWorld();
            if (w != null) {
                this.initializeRWorld(worldName, w, wt);
                Text.send(p, Language.file().getString("World.Imported").replace("%name%", worldName));
            } else {
                Text.send(p, Language.file().getString("World.Failed-To-Import").replace("%name%", worldName));
            }
        }
    }
    public void unregisterWorld(CommandSender p, RWorld r) {
        //remove world config and from plugin world list
        this.unloadWorld(r, false);
        r.deleteConfig();
        //remove from world list
        this.worlds_reg_dic.remove(r);

        Text.send(p, Language.file().getString("World.Unregistered").replace("%name%", r.getRWorldName()));
    }

    public RWorld getWorld(World w) {
        return worlds_reg_dic.keySet().stream()
                .filter(world -> world.getWorld().equals(w))
                .findFirst()
                .orElse(null);
    }
    public RWorld getWorld(String nome) {
        return worlds_reg_dic.keySet().stream()
                .filter(world -> world.getRWorldName().equalsIgnoreCase(nome))
                .findFirst()
                .orElse(null);
    }
    public void deleteWorld(CommandSender p, RWorld r, boolean removeFile) {

        if (r.getRWorldName().equalsIgnoreCase("world") || r.getRWorldName().startsWith("world_"))
        {
            Text.send(p, Language.file().getString("World.Delete-Default-Worlds"));
        } else {
            Text.send(p, Language.file().getString("World.Being-Deleted").replace("%name%", r.getRWorldName()));
            this.unloadWorld(r, false);
            r.deleteConfig();
            if (removeFile) {
                File target = new File(rr.getServer().getWorldContainer().getAbsolutePath(), r.getWorld().getName());
                try {
                    deleteDirectory(target);
                } catch (IOException e) {
                    Bukkit.getLogger().severe("Error while trying to delete directory " + target);
                    Bukkit.getLogger().severe(e.getMessage());
                    Text.send(p, Language.file().getString("Folder.Error-Removing-Files").replace("%name%", r.getRWorldName()));
                }

            }

            //remove from world list
            this.worlds_reg_dic.remove(r);

            Text.send(p, Language.file().getString("World.Deleted").replace("%name%", r.getRWorldName()));
        }
    }
    public void deleteDirectory(final File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        if (!isSymlink(directory)) {
            cleanDirectory(directory);
        }

        if (!directory.delete()) {
            rr.getLogger().warning("Unable to delete directory " + directory);
        }
    }
    private void cleanDirectory(final File directory) throws IOException {
        final File[] files = verifiedListFiles(directory);

        IOException exception = null;
        for (final File file : files) {
            try {
                forceDelete(file);
            } catch (final IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }
    public void forceDelete(final File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            final boolean filePresent = file.exists();
            if (!file.delete()) {
                if (!filePresent) {
                    throw new FileNotFoundException("File does not exist: " + file);
                }
                final String message = "Unable to delete file: " + file;
                throw new IOException(message);
            }
        }
    }
    public boolean isSymlink(final File file) {
        if (file == null) {
            throw new NullPointerException("File must no be null");
        }
        return Files.isSymbolicLink(file.toPath());
    }
    public File[] verifiedListFiles(File directory) throws IOException {
        if (!directory.exists()) {
            final String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            final String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        final File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + directory);
        }
        return files;
    }

    public void checkRealMinesRegions(Map<String, RMine> mines) {
        for (String mineName : mines.keySet()) {
            RMine mine = mines.get(mineName);
            RWorld rw = getWorld(mine.getWorld());

            if (!getRegionManager().hasRegion(rw, mineName)) {
                //create new region
                getRegionManager().createCubeRegionRealMines(mine, rw);
            } else {
                //update region location
                CuboidRegion r = (CuboidRegion) getRegionManager().getRegionPlusName(mineName + "@" + rw.getRWorldName());
                if (r != null) {
                    if (r.getCube().getPOS1() != mine.getPOS1()) {
                        r.setCube(new Cube(mine.getPOS1(), mine.getPOS2()));
                        continue;
                    }

                    if (r.getCube().getPOS2() != mine.getPOS2()) {
                        r.setCube(new Cube(mine.getPOS1(), mine.getPOS2()));
                    }
                }
            }
        }
    }

    public class VoidWorld extends ChunkGenerator {
        @Override
        public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
            return createChunkData(world);
        }

        public Location getFixedSpawnLocation(World world, Random random) {
            return new Location(world, 0.0D, 128.0D, 0.0D);
        }
    }
}