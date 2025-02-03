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
 * @author José Rodrigues © 2020-2025
 * @link https://github.com/joserodpt/RealRegions
 */

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.Default;
import dev.triumphteam.cmd.core.annotation.Optional;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import dev.triumphteam.cmd.core.annotation.Suggestion;
import joserodpt.realregions.api.RealRegionsAPI;
import joserodpt.realregions.api.config.RRConfig;
import joserodpt.realregions.api.config.RRLanguage;
import joserodpt.realregions.api.config.TranslatableLine;
import joserodpt.realregions.api.RWorld;
import joserodpt.realregions.api.regions.Region;
import joserodpt.realregions.api.utils.Text;
import joserodpt.realregions.plugin.gui.EntityViewer;
import joserodpt.realregions.plugin.gui.RegionSettingsGUI;
import joserodpt.realregions.plugin.gui.RegionsListGUI;
import joserodpt.realregions.plugin.gui.WorldsListGUI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

@Command(value = "realregions", alias = "rr")
public class RealRegionsCMD extends BaseCommand {

    private final String onlyPlayers = "[RealRegions] Only players can run this command.";

    RealRegionsAPI rra;

    public RealRegionsCMD(RealRegionsAPI r) {
        this.rra = r;
    }

    @Default
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

    @SubCommand(value = "reload", alias = "rl")
    @Permission("realregions.admin")
    @SuppressWarnings("unused")
    public void reloadcmd(final CommandSender commandSender) {
        RRConfig.reload();
        RRLanguage.reload();

        //reload worlds config
        rra.getWorldManagerAPI().getWorlds().values().forEach(RWorld::reloadConfig);
        TranslatableLine.SYSTEM_RELOADED.send(commandSender);
    }

    @SubCommand(value = "worlds", alias = "menu")
    @Permission("realregions.admin")
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

