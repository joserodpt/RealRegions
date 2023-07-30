package josegamerpt.realregions;

import josegamerpt.realregions.gui.EntityViewer;
import josegamerpt.realregions.gui.MaterialPicker;
import josegamerpt.realregions.gui.RegionGUI;
import josegamerpt.realregions.gui.WorldGUI;
import josegamerpt.realregions.gui.WorldViewer;
import josegamerpt.realregions.regions.RWorld;
import josegamerpt.realregions.commands.RealRegionsCMD;
import josegamerpt.realregions.managers.WorldManager;
import josegamerpt.realregions.config.Config;
import josegamerpt.realregions.regions.Region;
import josegamerpt.realregions.regions.RegionListener;
import josegamerpt.realregions.utils.PlayerInput;
import josegamerpt.realregions.utils.Text;
import me.mattstudios.mf.base.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.stream.Collectors;

public class RealRegions extends JavaPlugin {
    private final WorldManager worldManager = new WorldManager(this);
    private boolean newUpdate;

    public WorldManager getWorldManager() {
        return worldManager;
    }
    private static RealRegions pl;
    public static RealRegions getPlugin() {
        return pl;
    }
    @Override
    public void onEnable() {
        pl = this;
        new Metrics(this, 19311);

        getLogger().info("<------------------ RealRegions PT ------------------>".replace("PT", "| " +
                this.getDescription().getVersion()));

        saveDefaultConfig();
        Config.setup(this);

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new RegionListener(this), this);
        pm.registerEvents(WorldViewer.getListener(), this);
        pm.registerEvents(WorldGUI.getListener(), this);
        pm.registerEvents(MaterialPicker.getListener(), this);
        pm.registerEvents(PlayerInput.getListener(), this);
        pm.registerEvents(RegionGUI.getListener(), this);
        pm.registerEvents(EntityViewer.getListener(), this);

        CommandManager cm = new CommandManager(this);

        cm.getMessageHandler().register("cmd.no.permission", (sender) -> Text.send(sender, "&cYou don't have permission to execute this command!"));
        cm.getMessageHandler().register("cmd.no.exists", (sender) -> Text.send(sender, "&cThe command you're trying to use doesn't exist"));
        cm.getMessageHandler().register("cmd.wrong.usage", (sender) -> Text.send(sender, "&cWrong usage for the command!"));
        cm.getMessageHandler().register("cmd.no.console", sender -> Text.send(sender,  "&cCommand can't be used in the console!"));

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
        cm.getCompletionHandler().register("#worldtype", input ->
                Arrays.stream(RWorld.WorldType.values())
                        .map(Enum::name)
                        .collect(Collectors.toList())
        );

        cm.register(new RealRegionsCMD(this));

        worldManager.loadWorlds();

        getLogger().info("Loaded " + worldManager.getWorlds().size() + " worlds and " + worldManager.getRegionManager().getAllRegions().size() + " regions.");

        //start region visualizer
        worldManager.getRegionManager().startVisualizer();


        new UpdateChecker(this, 111629).getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                this.getLogger().info("The plugin is updated to the latest version.");
            } else {
                this.newUpdate = true;
                this.getLogger().info("There is a new update available! Version: " + version + " URL");
            }
        });


        getLogger().info("Plugin has been loaded.");
        getLogger().info("Author: JoseGamer_PT | " + this.getDescription().getWebsite());
        getLogger().info("<------------------ RealRegions PT ------------------>".replace("PT", "| " +
                this.getDescription().getVersion()));
    }

    public boolean hasNewUpdate() {
        return this.newUpdate;
    }
}
