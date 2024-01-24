package joserodpt.realregions;

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

import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realregions.config.Config;
import joserodpt.realregions.config.Language;
import joserodpt.realregions.gui.EntityViewer;
import joserodpt.realregions.gui.MaterialPicker;
import joserodpt.realregions.gui.RegionGUI;
import joserodpt.realregions.gui.WorldGUI;
import joserodpt.realregions.gui.WorldViewer;
import joserodpt.realregions.listeners.RealMinesListener;
import joserodpt.realregions.regions.RWorld;
import joserodpt.realregions.commands.RealRegionsCMD;
import joserodpt.realregions.managers.WorldManager;
import joserodpt.realregions.regions.Region;
import joserodpt.realregions.listeners.RegionListener;
import joserodpt.realregions.utils.PlayerInput;
import joserodpt.realregions.utils.Text;
import me.mattstudios.mf.base.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.stream.Collectors;

public class RealRegions extends JavaPlugin {
    private final WorldManager worldManager = new WorldManager(this);
    private boolean newUpdate;

    private RealMinesAPI rma = null;

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
        Language.setup(this);

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

        if (getServer().getPluginManager().getPlugin("RealMines") != null) {
            rma = RealMinesAPI.getInstance();
            pm.registerEvents(new RealMinesListener(this), this);
            getLogger().info("Hooked onto RealMines! Version: " + rma.getVersion());
            if (Config.file().getBoolean("RealRegions.Hooks.RealMines.Import-Mines")) {
                worldManager.checkRealMinesRegions(rma.getMineManager().getMines());
                getLogger().info("Loaded " + rma.getMineManager().getRegisteredMines().size() + " mine regions from RealMines.");
            }
         }

        getLogger().info("Loaded " + worldManager.getWorlds().size() + " worlds and " + worldManager.getRegionManager().getAllRegions().size() + " regions.");

        //start region visualizer
        worldManager.getRegionManager().startVisualizer();

        new UpdateChecker(this, 111629).getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                this.getLogger().info("The plugin is updated to the latest version.");
            } else {
                this.newUpdate = true;
                this.getLogger().warning("There is a new update available! Version: " + version + " -> https://www.spigotmc.org/resources/111629/");
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
