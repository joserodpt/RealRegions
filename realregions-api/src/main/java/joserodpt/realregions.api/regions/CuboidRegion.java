package joserodpt.realregions.api.regions;

/*
 *  ______           _______
 *  | ___ \         | | ___ \         (_)
 *  | |_/ /___  __ _| | |_/ /___  __ _ _  ___  _ __  ___
 *  |    // _ \/ _` | |    // _ \/ _` | |/ _ \| '_ \/ __|
 *  | |\ \  __/ (_| | | |\ \  __/ (_| | | (_) | | | \__ \
 *  \_| \_\___|\__,_|_\_| \_\___|\__, |_|\___/|_| |_|___/
 *                                __/ |
 *                               |___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2020-2024
 * @link https://github.com/joserodpt/RealRegions
 */

import joserodpt.realregions.api.RWorld;
import joserodpt.realregions.api.config.TranslatableLine;
import joserodpt.realregions.api.utils.Cube;
import joserodpt.realregions.api.utils.CubeVisualizer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class CuboidRegion extends Region {

    private Cube cube;
    private final CubeVisualizer cv;

    public CuboidRegion(String name, RWorld w, Material m, Location pos1, Location pos2) {
        super(name, w, m, RegionType.CUBOID);

        //Cube Region
        this.cube = new Cube(pos1, pos2);
        this.cv = new CubeVisualizer(this);
    }

    @Override
    public boolean canVisualize() {
        return true;
    }

    @Override
    public RegionType getType() {
        return RegionType.CUBOID;
    }

    @Override
    public void teleport(Player p, boolean silent) {
        if (!this.getRWorld().isLoaded()) {
            TranslatableLine.REGION_TP_UNLOADED_WORLD.send(p);
            return;
        }

        p.teleport(this.cube.getCenter());
        if (!silent) {
            TranslatableLine.REGION_TP.setV1(TranslatableLine.ReplacableVar.NAME.eq(super.getDisplayName())).setV2(TranslatableLine.ReplacableVar.WORLD.eq(super.getRWorld().getRWorldName())).send(p);
        }
    }

    public Cube getCube() {
        return this.cube;
    }

    public void setCube(Cube c) {
        this.cube = c;
    }

    public CubeVisualizer getCubeVisualizer() {
        return this.cv;
    }

    @Override
    public boolean isLocationInRegion(Location l) {
        return this.cube.contains(l);
    }
}
