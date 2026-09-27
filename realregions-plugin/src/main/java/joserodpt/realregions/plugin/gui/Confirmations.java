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

import joserodpt.realregions.api.RWorld;
import joserodpt.realregions.api.config.TranslatableLine;
import joserodpt.realregions.api.regions.Region;
import joserodpt.realutils.dialog.Dialogs;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * The yes-or-no questions asked before something that can't be undone: deleting a region, and
 * unloading or deleting a world.
 *
 * <p>Each one returns false when nothing was asked - console, a server without dialogs, or
 * {@code useDialogs} off - and the caller then goes ahead straight away, as it always did. Nothing
 * is asked either when the manager would refuse anyway (an infinite region, a default world), so
 * its own message is what the player sees.</p>
 */
public final class Confirmations {

    private Confirmations() {
    }

    public static boolean deleteRegion(final CommandSender sender, final Region r, final Runnable confirmed, final Runnable declined) {
        if (!(sender instanceof Player) || r.getType() == Region.RegionType.INFINITE || r.getOrigin() != Region.RegionOrigin.REALREGIONS) {
            return false;
        }
        return Dialogs.confirm((Player) sender,
                TranslatableLine.REGION_DELETE_CONFIRM_TITLE.setV1(TranslatableLine.ReplacableVar.NAME.eq(r.getDisplayName())).get(),
                TranslatableLine.REGION_DELETE_CONFIRM.setV1(TranslatableLine.ReplacableVar.NAME.eq(r.getDisplayName())).get(),
                TranslatableLine.REGION_DELETE_CONFIRM_BUTTON.get(), null, confirmed, declined);
    }

    public static boolean unloadWorld(final CommandSender sender, final RWorld rw, final Runnable confirmed, final Runnable declined) {
        if (!(sender instanceof Player) || isDefault(rw) || !rw.isLoaded()) {
            return false;
        }
        return Dialogs.confirm((Player) sender,
                TranslatableLine.WORLD_UNLOAD_CONFIRM_TITLE.setV1(TranslatableLine.ReplacableVar.NAME.eq(rw.getRWorldName())).get(),
                TranslatableLine.WORLD_UNLOAD_CONFIRM.setV1(TranslatableLine.ReplacableVar.NAME.eq(rw.getRWorldName())).get(),
                TranslatableLine.WORLD_UNLOAD_CONFIRM_BUTTON.get(), null, confirmed, declined);
    }

    /** Also for the files of a world that was never imported, which is all deleting one of those removes. */
    public static boolean deleteWorld(final CommandSender sender, final RWorld rw, final Runnable confirmed, final Runnable declined) {
        if (!(sender instanceof Player) || isDefault(rw)) {
            return false;
        }
        return Dialogs.confirm((Player) sender,
                TranslatableLine.WORLD_DELETE_CONFIRM_TITLE.setV1(TranslatableLine.ReplacableVar.NAME.eq(rw.getRWorldName())).get(),
                TranslatableLine.WORLD_DELETE_CONFIRM.setV1(TranslatableLine.ReplacableVar.NAME.eq(rw.getRWorldName())).get(),
                TranslatableLine.WORLD_DELETE_CONFIRM_BUTTON.get(), null, confirmed, declined);
    }

    //the same test WorldManager refuses to unload or delete by
    private static boolean isDefault(final RWorld rw) {
        return rw.getRWorldName().equalsIgnoreCase("world") || rw.getRWorldName().startsWith("world_");
    }
}
