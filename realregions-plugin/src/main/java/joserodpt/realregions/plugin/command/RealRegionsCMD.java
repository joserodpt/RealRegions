package joserodpt.realregions.plugin.command;

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
 * @author José Rodrigues © 2020-2025
 * @link https://github.com/joserodpt/RealRegions
 */

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import joserodpt.realregions.api.RealRegionsAPI;
import joserodpt.realregions.api.config.RRConfig;
import joserodpt.realregions.api.config.RRLanguage;
import joserodpt.realregions.api.config.TranslatableLine;
import joserodpt.realregions.api.RWorld;
import joserodpt.realregions.api.regions.Region;
import joserodpt.realutils.text.Text;
import joserodpt.realregions.plugin.gui.ConfigEditor;
import joserodpt.realregions.plugin.gui.Confirmations;
import joserodpt.realregions.plugin.gui.EntityViewer;
import joserodpt.realregions.plugin.gui.BlockedCommandsGUI;
import joserodpt.realregions.plugin.gui.RegionSettingsGUI;
import joserodpt.realregions.plugin.gui.RegionsListGUI;
import joserodpt.realregions.plugin.gui.WorldsListGUI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Single;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

import static joserodpt.realregions.api.config.TranslatableLine.TranslatableLinePlaceholder.INPUT;
import static joserodpt.realregions.api.config.TranslatableLine.TranslatableLinePlaceholder.NAME;
import static joserodpt.realregions.api.config.TranslatableLine.TranslatableLinePlaceholder.WORLD;

/**
 * Every {@code /rr} subcommand. Those that only make sense in game take a {@link Player} rather than
 * a {@link CommandSender}, which is what tells the console it can't run them: Lamp raises
 * SenderNotPlayerException and the exception handler answers it.
 */
@Command({"realregions", "rr"})
public class RealRegionsCMD {

    private final RealRegionsAPI rra;

    public RealRegionsCMD(RealRegionsAPI r) {
        this.rra = r;
    }

