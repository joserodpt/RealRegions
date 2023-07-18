package josegamerpt.realregions;

import josegamerpt.realregions.classes.RWorld;
import josegamerpt.realregions.commands.RealRegionsCMD;
import josegamerpt.realregions.managers.WorldManager;
import josegamerpt.realregions.config.Config;
import josegamerpt.realregions.gui.*;
import josegamerpt.realregions.regions.RRegion;
import josegamerpt.realregions.utils.PlayerInput;
import josegamerpt.realregions.utils.Text;
import me.mattstudios.mf.base.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.stream.Collectors;

public class RealRegions extends JavaPlugin {
    static RealRegions pl;
    private String prefix;
    private PluginManager pm;

    public PluginManager getPluginManager() {
        return pm;
    }

    private WorldManager worldManager = new WorldManager();
    public WorldManager getWorldManager() {
        return worldManager;
    }


    public static RealRegions getInstance() {
        return pl;
    }


    @Override
    public void onEnable() {
        pl = this;

        log(Level.INFO, "<------------------ RealRegions PT ------------------>".replace("PT", "| " +
                this.getDescription().getVersion()));

        saveDefaultConfig();
        Config.setup(this);

        prefix = Text.color(Config.file().getString("RealRegions.Prefix"));

        pm = Bukkit.getPluginManager();
        pm.registerEvents(WorldViewer.getListener(), this);
        pm.registerEvents(WorldGUI.getListener(), this);
        pm.registerEvents(MaterialPicker.getListener(), this);
        pm.registerEvents(PlayerInput.getListener(), this);
        pm.registerEvents(RegionGUI.getListener(), this);
        pm.registerEvents(EntityViewer.getListener(), this);

        CommandManager cm = new CommandManager(this);
        cm.hideTabComplete(true);
        cm.getCompletionHandler().register("#regions", input ->
             worldManager.getAllRegions()
                    .stream()
                    .map(RRegion::getRegionName)
                    .collect(Collectors.toList())
        );
        cm.getCompletionHandler().register("#mundos", input ->
                worldManager.getWorlds()
                        .stream()
                        .map(RWorld::getRWorldName)
                        .collect(Collectors.toList())
        );

        cm.register(new RealRegionsCMD(this));

        log(Level.INFO,"Loading Worlds and Regions.");
        worldManager.loadWorlds();

        log(Level.INFO,"Loaded " + worldManager.getWorlds().size() + " worlds and " + worldManager.getAllRegions().size() + " regions.");

        log(Level.INFO,"Plugin has been loaded.");
        log(Level.INFO,"Author: JoseGamer_PT | " + this.getDescription().getWebsite());
        log(Level.INFO, "<------------------ RealRegions PT ------------------>".replace("PT", "| " +
                this.getDescription().getVersion()));

    }

    public void log(Level lev, String string) {
        Bukkit.getLogger().log(lev, string);
    }

    public String getPrefix() {
        return prefix + " ";
    }

    public void setPrefix(String c) {
        prefix = c;
    }
}
