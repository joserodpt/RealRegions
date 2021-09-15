package josegamerpt.realregions;

import josegamerpt.realregions.commands.RealRegionsCMD;
import josegamerpt.realregions.managers.WorldManager;
import josegamerpt.realregions.config.Config;
import josegamerpt.realregions.gui.*;
import josegamerpt.realregions.listeners.PlayerListener;
import josegamerpt.realregions.listeners.WorldListener;
import josegamerpt.realregions.regions.Region;
import josegamerpt.realregions.utils.CubeVisualizer;
import josegamerpt.realregions.utils.PlayerInput;
import josegamerpt.realregions.utils.Text;
import me.mattstudios.mf.base.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

public class RealRegions extends JavaPlugin {

    static WorldManager worldManager = new WorldManager();

    static Plugin pl;
    static String prefix;
    public static Plugin getPL() {
        return pl;
    }

    public static WorldManager getWorldManager() {
        return worldManager;
    }

    @Override
    public void onEnable() {
        pl = this;

        String star = "<------------------ RealRegions PT ------------------>".replace("PT", "| " +
                this.getDescription().getVersion());
        log(Level.INFO,star);
        saveDefaultConfig();
        Config.setup(this);

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(WorldViewer.getListener(), this);
        pm.registerEvents(WorldGUI.getListener(), this);
        pm.registerEvents(MaterialPicker.getListener(), this);
        pm.registerEvents(PlayerInput.getListener(), this);
        pm.registerEvents(FlagGUI.getListener(), this);
        pm.registerEvents(new PlayerListener(), this);
        pm.registerEvents(new WorldListener(), this);
        pm.registerEvents(EntityViewer.getListener(), this);

        CommandManager cm = new CommandManager(this);
        cm.hideTabComplete(true);
        cm.register(new RealRegionsCMD(this));

        log(Level.INFO,"Loading Regions.");
        worldManager.loadWorlds();
        log(Level.INFO,"Loaded " + worldManager.getWorlds().size() + " worlds and " + worldManager.getRegions().size() + " regions.");

        prefix = Text.color(Config.file().getString("RealRegions.Prefix"));

        log(Level.INFO,"Plugin has been loaded.");
        log(Level.INFO,"Author: JoseGamer_PT | " + this.getDescription().getWebsite());
        log(Level.INFO,star);

        new BukkitRunnable() {

            @Override
            public void run() {
                for (Region r : getWorldManager().getRegions()) {
                    if (r.canVisualize()) {
                        CubeVisualizer v = r.getViewingMaster();
                        v.getCube().forEach(v::spawnParticle);
                    }
                }
            }

        }.runTaskTimer(this,0, 10);
    }

    public static void log(Level lev, String string) {
        Bukkit.getLogger().log(lev, string);
    }

    public static String getPrefix() {
        return prefix + " ";
    }

    public void setPrefix(String c) {
        prefix = c;
    }
}
