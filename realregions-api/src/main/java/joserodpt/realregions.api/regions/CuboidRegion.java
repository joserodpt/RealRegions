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
import joserodpt.realregions.api.utils.Itens;
import joserodpt.realregions.api.utils.Text;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CuboidRegion extends Region {

    private Cube cube;
    private final CubeVisualizer cv;

    public CuboidRegion(Location pos1, Location pos2, String name, String displayname, RWorld w, Material m, int pri, boolean announceEnterTitle, boolean announceEnterActionbar) {
        super(name, displayname, w, m, pri, RegionType.CUBOID, announceEnterTitle, announceEnterActionbar);

        //Cube Region
        this.cube = new Cube(pos1, pos2);
        this.cv = new CubeVisualizer(this);
    }

    @Override
    public boolean canVisualize() {
        return true;
    }

    @Override
    public ItemStack getItem() {
        List<String> desc = new ArrayList<>(Arrays.asList("&fPriority: &b" + this.priority, "&fBlocks: &b" + this.cube.getVolume() + " &7(&b " + this.cube.getSizeX() + " &fx &b" + this.cube.getSizeY() + " &fx &b" + this.cube.getSizeZ() + " &7)"));

        desc.addAll(flagsList(Text.styleBoolean(this.accessChests),
                Text.styleBoolean(this.accessCrafting),
                Text.styleBoolean(this.accessHoppers),
                Text.styleBoolean(this.blockBreak),
                Text.styleBoolean(this.blockInteract),
                Text.styleBoolean(this.blockPlace),
                Text.styleBoolean(this.containerInteract),
                Text.styleBoolean(this.entitySpawning),
                Text.styleBoolean(this.enter),
                Text.styleBoolean(this.explosions),
                Text.styleBoolean(this.hunger),
                Text.styleBoolean(this.itemDrop),
                Text.styleBoolean(this.itemPickup),
                Text.styleBoolean(this.pve),
                Text.styleBoolean(this.pvp),
                Text.styleBoolean(this.takeDamage),
                Text.styleBoolean(this.noChat),
                Text.styleBoolean(this.noConsumables),
                Text.styleBoolean(this.noFireSpreading),
                Text.styleBoolean(this.disabledNetherPortal),
                Text.styleBoolean(this.disabledEndPortal),
                Text.styleBoolean(this.leafDecay)));

        if (Objects.requireNonNull(this.getOrigin()) == RegionOrigin.REALMINES) {
            desc.remove(desc.size() - 1);
            desc.add("&7This region was imported from " + this.getOrigin().getDisplayName() + "&r&7. Delete it there.");
        }

        return Itens.createItem(super.getIcon(), 1, "&f" + super.getDisplayName() + " &7[&b" + this.getType().name() + "&7]", desc);
    }

    @Override
    public void saveData(Region.RegionData dr) {
        FileConfiguration cfg = super.getRWorld().getConfig();
        if (Objects.requireNonNull(dr) == RegionData.BOUNDS) {
            cfg.set("Regions." + this.getRegionName() + ".POS.1", Text.locToTex(getCube().getPOS1()));
            cfg.set("Regions." + this.getRegionName() + ".POS.2", Text.locToTex(getCube().getPOS2()));
        } else {
            saveData(RegionData.BOUNDS);
            super.saveData(dr);
        }

        super.getRWorld().saveConfig();
    }

    @Override
    public RegionType getType()
    {
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
