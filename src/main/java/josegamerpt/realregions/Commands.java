package josegamerpt.realregions;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import josegamerpt.realregions.classes.RWorld;
import josegamerpt.realregions.config.Config;
import josegamerpt.realregions.gui.WorldGUI;
import josegamerpt.realregions.gui.WorldViewer;
import josegamerpt.realregions.managers.WorldManager;
import josegamerpt.realregions.utils.Text;
import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

@Command("realregions")
@Alias({"rg", "rr"})
public class Commands extends CommandBase {

    @Default
    public void defaultCommand(final CommandSender commandSender) {
        Text.sendList(commandSender, Arrays.asList("", "         &fReal&eRegions", "&7Release &a" + RealRegions.getPL().getDescription().getVersion(), "",
                "/region menu (opens the RealRegions Menu)"));
        Player p = (Player) commandSender;
    }

    @SubCommand("reload")
    @Alias("rl")
    @Permission("realregions.reload")
    public void reloadcmd(final CommandSender commandSender) {
        Config.reload();
        RealRegions.prefix = Text.color(Config.file().getString("RealRegions.Prefix"));
        Text.send((Player) commandSender, "&aReloaded.");
    }

    @SubCommand("menu")
    @Alias("m")
    @Permission("realregions.menu")
    public void menu(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;
            WorldViewer wv = new WorldViewer(p);
            wv.openInventory(p);
        } else {
            commandSender.sendMessage("[RealRegions] Only players can run this command.");
        }
    }

    @SubCommand("create")
    @Completion("#range:1-20")
    public void create(final CommandSender sender, final String name) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("[RealRegions] Only players can run this command.");
            return;
        }

        Player p = (Player) sender;
        if (name == null) {
            Text.send(p, "Region name is empty.");
            return;
        }

        WorldEditPlugin w = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        try {
            com.sk89q.worldedit.regions.Region r = w.getSession(p).getSelection(w.getSession(p).getSelectionWorld());

            if (r != null) {
                Location min = new Location(p.getWorld(), r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ());
                Location max = new Location(p.getWorld(), r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ());

                RWorld rw = WorldManager.getWorld(p.getWorld());

                WorldManager.createRegion(name, min, max, rw);
                Text.send(p, "&aRegion created.");
                WorldGUI g = new WorldGUI(p, rw);
                g.openInventory(p);
            }
        } catch (Exception e) {
            Text.send(p, "You dont have any selection.");
        }
    }
}