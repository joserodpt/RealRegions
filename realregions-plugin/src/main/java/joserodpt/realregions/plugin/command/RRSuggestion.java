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

/**
 * The tab completion sources RealRegions commands share. Each one is wired to a provider in
 * {@link RRCommandManager}, and pulled onto a parameter with {@link SuggestFrom}.
 */
public enum RRSuggestion {
    /** Every region, as region@world, which is how the commands look them up. */
    REGIONS,
    /** Every registered world. */
    WORLDS,
    /** Registered worlds plus the folders that could be imported as one. */
    WORLDS_AND_IMPORTS,
    /** The world types that can be created or imported, leaving out the internal ones. */
    WORLD_TYPES,
    BOOLEANS,
    /** Region flag names, as {@code /rr flag} takes them. */
    FLAGS,
    GAMERULES
}
