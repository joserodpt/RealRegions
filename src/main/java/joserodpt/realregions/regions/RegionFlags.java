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

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public enum RegionFlags {
    ACCESS_CHESTS("Access-Chests"),
    ACCESS_CRAFTING_TABLES("Access-Crafting-Tables"),
    ACCESS_HOPPERS("Access-Hoppers"),
    BLOCK_BREAK("Block-Break"),
    BLOCK_INTERACTIONS("Block-Interactions"),
    BLOCK_PLACE("Block-Place"),
    CONTAINER_INTERACTIONS("Container-Interactions"),
    ENTER("Enter"),
    HUNGER("Hunger"),
    ITEM_DROP("Item-Drop"),
    ITEM_PICKUP("Item-Pickup"),
    PVE("PVE"),
    PVP("PVP"),
    TAKE_DAMAGE("Take-Damage");

    private final String permission;

    RegionFlags(String permission) {
        this.permission = permission;
    }

    private String getPermission() {
        return permission;
    }

    public String getBypassPermission(String world, String region) {
        return String.format("RealRegions.%s.%s.%s.Bypass", world, region, this.getPermission());
    }

    public void sendBypassPermissionToPlayer(Player p, String world, String region) {
        p.closeInventory();
        String perm = this.getBypassPermission(world, region);

        TextComponent m = new TextComponent("Click me to copy the bypass permission!");
        m.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(perm)));
        m.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + perm));
        p.spigot().sendMessage(m);
    }
}
