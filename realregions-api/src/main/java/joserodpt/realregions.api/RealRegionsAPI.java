package joserodpt.realregions.api;

import com.google.common.base.Preconditions;
import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realregions.api.managers.RegionManagerAPI;
import joserodpt.realregions.api.managers.WorldManagerAPI;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public abstract class RealRegionsAPI {

    private static RealRegionsAPI instance;
    /**
     * Gets instance of this API
     *
     * @return RealRegionsAPI API instance
     */
    public static RealRegionsAPI getInstance() {
        return instance;
    }

    /**
     * Sets the RealMinesAPI instance.
     * <b>Note! This method may only be called once</b>
     *
     * @param instance the new instance to set
     */
    public static void setInstance(RealRegionsAPI instance) {
        Preconditions.checkNotNull(instance, "instance");
        Preconditions.checkArgument(RealRegionsAPI.instance == null, "Instance already set");
        RealRegionsAPI.instance = instance;
    }
    public abstract Logger getLogger();

    public abstract JavaPlugin getPlugin();

    public abstract WorldManagerAPI getWorldManagerAPI();

    public abstract RegionManagerAPI getRegionManagerAPI();

    public abstract boolean hasNewUpdate();

    public abstract String getVersion();

    public abstract RealPermissionsAPI getRealPermissionsAPI();

    public abstract RealMinesAPI getRealMinesAPI();

    public abstract void setRealMinesAPI(RealMinesAPI instance);
}
