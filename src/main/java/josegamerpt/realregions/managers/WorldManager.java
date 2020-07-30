package josegamerpt.realregions.managers;

import josegamerpt.realregions.classes.RWorld;
import josegamerpt.realregions.classes.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class WorldManager {

    private static ArrayList<RWorld> worlds = new ArrayList<>();

    public static ArrayList<RWorld> getWorlds()
    {
        return worlds;
    }

    public static void loadWorlds() {
        for (World w : Bukkit.getWorlds())
        {
            RWorld a = new RWorld(w);
            worlds.add(a);
        }
    }

    public static ArrayList<Region> getRegions() {
        ArrayList<Region> r = new ArrayList<Region>();
        worlds.forEach(rWorld -> r.addAll(rWorld.getRegions()));
        return r;
    }

    public static void unload() {
        worlds.clear();
    }

    public static void getRegion(Player p) {
        ArrayList<Region> rgtmp = new ArrayList<>();
        for (Region region : getRegions()) {
            if (region.isLocationInRegion(p.getLocation()))
            {
                rgtmp.add(region);
            }
        }

        Collections.sort(rgtmp, Comparator.comparing(Region::getPriority));
        Collections.reverse(rgtmp);
    }

    public static Region isLocationInRegion(Location l) {
        ArrayList<Region> rgtmp = new ArrayList<>();
        for (Region region : getRegions()) {
            if (region.isLocationInRegion(l))
            {
                rgtmp.add(region);
            }
        }

        Collections.sort(rgtmp, Comparator.comparing(Region::getPriority));
        Collections.reverse(rgtmp);
        return rgtmp.get(0);
    }
}
