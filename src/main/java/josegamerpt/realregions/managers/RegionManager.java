package josegamerpt.realregions.managers;

import josegamerpt.realregions.RealRegions;
import josegamerpt.realregions.regions.RWorld;
import josegamerpt.realregions.regions.CuboidRegion;
import josegamerpt.realregions.regions.Region;
import josegamerpt.realregions.utils.CubeVisualizer;
import josegamerpt.realregions.utils.Text;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

public class RegionManager {

    private final WorldManager wm;
    private ArrayList<Region> view = new ArrayList<>();

    public ArrayList<Region> getView() {
        return view;
    }

    public RegionManager(WorldManager wm) {
        this.wm = wm;
    }

    public void saveRegions(RWorld w) {
        getRegions(w).forEach(rRegion -> rRegion.saveData(Region.RegionData.ALL));
    }

    public ArrayList<Region> loadRegions(RWorld w) {
        ArrayList<Region> loaded_regions = new ArrayList<>();
        for (String r : w.getConfig().getConfigurationSection("Regions").getKeys(false)) {
            Region.RegionType rt = Region.RegionType.valueOf(w.getConfig().getString("Regions." + r + ".Type"));
            String n = w.getConfig().getString("Regions." + r + ".Display-Name");
            Region reg = null;

            switch (rt)
            {
                case INFINITE:
                    reg = new Region(r, n, w, Material.valueOf(w.getConfig().getString("Regions." + r + ".Icon")), w.getConfig().getInt("Regions." + r + ".Priority"), Region.RegionType.INFINITE);
                    break;
                case CUBOID:
                    reg = new CuboidRegion(Text.textToLoc(w.getConfig().getString("Regions." + r + ".POS.1"), w.getWorld()),
                            Text.textToLoc(w.getConfig().getString("Regions." + r + ".POS.2"),  w.getWorld()),
                            ChatColor.stripColor(r), w.getConfig().getString("Regions." + r + ".Display-Name"), w,
                            Material.valueOf(w.getConfig().getString("Regions." + r + ".Icon")), w.getConfig().getInt("Regions." + r + ".Priority"));
                    break;
            }

            if (reg != null) {
                reg.blockinteract = w.getConfig().getBoolean("Regions." + r + ".Block.Interact");
                reg.containerinteract = w.getConfig().getBoolean("Regions." + r + ".Container.Interact");
                reg.blockbreak = w.getConfig().getBoolean("Regions." + r + ".Block.Break");
                reg.blockplace = w.getConfig().getBoolean("Regions." + r + ".Block.Place");
                reg.pvp = w.getConfig().getBoolean("Regions." + r + ".PVP");
                reg.pve = w.getConfig().getBoolean("Regions." + r + ".PVE");
                reg.hunger = w.getConfig().getBoolean("Regions." + r + ".Hunger");
                reg.takedamage = w.getConfig().getBoolean("Regions." + r + ".Damage");
                reg.explosions = w.getConfig().getBoolean("Regions." + r + ".Explosions");
                reg.itemdrop = w.getConfig().getBoolean("Regions." + r + ".Item.Drop");
                reg.itempickup = w.getConfig().getBoolean("Regions." + r + ".Item.Pickup");
                reg.entityspawning = w.getConfig().getBoolean("Regions." + r + ".Entity-Spawning");
                reg.enter = w.getConfig().getBoolean("Regions." + r + ".Enter");
                reg.accesscrafting = w.getConfig().getBoolean("Regions." + r + ".Access.Crafting-Table");
                reg.accesschests = w.getConfig().getBoolean("Regions." + r + ".Access.Chests");
                reg.accesshoppers = w.getConfig().getBoolean("Regions." + r + ".Access.Hoppers");
                loaded_regions.add(reg);
            }
        }

        return loaded_regions;
    }

    public void deleteRegion(CommandSender p, Region a) {
        if (a.getType() == Region.RegionType.INFINITE)
        {
            Text.send(p, "&fYou can't &cdelete " + a.getDisplayName() + " &fbecause its infinite.");
            return;
        }

        wm.getWorldsAndRegions().get(a.getRWorld()).remove(a);
        a.getRWorld().getConfig().set("Regions." + a.getRegionName(), null);
        a.getRWorld().saveConfig();

        Text.send(p, "&fRegion " + a.getDisplayName() + " &ahas been deleted. ");
    }

    public Region getRegionPlusName(String name) {
        try {
            String[] split = name.split("@");
            String world = split[1];
            String reg = split[0];

            RWorld w = wm.getWorld(world);
            if (w != null) {
                return wm.getWorldsAndRegions().containsKey(w) ? wm.getWorldsAndRegions().get(w).stream()
                        .filter(region -> region.getRegionName().equals(reg))
                        .findFirst()
                        .orElse(null) : null;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public boolean hasRegion(RWorld w, String name) {
        return wm.getWorldsAndRegions().getOrDefault(w, new ArrayList<>())
                .stream()
                .anyMatch(region -> region.getRegionName().equals(name));
    }

    public ArrayList<Region> getAllRegions() {
        return wm.getWorldsAndRegions().values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public Region getFirstPriorityRegionContainingLocation(Location l) {
        return wm.getWorldsAndRegions().values().stream()
                .flatMap(Collection::stream)
                .sorted(Comparator.comparingInt(Region::getPriority).reversed())
                .filter(region -> region.isLocationInRegion(l))
                .findFirst()
                .orElse(null);

    }

    public ArrayList<Region> getRegions(RWorld w) {
        return wm.getWorldsAndRegions().get(w);
    }

    public void createCubeRegion(String name, Location min, Location max, RWorld r) {
        CuboidRegion crg = new CuboidRegion(min, max, ChatColor.stripColor(Text.color(name)), name, r, Material.LIGHT_BLUE_STAINED_GLASS, 100);
        wm.getWorldsAndRegions().get(r).add(crg);

        //save region
        crg.saveData(Region.RegionData.ALL);
    }

    public void startVisualizer() {
        //visualizer loop
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Region region : view) {
                    if (region.canVisualize() && region.getRWorld().isLoaded()) {
                        CubeVisualizer v = ((CuboidRegion) region).getCubeVisualizer();
                        v.getCube().forEach(v::spawnParticle);
                    }
                }
            }
        }.runTaskTimer(RealRegions.getPlugin(),0, 10);
    }


}