    @SubCommand(value = "create", alias = "c")
    @Permission("realregions.admin")
    @SuppressWarnings("unused")
    public void create(final CommandSender commandSender, final String name) {
        if (!(commandSender instanceof Player)) {
            Text.send(commandSender, onlyPlayers);
            return;
        }

        Player p = (Player) commandSender;
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
                Text.send(commandSender, "&cError while getting player's worldedit selection. See console for details.");
                Bukkit.getLogger().severe("Error while getting player's worldedit selection:");
                e.printStackTrace();
            }
        } else {
            TranslatableLine.REGION_NAME_DUPLICATE.send(p);
        }
    }

    @SubCommand(value = "createworld", alias = "cw")
    @Permission("realregions.admin")
    @SuppressWarnings("unused")
    public void createworldcmd(final CommandSender commandSender, final String name, @Suggestion("#worldtype") final RWorld.WorldType worldtype) {
        if (name == null) {
            TranslatableLine.WORLD_NAME_EMPTY.send(commandSender);
            return;
        }

        if (worldtype == null) {
            TranslatableLine.WORLD_INVALID_TYPE.setV1(TranslatableLine.ReplacableVar.INPUT.eq("like that")).send(commandSender);
            return;
        }

        try {
            RWorld rw = rra.getWorldManagerAPI().createWorld(commandSender, name, worldtype);
            if (rw != null && commandSender instanceof Player) {
                rw.teleport((Player) commandSender, true);
            }
        } catch (Exception e) {
            TranslatableLine.WORLD_INVALID_TYPE.setV1(TranslatableLine.ReplacableVar.INPUT.eq(worldtype.name())).send(commandSender);
            e.printStackTrace();
        }
    }

    @SubCommand(value = "createtimedworld", alias = "ctw")
    @Permission("realregions.admin")
    @SuppressWarnings("unused")
    public void createtimedworldcmd(final CommandSender commandSender, final String name, @Suggestion("#worldtype") final RWorld.WorldType worldtype, final Integer time) {
        if (name == null) {
            TranslatableLine.WORLD_NAME_EMPTY.send(commandSender);
            return;
        }

        if (time == null || time <= 5) {
            Text.send(commandSender, "&cTime must be greater than 5");
            return;
        }

        if (worldtype == null) {
            TranslatableLine.WORLD_INVALID_TYPE.setV1(TranslatableLine.ReplacableVar.INPUT.eq("like that")).send(commandSender);
            return;
        }

        try {
            RWorld rw = rra.getWorldManagerAPI().createTimedWorld(commandSender, name, worldtype, time);
            if (rw != null && commandSender instanceof Player) {
                rw.teleport((Player) commandSender, true);
            }
        } catch (Exception e) {
            TranslatableLine.WORLD_INVALID_TYPE.setV1(TranslatableLine.ReplacableVar.INPUT.eq(worldtype.name())).send(commandSender);
            e.printStackTrace();
        }
    }

    @SubCommand("reset")
    @Permission("realregions.admin")
    public void resetworld(final CommandSender commandSender, @Suggestion("#mundos") final String name) {
        RWorld rw = rra.getWorldManagerAPI().getWorld(name);
        if (rw == null) {
            TranslatableLine.WORLD_NO_WORLD_NAMED.setV1(TranslatableLine.ReplacableVar.WORLD.eq(name)).send(commandSender);
            return;
        }

        rra.getWorldManagerAPI().resetWorld(rw);
        Text.send(commandSender, "&aWorld reseted.");
    }

    @SubCommand(value = "flags", alias = "region")
    @Permission("realregions.admin")
    @SuppressWarnings("unused")
    public void regioncmd(final CommandSender commandSender, @Suggestion("#regions") final String name) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            Region reg = rra.getRegionManagerAPI().getRegionPlusName(name);
            if (reg == null) {
                TranslatableLine.REGION_NON_EXISTENT_NAME.setV1(TranslatableLine.ReplacableVar.NAME.eq(name)).send(commandSender);
                return;
            }

            RegionSettingsGUI wv = new RegionSettingsGUI(p, reg, rra);
            wv.openInventory(p);
        } else {
            Text.send(commandSender, onlyPlayers);

        }
    }

    @SubCommand(value = "flag", alias = "f")
    @Permission("realregions.admin")
    @SuppressWarnings("unused")
    public void regioncmd(final CommandSender commandSender, @Suggestion("#regions") final String regionName, @Suggestion("#flags") final String flag, @Suggestion("#bool") @Optional String valueSTR) {
        Region reg = rra.getRegionManagerAPI().getRegionPlusName(regionName);
        if (reg == null) {
            TranslatableLine.REGION_NON_EXISTENT_NAME.setV1(TranslatableLine.ReplacableVar.NAME.eq(regionName)).send(commandSender);
            return;
        }

        if (valueSTR == null || valueSTR.isEmpty()) {
            switch (flag) {
                case "block_break":
                    TranslatableLine.REGION_FLAG_SET.setV1(TranslatableLine.ReplacableVar.NAME.eq(flag)).setV2(TranslatableLine.ReplacableVar.INPUT.eq(reg.blockBreak ? "&a✔ true" : "&c❌ false")).send(commandSender);
                    break;
                case "block_place":
                    TranslatableLine.REGION_FLAG_SET.setV1(TranslatableLine.ReplacableVar.NAME.eq(flag)).setV2(TranslatableLine.ReplacableVar.INPUT.eq(reg.blockPlace ? "&a✔ true" : "&c❌ false")).send(commandSender);
                    break;
                case "block_interact":
                    TranslatableLine.REGION_FLAG_SET.setV1(TranslatableLine.ReplacableVar.NAME.eq(flag)).setV2(TranslatableLine.ReplacableVar.INPUT.eq(reg.blockInteract ? "&a✔ true" : "&c❌ false")).send(commandSender);
                    break;
                case "container_interact":
                    TranslatableLine.REGION_FLAG_SET.setV1(TranslatableLine.ReplacableVar.NAME.eq(flag)).setV2(TranslatableLine.ReplacableVar.INPUT.eq(reg.containerInteract ? "&a✔ true" : "&c❌ false")).send(commandSender);
                    break;
                case "pvp":
                    TranslatableLine.REGION_FLAG_SET.setV1(TranslatableLine.ReplacableVar.NAME.eq(flag)).setV2(TranslatableLine.ReplacableVar.INPUT.eq(reg.pvp ? "&a✔ true" : "&c❌ false")).send(commandSender);
                    break;
                case "pve":
                    TranslatableLine.REGION_FLAG_SET.setV1(TranslatableLine.ReplacableVar.NAME.eq(flag)).setV2(TranslatableLine.ReplacableVar.INPUT.eq(reg.pve ? "&a✔ true" : "&c❌ false")).send(commandSender);
                    break;
                case "hunger":
                    TranslatableLine.REGION_FLAG_SET.setV1(TranslatableLine.ReplacableVar.NAME.eq(flag)).setV2(TranslatableLine.ReplacableVar.INPUT.eq(reg.hunger ? "&a✔ true" : "&c❌ false")).send(commandSender);
                    break;
                case "take_damage":
                    TranslatableLine.REGION_FLAG_SET.setV1(TranslatableLine.ReplacableVar.NAME.eq(flag)).setV2(TranslatableLine.ReplacableVar.INPUT.eq(reg.takeDamage ? "&a✔ true" : "&c❌ false")).send(commandSender);
                    break;
                case "explosions":
                    TranslatableLine.REGION_FLAG_SET.setV1(TranslatableLine.ReplacableVar.NAME.eq(flag)).setV2(TranslatableLine.ReplacableVar.INPUT.eq(reg.explosions ? "&a✔ true" : "&c❌ false")).send(commandSender);
                    break;
                case "item_pickup":
                    TranslatableLine.REGION_FLAG_SET.setV1(TranslatableLine.ReplacableVar.NAME.eq(flag)).setV2(TranslatableLine.ReplacableVar.INPUT.eq(reg.itemPickup ? "&a✔ true" : "&c❌ false")).send(commandSender);
                    break;
                case "item_drop":
                    TranslatableLine.REGION_FLAG_SET.setV1(TranslatableLine.ReplacableVar.NAME.eq(flag)).setV2(TranslatableLine.ReplacableVar.INPUT.eq(reg.itemDrop ? "&a✔ true" : "&c❌ false")).send(commandSender);
                    break;
                case "entity_spawning":
                    TranslatableLine.REGION_FLAG_SET.setV1(TranslatableLine.ReplacableVar.NAME.eq(flag)).setV2(TranslatableLine.ReplacableVar.INPUT.eq(reg.entitySpawning ? "&a✔ true" : "&c❌ false")).send(commandSender);
                    break;
                case "enter":
                    TranslatableLine.REGION_FLAG_SET.setV1(TranslatableLine.ReplacableVar.NAME.eq(flag)).setV2(TranslatableLine.ReplacableVar.INPUT.eq(reg.enter ? "&a✔ true" : "&c❌ false")).send(commandSender);
                    break;
                case "access_crafting":
                    TranslatableLine.REGION_FLAG_SET.setV1(TranslatableLine.ReplacableVar.NAME.eq(flag)).setV2(TranslatableLine.ReplacableVar.INPUT.eq(reg.accessCrafting ? "&a✔ true" : "&c❌ false")).send(commandSender);
                    break;
                case "access_chests":
                    TranslatableLine.REGION_FLAG_SET.setV1(TranslatableLine.ReplacableVar.NAME.eq(flag)).setV2(TranslatableLine.ReplacableVar.INPUT.eq(reg.accessChests ? "&a✔ true" : "&c❌ false")).send(commandSender);
                    break;
                case "access_hoppers":
                    TranslatableLine.REGION_FLAG_SET.setV1(TranslatableLine.ReplacableVar.NAME.eq(flag)).setV2(TranslatableLine.ReplacableVar.INPUT.eq(reg.accessHoppers ? "&a✔ true" : "&c❌ false")).send(commandSender);
                    break;
                case "no_chat":
                    TranslatableLine.REGION_FLAG_SET.setV1(TranslatableLine.ReplacableVar.NAME.eq(flag)).setV2(TranslatableLine.ReplacableVar.INPUT.eq(reg.noChat ? "&a✔ true" : "&c❌ false")).send(commandSender);
                    break;
                case "no_consumables":
                    TranslatableLine.REGION_FLAG_SET.setV1(TranslatableLine.ReplacableVar.NAME.eq(flag)).setV2(TranslatableLine.ReplacableVar.INPUT.eq(reg.noConsumables ? "&a✔ true" : "&c❌ false")).send(commandSender);
                    break;
                case "disabled_nether_portal":
                    TranslatableLine.REGION_FLAG_SET.setV1(TranslatableLine.ReplacableVar.NAME.eq(flag)).setV2(TranslatableLine.ReplacableVar.INPUT.eq(reg.disabledNetherPortal ? "&a✔ true" : "&c❌ false")).send(commandSender);
                    break;
                case "disabled_end_portal":
                    TranslatableLine.REGION_FLAG_SET.setV1(TranslatableLine.ReplacableVar.NAME.eq(flag)).setV2(TranslatableLine.ReplacableVar.INPUT.eq(reg.disabledEndPortal ? "&a✔ true" : "&c❌ false")).send(commandSender);
                    break;
                case "no_fire_spreading":
                    TranslatableLine.REGION_FLAG_SET.setV1(TranslatableLine.ReplacableVar.NAME.eq(flag)).setV2(TranslatableLine.ReplacableVar.INPUT.eq(reg.noFireSpreading ? "&a✔ true" : "&c❌ false")).send(commandSender);
                    break;
                case "item_pickup_only_owner":
                    TranslatableLine.REGION_FLAG_SET.setV1(TranslatableLine.ReplacableVar.NAME.eq(flag)).setV2(TranslatableLine.ReplacableVar.INPUT.eq(reg.itemPickupOnlyOwner ? "&a✔ true" : "&c❌ false")).send(commandSender);
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
            default:
                notFound = true;
                TranslatableLine.REGION_FLAG_UNKNOWN.send(commandSender);
                break;
        }

        if (!notFound) {
            TranslatableLine.REGION_FLAG_SET.setV1(TranslatableLine.ReplacableVar.NAME.eq(flag)).setV2(TranslatableLine.ReplacableVar.INPUT.eq(value ? "&a✔ true" : "&c❌ false")).send(commandSender);
        }
    }

    @SubCommand(value = "regions", alias = {"world", "r"})
    @Permission("realregions.admin")
    public void regionscmd(final CommandSender commandSender, @Suggestion("#mundos") final String name) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            RWorld rw = rra.getWorldManagerAPI().getWorld(name);
            if (rw == null) {
                TranslatableLine.WORLD_NO_WORLD_NAMED.setV1(TranslatableLine.ReplacableVar.WORLD.eq(name)).send(commandSender);
                return;
            }

            RegionsListGUI wv = new RegionsListGUI(p, rw, rra);
            wv.openInventory(p);
        } else {
            Text.send(commandSender, onlyPlayers);
        }
    }

    @SubCommand(value = "setworldspawn", alias = {"sws", "setspawn"})
    @Permission("realregions.admin")
    @SuppressWarnings("unused")
    public void setworldspawn(final CommandSender commandSender, @Suggestion("#mundos") @Optional String name) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            RWorld rw;

            if (name == null || name.isEmpty()) {
                rw = rra.getWorldManagerAPI().getWorld(p.getWorld());
                name = p.getWorld().getName();
            } else {
                rw = rra.getWorldManagerAPI().getWorld(name);
            }

            if (rw == null) {
                TranslatableLine.WORLD_NO_WORLD_NAMED.setV1(TranslatableLine.ReplacableVar.WORLD.eq(name)).send(commandSender);
                return;
            }

            rw.setWorldSpawn(p.getLocation());
            TranslatableLine.WORLD_SPAWN_SET.setV1(TranslatableLine.ReplacableVar.WORLD.eq(name)).send(commandSender);
        } else {
            Text.send(commandSender, onlyPlayers);
        }
    }

    @SubCommand("tp")
    @SuppressWarnings("unused")
    public void tpcmd(final CommandSender commandSender, @Suggestion("#mundos") final String name) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            RWorld rw = rra.getWorldManagerAPI().getWorld(name);
            if (rw == null) {
                TranslatableLine.WORLD_NO_WORLD_NAMED.setV1(TranslatableLine.ReplacableVar.WORLD.eq(name)).send(commandSender);
                return;
            }

            if (p.hasPermission("realregions.admin") || p.isOp() || p.hasPermission("realregions.tpworld." + rw.getRWorldName())) {
                rw.teleport(p, false);
            } else {
                Text.send(commandSender, "&cYou don't have permission to teleport to this world.");
            }
        } else {
            Text.send(commandSender, onlyPlayers);
        }
    }

    @SubCommand("tpo")
    @Permission("realregions.admin")
    @SuppressWarnings("unused")
    public void topcmd(final CommandSender commandSender, @Suggestion("#mundos") final String name, final Player player) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            if (player == null) {
                Text.send(commandSender, "&cPlayer not found.");
                return;
            }

            RWorld rw = rra.getWorldManagerAPI().getWorld(name);
            if (rw == null) {
                TranslatableLine.WORLD_NO_WORLD_NAMED.setV1(TranslatableLine.ReplacableVar.WORLD.eq(name)).send(commandSender);
                return;
            }

            rw.teleport(player, false);
            Text.send(commandSender, "&aTeleported " + player.getName() + " to " + name);
        } else {
            Text.send(commandSender, onlyPlayers);
        }
    }

    @SubCommand("tpr")
    @Permission("realregions.admin")
    @SuppressWarnings("unused")
    public void tprcmd(final CommandSender commandSender, @Suggestion("#regions") final String name) {
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
    @Permission("realregions.admin")
    @SuppressWarnings("unused")
    public void viewcmd(final CommandSender commandSender, @Suggestion("#regions") final String name) {
        Region reg = rra.getRegionManagerAPI().getRegionPlusName(name);
        if (reg == null) {
            TranslatableLine.REGION_NON_EXISTENT_NAME.setV1(TranslatableLine.ReplacableVar.NAME.eq(name)).send(commandSender);
            return;
        }

        rra.getRegionManagerAPI().toggleRegionView(commandSender, reg);
    }

    @SubCommand("unload")
    @Permission("realregions.admin")
    @SuppressWarnings("unused")
    public void unloadcmd(final CommandSender commandSender, @Suggestion("#mundos") final String name) {
        RWorld rw = rra.getWorldManagerAPI().getWorld(name);
        if (rw == null) {
            TranslatableLine.WORLD_NO_WORLD_NAMED.setV1(TranslatableLine.ReplacableVar.WORLD.eq(name)).send(commandSender);
            return;
        }

        rra.getWorldManagerAPI().unloadWorld(commandSender, rw);
    }

    @SubCommand("toggle-tpjoin")
    @Permission("realregions.admin")
    @SuppressWarnings("unused")
    public void toggletpjoin(final CommandSender commandSender, @Suggestion("#mundos") final String name) {
        RWorld rw = rra.getWorldManagerAPI().getWorld(name);
        if (rw == null) {
            TranslatableLine.WORLD_NO_WORLD_NAMED.setV1(TranslatableLine.ReplacableVar.WORLD.eq(name)).send(commandSender);
            return;
        }

        rw.setTPJoin(!rw.isTPJoinON());
        TranslatableLine.WORLD_TPJOIN_SET.setV1(TranslatableLine.ReplacableVar.INPUT.eq(rw.isTPJoinON() ? "&a✔ true" : "&c❌ false")).send(commandSender);
    }

    @SubCommand(value = "toggle-enter-title", alias = "toggle-title")
    @Permission("realregions.admin")
    @SuppressWarnings("unused")
    public void toggleentertitle(final CommandSender commandSender, @Suggestion("#regions") final String name) {
        Region rg = rra.getRegionManagerAPI().getRegionPlusName(name);
        if (rg == null) {
            TranslatableLine.REGION_NON_EXISTENT_NAME.setV1(TranslatableLine.ReplacableVar.NAME.eq(name)).send(commandSender);
            return;
        }

        rg.announceEnterTitle = !rg.announceEnterTitle;
        rg.saveData(Region.RegionData.SETTINGS);
        TranslatableLine.REGION_ENTERING_TOGGLE.setV1(TranslatableLine.ReplacableVar.INPUT.eq(rg.announceEnterTitle ? "&a✔ true" : "&c❌ false")).send(commandSender);
    }

    @SubCommand(value = "toggle-enter-actionbar", alias = "toggle-actionbar")
    @Permission("realregions.admin")
    @SuppressWarnings("unused")
    public void toggleenteractionbar(final CommandSender commandSender, @Suggestion("#regions") final String name) {
        Region rg = rra.getRegionManagerAPI().getRegionPlusName(name);
        if (rg == null) {
            TranslatableLine.REGION_NON_EXISTENT_NAME.setV1(TranslatableLine.ReplacableVar.NAME.eq(name)).send(commandSender);
            return;
        }

        rg.announceEnterActionbar = !rg.announceEnterActionbar;
        rg.saveData(Region.RegionData.SETTINGS);
        TranslatableLine.REGION_ENTERING_TOGGLE.setV1(TranslatableLine.ReplacableVar.INPUT.eq(rg.announceEnterActionbar ? "&a✔ true" : "&c❌ false")).send(commandSender);
    }

    @SubCommand(value = "toggle-inventories", alias = "toggle-invs")
    @Permission("realregions.admin")
    @SuppressWarnings("unused")
    public void toggleinventoriescmd(final CommandSender commandSender, @Suggestion("#mundos") final String name) {
        RWorld rw = rra.getWorldManagerAPI().getWorld(name);
        if (rw == null) {
            TranslatableLine.WORLD_NO_WORLD_NAMED.setV1(TranslatableLine.ReplacableVar.WORLD.eq(name)).send(commandSender);
            return;
        }

        rw.setWorldInventories(!rw.hasWorldInventories());
        TranslatableLine.WORLD_INVENTORIES_SET.setV1(TranslatableLine.ReplacableVar.WORLD.eq(rw.hasWorldInventories() ? "&a✔ true" : "&c❌ false")).send(commandSender);
    }

    @SubCommand("load")
    @Permission("realregions.admin")
    @SuppressWarnings("unused")
    public void loadcmd(final CommandSender commandSender, @Suggestion("#mundos") final String name) {
        RWorld rw = rra.getWorldManagerAPI().getWorld(name);
        if (rw == null) {
            TranslatableLine.WORLD_NO_WORLD_NAMED.setV1(TranslatableLine.ReplacableVar.WORLD.eq(name)).send(commandSender);
            return;
        }

        rra.getWorldManagerAPI().loadWorld(commandSender, name);
    }

    @SubCommand("unregister")
    @Permission("realregions.admin")
    @SuppressWarnings("unused")
    public void unregistercmd(final CommandSender commandSender, @Suggestion("#mundos") final String name) {
        RWorld rw = rra.getWorldManagerAPI().getWorld(name);
        if (rw == null) {
            TranslatableLine.WORLD_NO_WORLD_NAMED.setV1(TranslatableLine.ReplacableVar.WORLD.eq(name)).send(commandSender);
            return;
        }

        rra.getWorldManagerAPI().unregisterWorld(commandSender, rw);
    }

    @SubCommand("import")
    @Permission("realregions.admin")
    @SuppressWarnings("unused")
    public void importcmd(final CommandSender commandSender, final String name, @Suggestion("#worldtype") final RWorld.WorldType worldtype) {
        if (name == null) {
            TranslatableLine.WORLD_NAME_EMPTY.send(commandSender);
            return;
        }

        if (worldtype == null) {
            TranslatableLine.WORLD_INVALID_TYPE.setV1(TranslatableLine.ReplacableVar.INPUT.eq("like that")).send(commandSender);
            return;
        }

        try {
            rra.getWorldManagerAPI().importWorld(commandSender, name, worldtype);
        } catch (Exception e) {
            TranslatableLine.WORLD_INVALID_TYPE.setV1(TranslatableLine.ReplacableVar.INPUT.eq(worldtype.name())).send(commandSender);
        }
    }

    @SubCommand(value = "delete", alias = "del")
    @Permission("realregions.admin")
    @SuppressWarnings("unused")
    public void delregcmd(final CommandSender commandSender, @Suggestion("#regions") final String name) {
        Region reg = rra.getRegionManagerAPI().getRegionPlusName(name);
        if (reg == null) {
            TranslatableLine.REGION_NON_EXISTENT_NAME.setV1(TranslatableLine.ReplacableVar.NAME.eq(name)).send(commandSender);
            return;
        }

        rra.getRegionManagerAPI().deleteRegion(commandSender, reg);
    }

    @SubCommand(value = "rename", alias = "rn")
    @Permission("realregions.admin")
    @SuppressWarnings("unused")
    public void renamecmd(final CommandSender commandSender, @Suggestion("#regions") final String name, final String newname) {
        Region reg = rra.getRegionManagerAPI().getRegionPlusName(name);
        if (reg == null) {
            TranslatableLine.REGION_NON_EXISTENT_NAME.setV1(TranslatableLine.ReplacableVar.NAME.eq(name)).send(commandSender);
            return;
        }

        reg.setDisplayName(newname);
        reg.saveData(Region.RegionData.SETTINGS);
        TranslatableLine.REGION_RENAMED.setV1(TranslatableLine.ReplacableVar.NAME.eq(newname)).send(commandSender);
    }

    @SubCommand(value = "setbounds", alias = "sb")
    @Permission("realregions.admin")
    @SuppressWarnings("unused")
    public void setboundscmd(final CommandSender commandSender, @Suggestion("#regions") final String name) {
        if (commandSender instanceof Player) {
            Region reg = rra.getRegionManagerAPI().getRegionPlusName(name);
            if (reg == null) {
                TranslatableLine.REGION_NON_EXISTENT_NAME.setV1(TranslatableLine.ReplacableVar.NAME.eq(name)).send(commandSender);
                return;
            }

            rra.getRegionManagerAPI().setRegionBounds(reg, (Player) commandSender);
        } else {
            Text.send(commandSender, onlyPlayers);
        }
    }

    @SubCommand(value = "entities", alias = "ents")
    @Permission("realregions.admin")
    @SuppressWarnings("unused")
    public void entitiescmd(final CommandSender commandSender, @Suggestion("#mundos") final String name) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            RWorld rw = rra.getWorldManagerAPI().getWorld(name);
            if (rw == null) {
                TranslatableLine.WORLD_NO_WORLD_NAMED.setV1(TranslatableLine.ReplacableVar.WORLD.eq(name)).send(commandSender);
                return;
            }

            EntityViewer ev = new EntityViewer(p, rw, rra);
            ev.openInventory(p);
        } else {
            Text.send(commandSender, onlyPlayers);

        }
    }

    @SubCommand(value = "setgamerule", alias = "sgr")
    @Permission("realregions.admin")
    @SuppressWarnings("unused")
    public void setgamerulecmd(final CommandSender commandSender, @Suggestion("#mundos") final String name, @Suggestion("#gamerules") final String gameRule, final String op) {
        RWorld rw = rra.getWorldManagerAPI().getWorld(name);
        if (rw == null) {
            TranslatableLine.WORLD_NO_WORLD_NAMED.setV1(TranslatableLine.ReplacableVar.WORLD.eq(name)).send(commandSender);
            return;
        }

        if (rw.setGameRule(gameRule, op)) {
            Text.send(commandSender, TranslatableLine.WORLD_GAMERULE_SET.get() + " " + gameRule + "&r&f: &b" + op);
        } else {
            Text.send(commandSender, "&cInvalid gamerule: " + gameRule);
        }
    }

    @SubCommand(value = "deletew", alias = "delw")
    @Permission("realregions.admin")
    @SuppressWarnings("unused")
    public void deleteworldcmd(final CommandSender commandSender, @Suggestion("#mundosPLUSimport") final String name) {
        RWorld rw = rra.getWorldManagerAPI().getWorld(name);
        if (rw == null) {
            TranslatableLine.WORLD_NO_WORLD_NAMED.setV1(TranslatableLine.ReplacableVar.WORLD.eq(name)).send(commandSender);
            return;
        }

        rra.getWorldManagerAPI().deleteWorld(commandSender, rw);
    }

    @SubCommand(value = "players", alias = "plrs")
    @Permission("realregions.admin")
    @SuppressWarnings("unused")
    public void playerscmd(final CommandSender commandSender, @Suggestion("#mundos") final String name) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            RWorld rw = rra.getWorldManagerAPI().getWorld(name);
            if (rw == null) {
                TranslatableLine.WORLD_NO_WORLD_NAMED.setV1(TranslatableLine.ReplacableVar.WORLD.eq(name)).send(commandSender);
                return;
            }

            EntityViewer ev = new EntityViewer(p, rw, EntityType.PLAYER, rra);
            ev.openInventory(p);
        } else {
            Text.send(commandSender, onlyPlayers);
        }
    }
}