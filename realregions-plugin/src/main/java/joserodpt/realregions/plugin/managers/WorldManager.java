package joserodpt.realregions.plugin.managers;

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

import joserodpt.realregions.api.RealRegionsAPI;
import joserodpt.realregions.api.config.RRLanguage;
import joserodpt.realregions.api.managers.WorldManagerAPI;
import joserodpt.realregions.api.regions.RWorld;
import joserodpt.realregions.api.regions.Region;
import joserodpt.realregions.api.utils.Text;
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

public class WorldManager extends WorldManagerAPI {
    private final RealRegionsAPI rra;
    public WorldManager(RealRegionsAPI rra) {
        this.rra = rra;
    }

    private Map<String, RWorld> worlds = new HashMap<>();

    @Override
    public Map<String, RWorld> getWorlds() {
        return worlds;
    }

    @Override
    public List<RWorld> getWorldList() {
        return new ArrayList<>(this.worlds.values());
    }

    @Override
    public List<RWorld> getWorldsAndPossibleImports() {
        List<RWorld> ret = new ArrayList<>(this.getWorldList());
        ret.addAll(this.getPossibleImports());
        return ret;
    }
    @Override
    public RWorld getWorld(World w) {
        return this.getWorldList().stream()
                .filter(world -> world.getWorld().equals(w))
                .findFirst()
                .orElse(null);
    }
    @Override
    public RWorld getWorld(String nome) {
        return this.getWorldsAndPossibleImports().stream().filter(rWorld -> rWorld.getRWorldName().equalsIgnoreCase(nome)).findFirst().orElse(null);
    }
    @Override
    public List<RWorld> getPossibleImports() {
        List<RWorld> ret = new ArrayList<>();

        File worldsFolder = Bukkit.getWorldContainer();

        if (worldsFolder.exists() && worldsFolder.isDirectory()) {
            File[] worldFolders = worldsFolder.listFiles(File::isDirectory);
            if (worldFolders != null) {
                for (File worldFolder : worldFolders) {
                    File levelDatFile = new File(worldFolder, "level.dat");
                    if (levelDatFile.exists() && levelDatFile.isFile() && !this.isRWorld(worldFolder.getName())) {
                        ret.add(new RWorld(worldFolder.getName()));
                    }
                }
            } else {
                rra.getLogger().warning("Failed to list world folders.");
            }
        } else {
            rra.getLogger().warning("Worlds folder does not exist or is not a directory.");
        }

        return ret;
    }

    @Override
    protected boolean isRWorld(String name) {
        return this.getWorlds().containsKey(name);
    }

