package josegamerpt.realregions.classes;

import josegamerpt.realregions.utils.Itens;
import josegamerpt.realregions.utils.Text;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class EntityIcon {

    private Entity e;

    public EntityIcon(Entity e) {
        this.e = e;
    }

    public Entity getEntity() {
        return e;
    }

    public String getEntityName() {
        return e.getName();
    }

    public ItemStack getIcon() {
        String mat = this.e.getType().name().toUpperCase() + "_SPAWN_EGG";
        try {
            Material m = Material.valueOf(mat);
            if (this.e.getCustomName() == null)
            {
                return Itens.createItem(m, 1, "&f" + getEntityName(), Arrays.asList("&fLocation: " + Text.cords(this.e.getLocation()), "&7Click to teleport!"));
            } else {
                return Itens.createItem(m, 1, "&f" + this.e.getCustomName() + " &7[&r&f" + getEntityName() + "&7]", Arrays.asList("&fLocation: " + Text.cords(this.e.getLocation()), "&7Click to teleport!"));
            }
        } catch (Exception e) {
            if (this.e.getCustomName() == null) {
                return Itens.createItem(Material.PAINTING, 1, "&f" + getEntityName(), Arrays.asList("&8Icon could not be found.", "&fLocation: " + Text.cords(this.e.getLocation()), "&7Click to teleport!"));
            } else {
                return Itens.createItem(Material.PAINTING, 1, "&f" + this.e.getCustomName() + " &7[&r&f" + getEntityName() + "&7]", Arrays.asList("&8Icon could not be found.", "&fLocation: " + Text.cords(this.e.getLocation()), "&7Click to teleport!"));
            }
        }
    }
}
