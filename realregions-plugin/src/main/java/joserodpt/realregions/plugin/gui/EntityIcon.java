package joserodpt.realregions.plugin.gui;

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

import joserodpt.realregions.api.utils.Itens;
import joserodpt.realregions.api.utils.Text;
import org.bukkit.Location;
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

        this.distanceRelativeToPlayer = calculateDistance(p, e);
    }

    public Double getDistanceRelativeToPlayer() {
        return distanceRelativeToPlayer;
    }

    public Entity getEntity() {
        return this.e;
    }

    public String getEntityName() {
        return e.getName();
    }

    public ItemStack getIcon() {
        if (this.getEntity().getType() == EntityType.PLAYER) {
            return Itens.createItem(Material.PLAYER_HEAD, 1, "&f" + getEntityName(), Arrays.asList("&fLocation: &b" + Text.cords(this.getEntity().getLocation()), this.distanceRelativeToPlayer == -1 ? "" : "&fDistance relative to you: &b" + this.distanceRelativeToPlayer + "u","&7Click to teleport!"));
        }

        if (this.getEntity().getType() == EntityType.MINECART_CHEST) {
            return Itens.createItem(Material.CHEST_MINECART, 1, "&f" + getEntityName(), Arrays.asList("&fLocation: &b" + Text.cords(this.getEntity().getLocation()), this.distanceRelativeToPlayer == -1 ? "" : "&fDistance relative to you: &b" + this.distanceRelativeToPlayer + "u","&7Click to teleport!"));
        }

        String mat = this.getEntity().getType().name().toUpperCase() + "_SPAWN_EGG";
        try {
            Material m = Material.valueOf(mat);
            if (this.getEntity().getCustomName() == null)
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

    private double calculateDistance(Entity entity1, Entity entity2) {
        if (entity1.getLocation().getWorld() != entity2.getLocation().getWorld()) {
            return -1;
        }

        // Get the locations of the two entities
        Location location1 = entity1.getLocation();
        Location location2 = entity2.getLocation();

        // Use the distance formula to calculate the distance
        double dx = location2.getX() - location1.getX();
        double dy = location2.getY() - location1.getY();
        double dz = location2.getZ() - location1.getZ();

        double distanceSquared = dx * dx + dy * dy + dz * dz;

        return Math.round(Math.sqrt(distanceSquared) * 100.0) / 100.0;
    }
}
