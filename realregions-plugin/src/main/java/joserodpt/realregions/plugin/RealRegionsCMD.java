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

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import joserodpt.realregions.api.RealRegionsAPI;
import joserodpt.realregions.api.config.RRConfig;
import joserodpt.realregions.api.config.RRLanguage;
import joserodpt.realregions.api.config.TranslatableLine;
import joserodpt.realregions.api.regions.RWorld;
import joserodpt.realregions.api.regions.Region;
import joserodpt.realregions.api.utils.Text;
import joserodpt.realregions.plugin.gui.EntityViewer;
import joserodpt.realregions.plugin.gui.FlagSelectorGUI;
import joserodpt.realregions.plugin.gui.RegionsListGUI;
import joserodpt.realregions.plugin.gui.WorldsListGUI;
import me.mattstudios.mf.annotations.Alias;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.Completion;
import me.mattstudios.mf.annotations.Default;
import me.mattstudios.mf.annotations.Optional;
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

    RealRegionsAPI rra;
    public RealRegionsCMD(RealRegionsAPI r)
    {
        this.rra = r;
    }

    @Default
    public void defaultCommand(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;
            WorldsListGUI wv = new WorldsListGUI(p, WorldsListGUI.WorldSort.REGISTRATION_DATE, rra);
            wv.openInventory(p);
        } else {
            Text.sendList(commandSender, Arrays.asList("         &fReal&eRegions", "         &7Release &a" + rra.getPlugin().getDescription().getVersion()));
        }
    }

    @SubCommand("reload")
    @Alias("rl")
    @Permission("realregions.admin")
    public void reloadcmd(final CommandSender commandSender) {
        RRConfig.reload();
        RRLanguage.reload();

        //reload worlds config
        rra.getWorldManagerAPI().getWorlds().values().forEach(RWorld::reloadConfig);
        Text.send(commandSender, TranslatableLine.SYSTEM_RELOADED.get());
    }

    @SubCommand("worlds")
    @Alias("menu")
    @Permission("realregions.admin")
    public void worldscm(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;
            WorldsListGUI wv = new WorldsListGUI(p, WorldsListGUI.WorldSort.REGISTRATION_DATE, rra);
            wv.openInventory(p);
        } else {
            for (RWorld world : rra.getWorldManagerAPI().getWorldList().stream()
                    .sorted(Comparator.comparing(RWorld::getRWorldName)).collect(Collectors.toList())) {
                Text.send(commandSender, "&b" + world.getRWorldName() + " &f- [" + (world.isLoaded() ? "&aLoaded" : "&eUnloaded") + "&f]");
            }
        }
    }

    @SubCommand("create")
    @Alias("c")
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
            TranslatableLine.REGION_NAME_EMPTY.send(p);
            return;
        }

        RWorld rw = rra.getWorldManagerAPI().getWorld(p.getWorld());
        if (!rw.hasRegion(name)) {
            try {
                WorldEditPlugin w = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
                com.sk89q.worldedit.regions.Region r = w.getSession(p).getSelection(w.getSession(p).getSelectionWorld());

                if (r != null) {
                    Location min = new Location(p.getWorld(), r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ());
                    Location max = new Location(p.getWorld(), r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ());

                    rra.getRegionManagerAPI().createCubeRegion(name, min, max, rw);

                    TranslatableLine.REGION_CREATED.send(p);

                    RegionsListGUI g = new RegionsListGUI(p, rw, rra);
                    g.openInventory(p);
                }
            } catch (Exception e) {
                TranslatableLine.SELECTION_NONE.send(p);
                Bukkit.getLogger().severe("Error while getting player's worldedit selection:");
                Bukkit.getLogger().severe(e.getMessage());
            }
        } else {
            TranslatableLine.REGION_NAME_DUPLICATE.send(p);
        }
    }

    @SubCommand("createw")
    @Alias("cw")
    @Completion({"#range:1-20", "#worldtype"})
    @Permission("realregions.admin")
    @WrongUsage("&c/rr createw <name>")
    public void createworldcmd(final CommandSender commandSender, final String name, final String worldtype) {
        if (name == null) {
            Text.send(commandSender, TranslatableLine.WORLD_NAME_EMPTY.get());
            return;
        }

        try {
            rra.getWorldManagerAPI().createWorld(commandSender, name, RWorld.WorldType.valueOf(worldtype));
        } catch (Exception e) {
            Text.send(commandSender, TranslatableLine.WORLD_INVALID_TYPE.setV1(TranslatableLine.ReplacableVar.INPUT.eq(worldtype)).get());
        }
    }

    @SubCommand("flags")
    @Completion("#regions")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr reg <name>")
    public void regioncmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            Region reg = rra.getRegionManagerAPI().getRegionPlusName(name);
            if (reg == null) {
                Text.send(commandSender, TranslatableLine.REGION_NON_EXISTENT_NAME.setV1(TranslatableLine.ReplacableVar.NAME.eq(name)).get());
                return;
            }

            FlagSelectorGUI wv = new FlagSelectorGUI(p, reg, rra);
            wv.openInventory(p);
        } else {
            Text.send(commandSender, onlyPlayers);

        }
    }

    @SubCommand("flag")
    @Alias("f")
    @Completion({"#regions", "#flags"})
    @Permission("realregions.admin")
    @WrongUsage("&c/rr reg <name>")
    public void regioncmd(final CommandSender commandSender, final String name, final String flag, @Optional Boolean value) {
        Region reg = rra.getRegionManagerAPI().getRegionPlusName(name);
        if (reg == null) {
            Text.send(commandSender, TranslatableLine.REGION_NON_EXISTENT_NAME.setV1(TranslatableLine.ReplacableVar.NAME.eq(name)).get());
            return;
        }

        if (value == null) {
            switch (flag) {
                case "block_break":
                case "block_place":
                case "block_interact":
                case "container_interact":
                case "pvp":
                case "pve":
                case "hunger":
                case "take_damage":
                case "explosions":
                case "item_pickup":
                case "item_drop":
                case "entity_spawning":
                case "enter":
                case "access_crafting":
                case "access_chests":
                case "access_hoppers":
                case "no_chat":
                case "no_consumables":
                case "disabled_nether_portal":
                case "disabled_end_portal":
                case "no_fire_spreading":
                case "leaf_decay":
                    TranslatableLine.REGION_FLAG_SET.setV1(TranslatableLine.ReplacableVar.NAME.eq(flag)).setV2(TranslatableLine.ReplacableVar.INPUT.eq(value ? "&atrue" : "&cfalse")).send(commandSender);
                    break;
                default:
                    TranslatableLine.REGION_FLAG_UNKNOWN.send(commandSender);
                    break;
            }
            return;
        }

        boolean notFound = false;
        switch (flag) {
            case "block_break":
                reg.blockBreak = value;
                break;
            case "block_place":
                reg.blockPlace = value;
                break;
            case "block_interact":
                reg.blockInteract = value;
                break;
            case "container_interact":
                reg.containerInteract = value;
                break;
            case "pvp":
                reg.pvp = value;
                break;
            case "pve":
                reg.pve = value;
                break;
            case "hunger":
                reg.hunger = value;
                break;
            case "take_damage":
                reg.takeDamage = value;
                break;
            case "explosions":
                reg.explosions = value;
                break;
            case "item_pickup":
                reg.itemPickup = value;
                break;
            case "item_drop":
                reg.itemDrop = value;
                break;
            case "entity_spawning":
                reg.entitySpawning = value;
                break;
            case "enter":
                reg.enter = value;
                break;
            case "access_crafting":
                reg.accessCrafting = value;
                break;
            case "access_chests":
                reg.accessChests = value;
                break;
            case "access_hoppers":
                reg.accessHoppers = value;
                break;
            case "no_chat":
                reg.noChat = value;
                break;
            case "no_consumables":
                reg.noConsumables = value;
                break;
            case "disabled_nether_portal":
                reg.disabledNetherPortal = value;
                break;
            case "disabled_end_portal":
                reg.disabledEndPortal = value;
                break;
            case "no_fire_spreading":
                reg.noFireSpreading = value;
                break;
            case "leaf_decay":
                reg.leafDecay = value;
                break;
            default:
                notFound = true;
                TranslatableLine.REGION_FLAG_UNKNOWN.send(commandSender);
                break;
        }

        if (!notFound) {
            TranslatableLine.REGION_FLAG_SET.setV1(TranslatableLine.ReplacableVar.NAME.eq(flag)).setV2(TranslatableLine.ReplacableVar.INPUT.eq(value ? "&atrue" : "&cfalse")).send(commandSender);
        }
    }

    @SubCommand("regions")
    @Alias({"world", "r"})
    @Completion("#mundos")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr w <name>")
    public void regionscmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            RWorld rw = rra.getWorldManagerAPI().getWorld(name);
            if (rw == null) {
                Text.send(commandSender, TranslatableLine.WORLD_NO_WORLD_NAMED.setV1(TranslatableLine.ReplacableVar.WORLD.eq(name)).get());
                return;
            }

            RegionsListGUI wv = new RegionsListGUI(p, rw, rra);
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

            RWorld rw = rra.getWorldManagerAPI().getWorld(name);
            if (rw == null) {
                Text.send(commandSender, TranslatableLine.WORLD_NO_WORLD_NAMED.setV1(TranslatableLine.ReplacableVar.WORLD.eq(name)).get());
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

            Region reg = rra.getRegionManagerAPI().getRegionPlusName(name);
            if (reg == null) {
                
                TranslatableLine.REGION_NON_EXISTENT_NAME.setV1(TranslatableLine.ReplacableVar.NAME.eq(name)).send(p);
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
        Region reg = rra.getRegionManagerAPI().getRegionPlusName(name);
        if (reg == null) {
            Text.send(commandSender, TranslatableLine.REGION_NON_EXISTENT_NAME.setV1(TranslatableLine.ReplacableVar.NAME.eq(name)).get());
            return;
        }

        rra.getRegionManagerAPI().toggleRegionView(commandSender, reg);
    }

    @SubCommand("unload")
    @Completion("#mundos")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr unload <name>")
    public void unloadcmd(final CommandSender commandSender, final String name) {
        RWorld rw = rra.getWorldManagerAPI().getWorld(name);
        if (rw == null) {
            Text.send(commandSender, TranslatableLine.WORLD_NO_WORLD_NAMED.setV1(TranslatableLine.ReplacableVar.WORLD.eq(name)).get());

            return;
        }

        rra.getWorldManagerAPI().unloadWorld(commandSender, rw);
    }

    @SubCommand("load")
    @Completion("#mundos")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr load <name>")
    public void loadcmd(final CommandSender commandSender, final String name) {
        RWorld rw = rra.getWorldManagerAPI().getWorld(name);
        if (rw == null) {
            Text.send(commandSender, TranslatableLine.WORLD_NO_WORLD_NAMED.setV1(TranslatableLine.ReplacableVar.WORLD.eq(name)).get());

            return;
        }

        rra.getWorldManagerAPI().loadWorld(commandSender, name);
    }

    @SubCommand("unregister")
    @Completion("#mundos")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr unregister <name>")
    public void unregistercmd(final CommandSender commandSender, final String name) {
        RWorld rw = rra.getWorldManagerAPI().getWorld(name);
        if (rw == null) {
            Text.send(commandSender, TranslatableLine.WORLD_NO_WORLD_NAMED.setV1(TranslatableLine.ReplacableVar.WORLD.eq(name)).get());

            return;
        }

        rra.getWorldManagerAPI().unregisterWorld(commandSender, rw);
    }

    @SubCommand("import")
    @Completion({"#range:1-20", "#worldtype"})
    @Permission("realregions.admin")
    @WrongUsage("&c/rr import <name> <type>")
    public void importcmd(final CommandSender commandSender, final String name, final String worldtype) {
        try {
            rra.getWorldManagerAPI().importWorld(commandSender, name, RWorld.WorldType.valueOf(worldtype));
        } catch (Exception e) {
            Text.send(commandSender, TranslatableLine.WORLD_INVALID_TYPE.setV1(TranslatableLine.ReplacableVar.INPUT.eq(worldtype)).get());
        }
    }

    @SubCommand("delete")
    @Alias("del")
    @Completion("#regions")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr delete <name>")
    public void delregcmd(final CommandSender commandSender, final String name) {
        Region reg = rra.getRegionManagerAPI().getRegionPlusName(name);
        if (reg == null) {
            Text.send(commandSender, TranslatableLine.REGION_NON_EXISTENT_NAME.setV1(TranslatableLine.ReplacableVar.NAME.eq(name)).get());
            return;
        }

        rra.getRegionManagerAPI().deleteRegion(commandSender, reg);
    }

    @SubCommand("rename")
    @Alias("rn")
    @Completion("#regions")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr rename <region> <new name>")
    public void renamecmd(final CommandSender commandSender, final String name, final String newname) {
        Region reg = rra.getRegionManagerAPI().getRegionPlusName(name);
        if (reg == null) {
            Text.send(commandSender, TranslatableLine.REGION_NON_EXISTENT_NAME.setV1(TranslatableLine.ReplacableVar.NAME.eq(name)).get());
            return;
        }

        reg.setDisplayName(newname);
        Text.send(commandSender, "&aOK");

        rra.getRegionManagerAPI().deleteRegion(commandSender, reg);
    }

    @SubCommand("setbounds")
    @Alias("sb")
    @Completion("#regions")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr setbounds <region>")
    public void setboundscmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            Region reg = rra.getRegionManagerAPI().getRegionPlusName(name);
            if (reg == null) {
                Text.send(commandSender, TranslatableLine.REGION_NON_EXISTENT_NAME.setV1(TranslatableLine.ReplacableVar.NAME.eq(name)).get());
                return;
            }

            rra.getRegionManagerAPI().setRegionBounds(reg, (Player) commandSender);
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

            RWorld rw = rra.getWorldManagerAPI().getWorld(name);
            if (rw == null) {
                Text.send(commandSender, TranslatableLine.WORLD_NO_WORLD_NAMED.setV1(TranslatableLine.ReplacableVar.WORLD.eq(name)).get());
                return;
            }

            EntityViewer ev = new EntityViewer(p, rw, rra);
            ev.openInventory(p);
        } else {
            Text.send(commandSender, onlyPlayers);

        }
    }

    @SubCommand("deletew")
    @Alias("delw")
    @Completion("#mundosPLUSimport")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr delw <name>")
    public void deleteworldcmd(final CommandSender commandSender, final String name) {
        RWorld rw = rra.getWorldManagerAPI().getWorld(name);
        if (rw == null) {
            Text.send(commandSender, TranslatableLine.WORLD_NO_WORLD_NAMED.setV1(TranslatableLine.ReplacableVar.WORLD.eq(name)).get());
            return;
        }

        rra.getWorldManagerAPI().deleteWorld(commandSender, rw);
    }

    @SubCommand("players")
    @Alias("plrs")
    @Completion("#mundos")
    @Permission("realregions.admin")
    @WrongUsage("&c/rr players <name>")
    public void playerscmd(final CommandSender commandSender, final String name) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            RWorld rw = rra.getWorldManagerAPI().getWorld(name);
            if (rw == null) {
                Text.send(commandSender, TranslatableLine.WORLD_NO_WORLD_NAMED.setV1(TranslatableLine.ReplacableVar.WORLD.eq(name)).get());
                return;
            }

            EntityViewer ev = new EntityViewer(p, rw, EntityType.PLAYER, rra);
            ev.openInventory(p);
        } else {
            Text.send(commandSender, onlyPlayers);

        }
    }
}