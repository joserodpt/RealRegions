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

import joserodpt.realregions.api.RealRegionsAPI;
import joserodpt.realregions.api.config.TranslatableLine;
import joserodpt.realregions.api.regions.Region;
import joserodpt.realutils.dialog.DialogForm;
import joserodpt.realutils.dialog.DialogMenu;
import joserodpt.realutils.dialog.Dialogs;
import joserodpt.realutils.gui.GUIBuilder;
import joserodpt.realutils.gui.MaterialPickerGUI;
import joserodpt.realutils.gui.Pagination;
import joserodpt.realutils.input.PlayerInput;
import joserodpt.realutils.item.Items;
import joserodpt.realutils.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static joserodpt.realregions.api.config.TranslatableLine.TranslatableLinePlaceholder.INPUT;
import static joserodpt.realregions.api.config.TranslatableLine.TranslatableLinePlaceholder.NAME;

/**
 * The commands a region's Block Commands flag applies to, with the flag itself, a way to add more
 * and one entry per command to remove it.
 *
 * <p>Shown as a dialog where the server has them, and as a chest menu everywhere else, or when the
 * dialog can't be shown after all. Both page through the list the same way and go back to the
 * region's settings menu.</p>
 */
public final class BlockedCommandsGUI {

    /** Four rows of commands inside a glass border, as the other paged menus have them. */
    private static final int[] ENTRY_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43};
    private static final int[] PREVIOUS_SLOTS = {18, 27};
    private static final int[] NEXT_SLOTS = {26, 35};
    private static final int BACK_SLOT = 45;
    private static final int FLAG_SLOT = 47;
    private static final int ADD_SLOT = 49;
    private static final int CLEAR_SLOT = 51;
    private static final int CLOSE_SLOT = 53;
    /** In among where the entries would be, since it only shows when there are none. */
    private static final int EMPTY_SLOT = 22;

    /**
     * Commands on one page of the dialog. A dialog menu holds {@link DialogMenu#MAX_OPTIONS}
     * buttons, and flag, add, clear and the two page turns take five of them.
     */
    private static final int DIALOG_PAGE_SIZE = 12;

    private static final int LORE_WIDTH = 40;

    private final Player p;
    private final Region r;
    private final RealRegionsAPI rr;

    private BlockedCommandsGUI(final Player p, final Region r, final RealRegionsAPI rr) {
        this.p = p;
        this.r = r;
        this.rr = rr;
    }

    public static void open(final Player p, final Region r, final RealRegionsAPI rr) {
        new BlockedCommandsGUI(p, r, rr).open(0);
    }

    private void open(final int page) {
        if (!this.openDialog(page)) {
            this.openChest(page);
        }
    }

    // --- dialog ---

    private boolean openDialog(final int page) {
        final Pagination<String> pages = new Pagination<>(DIALOG_PAGE_SIZE, new ArrayList<>(this.r.blockedCommands));
        final int shown = shownPage(pages, page);

        final DialogMenu menu = new DialogMenu(TranslatableLine.REGION_BLOCKED_COMMANDS_TITLE.with(NAME, this.r.getDisplayName()).get(),
                this.description()).columns(2).icon(Material.COMMAND_BLOCK);

        menu.option(this.flagLabel(), TranslatableLine.REGION_BLOCKED_COMMANDS_FLAG_DESCRIPTION.get(), () -> {
            this.toggleFlag();
            this.open(shown);
        });
        menu.option(TranslatableLine.REGION_BLOCKED_COMMANDS_ADD.get(), TranslatableLine.REGION_BLOCKED_COMMANDS_ADD_DESCRIPTION.get(),
                () -> this.askInDialog(shown));
        if (!pages.isEmpty()) {
            menu.option(TranslatableLine.REGION_BLOCKED_COMMANDS_CLEAR.get(), TranslatableLine.REGION_BLOCKED_COMMANDS_CLEAR_DESCRIPTION.get(),
                    () -> this.confirmClear(shown));

            for (final String command : pages.getPage(shown)) {
                menu.option(entryName(command), TranslatableLine.REGION_BLOCKED_COMMANDS_ENTRY_REMOVE.get(), () -> {
                    this.remove(command);
                    this.open(shown);
                });
            }
        }
        if (pages.exists(shown - 1)) {
            menu.option(TranslatableLine.REGION_BLOCKED_COMMANDS_PREVIOUS_PAGE.get(), null, () -> this.open(shown - 1));
        }
        if (pages.exists(shown + 1)) {
            menu.option(TranslatableLine.REGION_BLOCKED_COMMANDS_NEXT_PAGE.get(), null, () -> this.open(shown + 1));
        }

        return menu.close(TranslatableLine.SYSTEM_DIALOG_BACK.get())
                .open(this.p, this::backToSettings, () -> this.openChest(shown));
    }

    private void askInDialog(final int page) {
        final boolean shown = new DialogForm(TranslatableLine.REGION_BLOCKED_COMMANDS_TITLE.with(NAME, this.r.getDisplayName()).get(),
                TranslatableLine.REGION_BLOCKED_COMMANDS_ADD_DESCRIPTION.get())
                .text("commands", TranslatableLine.REGION_BLOCKED_COMMANDS_ADD_FIELD.get(), "", 256)
                .open(this.p, answers -> this.add(answers.text("commands", "")), () -> this.open(page),
                        //the menu could be shown but the form not: typed in chat instead
                        this::askInChat);
        if (!shown) {
            this.askInChat();
        }
    }

    private void confirmClear(final int page) {
        final boolean asked = Dialogs.confirm(this.p,
                TranslatableLine.REGION_BLOCKED_COMMANDS_CLEAR_CONFIRM_TITLE.with(NAME, this.r.getDisplayName()).get(),
                TranslatableLine.REGION_BLOCKED_COMMANDS_CLEAR_CONFIRM.with(NAME, this.r.getDisplayName()).get(),
                TranslatableLine.REGION_BLOCKED_COMMANDS_CLEAR.get(), null, () -> {
                    this.clear();
                    this.open(0);
                }, () -> this.open(page));
        if (!asked) {
            //dialogs went away between the menu and the question: the chest asks for a shift-click instead
            this.openChest(page);
        }
    }

    // --- chest ---

    private void openChest(final int page) {
        final Pagination<String> pages = new Pagination<>(ENTRY_SLOTS.length, new ArrayList<>(this.r.blockedCommands));
        final int shown = shownPage(pages, page);

        final GUIBuilder inventory = new GUIBuilder(TranslatableLine.REGION_BLOCKED_COMMANDS_TITLE.with(NAME, this.r.getDisplayName()).get(),
                54, this.p.getUniqueId());

        final Set<Integer> entrySlots = Arrays.stream(ENTRY_SLOTS).boxed().collect(Collectors.toSet());
        for (int slot = 0; slot < 54; slot++) {
            if (!entrySlots.contains(slot)) {
                inventory.setItem(MaterialPickerGUI.placeholder, slot);
            }
        }

        if (pages.isEmpty()) {
            //an empty list blocks everything, which is worth saying on the screen that looks empty
            inventory.setItem(Items.createItem(Material.BARRIER, 1, TranslatableLine.REGION_BLOCKED_COMMANDS_EMPTY.get(),
                    lore(TranslatableLine.REGION_BLOCKED_COMMANDS_DESCRIPTION_ALL.get())), EMPTY_SLOT);
        } else {
            final List<String> listed = pages.getPage(shown);
            for (int i = 0; i < listed.size(); i++) {
                final String command = listed.get(i);
                inventory.addItem(e -> {
                    this.remove(command);
                    this.p.playSound(this.p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
                    this.openChest(shown);
                }, Items.createItem(Material.PAPER, 1, entryName(command),
                        lore(TranslatableLine.REGION_BLOCKED_COMMANDS_ENTRY_REMOVE.get())), ENTRY_SLOTS[i]);
            }
        }

        //always there, as they are on the other paged menus, and a turn past either end stays put
        for (final int slot : PREVIOUS_SLOTS) {
            inventory.addItem(e -> this.turnChestPage(pages, shown - 1),
                    Items.createItem(Material.YELLOW_STAINED_GLASS, 1, TranslatableLine.REGION_BLOCKED_COMMANDS_PREVIOUS_PAGE.get()), slot);
        }
        for (final int slot : NEXT_SLOTS) {
            inventory.addItem(e -> this.turnChestPage(pages, shown + 1),
                    Items.createItem(Material.GREEN_STAINED_GLASS, 1, TranslatableLine.REGION_BLOCKED_COMMANDS_NEXT_PAGE.get()), slot);
        }

        inventory.addItem(e -> this.backToSettings(),
                Items.createItem(Material.RED_BED, 1, TranslatableLine.SYSTEM_DIALOG_BACK.get()), BACK_SLOT);

        inventory.addItem(e -> {
                    this.toggleFlag();
                    this.p.playSound(this.p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
                    this.openChest(shown);
                }, Items.createItem(Material.COMMAND_BLOCK, 1, this.flagLabel(),
                        lore(this.description() + " " + TranslatableLine.REGION_BLOCKED_COMMANDS_FLAG_DESCRIPTION.get())), FLAG_SLOT);

        inventory.addItem(e -> this.askInChat(),
                Items.createItem(Material.NAME_TAG, 1, TranslatableLine.REGION_BLOCKED_COMMANDS_ADD.get(),
                        lore(TranslatableLine.REGION_BLOCKED_COMMANDS_ADD_DESCRIPTION.get())), ADD_SLOT);

        if (!pages.isEmpty()) {
            //no question to ask here, so a shift-click is what keeps a stray click from emptying it
            inventory.addItem(e -> {
                        if (e.isShiftClick()) {
                            this.clear();
                            this.p.playSound(this.p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
                            this.openChest(0);
                        }
                    }, Items.createItem(Material.LAVA_BUCKET, 1, TranslatableLine.REGION_BLOCKED_COMMANDS_CLEAR.get(),
                            lore(TranslatableLine.REGION_BLOCKED_COMMANDS_CLEAR_DESCRIPTION.get() + " "
                                    + TranslatableLine.REGION_BLOCKED_COMMANDS_CLEAR_SHIFT.get())), CLEAR_SLOT);
        }

        inventory.addItem(e -> this.p.closeInventory(),
                Items.createItem(Material.OAK_DOOR, 1, TranslatableLine.SYSTEM_DIALOG_CLOSE.get()), CLOSE_SLOT);

        inventory.openInventory(this.p);
    }

    private void turnChestPage(final Pagination<String> pages, final int page) {
        if (!pages.exists(page)) {
            return;
        }
        this.p.playSound(this.p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 50, 50);
        this.openChest(page);
    }

    /** A dialog text box where the server has one, otherwise chat; either way back to this list after. */
    private void askInChat() {
        this.p.closeInventory();
        new PlayerInput(this.p, true, this::add, input -> this.open(0));
    }

    // --- the list ---

    private void add(final String input) {
        final List<String> added = this.r.addBlockedCommands(Region.splitCommands(input));
        if (added.isEmpty()) {
            TranslatableLine.REGION_BLOCKED_COMMANDS_ALREADY_LISTED.with(NAME, this.r.getDisplayName()).send(this.p);
            this.open(0);
            return;
        }
        this.r.saveData(Region.RegionData.FLAGS);
        TranslatableLine.REGION_BLOCKED_COMMANDS_ADDED.with(NAME, this.r.getDisplayName())
                .with(INPUT, "&b/" + String.join("&f, &b/", added)).send(this.p);
        //the page the first of them is on, so the player sees what they added
        this.open(this.r.blockedCommands.indexOf(added.get(0)) / this.pageSize());
    }

    private void remove(final String command) {
        if (this.r.removeBlockedCommand(command)) {
            this.r.saveData(Region.RegionData.FLAGS);
            TranslatableLine.REGION_BLOCKED_COMMANDS_REMOVED.with(NAME, this.r.getDisplayName()).with(INPUT, command).send(this.p);
        }
    }

    private void clear() {
        this.r.setBlockedCommands(Collections.emptyList());
        this.r.saveData(Region.RegionData.FLAGS);
        TranslatableLine.REGION_BLOCKED_COMMANDS_CLEARED.with(NAME, this.r.getDisplayName()).send(this.p);
    }

    private void toggleFlag() {
        this.r.blockCommands = !this.r.blockCommands;
        this.r.saveData(Region.RegionData.FLAGS);
    }

    // --- shared bits ---

    private void backToSettings() {
        this.p.closeInventory();
        this.later(() -> new RegionSettingsGUI(this.p, this.r, this.rr).openInventory(this.p));
    }

    /** A couple of ticks later, and only while the player is still around: the settings menu reuses whatever inventory is open, ours included. */
    private void later(final Runnable run) {
        Bukkit.getScheduler().runTaskLater(this.rr.getPlugin(), () -> {
            if (this.p.isOnline()) {
                run.run();
            }
        }, 2);
    }

    private int pageSize() {
        return Dialogs.isSupported() ? DIALOG_PAGE_SIZE : ENTRY_SLOTS.length;
    }

    private String description() {
        return this.r.blockedCommands.isEmpty()
                ? TranslatableLine.REGION_BLOCKED_COMMANDS_DESCRIPTION_ALL.get()
                : TranslatableLine.REGION_BLOCKED_COMMANDS_DESCRIPTION_SOME.get();
    }

    private String flagLabel() {
        return TranslatableLine.REGION_BLOCKED_COMMANDS_FLAG.with(INPUT, RegionSettingsGUI.getStyle(this.r.blockCommands)).get();
    }

    private static String entryName(final String command) {
        return TranslatableLine.REGION_BLOCKED_COMMANDS_ENTRY.with(INPUT, command).get();
    }

    /** A removal can empty the last page out from under the player. */
    private static int shownPage(final Pagination<String> pages, final int page) {
        return pages.exists(page) ? page : Math.max(0, pages.totalPages() - 1);
    }

    /** One language line as item lore, broken into lines that fit a tooltip, each keeping the colour it was in. */
    private static List<String> lore(final String text) {
        final List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (final String word : Text.color(text).split(" ")) {
            if (line.length() > 0 && ChatColor.stripColor(line.toString()).length() + ChatColor.stripColor(word).length() >= LORE_WIDTH) {
                lines.add(line.toString());
                line = new StringBuilder(ChatColor.getLastColors(line.toString()));
            }
            if (line.length() > 0 && !ChatColor.stripColor(line.toString()).isEmpty()) {
                line.append(' ');
            }
            line.append(word);
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }
}
