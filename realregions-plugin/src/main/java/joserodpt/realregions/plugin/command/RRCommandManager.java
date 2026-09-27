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

import joserodpt.realregions.api.RWorld;
import joserodpt.realregions.api.RealRegionsAPI;
import joserodpt.realregions.api.config.TranslatableLine;
import joserodpt.realregions.api.regions.Region;
import com.mojang.brigadier.arguments.ArgumentType;
import joserodpt.realutils.command.LampExceptionHandler;
import org.bukkit.GameRule;
import revxrsal.commands.Lamp;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.BukkitLampConfig;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.brigadier.MinecraftArgumentType;
import revxrsal.commands.node.ParameterNode;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builds the Lamp instance every RealRegions command hangs off, and hands it the shared tab
 * completions and the language-file error messages. Lamp registers the commands straight onto the
 * server's command map, which is why none of them appear in plugin.yml.
 */
public final class RRCommandManager {

    private final Lamp<BukkitCommandActor> lamp;

    public RRCommandManager(final RealRegionsAPI rra) {
        final Map<RRSuggestion, SuggestionProvider<BukkitCommandActor>> suggestions = suggestions(rra);

        //Brigadier stays on. Lamp's own matcher treats leftover input as merely a worse match, so
        //`/rr reload junk` would quietly fall back to the bare `/rr` handler; Brigadier's tree
        //refuses it outright. Where it can't attach Lamp falls back on its own and
        //the exception handler's @Usage messages are what players see instead.
        final BukkitLampConfig<BukkitCommandActor> config = BukkitLampConfig.<BukkitCommandActor>builder(rra.getPlugin())
                .argumentTypes(types -> types.addTypeFactory(RRCommandManager::nameArgument))
                .build();

        this.lamp = BukkitLamp.builder(config)
                .exceptionHandler(new LampExceptionHandler(
                        TranslatableLine.SYSTEM_ERROR_COMMAND::send,
                        TranslatableLine.SYSTEM_ERROR_PERMISSION::send,
                        TranslatableLine.SYSTEM_PLAYER_ONLY::send,
                        TranslatableLine.SYSTEM_ERROR_USAGE::get))
                .suggestionProviders(providers -> providers.addProviderForAnnotation(
                        SuggestFrom.class, annotation -> suggestions.get(annotation.value())))
                .build();

        this.lamp.register(new RealRegionsCMD(rra));
    }

    /**
     * Brigadier's word type only takes {@code [A-Za-z0-9_.+-]} unquoted, so {@code /rr tpr test@world}
     * stops at the {@code @} with "expected whitespace to end one argument". GameProfileArgument reads
     * up to the next space whatever is in it, and only resolves a profile when asked to, which the
     * handlers never do: Lamp re-reads the raw input itself. It is kept to the {@link SuggestFrom}
     * arguments, whose suggestions replace the player names the client would otherwise offer.
     */
    private static ArgumentType<?> nameArgument(final ParameterNode<BukkitCommandActor, ?> parameter) {
        if (parameter.type() != String.class || parameter.isGreedy() || !parameter.annotations().contains(SuggestFrom.class)) {
            return null;
        }
        //empty where the server's GameProfileArgument can't be found, leaving Lamp's plain string
        return MinecraftArgumentType.GAME_PROFILE.<Object>getIfPresent().orElse(null);
    }

    private static Map<RRSuggestion, SuggestionProvider<BukkitCommandActor>> suggestions(final RealRegionsAPI rra) {
        final Map<RRSuggestion, SuggestionProvider<BukkitCommandActor>> sources = new EnumMap<>(RRSuggestion.class);

        sources.put(RRSuggestion.REGIONS, context -> rra.getRegionManagerAPI().getRegions().stream()
                .map(Region::getRegionNamePlusWorld)
                .collect(Collectors.toList()));

        sources.put(RRSuggestion.WORLDS, context -> rra.getWorldManagerAPI().getWorldList().stream()
                .map(RWorld::getRWorldName)
                .collect(Collectors.toList()));

        sources.put(RRSuggestion.WORLDS_AND_IMPORTS, context -> rra.getWorldManagerAPI().getWorldsAndPossibleImports().stream()
                .map(RWorld::getRWorldName)
                .collect(Collectors.toList()));

        //Lamp would offer every constant, UNKNOWN_TO_BE_IMPORTED included
        sources.put(RRSuggestion.WORLD_TYPES, SuggestionProvider.of("NORMAL", "NETHER", "THE_END", "VOID", "FLAT"));

        sources.put(RRSuggestion.BOOLEANS, SuggestionProvider.of("true", "false"));

        sources.put(RRSuggestion.FLAGS, SuggestionProvider.of(
                "block_break",
                "block_place",
                "block_interact",
                "container_interact",
                "pvp",
                "pve",
                "hunger",
                "take_damage",
                "explosions",
                "item_pickup",
                "item_drop",
                "entity_spawning",
                "enter",
                "access_crafting",
                "access_chests",
                "access_hoppers",
                "no_chat",
                "no_consumables",
                "disabled_nether_portal",
                "disabled_end_portal",
                "no_fire_spreading",
                "leaf_decay",
                "item_pickup_only_owner",
                "block_commands"));

        sources.put(RRSuggestion.GAMERULES, context -> Arrays.stream(GameRule.values())
                .map(GameRule::getName)
                .collect(Collectors.toList()));

        return sources;
    }

    public Lamp<BukkitCommandActor> getLamp() {
        return this.lamp;
    }
}
