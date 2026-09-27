package joserodpt.realregions.api.utils;

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

import joserodpt.realregions.api.config.RRConfig;
import org.bukkit.Location;
import org.bukkit.World;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * RealRegions' own formatting, for what RealUtils' Text has no use for elsewhere.
 */
public final class Format {

    private Format() {
    }

    public static String convertUnixTimeToDate(final long unixTime) {
        final Date date = new Date(unixTime * 1000L); // Convert seconds to milliseconds
        final SimpleDateFormat sdf = new SimpleDateFormat(Objects.requireNonNull(RRConfig.file().getString("RealRegions.Date-Format"))); // Format the date as needed
        return sdf.format(date);
    }

    public static String styleBoolean(final boolean a) {
        return a ? "&a✔ enabled" : "&c❌ disabled";
    }

    public static String cords(final Location l) {
        return "X: " + l.getBlockX() + " Y: " + l.getBlockY() + " Z: "
                + l.getBlockZ();
    }

    public static String locToTex(final Location pos) {
        return pos.getBlockX() + "%" + pos.getBlockY() + "%" + pos.getBlockZ();
    }

    public static Location textToLoc(final String string, final World w) {
        final String[] s = string.split("%");
        return new Location(w, Double.parseDouble(s[0]), Double.parseDouble(s[1]), Double.parseDouble(s[2]));
    }
}
