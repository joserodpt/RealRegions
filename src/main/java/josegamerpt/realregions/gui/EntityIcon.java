package josegamerpt.realregions.gui;

import josegamerpt.realregions.utils.DistanceCalculator;
import josegamerpt.realregions.utils.Itens;
import josegamerpt.realregions.utils.Text;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class EntityIcon {

    private final Entity e;
    private final Double distanceRelativeToPlayer;

    public EntityIcon(Player p, Entity e) {
        this.e = e;

        this.distanceRelativeToPlayer = DistanceCalculator.calculateDistance(p, e);
    }

    public Double getDistanceRelativeToPlayer() {
        return distanceRelativeToPlayer;
    }

    public Entity getEntity() {
        return e;
    }

    public String getEntityName() {
        return e.getName();
    }

    public ItemStack getIcon() {
        if (this.e.getType() == EntityType.PLAYER) {
            return Itens.createItem(Material.PLAYER_HEAD, 1, "&f" + getEntityName(), Arrays.asList("&fLocation: &b" + Text.cords(this.getEntity().getLocation()), this.distanceRelativeToPlayer == -1 ? "" : "&fDistance relative to you: &b" + this.distanceRelativeToPlayer + "u","&7Click to teleport!"));
        }

        String mat = this.getEntity().getType().name().toUpperCase() + "_SPAWN_EGG";
        try {
            Material m = Material.valueOf(mat);
            if (this.e.getCustomName() == null)
            {
                return Itens.createItem(m, 1, "&f" + getEntityName(), Arrays.asList("&fLocation: &b" + Text.cords(this.getEntity().getLocation()),this.distanceRelativeToPlayer == -1 ? "" : "&fDistance relative to you: &b" + this.distanceRelativeToPlayer + "u", "&7Click to teleport!"));
            } else {
                return Itens.createItem(m, 1, "&f" + this.getEntity().getCustomName() + " &7[&r&f" + getEntityName() + "&7]", Arrays.asList("&fLocation: &b" + Text.cords(this.getEntity().getLocation()),this.distanceRelativeToPlayer == -1 ? "" : "&fDistance relative to you: &b" + this.distanceRelativeToPlayer + "u", "&7Click to teleport!"));
            }
        } catch (Exception e) {
            if (this.getEntity().getCustomName() == null) {
                return Itens.createItem(Material.PAINTING, 1, "&f" + getEntityName(), Arrays.asList("&8Icon could not be found.", "&fLocation: &b" + Text.cords(this.getEntity().getLocation()),this.distanceRelativeToPlayer == -1 ? "" : "&fDistance relative to you: &b" + this.distanceRelativeToPlayer + "u", "&7Click to teleport!"));
            } else {
                return Itens.createItem(Material.PAINTING, 1, "&f" + this.getEntity().getCustomName() + " &7[&r&f" + getEntityName() + "&7]", Arrays.asList("&8Icon could not be found.", "&fLocation: &b" + Text.cords(this.getEntity().getLocation()),this.distanceRelativeToPlayer == -1 ? "" : "&fDistance relative to you: &b" + this.distanceRelativeToPlayer + "u", "&7Click to teleport!"));
            }
        }
    }
}
