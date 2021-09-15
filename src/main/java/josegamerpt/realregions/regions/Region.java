package josegamerpt.realregions.regions;

import josegamerpt.realregions.classes.Cube;
import josegamerpt.realregions.classes.RWorld;
import josegamerpt.realregions.utils.CubeVisualizer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface Region {

    enum Type {CUBOID}

    enum Data {REGION, ICON, SETTINGS, FLAGS}

    Boolean getView();

    Boolean hasEntitySpawning();

    Boolean hasExplosions();

    Region.Type getType();

    ItemStack getItem();

    void teleport(Player p);

    String getDisplayName();

    void setIcon(Material a);

    void saveData(Region.Data dr);

    RWorld getWorld();

    void toggleVisual(Player p);

    Cube getMaster();

    CubeVisualizer getViewingMaster();

    boolean canVisualize();

    String getName();

    boolean isGlobal();

    void setDisplayName(String s);

    void setPriority(Integer a);

    int getPriority();

    boolean isLocationInRegion(Location l);

    boolean hasBlockBreak();

    boolean hasBlockPlace();

    boolean hasHunger();

    boolean hasItemDrop();

    boolean hasItemPickup();

    boolean hasTakeDamage();

    boolean hasEnter();

    boolean hasBlockInteract();

    boolean hasContainerInteract();

    boolean hasAccessCrafting();

    boolean hasAccessHoppers();

    boolean hasAccessChests();

    boolean hasPVP();

    boolean hasPVE();

    void setBlockBreak(boolean b);

    void setBlockPlace(boolean b);

    void setHunger(boolean b);

    void setItemDrop(boolean b);

    void setItemPickup(boolean b);

    void setTakeDamage(boolean b);

    void setEnter(boolean b);

    void setBlockInteract(boolean b);

    void setContainerInteract(boolean b);

    void setAccessCrafting(boolean b);

    void setAccessHoppers(boolean b);

    void setAccessChests(boolean b);

    void setPVP(boolean b);

    void setPVE(boolean b);

    void setEntitySpawning(boolean b);

    void setExplosions(boolean b);

}
