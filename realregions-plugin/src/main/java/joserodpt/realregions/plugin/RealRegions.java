package joserodpt.realregions.plugin;

import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realregions.api.RealRegionsAPI;
import joserodpt.realregions.api.managers.RegionManagerAPI;
import joserodpt.realregions.api.managers.WorldManagerAPI;
import joserodpt.realregions.plugin.managers.RegionManager;
import joserodpt.realregions.plugin.managers.WorldManager;

import java.util.logging.Logger;

public class RealRegions extends RealRegionsAPI {

    private final Logger logger;
    private final RealRegionsPlugin plugin;
    private final WorldManagerAPI worldManagerAPI;
    private final RegionManagerAPI regionManagerAPI;
    private RealMinesAPI realMinesAPI;
    private RealPermissionsAPI realPermissionsAPI;

    public RealRegions(RealRegionsPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();

        this.worldManagerAPI = new WorldManager(this);
        this.regionManagerAPI = new RegionManager(this);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public RealRegionsPlugin getPlugin() {
        return plugin;
    }

    @Override
    public WorldManagerAPI getWorldManagerAPI() {
        return worldManagerAPI;
    }

    @Override
    public RegionManagerAPI getRegionManagerAPI() {
        return regionManagerAPI;
    }

    @Override
    public boolean hasNewUpdate() {
        return plugin.hasNewUpdate();
    }

    @Override
    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    @Override
    public RealPermissionsAPI getRealPermissionsAPI() {
        return this.realPermissionsAPI;
    }

    @Override
    public RealMinesAPI getRealMinesAPI() {
        return this.realMinesAPI;
    }

    @Override
    public void setRealMinesAPI(RealMinesAPI instance) {
        this.realMinesAPI = instance;
    }

    public void setRealPermissionsAPI(RealPermissionsAPI instance) {
        this.realPermissionsAPI = instance;
    }
}
