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
import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.pluginhookup.ExternalPlugin;
import joserodpt.realpermissions.api.pluginhookup.ExternalPluginPermission;
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
import org.bukkit.Material;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RealRegionsPlugin extends JavaPlugin {
    private final WorldManager worldManager = new WorldManager(this);
    private boolean newUpdate;
    private RealPermissionsAPI rpa = null;

    public RealPermissionsAPI getRealPermissionsAPI() {
        return rpa;
    }

    private RealMinesAPI rma = null;

    public RealMinesAPI getRealMinesAPI() {
        return rma;
    }

    public void setRealMinesAPI(RealMinesAPI rma) {
        this.rma = rma;
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }
    private static RealRegionsPlugin pl;
    public static RealRegionsPlugin getPlugin() {
        return pl;
    }
    @Override
    public void onEnable() {
        printASCII();

        final long start = System.currentTimeMillis();

        pl = this;
        new Metrics(this, 19311);

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
            pm.registerEvents(new RealMinesListener(this), this);
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

        if (getServer().getPluginManager().getPlugin("RealPermissions") != null) {
            //register RealRegions permissions onto RealPermissions
            rpa = RealPermissionsAPI.getInstance();
            List<ExternalPluginPermission> perms = new ArrayList<>(List.of(
                    new ExternalPluginPermission("realregions.admin", "Allow access to the main operator commands of RealRegions.", Arrays.asList("rr reload", "rr worlds", "rr create", "rr tp", "rr view", "rr del", "rr delw"))
            ));
            worldManager.getRegionManager().getAllRegions().forEach(region -> perms.addAll(region.getRegionBypassPermissions()));
            rpa.getHookupAPI().addHookup(new ExternalPlugin(this.getDescription().getName(), "&fReal&aRegions", this.getDescription().getDescription(), Material.GRASS_BLOCK, perms, this.getDescription().getVersion()));
        }

        getLogger().info("Finished loading in " + ((System.currentTimeMillis() - start) / 1000F) + " seconds.");
        getLogger().info("<------------------ RealRegions vPT ------------------>".replace("PT", this.getDescription().getVersion()));
    }

    private void printASCII() {
        logWithColor("&2 ______           _______           _      ");
        logWithColor("&2 | ___ \\         | | ___ \\         (_)  ");
        logWithColor("&2 | |_/ /___  __ _| | |_/ /___  __ _ _  ___  _ __  ___ ");
        logWithColor("&2 |    // _ \\/ _` | |    // _ \\/ _` | |/ _ \\| '_ \\/ __|");
        logWithColor("&2 | |\\ \\  __/ (_| | | |\\ \\  __/ (_| | | (_) | | | \\__ \\");
        logWithColor("&2 \\_| \\_\\___|\\__,_|_\\_| \\_\\___|\\__, |_|\\___/|_| |_|___/");
        logWithColor("&0  Made by: &9JoseGamer_PT        &2__/ |   &0Version: &9" + this.getDescription().getVersion());
        logWithColor("&2                              |___/");
    }

    public void logWithColor(String s) {
        getServer().getConsoleSender().sendMessage("[" + this.getDescription().getName() + "] " + Text.color(s));
    }

    public boolean hasNewUpdate() {
        return this.newUpdate;
    }
}
