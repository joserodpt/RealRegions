package josegamerpt.realregions.commands;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import josegamerpt.realregions.RealRegions;
import josegamerpt.realregions.classes.RWorld;
import josegamerpt.realregions.config.Config;
import josegamerpt.realregions.gui.RegionGUI;
import josegamerpt.realregions.gui.WorldGUI;
import josegamerpt.realregions.gui.WorldViewer;
import josegamerpt.realregions.utils.Text;
import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

@Command("realregions")
@Alias("rr")
public class RealRegionsCMD extends CommandBase {

    RealRegions rr;
    public RealRegionsCMD(RealRegions r)
    {
        this.rr = r;
    }

    @Default
    public void defaultCommand(final CommandSender commandSender) {
        Text.sendList(commandSender, Arrays.asList("         &fReal&eRegions", "&7Release &a" + RealRegions.getPL().getDescription().getVersion()));
    }

    @SubCommand("reload")
    @Alias("rl")
    @Permission("realregions.admin")
    public void reloadcmd(final CommandSender commandSender) {
        Config.reload();
        rr.setPrefix(Text.color(Config.file().getString("RealRegions.Prefix")));
        Text.send((Player) commandSender, "&aReloaded.");
    }

    @SubCommand("menu")
    @Alias("m")
    @Permission("realregions.admin")
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
    @Permission("realregions.admin")
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

        RWorld rw = RealRegions.getWorldManager().getWorld(p.getWorld());
        if (!rw.hasRegion(name)) {
            try {
                WorldEditPlugin w = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
                com.sk89q.worldedit.regions.Region r = w.getSession(p).getSelection(w.getSession(p).getSelectionWorld());

                if (r != null) {
                    Location min = new Location(p.getWorld(), r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ());
                    Location max = new Location(p.getWorld(), r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ());


                    RealRegions.getWorldManager().createCubeRegion(name, min, max, rw);
                    Text.send(p, "&aRegion created.");
                    WorldGUI g = new WorldGUI(p, rw);
                    g.openInventory(p);
                }
            } catch (Exception e) {
                Text.send(p, "You dont have any selection.");
            }
        } else {
            Text.send(p, "There is already a region with that name.");
        }
    }

    @SubCommand("region")
    @Alias("reg")
    @Completion("#regions")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr reg <name>")
    public void regioncmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            RegionGUI wv = new RegionGUI(p, RealRegions.getWorldManager().getRegion(name));
            wv.openInventory(p);
        } else {
            commandSender.sendMessage("[RealRegions] Only players can run this command.");
        }
    }
}