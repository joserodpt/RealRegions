package josegamerpt.realregions.managers;

import josegamerpt.realregions.classes.RWorld;
import josegamerpt.realregions.regions.Region;
import org.bukkit.*;

import java.util.*;

public class WorldManager {

    private HashMap<RWorld, ArrayList<Region>> worlds_reg_dic = new HashMap<>();

    public HashMap<RWorld, ArrayList<Region>> getWorldsAndRegions() {
        return worlds_reg_dic;
    }

    private RegionManager rm = new RegionManager(this);
    public RegionManager getRegionManager() {
        return rm;
    }
    public ArrayList<RWorld> getWorlds() {
        return new ArrayList<>(worlds_reg_dic.keySet());
    }

    public void loadWorlds() {
        for (World w : Bukkit.getWorlds()) {
            //load rworld
            RWorld rw = new RWorld(w);

            //load regions
            worlds_reg_dic.put(rw, rm.loadRegions(rw));
        }
    }

    public RWorld getWorld(World w) {
        return worlds_reg_dic.keySet().stream()
                .filter(world -> world.getWorld().equals(w))
                .findFirst()
                .orElse(null);
    }

    public RWorld getWorld(String nome) {
        return worlds_reg_dic.keySet().stream()
                .filter(world -> world.getRWorldName().equalsIgnoreCase(nome))
                .findFirst()
                .orElse(null);
    }
}
