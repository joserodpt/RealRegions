package josegamerpt.realregions.managers;

import josegamerpt.realregions.classes.RWorld;
import josegamerpt.realregions.regions.Region;
import josegamerpt.realregions.utils.Text;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.*;

public class WorldManager {

    private HashMap<RWorld, ArrayList<Region>> worlds_reg_dic = new HashMap<>();

    public HashMap<RWorld, ArrayList<Region>> getWorldsAndRegions() {
        return worlds_reg_dic;
    }

    private final RegionManager rm = new RegionManager(this);
    public RegionManager getRegionManager() {
        return rm;
    }
    public ArrayList<RWorld> getWorlds() {
        return new ArrayList<>(worlds_reg_dic.keySet());
    }

    public void loadWorlds() {

        //TODO: carregar os mundos pela sua configuração na pasta, não pelo bukkit

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

    public void unloadWorld(Player p, RWorld r) {
        if (r.getRWorldName().equalsIgnoreCase("world") || r.getRWorldName().startsWith("world_"))
        {
            Text.send(p, "&fYou can't &cunload &fdefault worlds.");
        } else {
            Bukkit.unloadWorld(r.getWorld(), true);
            Text.send(p, "&fWorld &aunloaded.");
        }
    }

    public void createWorld(Player p, String input) {
        WorldCreator worldCreator = new WorldCreator(input);
        worldCreator.environment(World.Environment.NORMAL); // You can also use NETHER or THE_END
        //worldCreator.generator(new EmptyChunkGenerator()); // We'll create this generator later

        World world = worldCreator.createWorld();
        if (world != null) {
            p.sendMessage("World created! " + world.getName());

            //registar mundo no real regions
        } else {
            p.sendMessage("Failed to create " + input);
        }
    }
}
