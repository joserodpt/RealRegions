package josegamerpt.realregions.config;

import java.io.File;
import java.io.IOException;

import josegamerpt.realregions.RealRegions;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class Config implements Listener {

	private static File file;
	private static FileConfiguration customFile;
	private static String name = "config.yml";

	public static void setup(Plugin p) {
		file = new File(p.getDataFolder(), name);

		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException ignored) {
				RealRegions.getPlugin().getLogger().severe("Error crating config.yml file!");
			}
		}
		customFile = YamlConfiguration.loadConfiguration(file);
	}

	public static FileConfiguration file() {
		return customFile;
	}

	public static void save() {
		try {
			customFile.save(file);
		} catch (IOException e) {
			RealRegions.getPlugin().getLogger().severe("Couldn't save " + name + "!");
		}
	}

	public static void reload() {
		customFile = YamlConfiguration.loadConfiguration(file);
	}
}