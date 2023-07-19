package josegamerpt.realregions.regions;

import josegamerpt.realregions.RealRegions;
import josegamerpt.realregions.classes.Cube;
import josegamerpt.realregions.classes.RWorld;
import josegamerpt.realregions.utils.CubeVisualizer;
import josegamerpt.realregions.utils.Itens;
import josegamerpt.realregions.utils.Text;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CuboidRegion extends Region {

    private final Cube cube;
    private final CubeVisualizer cv;

    public CuboidRegion(Location pos1, Location pos2, String name, String displayname, RWorld w, Material m, int pri) {
        super(name, displayname, w, m, pri, RegionType.CUBOID);

        //Cube Region
        this.cube = new Cube(pos1, pos2);
        this.cv = new CubeVisualizer(this);

        //TODO: make one loop only
        //region tick
        new BukkitRunnable() {
            @Override
            public void run() {
                if (canVisualize()) {
                    CubeVisualizer v = getCubeVisualizer();
                    v.getCube().forEach(v::spawnParticle);
                }
            }
        }.runTaskTimer(RealRegions.getInstance(),0, 10);
    }

    @Override
    public boolean canVisualize() {
        return true;
    }

    @Override
    public ItemStack getItem() {
        List<String> desc = new ArrayList<>();
        desc.add("&fPriority: &b" + this.priority);
        desc.add("&fBlocks: &b" + this.cube.getVolume() + " &7(&b " + this.cube.getSizeX() + " &fx &b" + this.cube.getSizeY() + " &fx &b" + this.cube.getSizeZ() + " &7)");

        super.flagsList(desc, Text.styleBoolean(this.accesschests), Text.styleBoolean(this.accesscrafting), Text.styleBoolean(this.accesshoppers), Text.styleBoolean(this.blockbreak), Text.styleBoolean(this.blockinteract), Text.styleBoolean(this.blockplace), Text.styleBoolean(this.containerinteract), Text.styleBoolean(this.entityspawning), Text.styleBoolean(this.enter), Text.styleBoolean(this.explosions), Text.styleBoolean(this.hunger), Text.styleBoolean(this.itemdrop), Text.styleBoolean(this.itempickup), Text.styleBoolean(this.pve), Text.styleBoolean(this.pvp), Text.styleBoolean(this.takedamage));

        return Itens.createItem(super.getIcon(), 1, "&f" + super.getDisplayName() + " &7[&b" + this.getType().name() + "&7]", desc);
    }

    @Override
    public void saveData(Region.RegionData dr) {
        FileConfiguration cfg = super.getRWorld().getConfig();
        switch (dr) {
            case SETTINGS:
                cfg.set("Regions." + this.getRegionName() + ".Type", this.getType().name());
                cfg.set("Regions." + this.getRegionName() + ".POS.1", Text.locToTex(getCube().getPOS(1)));
                cfg.set("Regions." + this.getRegionName() + ".POS.2", Text.locToTex(getCube().getPOS(2)));
                cfg.set("Regions." + this.getRegionName() + ".Display-Name", this.getDisplayName());
                cfg.set("Regions." + this.getRegionName() + ".Priority", this.priority);
                cfg.set("Regions." + this.getRegionName() + ".Icon", this.getIcon().name());
            case ICON:
            case FLAGS:
                super.saveData(dr);
                break;
            case ALL:
                this.saveData(RegionData.ICON);
                this.saveData(RegionData.FLAGS);
                this.saveData(RegionData.SETTINGS);
                break;
        }
    }

    @Override
    public void toggleVisual(Player p) {
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
        p.teleport(this.cube.getCenter());
        if (!silent) {
            Text.send(p, "&fYou teleported to region &b" + super.getDisplayName() + "&r &fon &a" + super.getRWorld().getRWorldName());
        }
    }

    public Cube getCube() {
        return this.cube;
    }

    public CubeVisualizer getCubeVisualizer() {
        return this.cv;
    }

    @Override
    public boolean isLocationInRegion(Location l) {
        return this.cube.contains(l);
    }
}
