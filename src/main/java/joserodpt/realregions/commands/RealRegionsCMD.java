package joserodpt.realregions.commands;

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

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import joserodpt.realregions.RealRegionsPlugin;
import joserodpt.realregions.config.Language;
import joserodpt.realregions.regions.RWorld;
import joserodpt.realregions.config.Config;
import joserodpt.realregions.gui.EntityViewer;
import joserodpt.realregions.gui.RegionGUI;
import joserodpt.realregions.gui.WorldGUI;
import joserodpt.realregions.gui.WorldViewer;
import joserodpt.realregions.regions.Region;
import joserodpt.realregions.utils.Text;
import me.mattstudios.mf.annotations.Alias;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.Completion;
import me.mattstudios.mf.annotations.Default;
import me.mattstudios.mf.annotations.Permission;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.annotations.WrongUsage;
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
    
    private final String onlyPlayers = "[RealRegions] Only players can run this command.";

    RealRegionsPlugin rr;
    public RealRegionsCMD(RealRegionsPlugin r)
    {
        this.rr = r;
    }

    @Default
    public void defaultCommand(final CommandSender commandSender) {
        Text.sendList(commandSender, Arrays.asList("         &fReal&eRegions", "         &7Release &a" + rr.getDescription().getVersion()));
    }

    @SubCommand("reload")
    @Alias("rl")
    @Permission("realregions.admin")
    public void reloadcmd(final CommandSender commandSender) {
        Config.reload();
        Language.reload();

        //reload worlds config
        rr.getWorldManager().getWorlds().forEach(RWorld::reloadConfig);
        Text.send(commandSender, Language.file().getString("System.Reloaded"));
    }

    @SubCommand("worlds")
    @Alias("menu")
    @Permission("realregions.admin")
    public void worldscm(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;
            WorldViewer wv = new WorldViewer(p, WorldViewer.WorldSort.TIME, rr);
            wv.openInventory(p);
        } else {
            for (RWorld world : rr.getWorldManager().getWorlds().stream()
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
            Text.send(sender, onlyPlayers);
            return;
        }

        Player p = (Player) sender;
        if (name == null) {
            Text.send(p, Language.file().getString("Region.Name-Empty"));
            return;
        }

        RWorld rw = rr.getWorldManager().getWorld(p.getWorld());
        if (!rr.getWorldManager().getRegionManager().hasRegion(rw, name)) {
            try {
                WorldEditPlugin w = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
                com.sk89q.worldedit.regions.Region r = w.getSession(p).getSelection(w.getSession(p).getSelectionWorld());

                if (r != null) {
                    Location min = new Location(p.getWorld(), r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ());
                    Location max = new Location(p.getWorld(), r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ());

                    rr.getWorldManager().getRegionManager().createCubeRegion(name, min, max, rw);

                    Text.send(p, Language.file().getString("Region.Created"));

                    WorldGUI g = new WorldGUI(p, rw, rr);
                    g.openInventory(p);
                }
            } catch (Exception e) {
                Text.send(p, Language.file().getString("Selection.None"));
                Bukkit.getLogger().severe("Error while getting player's worldedit selection:");
                Bukkit.getLogger().severe(e.getMessage());
            }
        } else {
            Text.send(p, Language.file().getString("Region.Name-Duplicate"));
        }
    }

    @SubCommand("createw")
    @Completion({"#range:1-20", "#worldtype"})
    @Permission("realregions.admin")
    @WrongUsage("&c/rr createw <name>")
    public void createworldcmd(final CommandSender commandSender, final String name, final String worldtype) {
        if (name == null) {
            Text.send(commandSender, Language.file().getString("World.Name-Empty"));
            return;
        }

        try {
            rr.getWorldManager().createWorld(commandSender, name, RWorld.WorldType.valueOf(worldtype));
        } catch (Exception e) {
            Text.send(commandSender, Language.file().getString("World.Invalid-Type").replace("%type%", worldtype));
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

            Region reg = rr.getWorldManager().getRegionManager().getRegionPlusName(name);
            if (reg == null) {
                Text.send(p, "There is no region named &c" + name + ". &fMake sure the world and region name are correct.");
                return;
            }

            RegionGUI wv = new RegionGUI(p, reg, rr);
            wv.openInventory(p);
        } else {
            Text.send(commandSender, onlyPlayers);

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

            RWorld rw = rr.getWorldManager().getWorld(name);
            if (rw == null) {
                Text.send(p, Language.file().getString("World.No-World-Named").replace("%world%", name));
                return;
            }

            WorldGUI wv = new WorldGUI(p, rw, rr);
            wv.openInventory(p);
        } else {
            Text.send(commandSender, onlyPlayers);
        }
    }

    @SubCommand("tp")
    @Completion("#mundos")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr tp <name>")
    public void tpcmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            RWorld rw = rr.getWorldManager().getWorld(name);
            if (rw == null) {
                Text.send(p, Language.file().getString("World.No-World-Named").replace("%world%", name));
                return;
            }

            rw.teleport(p, false);
        } else {
            Text.send(commandSender, onlyPlayers);
        }
    }

    @SubCommand("tpr")
    @Completion("#regions")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr tpr <name>")
    public void tprcmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            Region reg = rr.getWorldManager().getRegionManager().getRegionPlusName(name);
            if (reg == null) {
                Text.send(p, Language.file().getString("Region.Non-Existent-Name").replace("%name%", name));
                return;
            }

            reg.teleport(p, false);
        } else {
            Text.send(commandSender, onlyPlayers);

        }
    }

    @SubCommand("view")
    @Completion("#regions")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr view <name>")
    public void viewcmd(final CommandSender commandSender, final String name) {
        Region reg = rr.getWorldManager().getRegionManager().getRegionPlusName(name);
        if (reg == null) {
            Text.send(commandSender, Language.file().getString("Region.Non-Existent-Name").replace("%name%", name));
            return;
        }

        reg.toggleVisual(commandSender);
    }

    @SubCommand("unload")
    @Completion("#mundos")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr unload <name>")
    public void unloadcmd(final CommandSender commandSender, final String name) {
        RWorld rw = rr.getWorldManager().getWorld(name);
        if (rw == null) {
            Text.send(commandSender, Language.file().getString("World.No-World-Named").replace("%world%", name));
            return;
        }

        rr.getWorldManager().unloadWorld(commandSender, rw);
    }

    @SubCommand("load")
    @Completion("#mundos")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr load <name>")
    public void loadcmd(final CommandSender commandSender, final String name) {
        RWorld rw = rr.getWorldManager().getWorld(name);
        if (rw == null) {
            Text.send(commandSender, Language.file().getString("World.No-World-Named").replace("%world%", name));
            return;
        }

        rr.getWorldManager().loadWorld(commandSender, name);
    }

    @SubCommand("unregister")
    @Completion("#mundos")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr unregister <name>")
    public void unregistercmd(final CommandSender commandSender, final String name) {
        RWorld rw = rr.getWorldManager().getWorld(name);
        if (rw == null) {
            Text.send(commandSender, Language.file().getString("World.No-World-Named").replace("%world%", name));
            return;
        }

        rr.getWorldManager().unregisterWorld(commandSender, rw);
    }

    @SubCommand("import")
    @Completion({"#range:1-20", "#worldtype"})
    @Permission("realregions.admin")
    @WrongUsage("&c/rr import <name> <type>")
    public void importcmd(final CommandSender commandSender, final String name, final String worldtype) {
        try {
            rr.getWorldManager().importWorld(commandSender, name, RWorld.WorldType.valueOf(worldtype));
        } catch (Exception e) {
            Text.send(commandSender, Language.file().getString("World.Invalid-Type").replace("%type%", worldtype));
        }
    }

    @SubCommand("delete")
    @Alias("del")
    @Completion("#regions")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr delete <name>")
    public void delregcmd(final CommandSender commandSender, final String name) {
        Region reg = rr.getWorldManager().getRegionManager().getRegionPlusName(name);
        if (reg == null) {
            Text.send(commandSender, Language.file().getString("Region.Non-Existent-Name").replace("%name%", name));
            return;
        }

        rr.getWorldManager().getRegionManager().deleteRegion(commandSender, reg);
    }

    @SubCommand("setbounds")
    @Alias("sb")
    @Completion("#regions")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr setbounds <region>")
    public void setboundscmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            Region reg = rr.getWorldManager().getRegionManager().getRegionPlusName(name);
            if (reg == null) {
                Text.send(commandSender, Language.file().getString("Region.Non-Existent-Name").replace("%name%", name));
                return;
            }

            rr.getWorldManager().getRegionManager().setRegionBounds(reg, (Player) commandSender);
        } else {
            Text.send(commandSender, this.onlyPlayers);
        }
    }

    @SubCommand("entities")
    @Alias("ents")
    @Completion("#mundos")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr ents <name>")
    public void entitiescmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            RWorld rw = rr.getWorldManager().getWorld(name);
            if (rw == null) {
                Text.send(p, Language.file().getString("World.No-World-Named").replace("%world%", name));
                return;
            }

            EntityViewer ev = new EntityViewer(p, rw, rr);
            ev.openInventory(p);
        } else {
            Text.send(commandSender, onlyPlayers);

        }
    }

    @SubCommand("deletew")
    @Alias("delw")
    @Completion("#mundos")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr delw <name>")
    public void deleteworldcmd(final CommandSender commandSender, final String name) {
        RWorld rw = rr.getWorldManager().getWorld(name);
        if (rw == null) {
            Text.send(commandSender, Language.file().getString("World.No-World-Named").replace("%world%", name));
            return;
        }

        rr.getWorldManager().deleteWorld(commandSender, rw, true);
    }

    @SubCommand("players")
    @Alias("plrs")
    @Completion("#mundos")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr players <name>")
    public void playerscmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            RWorld rw = rr.getWorldManager().getWorld(name);
            if (rw == null) {
                Text.send(p, Language.file().getString("World.No-World-Named").replace("%world%", name));
                return;
            }

            EntityViewer ev = new EntityViewer(p, rw, EntityType.PLAYER, rr);
            ev.openInventory(p);
        } else {
            Text.send(commandSender, onlyPlayers);

        }
    }
}