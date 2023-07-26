package josegamerpt.realregions.regions;

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
