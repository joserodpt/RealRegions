package josegamerpt.realregions.managers;

import josegamerpt.realregions.classes.RWorld;
import josegamerpt.realregions.regions.CuboidRRegion;
import josegamerpt.realregions.regions.RRegion;
import josegamerpt.realregions.utils.Text;
import org.bukkit.*;

import java.util.*;
import java.util.stream.Collectors;

public class WorldManager {

    private RegionManager rm = new RegionManager(this);

    private HashMap<RWorld, ArrayList<RRegion>> regions = new HashMap<>();

    public RegionManager getRegionManager() {
        return rm;
    }

    public ArrayList<RWorld> getWorlds() {
        return new ArrayList<>(regions.keySet());
    }

    public void loadWorlds() {
        for (World w : Bukkit.getWorlds()) {
            //load rworld
            RWorld rw = new RWorld(w);

            //load regions
            regions.put(rw, rm.loadRegions(rw));
        }
    }

    public ArrayList<RRegion> getAllRegions() {
        return regions.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<RRegion> getRegions(RWorld w) {
        return regions.get(w);
    }



    public void createCubeRegion(String name, Location min, Location max, RWorld r) {
        CuboidRRegion crg = new CuboidRRegion(min, max, ChatColor.stripColor(Text.color(name)), name, r, Material.LIGHT_BLUE_STAINED_GLASS, 100);
        regions.get(r).add(crg);

        //save region
        crg.saveData(RRegion.RegionData.ALL);
    }

    public RWorld getWorld(World w) {
        return regions.keySet().stream()
                .filter(world -> world.getWorld().equals(w))
                .findFirst()
                .orElse(null);
    }

    public RWorld getWorld(String nome) {
        return regions.keySet().stream()
                .filter(world -> world.getRWorldName().equalsIgnoreCase(nome))
                .findFirst()
                .orElse(null);
    }


    public RRegion getRegion(RWorld w, String name) {
        return regions.containsKey(w) ? regions.get(w).stream()
                .filter(region -> region.getRegionName().equals(name))
                .findFirst()
                .orElse(null) : null; // World not found in the HashMap
    }

    public boolean hasRegion(RWorld w, String name) {
        return regions.getOrDefault(w, new ArrayList<>())
                .stream()
                .anyMatch(region -> region.getRegionName().equals(name));
    }
}
