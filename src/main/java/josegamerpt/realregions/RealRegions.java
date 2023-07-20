package josegamerpt.realregions;

import josegamerpt.realregions.regions.RWorld;
import josegamerpt.realregions.commands.RealRegionsCMD;
import josegamerpt.realregions.managers.WorldManager;
import josegamerpt.realregions.config.Config;
import josegamerpt.realregions.gui.*;
import josegamerpt.realregions.regions.Region;
import josegamerpt.realregions.regions.RegionListener;
import josegamerpt.realregions.utils.PlayerInput;
import josegamerpt.realregions.utils.Text;
import me.mattstudios.mf.base.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.stream.Collectors;

public class RealRegions extends JavaPlugin {
    static RealRegions pl;
    private String prefix;
    private final WorldManager worldManager = new WorldManager();
    public WorldManager getWorldManager() {
        return worldManager;
    }
    public static RealRegions getPlugin() {
        return pl;
    }
    @Override
    public void onEnable() {
        pl = this;

        getLogger().info("<------------------ RealRegions PT ------------------>".replace("PT", "| " +
                this.getDescription().getVersion()));

        saveDefaultConfig();
        Config.setup(this);

        prefix = Text.color(Config.getConfig().getString("RealRegions.Prefix"));

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new RegionListener(), this);
        pm.registerEvents(WorldViewer.getListener(), this);
        pm.registerEvents(WorldGUI.getListener(), this);
        pm.registerEvents(MaterialPicker.getListener(), this);
        pm.registerEvents(PlayerInput.getListener(), this);
        pm.registerEvents(RegionGUI.getListener(), this);
        pm.registerEvents(EntityViewer.getListener(), this);

        CommandManager cm = new CommandManager(this);
        cm.hideTabComplete(true);
        cm.getCompletionHandler().register("#regions", input ->
             worldManager.getRegionManager().getAllRegions()
                    .stream()
                    .map(Region::getRegionNamePlusWorld)
                    .collect(Collectors.toList())
        );
        cm.getCompletionHandler().register("#mundos", input ->
                worldManager.getWorlds()
                        .stream()
                        .map(RWorld::getRWorldName)
                        .collect(Collectors.toList())
        );

        cm.register(new RealRegionsCMD(this));

        worldManager.loadWorlds();

        getLogger().info("Loaded " + worldManager.getWorlds().size() + " worlds and " + worldManager.getRegionManager().getAllRegions().size() + " regions.");

        //start region visualizer
        worldManager.getRegionManager().startVisualizer();

        getLogger().info("Plugin has been loaded.");
        getLogger().info("Author: JoseGamer_PT | " + this.getDescription().getWebsite());
        getLogger().info("<------------------ RealRegions PT ------------------>".replace("PT", "| " +
                this.getDescription().getVersion()));
    }

    public String getPrefix() {
        return this.prefix + " ";
    }

    public void setPrefix(String c) {
        this.prefix = c;
    }
}
