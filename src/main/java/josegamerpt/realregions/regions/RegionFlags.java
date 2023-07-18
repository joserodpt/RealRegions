package josegamerpt.realregions.regions;

public enum RegionFlags {
    ACCESS_CHESTS("Access-Chests"),
    ACCESS_CRAFTING_TABLES("Access-Crafting-Tables"),
    ACCESS_HOPPERS("Access-Hoppers"),
    BLOCK_BREAK("Block-Break"),
    BLOCK_INTERACTIONS("Block-Interactions"),
    BLOCK_PLACE("Block-Place"),
    CONTAINER_INTERACTIONS("Container-Interactions"),
    ENTITY_SPAWNING("Entity-Spawning"),
    ENTER("Enter"),
    EXPLOSIONS("Explosions"),
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
}
