package joserodpt.realregions.api.managers;

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

import joserodpt.realregions.api.RWorld;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public abstract class WorldManagerAPI {
    public abstract Map<String, RWorld> getWorlds();

    public abstract Collection<RWorld> getWorldList();

    public abstract Collection<RWorld> getWorldsAndPossibleImports();

    public abstract RWorld getWorld(World w);

    public abstract RWorld getWorld(String nome);

    public abstract Collection<RWorld> getPossibleImports();

    protected abstract boolean isRWorld(String name);

    public abstract void loadWorlds();

    public abstract RWorld createTimedWorld(CommandSender p, String worldName, RWorld.WorldType wt, int time);

    public abstract RWorld createWorld(CommandSender p, String worldName, RWorld.WorldType wt);

    public abstract void loadWorld(CommandSender p, String worldName);

    public abstract void unloadWorld(RWorld rw, boolean save);

    public abstract void unloadWorld(CommandSender p, RWorld r);

    public abstract void importWorld(CommandSender p, String worldName, RWorld.WorldType wt);

    public abstract void unregisterWorld(CommandSender p, RWorld r);

    public abstract void deleteWorld(CommandSender p, RWorld r);

    public abstract void removeWorldFiles(CommandSender p, RWorld r);

    public abstract void deleteDirectory(File directory) throws IOException;

    protected abstract void cleanDirectory(File directory) throws IOException;

    public abstract void forceDelete(File file) throws IOException;

    public abstract boolean isSymlink(File file);

    public abstract File[] verifiedListFiles(File directory) throws IOException;

    public abstract void resetWorld(RWorld ref);
}
