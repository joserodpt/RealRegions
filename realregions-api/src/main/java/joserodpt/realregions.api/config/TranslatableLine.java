package joserodpt.realregions.api.config;

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

import dev.dejvokep.boostedyaml.YamlDocument;
import joserodpt.realutils.text.LanguageLine;
import joserodpt.realutils.text.LanguageMessage;
import joserodpt.realutils.text.Placeholder;

/**
 * Every line the plugin says to a player, as a constant pointing at its route in language.yml.
 *
 * <p>Placeholders are filled with {@link #with(Placeholder, Object)}, which hands back a new
 * {@link LanguageMessage} rather than changing the constant:</p>
 *
 * <pre>{@code
 * TranslatableLine.REGION_TP.with(NAME, region.getDisplayName()).with(WORLD, world.getRWorldName()).send(p);
 * }</pre>
 */
public enum TranslatableLine implements LanguageLine {

    REGION_CREATED("Region.Created"),
    REGION_NAME_EMPTY("Region.Name-Empty"),
    REGION_NAME_DUPLICATE("Region.Name-Duplicate"),
    REGION_NON_EXISTENT_NAME("Region.Non-Existent-Name"),
    REGION_NOT_IN_WORLD("Region.Not-In-World"),
    REGION_DISPLAY_NAME_CHANGED("Region.Display-Name-Changed"),
    REGION_CANT_DELETE_INFINITE("Region.Cant-Delete-Infinite"),
    REGION_IMPORTED_FROM_EXTERNAL("Region.Imported-From-External"),
    REGION_DELETED("Region.Deleted"),
    REGION_CANT_BREAK_BLOCK("Region.Cant-Break-Block"),
    REGION_CANT_PLACE_BLOCK("Region.Cant-Place-Block"),
    REGION_CANT_DROP_ITEMS("Region.Cant-Drop-Items"),
    REGION_CANT_PICKUP_ITEMS("Region.Cant-Pickup-Items"),
    REGION_CANT_ENTER_HERE("Region.Cant-Enter-Here"),
    REGION_CANT_INTERACT_BLOCKS("Region.Cant-Interact-Blocks"),
    REGION_CANT_INTERACT_CONTAINER("Region.Cant-Interact-Container"),
    REGION_CANT_INTERACT_CRAFTING_TABLES("Region.Cant-Interact-Crafting-Tables"),
    REGION_CANT_INTERACT_HOPPER("Region.Cant-Interact-Hopper"),
    REGION_CANT_OPEN_CHEST("Region.Cant-Open-Chest"),
    REGION_CANT_PVP("Region.Cant-PVP"),
    REGION_CANT_PVE("Region.Cant-PVE"),
    REGION_CANT_CHAT("Region.Cant-Chat"),
    REGION_CANT_USE_COMMAND("Region.Cant-Use-Command"),
    REGION_BLOCKED_COMMANDS_SET("Region.Blocked-Commands-Set"),
    REGION_BLOCKED_COMMANDS_TITLE("Region.Blocked-Commands.Title"),
    REGION_BLOCKED_COMMANDS_DESCRIPTION_ALL("Region.Blocked-Commands.Description-All"),
    REGION_BLOCKED_COMMANDS_DESCRIPTION_SOME("Region.Blocked-Commands.Description-Some"),
    REGION_BLOCKED_COMMANDS_FLAG("Region.Blocked-Commands.Flag"),
    REGION_BLOCKED_COMMANDS_FLAG_DESCRIPTION("Region.Blocked-Commands.Flag-Description"),
    REGION_BLOCKED_COMMANDS_ADD("Region.Blocked-Commands.Add"),
    REGION_BLOCKED_COMMANDS_ADD_DESCRIPTION("Region.Blocked-Commands.Add-Description"),
    REGION_BLOCKED_COMMANDS_ADD_FIELD("Region.Blocked-Commands.Add-Field"),
    REGION_BLOCKED_COMMANDS_CLEAR("Region.Blocked-Commands.Clear"),
    REGION_BLOCKED_COMMANDS_CLEAR_DESCRIPTION("Region.Blocked-Commands.Clear-Description"),
    REGION_BLOCKED_COMMANDS_CLEAR_SHIFT("Region.Blocked-Commands.Clear-Shift"),
    REGION_BLOCKED_COMMANDS_CLEAR_CONFIRM_TITLE("Region.Blocked-Commands.Clear-Confirm-Title"),
    REGION_BLOCKED_COMMANDS_CLEAR_CONFIRM("Region.Blocked-Commands.Clear-Confirm"),
    REGION_BLOCKED_COMMANDS_ENTRY("Region.Blocked-Commands.Entry"),
    REGION_BLOCKED_COMMANDS_ENTRY_REMOVE("Region.Blocked-Commands.Entry-Remove"),
    REGION_BLOCKED_COMMANDS_EMPTY("Region.Blocked-Commands.Empty"),
    REGION_BLOCKED_COMMANDS_PREVIOUS_PAGE("Region.Blocked-Commands.Previous-Page"),
    REGION_BLOCKED_COMMANDS_NEXT_PAGE("Region.Blocked-Commands.Next-Page"),
    REGION_BLOCKED_COMMANDS_ADDED("Region.Blocked-Commands.Added"),
    REGION_BLOCKED_COMMANDS_ALREADY_LISTED("Region.Blocked-Commands.Already-Listed"),
    REGION_BLOCKED_COMMANDS_REMOVED("Region.Blocked-Commands.Removed"),
    REGION_BLOCKED_COMMANDS_CLEARED("Region.Blocked-Commands.Cleared"),
    REGION_CANT_CONSUME("Region.Cant-Consume"),
    REGION_DISABLED_END_PORTAL("Region.Disabled-End-Portal"),
    REGION_DISABLED_NETHER_PORTAL("Region.Disabled-Nether-Portal"),
    REGION_SET_BOUNDS("Region.Region-Set-Bounds"),
    REGION_FLAG_UNKNOWN("Region.Flag-Unknown"),
    REGION_FLAG_SET("Region.Flag-Set"),
    REGION_RENAMED("Region.Renamed"),
    REGION_TP_UNLOADED_WORLD("Region.TP-Unloaded-World"),
    REGION_TP("Region.TP"),
    REGION_REDEFINE_EXTERNAL_PLUGIN("Region.Redefine-External-Plugin"),
    REGION_CANT_VIEW_INFINITE_REGION("Region.Cant-View-Infinite-Region"),
    REGION_VIEW_REGION("Region.View-Region"),
    REGION_ENTERING_TITLE("Region.Entering.Title"),
    REGION_ENTERING_SUBTITLE("Region.Entering.Subtitle"),
    REGION_ENTERING_TOGGLE("Region.Entering.Toggle"),
    REGION_DELETE_CONFIRM_TITLE("Region.Delete-Confirm-Title"),
    REGION_DELETE_CONFIRM("Region.Delete-Confirm"),
    REGION_DELETE_CONFIRM_BUTTON("Region.Delete-Confirm-Button"),