    @CommandPlaceholder
    @SuppressWarnings("unused")
    public void defaultCommand(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;
            if (p.hasPermission("realregions.admin") || p.isOp()) {
                WorldsListGUI wv = new WorldsListGUI(p, WorldsListGUI.WorldSort.REGISTRATION_DATE, rra);
                wv.openInventory(p);
            } else {
                Text.sendList(commandSender, Arrays.asList("         &fReal&eRegions", "         &7Release &a" + rra.getPlugin().getDescription().getVersion()));
            }
        } else {
            Text.sendList(commandSender, Arrays.asList("         &fReal&eRegions", "         &7Release &a" + rra.getPlugin().getDescription().getVersion()));
        }
    }

    @Subcommand({"reload", "rl"})
    @CommandPermission("realregions.admin")
    @SuppressWarnings("unused")
    public void reloadcmd(final CommandSender commandSender) {
        RRConfig.reload();
        RRLanguage.reload();

        //reload worlds config
        rra.getWorldManagerAPI().getWorlds().values().forEach(RWorld::reloadConfig);
        TranslatableLine.SYSTEM_RELOADED.send(commandSender);
    }

    /** config.yml as dialogs, where the server has them. */
    @Subcommand("settings")
    @CommandPermission("realregions.admin")
    @SuppressWarnings("unused")
    public void settingscmd(final Player p) {
        ConfigEditor.open(p);
    }

    @Subcommand({"worlds", "menu"})
    @CommandPermission("realregions.admin")
    @SuppressWarnings("unused")
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

    @Subcommand({"create", "c"})
    @CommandPermission("realregions.admin")
    @Usage("&c/rr create <name>")
    @SuppressWarnings("unused")
    public void create(final Player p, @Single final String name) {
        if (name == null || name.isEmpty()) {
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
                Text.send(p, "&cError while getting player's worldedit selection. See console for details.");
                Bukkit.getLogger().severe("Error while getting player's worldedit selection:");
                e.printStackTrace();
            }
        } else {
            TranslatableLine.REGION_NAME_DUPLICATE.send(p);
        }
    }

    @Subcommand({"createworld", "cw"})
    @CommandPermission("realregions.admin")
    @Usage("&c/rr createworld <name> <type>")
    @SuppressWarnings("unused")
    public void createworldcmd(final CommandSender commandSender, @Single final String name, @SuggestFrom(RRSuggestion.WORLD_TYPES) final RWorld.WorldType worldtype) {
        if (name == null) {
            TranslatableLine.WORLD_NAME_EMPTY.send(commandSender);
            return;
        }

        if (worldtype == null) {
            TranslatableLine.WORLD_INVALID_TYPE.with(INPUT, "like that").send(commandSender);
            return;
        }

        try {
            RWorld rw = rra.getWorldManagerAPI().createWorld(commandSender, name, worldtype);
            if (rw != null && commandSender instanceof Player) {
                rw.teleport((Player) commandSender, true);
            }
        } catch (Exception e) {
            TranslatableLine.WORLD_INVALID_TYPE.with(INPUT, worldtype.name()).send(commandSender);
            e.printStackTrace();
        }
    }

    @Subcommand({"createtimedworld", "ctw"})
    @CommandPermission("realregions.admin")
    @Usage("&c/rr createtimedworld <name> <type> <seconds>")
    @SuppressWarnings("unused")
    public void createtimedworldcmd(final CommandSender commandSender, @Single final String name, @SuggestFrom(RRSuggestion.WORLD_TYPES) final RWorld.WorldType worldtype, final Integer time) {
        if (name == null) {
            TranslatableLine.WORLD_NAME_EMPTY.send(commandSender);
            return;
        }

        if (time == null || time <= 5) {
            Text.send(commandSender, "&cTime must be greater than 5");
            return;
        }

        if (worldtype == null) {
            TranslatableLine.WORLD_INVALID_TYPE.with(INPUT, "like that").send(commandSender);
            return;
        }

        try {
            RWorld rw = rra.getWorldManagerAPI().createTimedWorld(commandSender, name, worldtype, time);
            if (rw != null && commandSender instanceof Player) {
                rw.teleport((Player) commandSender, true);
            }
        } catch (Exception e) {
            TranslatableLine.WORLD_INVALID_TYPE.with(INPUT, worldtype.name()).send(commandSender);
            e.printStackTrace();
        }
    }

    @Subcommand("reset")
    @CommandPermission("realregions.admin")
    @Usage("&c/rr reset <world>")
    public void resetworld(final CommandSender commandSender, @SuggestFrom(RRSuggestion.WORLDS) @Single final String name) {
        RWorld rw = rra.getWorldManagerAPI().getWorld(name);
        if (rw == null) {
            TranslatableLine.WORLD_NO_WORLD_NAMED.with(WORLD, name).send(commandSender);
            return;
        }

        rra.getWorldManagerAPI().resetWorld(rw);
        Text.send(commandSender, "&aWorld reseted.");
    }

    @Subcommand({"flags", "region"})
    @CommandPermission("realregions.admin")
    @Usage("&c/rr flags <region@world>")
    @SuppressWarnings("unused")
    public void regioncmd(final Player p, @SuggestFrom(RRSuggestion.REGIONS) @Single final String name) {
        Region reg = rra.getRegionManagerAPI().getRegionPlusName(name);
        if (reg == null) {
            TranslatableLine.REGION_NON_EXISTENT_NAME.with(NAME, name).send(p);
            return;
        }

        RegionSettingsGUI wv = new RegionSettingsGUI(p, reg, rra);
        wv.openInventory(p);
    }

    @Subcommand({"flag", "f"})
    @CommandPermission("realregions.admin")
    @Usage("&c/rr flag <region@world> <flag> [true/false]")
    @SuppressWarnings("unused")
    public void regioncmd(final CommandSender commandSender, @SuggestFrom(RRSuggestion.REGIONS) @Single final String regionName, @SuggestFrom(RRSuggestion.FLAGS) @Single final String flag, @Optional @SuggestFrom(RRSuggestion.BOOLEANS) @Single String valueSTR) {
        Region reg = rra.getRegionManagerAPI().getRegionPlusName(regionName);
        if (reg == null) {
            TranslatableLine.REGION_NON_EXISTENT_NAME.with(NAME, regionName).send(commandSender);
            return;
        }

        if (valueSTR == null || valueSTR.isEmpty()) {
            switch (flag) {
                case "block_break":
                    TranslatableLine.REGION_FLAG_SET.with(NAME, flag).with(INPUT, reg.blockBreak ? "&a✔ true" : "&c❌ false").send(commandSender);
                    break;
                case "block_place":
                    TranslatableLine.REGION_FLAG_SET.with(NAME, flag).with(INPUT, reg.blockPlace ? "&a✔ true" : "&c❌ false").send(commandSender);
                    break;
                case "block_interact":
                    TranslatableLine.REGION_FLAG_SET.with(NAME, flag).with(INPUT, reg.blockInteract ? "&a✔ true" : "&c❌ false").send(commandSender);
                    break;
                case "container_interact":
                    TranslatableLine.REGION_FLAG_SET.with(NAME, flag).with(INPUT, reg.containerInteract ? "&a✔ true" : "&c❌ false").send(commandSender);
                    break;
                case "pvp":
                    TranslatableLine.REGION_FLAG_SET.with(NAME, flag).with(INPUT, reg.pvp ? "&a✔ true" : "&c❌ false").send(commandSender);
                    break;
                case "pve":
                    TranslatableLine.REGION_FLAG_SET.with(NAME, flag).with(INPUT, reg.pve ? "&a✔ true" : "&c❌ false").send(commandSender);
                    break;
                case "hunger":
                    TranslatableLine.REGION_FLAG_SET.with(NAME, flag).with(INPUT, reg.hunger ? "&a✔ true" : "&c❌ false").send(commandSender);
                    break;
                case "take_damage":
                    TranslatableLine.REGION_FLAG_SET.with(NAME, flag).with(INPUT, reg.takeDamage ? "&a✔ true" : "&c❌ false").send(commandSender);
                    break;
                case "explosions":
                    TranslatableLine.REGION_FLAG_SET.with(NAME, flag).with(INPUT, reg.explosions ? "&a✔ true" : "&c❌ false").send(commandSender);
                    break;
                case "item_pickup":
                    TranslatableLine.REGION_FLAG_SET.with(NAME, flag).with(INPUT, reg.itemPickup ? "&a✔ true" : "&c❌ false").send(commandSender);
                    break;
                case "item_drop":
                    TranslatableLine.REGION_FLAG_SET.with(NAME, flag).with(INPUT, reg.itemDrop ? "&a✔ true" : "&c❌ false").send(commandSender);
                    break;
                case "entity_spawning":
                    TranslatableLine.REGION_FLAG_SET.with(NAME, flag).with(INPUT, reg.entitySpawning ? "&a✔ true" : "&c❌ false").send(commandSender);
                    break;
                case "enter":
                    TranslatableLine.REGION_FLAG_SET.with(NAME, flag).with(INPUT, reg.enter ? "&a✔ true" : "&c❌ false").send(commandSender);
                    break;
                case "access_crafting":
                    TranslatableLine.REGION_FLAG_SET.with(NAME, flag).with(INPUT, reg.accessCrafting ? "&a✔ true" : "&c❌ false").send(commandSender);
                    break;
                case "access_chests":
                    TranslatableLine.REGION_FLAG_SET.with(NAME, flag).with(INPUT, reg.accessChests ? "&a✔ true" : "&c❌ false").send(commandSender);
                    break;
                case "access_hoppers":
                    TranslatableLine.REGION_FLAG_SET.with(NAME, flag).with(INPUT, reg.accessHoppers ? "&a✔ true" : "&c❌ false").send(commandSender);
                    break;
                case "no_chat":
                    TranslatableLine.REGION_FLAG_SET.with(NAME, flag).with(INPUT, reg.noChat ? "&a✔ true" : "&c❌ false").send(commandSender);
                    break;
                case "no_consumables":
                    TranslatableLine.REGION_FLAG_SET.with(NAME, flag).with(INPUT, reg.noConsumables ? "&a✔ true" : "&c❌ false").send(commandSender);
                    break;
                case "disabled_nether_portal":
                    TranslatableLine.REGION_FLAG_SET.with(NAME, flag).with(INPUT, reg.disabledNetherPortal ? "&a✔ true" : "&c❌ false").send(commandSender);
                    break;
                case "disabled_end_portal":
                    TranslatableLine.REGION_FLAG_SET.with(NAME, flag).with(INPUT, reg.disabledEndPortal ? "&a✔ true" : "&c❌ false").send(commandSender);
                    break;
                case "no_fire_spreading":
                    TranslatableLine.REGION_FLAG_SET.with(NAME, flag).with(INPUT, reg.noFireSpreading ? "&a✔ true" : "&c❌ false").send(commandSender);
                    break;
                case "item_pickup_only_owner":
                    TranslatableLine.REGION_FLAG_SET.with(NAME, flag).with(INPUT, reg.itemPickupOnlyOwner ? "&a✔ true" : "&c❌ false").send(commandSender);
                    break;
                case "block_commands":
                    TranslatableLine.REGION_FLAG_SET.with(NAME, flag).with(INPUT, reg.blockCommands ? "&a✔ true" : "&c❌ false").send(commandSender);
                    break;
                default:
                    TranslatableLine.REGION_FLAG_UNKNOWN.send(commandSender);
                    break;
            }

            return;
        }

        boolean value = Boolean.parseBoolean(valueSTR);
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
            case "item_pickup_only_owner":
                reg.itemPickupOnlyOwner = value;
                break;
            case "block_commands":
                reg.blockCommands = value;
                break;
            default:
                notFound = true;
                TranslatableLine.REGION_FLAG_UNKNOWN.send(commandSender);
                break;
        }

        if (!notFound) {
            reg.saveData(Region.RegionData.FLAGS);
            TranslatableLine.REGION_FLAG_SET.with(NAME, flag).with(INPUT, value ? "&a✔ true" : "&c❌ false").send(commandSender);
        }
    }

    @Subcommand({"blockcommands", "bc"})
    @CommandPermission("realregions.admin")
    @Usage("&c/rr blockcommands <region@world> [all | command...]")
    @SuppressWarnings("unused")
    public void blockcommandscmd(final CommandSender commandSender, @SuggestFrom(RRSuggestion.REGIONS) @Single final String name, @Optional final String commands) {
        Region reg = rra.getRegionManagerAPI().getRegionPlusName(name);
        if (reg == null) {
            TranslatableLine.REGION_NON_EXISTENT_NAME.with(NAME, name).send(commandSender);
            return;
        }

        if (commands != null) {
            //turning the flag on is left to /rr flag, so a list can be set up before it applies
            reg.setBlockedCommands(commands.trim().equalsIgnoreCase("all") ? Collections.emptyList() : Region.splitCommands(commands));
            reg.saveData(Region.RegionData.FLAGS);
        } else if (commandSender instanceof Player) {
            //nothing to set: the list, to add to and remove from
            BlockedCommandsGUI.open((Player) commandSender, reg, rra);
            return;
        }
        TranslatableLine.REGION_BLOCKED_COMMANDS_SET.with(NAME, reg.getDisplayName())
                .with(INPUT, RegionSettingsGUI.blockedCommandsText(reg)).send(commandSender);
    }

    @Subcommand({"regions", "world", "r"})
    @CommandPermission("realregions.admin")
    @Usage("&c/rr regions <world>")
    public void regionscmd(final Player p, @SuggestFrom(RRSuggestion.WORLDS) @Single final String name) {
        RWorld rw = rra.getWorldManagerAPI().getWorld(name);
        if (rw == null) {
            TranslatableLine.WORLD_NO_WORLD_NAMED.with(WORLD, name).send(p);
            return;
        }

        RegionsListGUI wv = new RegionsListGUI(p, rw, rra);
        wv.openInventory(p);
    }

    @Subcommand({"setworldspawn", "sws", "setspawn"})
    @CommandPermission("realregions.admin")
    @Usage("&c/rr setworldspawn [world]")
    @SuppressWarnings("unused")
    public void setworldspawn(final Player p, @Optional @SuggestFrom(RRSuggestion.WORLDS) @Single String name) {
        RWorld rw;

        if (name == null || name.isEmpty()) {
            rw = rra.getWorldManagerAPI().getWorld(p.getWorld());
            name = p.getWorld().getName();
        } else {
            rw = rra.getWorldManagerAPI().getWorld(name);
        }

        if (rw == null) {
            TranslatableLine.WORLD_NO_WORLD_NAMED.with(WORLD, name).send(p);
            return;
        }

        rw.setWorldSpawn(p.getLocation());
        TranslatableLine.WORLD_SPAWN_SET.with(WORLD, name).send(p);
    }

    @Subcommand("tp")
    @Usage("&c/rr tp <world>")
    @SuppressWarnings("unused")
    public void tpcmd(final Player p, @SuggestFrom(RRSuggestion.WORLDS) @Single final String name) {
        RWorld rw = rra.getWorldManagerAPI().getWorld(name);
        if (rw == null) {
            TranslatableLine.WORLD_NO_WORLD_NAMED.with(WORLD, name).send(p);
            return;
        }

        if (p.hasPermission("realregions.admin") || p.isOp() || p.hasPermission("realregions.tpworld." + rw.getRWorldName())) {
            rw.teleport(p, false);
        } else {
            Text.send(p, "&cYou don't have permission to teleport to this world.");
        }
    }

    @Subcommand("tpo")
    @CommandPermission("realregions.admin")
    @Usage("&c/rr tpo <world> <player>")
    @SuppressWarnings("unused")
    public void topcmd(final CommandSender commandSender, @SuggestFrom(RRSuggestion.WORLDS) @Single final String name, final Player player) {
        if (player == null) {
            Text.send(commandSender, "&cPlayer not found.");
            return;
        }

        RWorld rw = rra.getWorldManagerAPI().getWorld(name);
        if (rw == null) {
            TranslatableLine.WORLD_NO_WORLD_NAMED.with(WORLD, name).send(commandSender);
            return;
        }

        rw.teleport(player, false);
        Text.send(commandSender, "&aTeleported " + player.getName() + " to " + name);
    }

    @Subcommand("tpr")
    @CommandPermission("realregions.admin")
    @Usage("&c/rr tpr <region@world>")
    @SuppressWarnings("unused")
    public void tprcmd(final Player p, @SuggestFrom(RRSuggestion.REGIONS) @Single final String name) {
        Region reg = rra.getRegionManagerAPI().getRegionPlusName(name);
        if (reg == null) {
            TranslatableLine.REGION_NON_EXISTENT_NAME.with(NAME, name).send(p);
            return;
        }

        reg.teleport(p, false);
    }

    @Subcommand("view")
    @CommandPermission("realregions.admin")
    @Usage("&c/rr view <region@world>")
    @SuppressWarnings("unused")
    public void viewcmd(final CommandSender commandSender, @SuggestFrom(RRSuggestion.REGIONS) @Single final String name) {
        Region reg = rra.getRegionManagerAPI().getRegionPlusName(name);
        if (reg == null) {
            TranslatableLine.REGION_NON_EXISTENT_NAME.with(NAME, name).send(commandSender);
            return;
        }

        rra.getRegionManagerAPI().toggleRegionView(commandSender, reg);
    }

    @Subcommand("unload")
    @CommandPermission("realregions.admin")
    @Usage("&c/rr unload <world>")
    @SuppressWarnings("unused")
    public void unloadcmd(final CommandSender commandSender, @SuggestFrom(RRSuggestion.WORLDS) @Single final String name) {
        RWorld rw = rra.getWorldManagerAPI().getWorld(name);
        if (rw == null) {
            TranslatableLine.WORLD_NO_WORLD_NAMED.with(WORLD, name).send(commandSender);
            return;
        }

        final Runnable unload = () -> rra.getWorldManagerAPI().unloadWorld(commandSender, rw);
        //asked first where the server has dialogs; the console, and servers without, unload straight away
        if (!Confirmations.unloadWorld(commandSender, rw, unload, null)) {
            unload.run();
        }
    }

    @Subcommand("toggle-tpjoin")
    @CommandPermission("realregions.admin")
    @Usage("&c/rr toggle-tpjoin <world>")
    @SuppressWarnings("unused")
    public void toggletpjoin(final CommandSender commandSender, @SuggestFrom(RRSuggestion.WORLDS) @Single final String name) {
        RWorld rw = rra.getWorldManagerAPI().getWorld(name);
        if (rw == null) {
            TranslatableLine.WORLD_NO_WORLD_NAMED.with(WORLD, name).send(commandSender);
            return;
        }

        rw.setTPJoin(!rw.isTPJoinON());
        TranslatableLine.WORLD_TPJOIN_SET.with(INPUT, rw.isTPJoinON() ? "&a✔ true" : "&c❌ false").send(commandSender);
    }

    @Subcommand({"toggle-enter-title", "toggle-title"})
    @CommandPermission("realregions.admin")
    @Usage("&c/rr toggle-enter-title <region@world>")
    @SuppressWarnings("unused")
    public void toggleentertitle(final CommandSender commandSender, @SuggestFrom(RRSuggestion.REGIONS) @Single final String name) {
        Region rg = rra.getRegionManagerAPI().getRegionPlusName(name);
        if (rg == null) {
            TranslatableLine.REGION_NON_EXISTENT_NAME.with(NAME, name).send(commandSender);
            return;
        }

        rg.announceEnterTitle = !rg.announceEnterTitle;
        rg.saveData(Region.RegionData.SETTINGS);
        TranslatableLine.REGION_ENTERING_TOGGLE.with(INPUT, rg.announceEnterTitle ? "&a✔ true" : "&c❌ false").send(commandSender);
    }

    @Subcommand({"toggle-enter-actionbar", "toggle-actionbar"})
    @CommandPermission("realregions.admin")
    @Usage("&c/rr toggle-enter-actionbar <region@world>")
    @SuppressWarnings("unused")
    public void toggleenteractionbar(final CommandSender commandSender, @SuggestFrom(RRSuggestion.REGIONS) @Single final String name) {
        Region rg = rra.getRegionManagerAPI().getRegionPlusName(name);
        if (rg == null) {
            TranslatableLine.REGION_NON_EXISTENT_NAME.with(NAME, name).send(commandSender);
            return;
        }

        rg.announceEnterActionbar = !rg.announceEnterActionbar;
        rg.saveData(Region.RegionData.SETTINGS);
        TranslatableLine.REGION_ENTERING_TOGGLE.with(INPUT, rg.announceEnterActionbar ? "&a✔ true" : "&c❌ false").send(commandSender);
    }

    @Subcommand({"toggle-inventories", "toggle-invs"})
    @CommandPermission("realregions.admin")
    @Usage("&c/rr toggle-inventories <world>")
    @SuppressWarnings("unused")
    public void toggleinventoriescmd(final CommandSender commandSender, @SuggestFrom(RRSuggestion.WORLDS) @Single final String name) {
        RWorld rw = rra.getWorldManagerAPI().getWorld(name);
        if (rw == null) {
            TranslatableLine.WORLD_NO_WORLD_NAMED.with(WORLD, name).send(commandSender);
            return;
        }

        rw.setWorldInventories(!rw.hasWorldInventories());
        TranslatableLine.WORLD_INVENTORIES_SET.with(WORLD, rw.hasWorldInventories() ? "&a✔ true" : "&c❌ false").send(commandSender);
    }

    @Subcommand("load")
    @CommandPermission("realregions.admin")
    @Usage("&c/rr load <world>")
    @SuppressWarnings("unused")
    public void loadcmd(final CommandSender commandSender, @SuggestFrom(RRSuggestion.WORLDS) @Single final String name) {
        RWorld rw = rra.getWorldManagerAPI().getWorld(name);
        if (rw == null) {
            TranslatableLine.WORLD_NO_WORLD_NAMED.with(WORLD, name).send(commandSender);
            return;
        }

        rra.getWorldManagerAPI().loadWorld(commandSender, name);
    }

    @Subcommand("unregister")
    @CommandPermission("realregions.admin")
    @Usage("&c/rr unregister <world>")
    @SuppressWarnings("unused")
    public void unregistercmd(final CommandSender commandSender, @SuggestFrom(RRSuggestion.WORLDS) @Single final String name) {
        RWorld rw = rra.getWorldManagerAPI().getWorld(name);
        if (rw == null) {
            TranslatableLine.WORLD_NO_WORLD_NAMED.with(WORLD, name).send(commandSender);
            return;
        }

        rra.getWorldManagerAPI().unregisterWorld(commandSender, rw);
    }

    @Subcommand("import")
    @CommandPermission("realregions.admin")
    @Usage("&c/rr import <world> <type>")
    @SuppressWarnings("unused")
    public void importcmd(final CommandSender commandSender, @Single final String name, @SuggestFrom(RRSuggestion.WORLD_TYPES) final RWorld.WorldType worldtype) {
        if (name == null) {
            TranslatableLine.WORLD_NAME_EMPTY.send(commandSender);
            return;
        }

        if (worldtype == null) {
            TranslatableLine.WORLD_INVALID_TYPE.with(INPUT, "like that").send(commandSender);
            return;
        }

        try {
            rra.getWorldManagerAPI().importWorld(commandSender, name, worldtype);
        } catch (Exception e) {
            TranslatableLine.WORLD_INVALID_TYPE.with(INPUT, worldtype.name()).send(commandSender);
        }
    }

    @Subcommand({"delete", "del"})
    @CommandPermission("realregions.admin")
    @Usage("&c/rr delete <region@world>")
    @SuppressWarnings("unused")
    public void delregcmd(final CommandSender commandSender, @SuggestFrom(RRSuggestion.REGIONS) @Single final String name) {
        Region reg = rra.getRegionManagerAPI().getRegionPlusName(name);
        if (reg == null) {
            TranslatableLine.REGION_NON_EXISTENT_NAME.with(NAME, name).send(commandSender);
            return;
        }

        final Runnable delete = () -> rra.getRegionManagerAPI().deleteRegion(commandSender, reg);
        //asked first where the server has dialogs; the console, and servers without, delete straight away
        if (!Confirmations.deleteRegion(commandSender, reg, delete, null)) {
            delete.run();
        }
    }

    @Subcommand({"rename", "rn"})
    @CommandPermission("realregions.admin")
    @Usage("&c/rr rename <region@world> <new name>")
    @SuppressWarnings("unused")
    public void renamecmd(final CommandSender commandSender, @SuggestFrom(RRSuggestion.REGIONS) @Single final String name, @Single final String newname) {
        Region reg = rra.getRegionManagerAPI().getRegionPlusName(name);
        if (reg == null) {
            TranslatableLine.REGION_NON_EXISTENT_NAME.with(NAME, name).send(commandSender);
            return;
        }

        reg.setDisplayName(newname);
        reg.saveData(Region.RegionData.SETTINGS);
        TranslatableLine.REGION_RENAMED.with(NAME, newname).send(commandSender);
    }

    @Subcommand({"setbounds", "sb"})
    @CommandPermission("realregions.admin")
    @Usage("&c/rr setbounds <region@world>")
    @SuppressWarnings("unused")
    public void setboundscmd(final Player p, @SuggestFrom(RRSuggestion.REGIONS) @Single final String name) {
        Region reg = rra.getRegionManagerAPI().getRegionPlusName(name);
        if (reg == null) {
            TranslatableLine.REGION_NON_EXISTENT_NAME.with(NAME, name).send(p);
            return;
        }

        rra.getRegionManagerAPI().setRegionBounds(reg, p);
    }

    @Subcommand({"entities", "ents"})
    @CommandPermission("realregions.admin")
    @Usage("&c/rr entities <world>")
    @SuppressWarnings("unused")
    public void entitiescmd(final Player p, @SuggestFrom(RRSuggestion.WORLDS) @Single final String name) {
        RWorld rw = rra.getWorldManagerAPI().getWorld(name);
        if (rw == null) {
            TranslatableLine.WORLD_NO_WORLD_NAMED.with(WORLD, name).send(p);
            return;
        }

        EntityViewer ev = new EntityViewer(p, rw, rra);
        ev.openInventory(p);
    }

    @Subcommand({"setgamerule", "sgr"})
    @CommandPermission("realregions.admin")
    @Usage("&c/rr setgamerule <world> <gamerule> <value>")
    @SuppressWarnings("unused")
    public void setgamerulecmd(final CommandSender commandSender, @SuggestFrom(RRSuggestion.WORLDS) @Single final String name, @SuggestFrom(RRSuggestion.GAMERULES) @Single final String gameRule, @Single final String op) {
        RWorld rw = rra.getWorldManagerAPI().getWorld(name);
        if (rw == null) {
            TranslatableLine.WORLD_NO_WORLD_NAMED.with(WORLD, name).send(commandSender);
            return;
        }

        if (rw.setGameRule(gameRule, op)) {
            Text.send(commandSender, TranslatableLine.WORLD_GAMERULE_SET.get() + " " + gameRule + "&r&f: &b" + op);
        } else {
            Text.send(commandSender, "&cInvalid gamerule: " + gameRule);
        }
    }

    @Subcommand({"deletew", "delw"})
    @CommandPermission("realregions.admin")
    @Usage("&c/rr deletew <world>")
    @SuppressWarnings("unused")
    public void deleteworldcmd(final CommandSender commandSender, @SuggestFrom(RRSuggestion.WORLDS_AND_IMPORTS) @Single final String name) {
        RWorld rw = rra.getWorldManagerAPI().getWorld(name);
        if (rw == null) {
            TranslatableLine.WORLD_NO_WORLD_NAMED.with(WORLD, name).send(commandSender);
            return;
        }

        final Runnable delete = () -> rra.getWorldManagerAPI().deleteWorld(commandSender, rw);
        //asked first where the server has dialogs; the console, and servers without, delete straight away
        if (!Confirmations.deleteWorld(commandSender, rw, delete, null)) {
            delete.run();
        }
    }

    @Subcommand({"players", "plrs"})
    @CommandPermission("realregions.admin")
    @Usage("&c/rr players <world>")
    @SuppressWarnings("unused")
    public void playerscmd(final Player p, @SuggestFrom(RRSuggestion.WORLDS) @Single final String name) {
        RWorld rw = rra.getWorldManagerAPI().getWorld(name);
        if (rw == null) {
            TranslatableLine.WORLD_NO_WORLD_NAMED.with(WORLD, name).send(p);
            return;
        }

        EntityViewer ev = new EntityViewer(p, rw, EntityType.PLAYER, rra);
        ev.openInventory(p);
    }
}