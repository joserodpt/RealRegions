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
import me.mattstudios.mf.base.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

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
        log(Level.INFO,star);
        saveDefaultConfig();
        Config.setup(this);

        pm.registerEvents(WorldViewer.getListener(), this);
        pm.registerEvents(WorldGUI.getListener(), this);
        pm.registerEvents(MaterialPicker.getListener(), this);
        pm.registerEvents(PlayerInput.getListener(), this);
        pm.registerEvents(FlagGUI.getListener(), this);
        pm.registerEvents(new PlayerListener(), this);
        pm.registerEvents(new WorldListener(), this);
        pm.registerEvents(EntityViewer.getListener(), this);

        new CommandManager(this).register(new Commands());

        log(Level.INFO,"Loading Regions.");
        WorldManager.loadWorlds();
        log(Level.INFO,"Loaded %1 worlds and %2 regions.".replace("%1", WorldManager.getWorlds().size() + "").replace("%2", WorldManager.getRegions().size() + ""));

        prefix = Text.color(Config.file().getString("RealRegions.Prefix"));

        log(Level.INFO,"Plugin has been loaded.");
        log(Level.INFO,"Author: JoseGamer_PT | " + this.getDescription().getWebsite());
        log(Level.INFO,star);

        new BukkitRunnable() {

            @Override
            public void run() {
                for (Region r : WorldManager.getRegions()) {
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
}