    //WORLD

    WORLD_BEING_IMPORTED("World.Being-Imported"),
    WORLD_IMPORTED("World.Imported"),
    WORLD_FAILED_TO_IMPORT("World.Failed-To-Import"),
    WORLD_BEING_CREATED("World.Being-Created"),
    WORLD_CREATED("World.Created"),
    WORLD_FAILED_TO_CREATE("World.Failed-To-Create"),
    WORLD_ALREADY_LOADED("World.Already-Loaded"),
    WORLD_LOADED("World.Loaded"),
    WORLD_UNLOAD_DEFAULT_WORLDS("World.Unload-Default-Worlds"),
    WORLD_ALREADY_UNLOADED("World.Already-Unloaded"),
    WORLD_BEING_UNLOADED("World.Being-Unloaded"),
    WORLD_UNLOADED("World.Unloaded"),
    WORLD_DELETE_DEFAULT_WORLDS("World.Delete-Default-Worlds"),
    WORLD_BEING_DELETED("World.Being-Deleted"),
    WORLD_DELETED("World.Deleted"),
    WORLD_UNREGISTERED("World.Unregistered"),
    WORLD_NO_WORLD_NAMED("World.No-World-Named"),
    WORLD_TPJOIN_SET("World.TP-Join-Set"),
    WORLD_GAMERULE_SET("World.Gamerule-Set"),
    WORLD_INVENTORIES_SET("World.Inventories-Set"),

    WORLD_SPAWN_SET("World.Spawn-Set"),
    WORLD_NAME_EMPTY("World.Name-Empty"),
    WORLD_INVALID_TYPE("World.Invalid-Type"),
    WORLD_TP_UNLOADED("World.TP-Unloaded-World"),
    WORLD_TP("World.TP"),
    WORLD_UNLOAD_CONFIRM_TITLE("World.Unload-Confirm-Title"),
    WORLD_UNLOAD_CONFIRM("World.Unload-Confirm"),
    WORLD_UNLOAD_CONFIRM_BUTTON("World.Unload-Confirm-Button"),
    WORLD_DELETE_CONFIRM_TITLE("World.Delete-Confirm-Title"),
    WORLD_DELETE_CONFIRM("World.Delete-Confirm"),
    WORLD_DELETE_CONFIRM_BUTTON("World.Delete-Confirm-Button"),

    //MENU
    MENU_UNLOADED_WORLD("Menu.Unloaded-World"),
    SEARCH_NO_RESULTS("Search.No-Results"),
    SELECTION_NONE("Selection.None"),
    INPUT_NOT_NUMBER("Input.Not-Number"),

    PRIORITY_CHANGED("Priority.Changed"),

    SYSTEM_RELOADED("System.Reloaded"),
    SYSTEM_NOT_FOUND("System.Not-Found"),
    SYSTEM_ERROR_REMOVING_FILES("System.Error-Removing-Files"),
    SYSTEM_INPUT_CANCELLED("System.Input-Cancelled"),
    SYSTEM_ERROR_OCCURRED("System.Error-Occurred"),
    SYSTEM_DIALOG_CONFIRM("System.Dialog-Confirm"),
    SYSTEM_DIALOG_CANCEL("System.Dialog-Cancel"),
    SYSTEM_DIALOG_SAVE("System.Dialog-Save"),
    SYSTEM_DIALOG_BACK("System.Dialog-Back"),
    SYSTEM_DIALOG_CLOSE("System.Dialog-Close"),
    SYSTEM_SETTINGS_SAVED("System.Settings-Saved"),
    SYSTEM_SETTINGS_NEED_DIALOGS("System.Settings-Need-Dialogs"),
    SYSTEM_PLAYER_ONLY("System.Player-Only"),
    SYSTEM_ERROR_PERMISSION("System.Error-Permission"),
    SYSTEM_ERROR_COMMAND("System.Error-Command"),
    SYSTEM_ERROR_USAGE("System.Error-Usage");

    private final String configPath;

    TranslatableLine(String configPath) {
        this.configPath = configPath;
    }

    @Override
    public String getPath() {
        return this.configPath;
    }

    @Override
    public YamlDocument getLanguageFile() {
        return RRLanguage.file();
    }

    /** The tokens a line in language.yml may contain. {@code NAME} is written {@code %name%}. */
    public enum TranslatableLinePlaceholder implements Placeholder {
        NAME, WORLD, INPUT
    }
}
