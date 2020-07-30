package josegamerpt.realregions;

import josegamerpt.realregions.classes.Region;
import josegamerpt.realregions.config.Config;
import josegamerpt.realregions.gui.*;
import josegamerpt.realregions.listeners.PlayerListener;
import josegamerpt.realregions.listeners.WorldListener;
import josegamerpt.realregions.managers.WorldManager;
import josegamerpt.realregions.utils.CubeVisualizer;
import josegamerpt.realregions.utils.PlayerInput;
import josegamerpt.realregions.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.swing.*;

public class RealRegions extends JavaPlugin {

    PluginManager pm = Bukkit.getPluginManager();

    public static String prefix;

    private static Plugin pl;

    public static Plugin getPL() {
        return pl;
    }

    @Override
    public void onEnable() {
        pl = this;

        String star = "<------------------ RealRegions PT ------------------>".replace("PT", "| " +
                this.getDescription().getVersion());
        log(star);
        log("Loading Config Files.");
        saveDefaultConfig();
        Config.setup(this);

        log("Registering Events.");
        pm.registerEvents(WorldViewer.getListener(), this);
        pm.registerEvents(WorldGUI.getListener(), this);
        pm.registerEvents(MaterialPicker.getListener(), this);
        pm.registerEvents(PlayerInput.getListener(), this);
        pm.registerEvents(FlagGUI.getListener(), this);
        pm.registerEvents(new PlayerListener(), this);
        pm.registerEvents(new WorldListener(), this);
        pm.registerEvents(EntityViewer.getListener(), this);

        log("Registering Commands.");
        getCommand("realregions").setExecutor(new Command());

        log("Loading Regions.");
        WorldManager.loadWorlds();
        log("Loaded %1 worlds and %2 regions.".replace("%1", WorldManager.getWorlds().size() + "").replace("%2", WorldManager.getRegions().size() + ""));

        prefix = Text.color(Config.file().getString("RealRegions.Prefix"));

        log("Plugin has been loaded.");
        log("Author: JoseGamer_PT | " + this.getDescription().getWebsite());
        log(star);

        new BukkitRunnable() {

            @Override
            public void run() {
                for (Region r : WorldManager.getRegions()) {
                    if (r.canVisualize()) {
                        CubeVisualizer v = r.getViewingMaster();
                        v.getCube().forEach(location -> v.spawnParticle(location));
                    }
                }
            }

        }.runTaskTimer(this,0, 10);
    }

    public void onDisable() {
        WorldManager.unload();
    }

    public static void log(String string) {
        System.out.print(string);
    }

    public static String getPrefix() {
        return prefix + " ";
    }
}
