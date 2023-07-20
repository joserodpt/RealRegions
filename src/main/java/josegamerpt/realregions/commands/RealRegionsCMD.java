package josegamerpt.realregions.commands;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import josegamerpt.realregions.RealRegions;
import josegamerpt.realregions.regions.RWorld;
import josegamerpt.realregions.config.Config;
import josegamerpt.realregions.gui.EntityViewer;
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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

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
        Text.sendList(commandSender, Arrays.asList("         &fReal&eRegions", "         &7Release &a" + RealRegions.getPlugin().getDescription().getVersion()));
    }

    @SubCommand("reload")
    @Alias("rl")
    @Permission("realregions.admin")
    public void reloadcmd(final CommandSender commandSender) {
        Config.reload();
        //reload prefix
        rr.setPrefix(Text.color(Config.getConfig().getString("RealRegions.Prefix")));

        //reload worlds config
        RealRegions.getPlugin().getWorldManager().getWorlds().forEach(RWorld::reloadConfig);
        Text.send(commandSender, "&aReloaded.");
    }

    @SubCommand("worlds")
    @Alias("menu")
    @Permission("realregions.admin")
    public void worldscm(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;
            WorldViewer wv = new WorldViewer(p, WorldViewer.WorldSort.TIME);
            wv.openInventory(p);
        } else {
            for (RWorld world : RealRegions.getPlugin().getWorldManager().getWorlds().stream()
                    .sorted(Comparator.comparing(RWorld::getRWorldName)).collect(Collectors.toList())) {
                Text.send(commandSender, "&b" + world.getRWorldName() + " &f- [" + (world.isLoaded() ? "&aLoaded" : "&eUnloaded") + "&f]");
            }
        }
    }

    @SubCommand("create")
    @Completion("#range:1-20")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr create <name>")
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

        RWorld rw = RealRegions.getPlugin().getWorldManager().getWorld(p.getWorld());
        if (!RealRegions.getPlugin().getWorldManager().getRegionManager().hasRegion(rw, name)) {
            try {
                WorldEditPlugin w = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
                com.sk89q.worldedit.regions.Region r = w.getSession(p).getSelection(w.getSession(p).getSelectionWorld());

                if (r != null) {
                    Location min = new Location(p.getWorld(), r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ());
                    Location max = new Location(p.getWorld(), r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ());

                    RealRegions.getPlugin().getWorldManager().getRegionManager().createCubeRegion(name, min, max, rw);
                    Text.send(p, "&aRegion created.");
                    WorldGUI g = new WorldGUI(p, rw);
                    g.openInventory(p);
                }
            } catch (Exception e) {
                Text.send(p, "You don't have any selection.");
                e.printStackTrace();
            }
        } else {
            Text.send(p, "There is already a region with that name.");
        }
    }

    @SubCommand("createw")
    @Completion({"#range:1-20", "#worldtype"})
    @Permission("realregions.admin")
    @WrongUsage("&c/rr createw <name>")
    public void createworldcmd(final CommandSender commandSender, final String name, final String worldtype) {
        if (name == null) {
            Text.send(commandSender, "World name is empty.");
            return;
        }

        try {
            RealRegions.getPlugin().getWorldManager().createWorld(commandSender, name, RWorld.WorldType.valueOf(worldtype));
        } catch (Exception e) {
            Text.send(commandSender, "&cThere is no world type named " + worldtype);
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

            Region rr = RealRegions.getPlugin().getWorldManager().getRegionManager().getRegionPlusName(name);
            if (rr == null) {
                Text.send(p, "There is no region named &c" + name + ". &fMake sure the world and region name are correct.");
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

            RWorld rw = RealRegions.getPlugin().getWorldManager().getWorld(name);
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

    @SubCommand("tp")
    @Completion("#mundos")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr tp <name>")
    public void tpcmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            RWorld rw = RealRegions.getPlugin().getWorldManager().getWorld(name);
            if (rw == null) {
                Text.send(p, "There is no world named &c" + name);
                return;
            }

            rw.teleport(p, false);
        } else {
            commandSender.sendMessage("[RealRegions] Only players can run this command.");
        }
    }

    @SubCommand("tpr")
    @Completion("#regions")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr tpr <name>")
    public void tprcmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            Region rr = RealRegions.getPlugin().getWorldManager().getRegionManager().getRegionPlusName(name);
            if (rr == null) {
                Text.send(p, "There is no region named &c" + name + ". &fMake sure the world and region name are correct.");
                return;
            }

            rr.teleport(p, false);
        } else {
            commandSender.sendMessage("[RealRegions] Only players can run this command.");
        }
    }

    @SubCommand("view")
    @Completion("#regions")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr view <name>")
    public void viewcmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            Region rr = RealRegions.getPlugin().getWorldManager().getRegionManager().getRegionPlusName(name);
            if (rr == null) {
                Text.send(p, "There is no region named &c" + name + ". &fMake sure the world and region name are correct.");
                return;
            }

            rr.toggleVisual(p);
        } else {
            commandSender.sendMessage("[RealRegions] Only players can run this command.");
        }
    }

    @SubCommand("unload")
    @Completion("#mundos")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr unload <name>")
    public void unloadcmd(final CommandSender commandSender, final String name) {
        RWorld rw = RealRegions.getPlugin().getWorldManager().getWorld(name);
        if (rw == null) {
            Text.send(commandSender, "There is no world named &c" + name);
            return;
        }

        RealRegions.getPlugin().getWorldManager().unloadWorld(commandSender, rw);
    }

    @SubCommand("load")
    @Completion("#mundos")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr load <name>")
    public void loadcmd(final CommandSender commandSender, final String name) {
        RWorld rw = RealRegions.getPlugin().getWorldManager().getWorld(name);
        if (rw == null) {
            Text.send(commandSender, "There is no world named &c" + name);
            return;
        }

        RealRegions.getPlugin().getWorldManager().loadWorld(commandSender, name);
    }

    @SubCommand("unregister")
    @Completion("#mundos")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr unregister <name>")
    public void unregistercmd(final CommandSender commandSender, final String name) {
        RWorld rw = RealRegions.getPlugin().getWorldManager().getWorld(name);
        if (rw == null) {
            Text.send(commandSender, "There is no world named &c" + name);
            return;
        }

        RealRegions.getPlugin().getWorldManager().unregisterWorld(commandSender, rw);
    }

    @SubCommand("import")
    @Completion({"#range:1-20", "#worldtype"})
    @Permission("realregions.admin")
    @WrongUsage("&c/rr import <name>")
    public void importcmd(final CommandSender commandSender, final String name, final String worldtype) {
        try {
            RealRegions.getPlugin().getWorldManager().importWorld(commandSender, name, RWorld.WorldType.valueOf(worldtype));
        } catch (Exception e) {
            Text.send(commandSender, "&cThere is no world type named " + worldtype);
        }
    }

    @SubCommand("delete")
    @Alias("del")
    @Completion("#regions")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr deleter <name>")
    public void delregcmd(final CommandSender commandSender, final String name) {
        Region rr = RealRegions.getPlugin().getWorldManager().getRegionManager().getRegionPlusName(name);
        if (rr == null) {
            Text.send(commandSender, "There is no region named &c" + name + ". &fMake sure the world and region name are correct.");
            return;
        }

        RealRegions.getPlugin().getWorldManager().getRegionManager().deleteRegion(commandSender, rr);
    }

    @SubCommand("entities")
    @Alias("ents")
    @Completion("#mundos")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr ents <name>")
    public void entitiescmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            RWorld rw = RealRegions.getPlugin().getWorldManager().getWorld(name);
            if (rw == null) {
                Text.send(p, "There is no world named &c" + name);
                return;
            }

            EntityViewer ev = new EntityViewer(p, rw);
            ev.openInventory(p);
        } else {
            commandSender.sendMessage("[RealRegions] Only players can run this command.");
        }
    }

    @SubCommand("deletew")
    @Alias("delw")
    @Completion("#mundos")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr delw <name>")
    public void deleteworldcmd(final CommandSender commandSender, final String name) {
        RWorld rw = RealRegions.getPlugin().getWorldManager().getWorld(name);
        if (rw == null) {
            Text.send(commandSender, "There is no world named &c" + name);
            return;
        }

        RealRegions.getPlugin().getWorldManager().deleteWorld(commandSender, rw, true);
    }

    @SubCommand("players")
    @Alias("plrs")
    @Completion("#mundos")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr players <name>")
    public void playerscmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            RWorld rw = RealRegions.getPlugin().getWorldManager().getWorld(name);
            if (rw == null) {
                Text.send(p, "There is no world named &c" + name);
                return;
            }

            EntityViewer ev = new EntityViewer(p, rw, EntityType.PLAYER);
            ev.openInventory(p);
        } else {
            commandSender.sendMessage("[RealRegions] Only players can run this command.");
        }
    }
}