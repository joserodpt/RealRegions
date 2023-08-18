package joserodpt.realregions.utils;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import joserodpt.realregions.config.Config;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Text {
	public static String convertUnixTimeToDate(long unixTime) {
		Date date = new Date(unixTime * 1000L); // Convert seconds to milliseconds
		SimpleDateFormat sdf = new SimpleDateFormat(Objects.requireNonNull(Config.file().getString("RealRegions.Date-Format"))); // Format the date as needed
		return sdf.format(date);
	}
	public static String styleBoolean(boolean a) {
		return a ? "&a✔ enabled" : "&c❌ disabled";
	}

	public static String color(final String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}

	public static void sendList(CommandSender cs, List<String> list) {
		list.forEach(s -> cs.sendMessage(Text.color(s)));
	}

	public static List<String> color(List<String> list) {
		return list.stream()
				.map(s -> Text.color("&f" + s))
				.collect(Collectors.toCollection(ArrayList::new));
	}

	public static String cords(Location l) {
		return "X: " + l.getBlockX() + " Y: " + l.getBlockY() + " Z: "
				+ l.getBlockZ();
	}

	public static void send(Player p, String string) {
		p.sendMessage(Text.color(Config.file().getString("RealRegions.Prefix") + " &r" + string));
	}
	public static void send(CommandSender p, String string) {
		p.sendMessage(Text.color(Config.file().getString("RealRegions.Prefix") + " &r" + string));
	}

    public static String locToTex(Location pos) {
		return pos.getBlockX() + "%" + pos.getBlockY() + "%" + pos.getBlockZ();
    }

	public static Location textToLoc(String string, World w) {
		String[] s = string.split("%");
		return new Location(w, Double.parseDouble(s[0]), Double.parseDouble(s[1]), Double.parseDouble(s[2]));
	}
}