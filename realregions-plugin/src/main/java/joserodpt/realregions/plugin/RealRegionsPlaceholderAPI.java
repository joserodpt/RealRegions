package joserodpt.realregions.plugin;

import joserodpt.realregions.api.RealRegionsAPI;
import joserodpt.realregions.api.regions.Region;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class RealRegionsPlaceholderAPI extends PlaceholderExpansion {

    private final RealRegionsAPI rra;

    public RealRegionsPlaceholderAPI(RealRegionsAPI rra) {
        this.rra = rra;
    }

    @Override
    @NotNull
    public String getAuthor() {
        return rra.getPlugin().getDescription().getAuthors().toString();
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return rra.getPlugin().getDescription().getName();
    }

    @Override
    @NotNull
    public String getVersion() {
        return rra.getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equalsIgnoreCase("region_name")) {
            return rra.getRegionManagerAPI().getFirstPriorityRegionContainingLocation(player.getPlayer().getLocation()).getRegionName();
        }

        if (params.equalsIgnoreCase("region_displayname")) {
            return rra.getRegionManagerAPI().getFirstPriorityRegionContainingLocation(player.getPlayer().getLocation()).getDisplayName();
        }

        if (params.equalsIgnoreCase("total_regions")) {
            return rra.getRegionManagerAPI().getRegions().size() + "";
        }

        if (params.equalsIgnoreCase("total_created_regions")) {
            return rra.getRegionManagerAPI().getRegions().stream().filter(region -> region.getType() == Region.RegionType.CUBOID).count() + "";
        }

        if (params.equalsIgnoreCase("total_global_regions")) {
            return rra.getRegionManagerAPI().getRegions().stream().filter(region -> region.getType() == Region.RegionType.INFINITE).count() + "";
        }

        return null; // 
    }
}