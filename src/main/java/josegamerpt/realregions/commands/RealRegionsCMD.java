package josegamerpt.realregions.commands;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import josegamerpt.realregions.RealRegions;
import josegamerpt.realregions.classes.RWorld;
import josegamerpt.realregions.config.Config;
import josegamerpt.realregions.gui.RegionGUI;
import josegamerpt.realregions.gui.WorldGUI;
import josegamerpt.realregions.gui.WorldViewer;
import josegamerpt.realregions.regions.Region;
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
        Text.sendList(commandSender, Arrays.asList("         &fReal&eRegions", "&7Release &a" + RealRegions.getInstance().getDescription().getVersion()));
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

        RWorld rw = RealRegions.getInstance().getWorldManager().getWorld(p.getWorld());
        if (!RealRegions.getInstance().getWorldManager().getRegionManager().hasRegion(rw, name)) {
            try {
                WorldEditPlugin w = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
                com.sk89q.worldedit.regions.Region r = w.getSession(p).getSelection(w.getSession(p).getSelectionWorld());

                if (r != null) {
                    Location min = new Location(p.getWorld(), r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ());
                    Location max = new Location(p.getWorld(), r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ());

                    RealRegions.getInstance().getWorldManager().getRegionManager().createCubeRegion(name, min, max, rw);
                    Text.send(p, "&aRegion created.");
                    WorldGUI g = new WorldGUI(p, rw);
                    g.openInventory(p);
                }
            } catch (Exception e) {
                Text.send(p, "You dont have any selection.");
                e.printStackTrace();
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

            RWorld rw = RealRegions.getInstance().getWorldManager().getWorld(p.getWorld());
            if (rw == null) {
                Text.send(p, "There is no world named &c" + name);
                return;
            }

            Region rr = RealRegions.getInstance().getWorldManager().getRegionManager().getRegion(rw, name);
            if (rr == null) {
                Text.send(p, "There is no region named &c" + name);
                return;
            }

            RegionGUI wv = new RegionGUI(p, rr);
            wv.openInventory(p);
        } else {
            commandSender.sendMessage("[RealRegions] Only players can run this command.");
        }
    }

    @SubCommand("world")
    @Alias("w")
    @Completion("#mundos")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr w <name>")
    public void worldcmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            RWorld rw = RealRegions.getInstance().getWorldManager().getWorld(name);
            if (rw == null) {
                Text.send(p, "There is no world named &c" + name);
                return;
            }

            WorldGUI wv = new WorldGUI(p, rw);
            wv.openInventory(p);
        } else {
            commandSender.sendMessage("[RealRegions] Only players can run this command.");
        }
    }
}