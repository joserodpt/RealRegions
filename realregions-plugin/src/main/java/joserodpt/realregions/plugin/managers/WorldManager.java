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
 * @author José Rodrigues © 2020-2025
 * @link https://github.com/joserodpt/RealRegions
 */

import joserodpt.realregions.api.RealRegionsAPI;
import joserodpt.realregions.api.config.RRConfig;
import joserodpt.realregions.api.config.TranslatableLine;
import joserodpt.realregions.api.managers.WorldManagerAPI;
import joserodpt.realregions.api.RWorld;
import joserodpt.realregions.api.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
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
import java.util.Objects;
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
        if (nome == null || nome.isEmpty()) {
            nome = "world";
        }
        String finalNome = nome;
        return this.getWorldsAndPossibleImports().stream().filter(rWorld -> rWorld.getRWorldName().equalsIgnoreCase(finalNome)).findFirst().orElse(null);
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
            for (File world : Objects.requireNonNull(folder.listFiles())) {
                if (!world.getName().endsWith(".yml"))
                    continue; // ignore non-yml files

                String worldName = world.getName().replace(".yml", "");
                //temporary load world config file to see if it is to load world
                FileConfiguration worldConfig = YamlConfiguration.loadConfiguration(world);

                RWorld.WorldType wt;
                try {
                    wt = RWorld.WorldType.valueOf(worldConfig.getString("Settings.Type"));
                } catch (IllegalArgumentException | NullPointerException e) {
                    rra.getLogger().severe("Error while loading world " + worldName + ". Invalid world type " + worldConfig.getString("Settings.Type") + " Skipping!.");
                    continue;
                }


                try {
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
                } catch (Exception e) {
                    rra.getLogger().severe("Error while loading world " + worldName + ". Please verify the file, it may be corrupted.");
                    e.printStackTrace();
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
    public RWorld createTimedWorld(CommandSender p, String worldName, RWorld.WorldType wt, int time) {
        RWorld rw = createWorld(p, worldName, wt);
        if (rw != null) {
            rw.setResetEverySeconds(time);
            return rw;
        }
        return null;
    }

    @Override
    public RWorld createWorld(CommandSender p, String worldName, RWorld.WorldType wt) {
        TranslatableLine.WORLD_BEING_CREATED.setV1(TranslatableLine.ReplacableVar.NAME.eq(worldName)).send(p);

        World world = generateWorld(worldName, wt);
        if (world != null) {
            //registar mundo no real regions
            this.worlds.put(worldName, new RWorld(worldName, world, wt));

            TranslatableLine.WORLD_CREATED.setV1(TranslatableLine.ReplacableVar.NAME.eq(worldName)).send(p);

            return this.worlds.get(worldName);
        } else {
            TranslatableLine.WORLD_FAILED_TO_CREATE.setV1(TranslatableLine.ReplacableVar.NAME.eq(worldName)).send(p);
        }
        return null;
    }

    private World generateWorld(String worldName, RWorld.WorldType wt) {
        WorldCreator worldCreator = new WorldCreator(worldName);

        switch (wt) {
            case VOID:
                worldCreator.environment(World.Environment.NORMAL);
                worldCreator.generateStructures(false);
                worldCreator.generator(new VoidWorld());
                break;
            case NETHER:
                worldCreator.environment(World.Environment.NETHER);
                break;
            case THE_END:
                worldCreator.environment(World.Environment.THE_END);
                break;
            case FLAT:
            case NORMAL:
                worldCreator.environment(World.Environment.NORMAL);
                break;
            default:
                throw new IllegalStateException("Unexpected value in World Type (is this a bug?): " + wt.name());
        }


        return wt == RWorld.WorldType.FLAT ? worldCreator.type(WorldType.FLAT).createWorld() : worldCreator.createWorld();
    }

    @Override
    public void loadWorld(CommandSender p, String worldName) {
        RWorld rw = getWorld(worldName);
        if (rw.isLoaded()) {
            TranslatableLine.WORLD_ALREADY_LOADED.send(p);
        } else {
            WorldCreator worldCreator = new WorldCreator(worldName);
            World w = worldCreator.createWorld();
            if (w != null) {
                rw.setWorld(w);
                rw.setLoaded(true);
            } else {
                Bukkit.getLogger().severe("Failed to load world: " + worldName);
            }

            TranslatableLine.WORLD_LOADED.setV1(TranslatableLine.ReplacableVar.NAME.eq(worldName)).send(p);
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
        if (r.getRWorldName().equalsIgnoreCase("world") || r.getRWorldName().startsWith("world_")) {
            TranslatableLine.WORLD_UNLOAD_DEFAULT_WORLDS.send(p);
        } else {
            if (!r.isLoaded()) {
                TranslatableLine.WORLD_ALREADY_UNLOADED.send(p);
            } else {
                TranslatableLine.WORLD_BEING_UNLOADED.setV1(TranslatableLine.ReplacableVar.NAME.eq(r.getRWorldName())).send(p);
                unloadWorld(r, true);
                TranslatableLine.WORLD_UNLOADED.send(p);
            }
        }
    }

    @Override
    public void importWorld(CommandSender p, String worldName, RWorld.WorldType wt) {
        //check if folder exists
        File worldFolder = new File(Bukkit.getWorldContainer() + "/" + worldName);

        //if it doesn't exist, display an warning
        if (!worldFolder.exists() || !worldFolder.isDirectory()) {
            TranslatableLine.SYSTEM_NOT_FOUND.setV1(TranslatableLine.ReplacableVar.NAME.eq(worldName)).send(p);
        } else {
            TranslatableLine.WORLD_BEING_IMPORTED.setV1(TranslatableLine.ReplacableVar.NAME.eq(worldName)).send(p);

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
                TranslatableLine.WORLD_IMPORTED.setV1(TranslatableLine.ReplacableVar.NAME.eq(worldName)).send(p);
            } else {
                TranslatableLine.WORLD_FAILED_TO_IMPORT.setV1(TranslatableLine.ReplacableVar.NAME.eq(worldName)).send(p);
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

        TranslatableLine.WORLD_UNREGISTERED.setV1(TranslatableLine.ReplacableVar.NAME.eq(r.getRWorldName())).send(p);
    }

    @Override
    public void deleteWorld(CommandSender p, RWorld r) {
        if (r.getRWorldName().equalsIgnoreCase("world") || r.getRWorldName().startsWith("world_")) {
            TranslatableLine.WORLD_DELETE_DEFAULT_WORLDS.send(p);
            return;
        }

        if (r.getWorldType() == RWorld.WorldType.UNKNOWN_TO_BE_IMPORTED) {
            removeWorldFiles(p, r);
        } else {
            TranslatableLine.WORLD_BEING_DELETED.setV1(TranslatableLine.ReplacableVar.NAME.eq(r.getRWorldName())).send(p);

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
            TranslatableLine.WORLD_DELETED.setV1(TranslatableLine.ReplacableVar.NAME.eq(r.getRWorldName())).send(p);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Error while trying to delete directory " + target);
            Bukkit.getLogger().severe(e.getMessage());
            TranslatableLine.SYSTEM_ERROR_REMOVING_FILES.setV1(TranslatableLine.ReplacableVar.NAME.eq(r.getRWorldName())).send(p);
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

    @Override
    public void resetWorld(RWorld r) {
        World w = r.getWorld();
        if (w != null) {
            w.getPlayers().forEach(player -> {
                RWorld tp = getWorld(RRConfig.file().getString("RealRegions.Fallback-World"));
                tp.teleport(player, false);
            });
            w.getEntities().forEach(entity -> {
                if (!(entity instanceof Player)) {
                    entity.remove();
                }
            });
        }

        this.unloadWorld(r, false);
        removeWorldFiles(Bukkit.getConsoleSender(), r);
        World world = generateWorld(r.getRWorldName(), r.getWorldType());
        if (world != null) {
            r.setWorld(world);
            r.setLoaded(true);
        } else {
            Bukkit.getLogger().severe("Failed to reset world: " + r.getRWorldName());
        }
    }

    public class VoidWorld extends ChunkGenerator {
        @Override
        public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
            ChunkData chunkData = createChunkData(world);

            if (x == 0 && z == 0) {
                chunkData.setBlock(0, 0, 0, Material.BEDROCK);
            }
            return createChunkData(world);
        }

        public Location getFixedSpawnLocation(World world, Random random) {
            return new Location(world, 0.0D, 128.0D, 0.0D);
        }
    }
}