package joserodpt.realregions.api.managers;

import joserodpt.realmines.api.mine.RMine;
import joserodpt.realregions.api.regions.RWorld;
import joserodpt.realregions.api.regions.Region;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class RegionManagerAPI {
    public abstract List<Region> getViewing();

    public abstract List<Region> getRegions();

    public abstract void deleteRegion(CommandSender p, Region a);

    public abstract void deleteRegion(Region a);

    public abstract Region getRegionPlusName(String name);

    public abstract Region getFirstPriorityRegionContainingLocation(Location l);

    public abstract void createCubeRegion(String name, Location min, Location max, RWorld r);

    public abstract void createCubeRegionRealMines(RMine mine, RWorld rw);

    public abstract void startVisualizer();

    public abstract void setRegionBounds(Region reg, Player p);

    public abstract void checkRealMinesRegions(Map<String, RMine> mines);

    public abstract void toggleRegionView(CommandSender commandSender, Region reg);

    public abstract Map<UUID, Region> getLastRegions();
}
