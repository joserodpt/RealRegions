package josegamerpt.realregions.managers;

import josegamerpt.realregions.classes.RWorld;
import josegamerpt.realregions.regions.CuboidRegion;
import josegamerpt.realregions.regions.Region;
import josegamerpt.realregions.utils.Text;
import org.bukkit.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class WorldManager {

    private ArrayList<RWorld> worlds = new ArrayList<>();

    public ArrayList<RWorld> getWorlds() {
        return this.worlds;
    }

    public void loadWorlds() {Bukkit.getWorlds().forEach(wrld -> this.worlds.add(new RWorld(wrld)));}

    public void createCubeRegion(String name, Location min, Location max, RWorld r) {
        Region r1 = new CuboidRegion(min, max, ChatColor.stripColor(Text.color(name)), name, r, Material.LIGHT_BLUE_STAINED_GLASS, 100);
        r.addRegion(r1);
        r1.saveData(Region.Data.REGION);
    }

    public ArrayList<Region> getRegions() {
        ArrayList<Region> r = new ArrayList<>();
        this.worlds.forEach(rWorld -> r.addAll(rWorld.getRegions()));
        return r;
    }

    public Region isLocationInRegion(Location l) {
        ArrayList<Region> rgtmp = new ArrayList<>();
        for (Region region : getRegions()) {
            if (region.isLocationInRegion(l)) {
                rgtmp.add(region);
            }
        }

        rgtmp.sort(Comparator.comparing(Region::getPriority));
        Collections.reverse(rgtmp);
        return rgtmp.get(0);
    }

    public RWorld getWorld(World world) {
        for (RWorld rWorld : this.worlds) {
            if (rWorld.getWorld() == world) {
                return rWorld;
            }
        }
        return null;
    }
}
