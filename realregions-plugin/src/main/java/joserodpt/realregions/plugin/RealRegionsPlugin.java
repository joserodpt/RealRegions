package joserodpt.realregions.plugin;

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

import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.pluginhookup.ExternalPlugin;
import joserodpt.realpermissions.api.pluginhookup.ExternalPluginPermission;
import joserodpt.realregions.api.RealRegionsAPI;
import joserodpt.realregions.api.config.RRConfig;
import joserodpt.realregions.api.config.RRLanguage;
import joserodpt.realregions.api.regions.RWorld;
import joserodpt.realregions.api.regions.Region;
import joserodpt.realregions.api.utils.PlayerInput;
import joserodpt.realregions.api.utils.Text;
import joserodpt.realregions.plugin.gui.EntityViewer;
import joserodpt.realregions.plugin.gui.FlagSelectorGUI;
import joserodpt.realregions.plugin.gui.MaterialPickerGUI;
import joserodpt.realregions.plugin.gui.RegionsListGUI;
import joserodpt.realregions.plugin.gui.WorldsListGUI;
import joserodpt.realregions.plugin.listeners.RealMinesListener;
import joserodpt.realregions.plugin.listeners.RegionListener;
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
    private boolean newUpdate;
    private RealRegions realRegions;

    private static RealRegionsPlugin pl;
    public static RealRegionsPlugin getPlugin() {
        return pl;
    }
    @Override
    public void onEnable() {
        printASCII();

        final long start = System.currentTimeMillis();

        pl = this;
        realRegions = new RealRegions(this);
        RealRegionsAPI.setInstance(realRegions);

        new Metrics(this, 19311);

        saveDefaultConfig();
        RRConfig.setup(this);
        RRLanguage.setup(this);

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new RegionListener(realRegions), this);
        pm.registerEvents(WorldsListGUI.getListener(), this);
        pm.registerEvents(RegionsListGUI.getListener(), this);
        pm.registerEvents(MaterialPickerGUI.getListener(), this);
        pm.registerEvents(PlayerInput.getListener(), this);
        pm.registerEvents(FlagSelectorGUI.getListener(), this);
        pm.registerEvents(EntityViewer.getListener(), this);

        CommandManager cm = new CommandManager(this);

        cm.getMessageHandler().register("cmd.no.permission", (sender) -> Text.send(sender, "&cYou don't have permission to execute this command!"));
        cm.getMessageHandler().register("cmd.no.exists", (sender) -> Text.send(sender, "&cThe command you're trying to use doesn't exist"));
        cm.getMessageHandler().register("cmd.wrong.usage", (sender) -> Text.send(sender, "&cWrong usage for the command!"));
        cm.getMessageHandler().register("cmd.no.console", sender -> Text.send(sender,  "&cCommand can't be used in the console!"));

        cm.hideTabComplete(true);
        cm.getCompletionHandler().register("#regions", input ->
                realRegions.getRegionManagerAPI().getRegions()
                        .stream()
                        .map(Region::getRegionNamePlusWorld)
                        .collect(Collectors.toList())
        );
        cm.getCompletionHandler().register("#mundos", input ->
                realRegions.getWorldManagerAPI().getWorldList()
                        .stream()
                        .map(RWorld::getRWorldName)
                        .collect(Collectors.toList())
        );
        cm.getCompletionHandler().register("#mundosPLUSimport", input ->
                realRegions.getWorldManagerAPI().getWorldsAndPossibleImports()
                        .stream()
                        .map(RWorld::getRWorldName)
                        .collect(Collectors.toList())
        );
        cm.getCompletionHandler().register("#worldtype", input ->
                Arrays.asList("NORMAL", "NETHER", "THE_END", "VOID")
        );

        cm.register(new RealRegionsCMD(realRegions));

        realRegions.getWorldManagerAPI().loadWorlds();

        if (getServer().getPluginManager().getPlugin("RealMines") != null) {
            pm.registerEvents(new RealMinesListener(realRegions), this);
        }

        getLogger().info("Loaded " + realRegions.getWorldManagerAPI().getWorlds().size() + " worlds and " + realRegions.getRegionManagerAPI().getRegions().size() + " regions.");

        //start region visualizer
        realRegions.getRegionManagerAPI().startVisualizer();

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
            realRegions.setRealPermissionsAPI(RealPermissionsAPI.getInstance());
            List<ExternalPluginPermission> perms = new ArrayList<>(List.of(
                    new ExternalPluginPermission("realregions.admin", "Allow access to the main operator commands of RealRegions.", Arrays.asList("rr reload", "rr worlds", "rr create", "rr tp", "rr view", "rr del", "rr delw"))
            ));
            realRegions.getRegionManagerAPI().getRegions().forEach(region -> perms.addAll(region.getRegionBypassPermissions()));
            realRegions.getRealPermissionsAPI().getHookupAPI().addHookup(new ExternalPlugin(this.getDescription().getName(), "&fReal&aRegions", this.getDescription().getDescription(), Material.GRASS_BLOCK, perms, this.getDescription().getVersion()));
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) { //
            new RealRegionsPlaceholderAPI(realRegions).register(); //
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
        logWithColor("&8  Made by: &9JoseGamer_PT        &2__/ |   &8Version: &9" + this.getDescription().getVersion());
        logWithColor("&2                              |___/");
    }

    public void logWithColor(String s) {
        getServer().getConsoleSender().sendMessage("[" + this.getDescription().getName() + "] " + Text.color(s));
    }

    public boolean hasNewUpdate() {
        return this.newUpdate;
    }
}
