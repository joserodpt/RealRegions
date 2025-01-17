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
 * @author José Rodrigues © 2020-2025
 * @link https://github.com/joserodpt/RealRegions
 */

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public enum RegionFlags {
    ACCESS_CHESTS("access-chests"),
    ACCESS_CRAFTING_TABLES("access-crafting-tables"),
    ACCESS_HOPPERS("access-hoppers"),
    BLOCK_BREAK("block-break"),
    BLOCK_INTERACTIONS("block-interactions"),
    BLOCK_PLACE("block-place"),
    CONTAINER_INTERACTIONS("container-interactions"),
    ENTER("enter"),
    HUNGER("hunger"),
    ITEM_DROP("item-drop"),
    ITEM_PICKUP("item-pickup"),
    ITEM_PICKUP_ONLY_OWNER("item-pickup-only-owner"),
    PVE("pve"),
    PVP("pvp"),
    TAKE_DAMAGE("take-damage"),
    NO_CHAT("no-chat"),
    NO_CONSUMABLES("no-consumables"),
    DISABLED_NETHER_PORTAL("disabled-nether-portal"),
    DISABLED_END_PORTAL("disabled-end-portal");


    private final String permission;

    RegionFlags(String permission) {
        this.permission = permission;
    }

    private String getPermission() {
        return permission;
    }

    public String getBypassPermission(String world, String region) {
        return String.format("realregions.%s.%s.%s.bypass", world, region, this.getPermission());
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
