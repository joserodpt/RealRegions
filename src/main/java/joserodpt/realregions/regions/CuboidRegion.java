package joserodpt.realregions.regions;

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
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealRegions
 */

import joserodpt.realregions.utils.Cube;
import joserodpt.realregions.utils.CubeVisualizer;
import joserodpt.realregions.utils.Itens;
import joserodpt.realregions.utils.Text;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
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

    public CuboidRegion(Location pos1, Location pos2, String name, String displayname, RWorld w, Material m, int pri) {
        super(name, displayname, w, m, pri, RegionType.CUBOID);

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

        if (Objects.requireNonNull(this.getOrigin()) == RegionOrigin.REALMINES) {
            desc.addAll(flagsList(Text.styleBoolean(this.accesschests), Text.styleBoolean(this.accesscrafting), Text.styleBoolean(this.accesshoppers), Text.styleBoolean(this.blockbreak), Text.styleBoolean(this.blockinteract), Text.styleBoolean(this.blockplace), Text.styleBoolean(this.containerinteract), Text.styleBoolean(this.entityspawning), Text.styleBoolean(this.enter), Text.styleBoolean(this.explosions), Text.styleBoolean(this.hunger), Text.styleBoolean(this.itemdrop), Text.styleBoolean(this.itempickup), Text.styleBoolean(this.pve), Text.styleBoolean(this.pvp), Text.styleBoolean(this.takedamage)));
            desc.remove(desc.size() - 1);
            desc.add("&7This region was imported from " + this.getOrigin().getDisplayName() + "&r&7. Delete it there.");
        } else {
            desc.addAll(flagsList(Text.styleBoolean(this.accesschests), Text.styleBoolean(this.accesscrafting), Text.styleBoolean(this.accesshoppers), Text.styleBoolean(this.blockbreak), Text.styleBoolean(this.blockinteract), Text.styleBoolean(this.blockplace), Text.styleBoolean(this.containerinteract), Text.styleBoolean(this.entityspawning), Text.styleBoolean(this.enter), Text.styleBoolean(this.explosions), Text.styleBoolean(this.hunger), Text.styleBoolean(this.itemdrop), Text.styleBoolean(this.itempickup), Text.styleBoolean(this.pve), Text.styleBoolean(this.pvp), Text.styleBoolean(this.takedamage)));
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
    public void toggleVisual(CommandSender p) {
        super.setBeingVisualized(!super.isBeingVisualized());
        Text.send(p, "&fVisualizing " + super.getDisplayName() + ": " + Text.styleBoolean(super.isBeingVisualized()));
    }

    @Override
    public RegionType getType()
    {
        return RegionType.CUBOID;
    }

    @Override
    public void teleport(Player p, boolean silent) {
        if (!this.getRWorld().isLoaded()) {
            Text.send(p, "&cYou can't teleport to this region because it belongs to a world that is unloaded.");
            return;
        }

        p.teleport(this.cube.getCenter());
        if (!silent) {
            Text.send(p, "&fYou teleported to region &b" + super.getDisplayName() + "&r &fon &a" + super.getRWorld().getRWorldName());
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
