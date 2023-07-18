package josegamerpt.realregions.managers;

import josegamerpt.realregions.classes.RWorld;
import josegamerpt.realregions.regions.CuboidRRegion;
import josegamerpt.realregions.regions.RRegion;
import josegamerpt.realregions.utils.Text;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.ArrayList;

public class RegionManager {

    private final WorldManager wm;

    public RegionManager(WorldManager wm) {
        this.wm = wm;
    }

    public void saveRegions(RWorld w) {
        wm.getRegions(w).forEach(rRegion -> rRegion.saveData(RRegion.RegionData.ALL));
    }

    public ArrayList<RRegion> loadRegions(RWorld w) {
        ArrayList<RRegion> loaded_regions = new ArrayList<>();
        for (String r : w.getConfig().getConfigurationSection("Regions").getKeys(false)) {
            RRegion.RegionType rt = RRegion.RegionType.valueOf(w.getConfig().getString("Regions." + r + ".Type"));
            String n = w.getConfig().getString("Regions." + r + ".Display-Name");
            RRegion reg = null;

            switch (rt)
            {
                case INFINITE:
                    reg = new RRegion(r, n, w, Material.valueOf(w.getConfig().getString("Regions." + r + ".Icon")), w.getConfig().getInt("Regions." + r + ".Priority"), RRegion.RegionType.INFINITE);
                    break;
                case CUBOID:
                    reg = new CuboidRRegion(Text.textToLoc(w.getConfig().getString("Regions." + r + ".POS.1"), w.getWorld()),
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

    public void deleteRegion(RRegion a) {
        wm.getRegions(a.getRWorld()).remove(a);
        a.getRWorld().getConfig().set("Regions." + a.getRegionName(), null);
        a.getRWorld().saveConfig();
    }
}