    @Override
    public void loadWorlds() {
        //check if folder "worlds" exists in RealRegions
        File folder = new File(rra.getPlugin().getDataFolder() + "/worlds");

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
                        rra.getLogger().severe(worldName + " folder NOT FOUND in server's directory. This world will not be loaded onto RealRegions. Please verify.");
                    } else {
                        WorldCreator worldCreator = new WorldCreator(worldName);
                        World w = worldCreator.createWorld();
                        if (w != null) {
                            this.getWorlds().put(worldName, new RWorld(worldName, w, wt));
                        } else {
                            Bukkit.getLogger().severe("Failed to load world: " + worldName);
                        }
                    }
                } else {
                    //don't load world, but it's registered
                    //load rworld object but don't load the world
                    worlds.put(worldName, new RWorld(worldName, wt));
                }
            }
        } else {
            //folder doesn't exist, load default worlds from Bukkit
            rra.getLogger().info("First startup, importing default worlds.");
            for (World world : Bukkit.getWorlds()) {
                this.getWorlds().put(world.getName(), new RWorld(world.getName(), world, RWorld.WorldType.valueOf(world.getEnvironment().name())));
            }
        }
    }
    @Override
    public void createWorld(CommandSender p, String worldName, RWorld.WorldType wt) {
        Text.send(p, RRLanguage.file().getString("World.Being-Created").replace("%name%", worldName));

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
            this.worlds.put(worldName, new RWorld(worldName, world, wt));

            Text.send(p, RRLanguage.file().getString("World.Created").replace("%name%", worldName));
        } else {
            Text.send(p, RRLanguage.file().getString("World.Failed-To-Create").replace("%name%", worldName));
        }
    }
    @Override
    public void loadWorld(CommandSender p, String worldName) {
        RWorld rw = getWorld(worldName);
        if (rw.isLoaded()) {
            Text.send(p, RRLanguage.file().getString("World.Already-Loaded"));

        } else {
            WorldCreator worldCreator = new WorldCreator(worldName);
            World w = worldCreator.createWorld();
            if (w != null) {
                rw.setWorld(w);
                rw.setLoaded(true);
            } else {
                Bukkit.getLogger().severe("Failed to load world: " + worldName);
            }

            Text.send(p, RRLanguage.file().getString("World.Loaded").replace("%name%", worldName));
        }
    }
    @Override
    public void unloadWorld(RWorld rw, boolean save) {
        rw.setLoaded(false);
        rw.getRegionList().forEach(region -> region.saveData(Region.RegionData.ALL));

        World world = rw.getWorld();

        if (world != null) {
            RWorld tp = getWorld("world");
            for (Player player : world.getPlayers()) {
                tp.teleport(player, false);
            }
        }
        rra.getPlugin().getServer().unloadWorld(world, save);
    }
    @Override
    public void unloadWorld(CommandSender p, RWorld r) {
        if (r.getRWorldName().equalsIgnoreCase("world") || r.getRWorldName().startsWith("world_"))
        {
            Text.send(p, RRLanguage.file().getString("World.Unload-Default-Worlds"));
        } else {
            if (!r.isLoaded()) {
                Text.send(p, RRLanguage.file().getString("World.Already-Unloaded"));
            } else {
                Text.send(p, RRLanguage.file().getString("World.Being-Unloaded").replace("%name%", r.getRWorldName()));
                unloadWorld(r, true);
                Text.send(p, RRLanguage.file().getString("World.Unloaded"));
            }
        }
    }
    @Override
    public void importWorld(CommandSender p, String worldName, RWorld.WorldType wt) {
        //check if folder exists
        File worldFolder = new File(Bukkit.getWorldContainer() + "/" + worldName);

        //if it doesn't exist, display an warning
        if (!worldFolder.exists() || !worldFolder.isDirectory()) {
            Text.send(p, RRLanguage.file().getString("Folder.Not-Found").replace("%name%", worldName));
        } else {
            Text.send(p, RRLanguage.file().getString("World.Being-Imported").replace("%name%", worldName));

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
                this.getWorlds().put(worldName, new RWorld(worldName, w, wt));
                Text.send(p, RRLanguage.file().getString("World.Imported").replace("%name%", worldName));
            } else {
                Text.send(p, RRLanguage.file().getString("World.Failed-To-Import").replace("%name%", worldName));
            }
        }
    }
    @Override
    public void unregisterWorld(CommandSender p, RWorld r) {
        if (r.getWorldType() == RWorld.WorldType.UNKNOWN_TO_BE_IMPORTED) {
            return;
        }
        //remove world config and from plugin world list
        this.unloadWorld(r, false);
        r.deleteConfig();
        //remove from world list
        this.getWorlds().remove(r.getRWorldName());

        Text.send(p, RRLanguage.file().getString("World.Unregistered").replace("%name%", r.getRWorldName()));
    }
    @Override
    public void deleteWorld(CommandSender p, RWorld r) {
        if (r.getRWorldName().equalsIgnoreCase("world") || r.getRWorldName().startsWith("world_"))
        {
            Text.send(p, RRLanguage.file().getString("World.Delete-Default-Worlds"));
            return;
        }

        if (r.getWorldType() == RWorld.WorldType.UNKNOWN_TO_BE_IMPORTED) {
            removeWorldFiles(p, r);
        } else {
            Text.send(p, RRLanguage.file().getString("World.Being-Deleted").replace("%name%", r.getRWorldName()));
            this.unloadWorld(r, false);
            r.deleteConfig();
            removeWorldFiles(p, r);

            //remove from world list
            this.getWorlds().remove(r.getRWorldName());
        }
    }

    @Override
    public void removeWorldFiles(CommandSender p, RWorld r) {
        File target = new File(rra.getPlugin().getServer().getWorldContainer().getAbsolutePath(), r.getRWorldName());
        try {
            deleteDirectory(target);
            Text.send(p, RRLanguage.file().getString("World.Deleted").replace("%name%", r.getRWorldName()));
        } catch (IOException e) {
            Bukkit.getLogger().severe("Error while trying to delete directory " + target);
            Bukkit.getLogger().severe(e.getMessage());
            Text.send(p, RRLanguage.file().getString("Folder.Error-Removing-Files").replace("%name%", r.getRWorldName()));
        }
    }

    @Override
    public void deleteDirectory(final File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        if (!isSymlink(directory)) {
            cleanDirectory(directory);
        }

        if (!directory.delete()) {
            rra.getLogger().warning("Unable to delete directory " + directory);
        }
    }
    @Override
    protected void cleanDirectory(final File directory) throws IOException {
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
    @Override
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
    @Override
    public boolean isSymlink(final File file) {
        if (file == null) {
            throw new NullPointerException("File must no be null");
        }
        return Files.isSymbolicLink(file.toPath());
    }
    @Override
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