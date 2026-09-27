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
 * @author José Rodrigues © 2020-2025
 * @link https://github.com/joserodpt/RealRegions
 */

import joserodpt.realregions.api.config.RRConfig;
import joserodpt.realregions.api.config.TranslatableLine;
import joserodpt.realutils.dialog.SettingsDialog;
import joserodpt.realutils.dialog.SettingsStore;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * config.yml as dialogs, for {@code /rr settings}: a menu of categories, each its own form. There is
 * no inventory version, so on a server without dialogs the player is told to edit the file.
 *
 * <p>Everything here is read where it is used, so a save applies straight away, bar importing
 * RealMines' mines, which happens once when RealMines loads.</p>
 */
public final class ConfigEditor {

    private ConfigEditor() {
    }

    public static void open(final Player p) {
        settings().open(p, SettingsStore.of(RRConfig.file()::get, RRConfig.file()::set, RRConfig::save),
                () -> TranslatableLine.SYSTEM_SETTINGS_NEED_DIALOGS.send(p));
    }

    private static SettingsDialog settings() {
        final SettingsDialog settings = new SettingsDialog("&fReal&aRegions &8| &fSettings")
                .icon(Material.GRASS_BLOCK)
                .onSave((p, category) -> TranslatableLine.SYSTEM_SETTINGS_SAVED.send(p));

        settings.category("&eGeneral", "&7Prefix, dates and dialogs")
                .text("RealRegions.Prefix", "Plugin prefix", 64)
                .text("RealRegions.Date-Format", "Date format", 64).note("e.g yyyy-MM-dd HH:mm:ss")
                .text("RealRegions.Fallback-World", "World players are sent to when theirs is reset", 64)
                .toggle("RealRegions.useDialogs", "Use dialogs").note("off: chat prompts");

        settings.category("&aRegions", "&7Messages and effects")
                .toggle("RealRegions.Disable-Alert-Messages", "Hide messages when a region is entered or left")
                .toggle("RealRegions.Effects.Particles", "Particles")
                .toggle("RealRegions.Effects.Sounds", "Sounds");

        settings.category("&6Hooks", "&7Other plugins")
                .toggle("RealRegions.Hooks.RealMines.Import-Mines", "Import RealMines' mines as regions")
                .note("existing mines after a restart");
        return settings;
    }
}
