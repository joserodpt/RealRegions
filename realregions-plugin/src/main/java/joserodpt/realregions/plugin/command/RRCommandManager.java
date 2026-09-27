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
import joserodpt.realutils.command.LampExceptionHandler;
import org.bukkit.GameRule;
import revxrsal.commands.Lamp;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

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
        this.lamp = BukkitLamp.builder(rra.getPlugin())
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
                "item_pickup_only_owner"));

        sources.put(RRSuggestion.GAMERULES, context -> Arrays.stream(GameRule.values())
                .map(GameRule::getName)
                .collect(Collectors.toList()));

        return sources;
    }

    public Lamp<BukkitCommandActor> getLamp() {
        return this.lamp;
    }
}
