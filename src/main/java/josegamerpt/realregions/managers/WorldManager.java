package josegamerpt.realregions.managers;

import josegamerpt.realregions.Debugger;
import josegamerpt.realregions.classes.Data;
import josegamerpt.realregions.classes.RWorld;
import josegamerpt.realregions.classes.Region;
import josegamerpt.realregions.utils.Text;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class WorldManager {

    private static final ArrayList<RWorld> worlds = new ArrayList<>();

    public static ArrayList<RWorld> getWorlds() {
        Debugger.debug(WorldManager.class, worlds.size() + "");
        return worlds;
    }

    public static void loadWorlds() {
        Bukkit.getWorlds().forEach(wrld -> worlds.add(new RWorld(wrld)));
    }

    public static void createRegion(String name, Location min, Location max, RWorld r) {
        Region r1 = new Region(min, max, ChatColor.stripColor(Text.color(name)), name, r, Material.LIGHT_BLUE_STAINED_GLASS, 100);
        r.addRegion(r1);
        r1.saveData(Data.Region.REGION);
    }

    public static ArrayList<Region> getRegions() {
        ArrayList<Region> r = new ArrayList<>();
        worlds.forEach(rWorld -> r.addAll(rWorld.getRegions()));
        return r;
    }

    public static void getRegion(Player p) {
        ArrayList<Region> rgtmp = new ArrayList<>();
        for (Region region : getRegions()) {
            if (region.isLocationInRegion(p.getLocation())) {
                rgtmp.add(region);
            }
        }

        Collections.sort(rgtmp, Comparator.comparing(Region::getPriority));
        Collections.reverse(rgtmp);
    }

    public static Region isLocationInRegion(Location l) {
        ArrayList<Region> rgtmp = new ArrayList<>();
        for (Region region : getRegions()) {
            if (region.isLocationInRegion(l)) {
                rgtmp.add(region);
            }
        }

        Collections.sort(rgtmp, Comparator.comparing(Region::getPriority));
        Collections.reverse(rgtmp);
        return rgtmp.get(0);
    }

    public static RWorld getWorld(World world) {
        for (RWorld rWorld : worlds) {
            if (rWorld.getWorld() == world) {
                return rWorld;
            }
        }
        return null;
    }
}
