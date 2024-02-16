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

import joserodpt.realregions.api.RealRegionsAPI;
import joserodpt.realregions.api.config.ReplacableVar;
import joserodpt.realregions.api.config.TranslatableLine;
import joserodpt.realregions.api.regions.Region;
import joserodpt.realregions.api.regions.RegionFlags;
import joserodpt.realregions.api.utils.Text;
import joserodpt.realregions.api.utils.Itens;
import joserodpt.realregions.api.utils.PlayerInput;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlagSelectorGUI {

	private static Map<UUID, FlagSelectorGUI> inventories = new HashMap<>();
	private Inventory inv;

	private ItemStack close = Itens.createItem(Material.OAK_DOOR, 1, "&cClose",
			Collections.singletonList("&fClick here to close this menu."));

	private UUID uuid;
	private Region r;
	private RealRegionsAPI rr;

	public FlagSelectorGUI(Player as, Region r, RealRegionsAPI rr) {
		this.rr = rr;
		this.uuid = as.getUniqueId();
		inv = Bukkit.getServer().createInventory(null, 45, Text.color("&8Real&eRegions &8| " + r.getDisplayName()));

		this.r = r;
		load();

		this.register();
	}

	public void load() {
		//row1
		inv.setItem(1, Itens.createItem(Material.GOLDEN_APPLE, 1, "&7&lNo Consumables &r&7[" + getStyle(r.noConsumables) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows item consumables.", "&e&nPermissions",
						"  &eBypass&f: " + RegionFlags.NO_CONSUMABLES.getBypassPermission(r.getRWorld().getRWorldName(), r.getRegionName()),
						"&f&nLeft-click&r&f to change value", "&f&nRight-click&r&f to copy bypass permission")));

		inv.setItem(2, Itens.createItem(Material.DIAMOND_PICKAXE, 1, "&7&lBlock Breaking &r&7[" + getStyle(r.blockBreak) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows block breaking.", "&e&nPermissions",
						"  &eBypass&f: " + RegionFlags.BLOCK_BREAK.getBypassPermission(r.getRWorld().getRWorldName(), r.getRegionName()),
						"&f&nLeft-click&r&f to change value", "&f&nRight-click&r&f to copy bypass permission")));

		inv.setItem(3, Itens.createItem(Material.GRASS_BLOCK, 1, "&7&lBlock Placing &r&7[" + getStyle(r.blockPlace) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows block placing.", "&e&nPermissions",
						"  &eBypass&f: " + RegionFlags.BLOCK_PLACE.getBypassPermission(r.getRWorld().getRWorldName(), r.getRegionName()),
						"&f&nLeft-click&r&f to change value", "&f&nRight-click&r&f to copy bypass permission")));

		inv.setItem(4, Itens.createItem(Material.STONE, 1, "&7&lBlock Interaction &r&7[" + getStyle(r.blockInteract) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows Block Interaction.", "&e&nPermissions",
						"  &eBypass&f: " + RegionFlags.BLOCK_INTERACTIONS.getBypassPermission(r.getRWorld().getRWorldName(), r.getRegionName()),
						"&f&nLeft-click&r&f to change value", "&f&nRight-click&r&f to copy bypass permission")));

		inv.setItem(5, Itens.createItem(Material.DIAMOND_SWORD, 1, "&7&lPVP &r&7[" + getStyle(r.pvp) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows PVP (player vs player).", "&e&nPermissions",
						"  &eBypass&f: " + RegionFlags.PVP.getBypassPermission(r.getRWorld().getRWorldName(), r.getRegionName()),
						"&f&nLeft-click&r&f to change value", "&f&nRight-click&r&f to copy bypass permission")));

		inv.setItem(6, Itens.createItem(Material.IRON_SWORD, 1, "&7&lPVE &r&7[" + getStyle(r.pve) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows PVE (player vs entity).", "&e&nPermissions",
						"  &eBypass&f: " + RegionFlags.PVE.getBypassPermission(r.getRWorld().getRWorldName(), r.getRegionName()),
						"&f&nLeft-click&r&f to change value", "&f&nRight-click&r&f to copy bypass permission")));
		inv.setItem(7, Itens.createItem(Material.OBSIDIAN, 1, "&7&lDisabled Nether Portal &r&7[" + getStyle(r.disabledNetherPortal) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows Nether Portal Entering.", "&e&nPermissions",
						"  &eBypass&f: " + RegionFlags.DISABLED_NETHER_PORTAL.getBypassPermission(r.getRWorld().getRWorldName(), r.getRegionName()),
						"&f&nLeft-click&r&f to change value", "&f&nRight-click&r&f to copy bypass permission")));

		//row2

		inv.setItem(10, Itens.createItem(Material.ORANGE_WOOL, 1, "&7&lNo Fire Spreading &r&7[" + getStyle(r.noFireSpreading) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows spreakind of fire and mushrooms.", "&e&nPermissions",
						"  &fNot applicable for Players.",
						"&f&nLeft-click&r&f to change value")));

		inv.setItem(11, Itens.createItem(Material.CRAFTING_TABLE, 1, "&7&lCrafting &r&7[" + getStyle(r.accessCrafting) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows access to Crafting Tables.", "&e&nPermissions",
						"  &eBypass&f: " + RegionFlags.ACCESS_CRAFTING_TABLES.getBypassPermission(r.getRWorld().getRWorldName(), r.getRegionName()),
						"&f&nLeft-click&r&f to change value", "&f&nRight-click&r&f to copy bypass permission")));

		inv.setItem(12, Itens.createItem(Material.CHEST, 1, "&7&lChests &r&7[" + getStyle(r.accessChests) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows chest interactions.", "&e&nPermissions",
						"  &eBypass&f: " + RegionFlags.ACCESS_CHESTS.getBypassPermission(r.getRWorld().getRWorldName(), r.getRegionName()),
						"&f&nLeft-click&r&f to change value", "&f&nRight-click&r&f to copy bypass permission")));

		inv.setItem(13, Itens.createItem(Material.HOPPER, 1, "&7&lHoppers &r&7[" + getStyle(r.accessHoppers) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows hopper interactions.", "&e&nPermissions",
						"  &eBypass&f: " + RegionFlags.ACCESS_HOPPERS.getBypassPermission(r.getRWorld().getRWorldName(), r.getRegionName()),
						"&f&nLeft-click&r&f to change value", "&f&nRight-click&r&f to copy bypass permission")));

		inv.setItem(14, Itens.createItem(Material.ENDER_CHEST, 1, "&7&lContainer Interaction &r&7[" + getStyle(r.containerInteract) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows container interactions.", "&e&nPermissions",
						"  &eBypass&f: " + RegionFlags.CONTAINER_INTERACTIONS.getBypassPermission(r.getRWorld().getRWorldName(), r.getRegionName()),
						"&f&nLeft-click&r&f to change value", "&f&nRight-click&r&f to copy bypass permission")));

		inv.setItem(15, Itens.createItem(Material.CREEPER_SPAWN_EGG, 1, "&7&lEntity Spawning &r&7[" + getStyle(r.entitySpawning) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows Entity Spawning.", "&e&nPermissions",
						"  &fNot applicable for Players.",
						"&f&nLeft-click&r&f to change value")));

		inv.setItem(16, Itens.createItem(Material.END_PORTAL_FRAME, 1, "&7&lDisabled End Portal &r&7[" + getStyle(r.disabledEndPortal) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows End Portal Entering.", "&e&nPermissions",
						"  &eBypass&f: " + RegionFlags.DISABLED_END_PORTAL.getBypassPermission(r.getRWorld().getRWorldName(), r.getRegionName()),
						"&f&nLeft-click&r&f to change value", "&f&nRight-click&r&f to copy bypass permission")));


		//row3

		inv.setItem(20, Itens.createItem(Material.BARRIER, 1, "&7&lEnter &r&7[" + getStyle(r.enter) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows player access to this region.", "&e&nPermissions",
						"  &eBypass&f: " + RegionFlags.ENTER.getBypassPermission(r.getRWorld().getRWorldName(), r.getRegionName()),
						"&f&nLeft-click&r&f to change value", "&f&nRight-click&r&f to copy bypass permission")));

		inv.setItem(21, Itens.createItem(Material.TNT, 1, "&7&lExplosions &r&7[" + getStyle(r.explosions) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows explosions.", "&e&nPermissions",
						"  &fNot applicable for Players.",
						"&f&nLeft-click&r&f to change value")));

		inv.setItem(22, Itens.createItem(Material.COOKED_BEEF, 1, "&7&lHunger &r&7[" + getStyle(r.hunger) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows Hunger.", "&e&nPermissions",
						"  &eBypass&f: " + RegionFlags.HUNGER.getBypassPermission(r.getRWorld().getRWorldName(), r.getRegionName()),
						"&f&nLeft-click&r&f to change value", "&f&nRight-click&r&f to copy bypass permission")));

		inv.setItem(23, Itens.createItem(Material.DROPPER, 1, "&7&lItem Drop &r&7[" + getStyle(r.itemDrop) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows item drop.", "&e&nPermissions",
						"  &eBypass&f: " + RegionFlags.ITEM_DROP.getBypassPermission(r.getRWorld().getRWorldName(), r.getRegionName()),
						"&f&nLeft-click&r&f to change value", "&f&nRight-click&r&f to copy bypass permission")));

		inv.setItem(24, Itens.createItem(Material.HOPPER_MINECART, 1, "&7&lItem Pickup &r&7[" + getStyle(r.itemPickup) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows Item Pickup.", "&e&nPermissions",
						"  &eBypass&f: " + RegionFlags.ITEM_PICKUP.getBypassPermission(r.getRWorld().getRWorldName(), r.getRegionName()),
						"&f&nLeft-click&r&f to change value", "&f&nRight-click&r&f to copy bypass permission")));


		//row4
		inv.setItem(30, Itens.createItem(Material.FLINT_AND_STEEL, 1, "&7&lTake Damage &r&7[" + getStyle(r.takeDamage) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows Damage.", "&e&nPermissions",
						"  &eBypass&f: " + RegionFlags.TAKE_DAMAGE.getBypassPermission(r.getRWorld().getRWorldName(), r.getRegionName()),
						"&f&nLeft-click&r&f to change value", "&f&nRight-click&r&f to copy bypass permission")));

		inv.setItem(31, Itens.createItem(Material.FILLED_MAP, 1, "&7&lNo Chat &r&7[" + getStyle(r.noChat) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows Typing in Chat.", "&e&nPermissions",
						"  &eBypass&f: " + RegionFlags.NO_CHAT.getBypassPermission(r.getRWorld().getRWorldName(), r.getRegionName()),
						"&f&nLeft-click&r&f to change value", "&f&nRight-click&r&f to copy bypass permission")));

		inv.setItem(32, Itens.createItem(Material.EMERALD, 1, "&7&lPriority &r&7[&b&l" + r.getPriority() + "&r&7]",
				Arrays.asList("&e&nDescription", "  Region Priority over others.",
						"Click to change the value.")));

		inv.setItem(38, Itens.createItem(Material.ENDER_PEARL, 1, "&fTeleport to this region."));
		inv.setItem(40, close);
		inv.setItem(42, Itens.createItem(Material.LAVA_BUCKET, 1, "&cDelete this region."));
	}

	private String getStyle(boolean b) {
		return b ? "&a&lENABLED" : "&c&lDISABLED";
	}

	public void openInventory(Player target) {
		Inventory inv = getInventory();
		InventoryView openInv = target.getOpenInventory();
		if (openInv != null) {
			Inventory openTop = target.getOpenInventory().getTopInventory();
			if (openTop != null && openTop.getType().name().equalsIgnoreCase(inv.getType().name())) {
				openTop.setContents(inv.getContents());
			} else {
				target.openInventory(inv);
			}
		}
	}

	public static Listener getListener() {
		return new Listener() {
			@EventHandler
			public void onClick(InventoryClickEvent event) {
				HumanEntity clicker = event.getWhoClicked();
				if (clicker instanceof Player) {
					Player player = (Player) clicker;
					if (event.getCurrentItem() == null) {
						return;
					}
					UUID uuid = clicker.getUniqueId();
					if (inventories.containsKey(uuid)) {
						FlagSelectorGUI current = inventories.get(uuid);
						if (event.getInventory().getHolder() != current.getInventory().getHolder()) {
							return;
						}

						event.setCancelled(true);

						switch (event.getRawSlot())
						{
							case 38:
								player.closeInventory();
								current.r.teleport(player, false);
								break;
							case 40:
								player.closeInventory();
								new BukkitRunnable()
								{
									public void run()
									{
										RegionsListGUI mp = new RegionsListGUI(player, current.r.getRWorld(), current.rr);
										mp.openInventory(player);
									}
								}.runTaskLater(current.rr.getPlugin(), 2);
								break;
							case 42:
								player.closeInventory();
								current.rr.getRegionManagerAPI().deleteRegion(player, current.r);
								new BukkitRunnable()
								{
									public void run()
									{
										RegionsListGUI mp = new RegionsListGUI(player, current.r.getRWorld(), current.rr);
										mp.openInventory(player);
									}
								}.runTaskLater(current.rr.getPlugin(), 2);
								break;

							case 1:
								if (event.getClick() == ClickType.LEFT) {
									current.r.noConsumables = !current.r.noConsumables;
									current.r.saveData(Region.RegionData.FLAGS);
									current.load();
									player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								} else if (event.getClick() == ClickType.RIGHT) {
									RegionFlags.NO_CONSUMABLES.sendBypassPermissionToPlayer(player, current.r.getRWorld().getRWorldName(), current.r.getRegionName());
								}
								break;
							case 2:
								if (event.getClick() == ClickType.LEFT) {
									current.r.blockBreak = !current.r.blockBreak;
									current.r.saveData(Region.RegionData.FLAGS);
									current.load();
									player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								} else if (event.getClick() == ClickType.RIGHT) {
									RegionFlags.BLOCK_BREAK.sendBypassPermissionToPlayer(player, current.r.getRWorld().getRWorldName(), current.r.getRegionName());
								}
								break;
							case 3:
								if (event.getClick() == ClickType.LEFT) {
									current.r.blockPlace = !current.r.blockPlace;
									current.r.saveData(Region.RegionData.FLAGS);
									current.load();
									player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								} else if (event.getClick() == ClickType.RIGHT) {
									RegionFlags.BLOCK_PLACE.sendBypassPermissionToPlayer(player, current.r.getRWorld().getRWorldName(), current.r.getRegionName());
								}
								break;

							case 4:
								if (event.getClick() == ClickType.LEFT) {
									current.r.blockInteract = !current.r.blockInteract;
									current.r.saveData(Region.RegionData.FLAGS);
									current.load();
									player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								} else if (event.getClick() == ClickType.RIGHT) {
									RegionFlags.BLOCK_INTERACTIONS.sendBypassPermissionToPlayer(player, current.r.getRWorld().getRWorldName(), current.r.getRegionName());
								}
								break;

							case 5:
								if (event.getClick() == ClickType.LEFT) {
									current.r.pvp = !current.r.pvp;
									current.r.saveData(Region.RegionData.FLAGS);
									current.load();
									player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								} else if (event.getClick() == ClickType.RIGHT) {
									RegionFlags.PVP.sendBypassPermissionToPlayer(player, current.r.getRWorld().getRWorldName(), current.r.getRegionName());
								}
								break;

							case 6:
								if (event.getClick() == ClickType.LEFT) {
									current.r.pve = !current.r.pve;
									current.r.saveData(Region.RegionData.FLAGS);
									current.load();
									player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								} else if (event.getClick() == ClickType.RIGHT) {
									RegionFlags.PVE.sendBypassPermissionToPlayer(player, current.r.getRWorld().getRWorldName(), current.r.getRegionName());
								}
								break;
							case 7:
								if (event.getClick() == ClickType.LEFT) {
									current.r.disabledNetherPortal = !current.r.disabledNetherPortal;
									current.r.saveData(Region.RegionData.FLAGS);
									current.load();
									player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								} else if (event.getClick() == ClickType.RIGHT) {
									RegionFlags.PVE.sendBypassPermissionToPlayer(player, current.r.getRWorld().getRWorldName(), current.r.getRegionName());
								}
								break;

							case 11:
								if (event.getClick() == ClickType.LEFT) {
									current.r.accessCrafting = !current.r.accessCrafting;
									current.r.saveData(Region.RegionData.FLAGS);
									current.load();
									player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								} else if (event.getClick() == ClickType.RIGHT) {
									RegionFlags.ACCESS_CRAFTING_TABLES.sendBypassPermissionToPlayer(player, current.r.getRWorld().getRWorldName(), current.r.getRegionName());
								}
								break;

							case 10:
								current.r.noFireSpreading = !current.r.noFireSpreading;
								current.r.saveData(Region.RegionData.FLAGS);
								current.load();
								player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								break;

							case 12:
								if (event.getClick() == ClickType.LEFT) {
									current.r.accessChests = !current.r.accessChests;
									current.r.saveData(Region.RegionData.FLAGS);
									current.load();
									player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								} else if (event.getClick() == ClickType.RIGHT) {
									RegionFlags.ACCESS_CRAFTING_TABLES.sendBypassPermissionToPlayer(player, current.r.getRWorld().getRWorldName(), current.r.getRegionName());
								}
								break;

							case 13:
								if (event.getClick() == ClickType.LEFT) {
									current.r.accessHoppers = !current.r.accessHoppers;
									current.r.saveData(Region.RegionData.FLAGS);
									current.load();
									player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								} else if (event.getClick() == ClickType.RIGHT) {
									RegionFlags.ACCESS_HOPPERS.sendBypassPermissionToPlayer(player, current.r.getRWorld().getRWorldName(), current.r.getRegionName());
								}
								break;

							case 14:
								if (event.getClick() == ClickType.LEFT) {
									current.r.containerInteract = !current.r.containerInteract;
									current.r.saveData(Region.RegionData.FLAGS);
									current.load();
									player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								} else if (event.getClick() == ClickType.RIGHT) {
									RegionFlags.CONTAINER_INTERACTIONS.sendBypassPermissionToPlayer(player, current.r.getRWorld().getRWorldName(), current.r.getRegionName());
								}
								break;

							case 15:
								current.r.entitySpawning = !current.r.entitySpawning;
								current.r.saveData(Region.RegionData.FLAGS);
								current.load();
								player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								break;

							case 16:
								current.r.disabledEndPortal = !current.r.disabledEndPortal;
								current.r.saveData(Region.RegionData.FLAGS);
								current.load();
								player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								break;

							case 20:
								if (event.getClick() == ClickType.LEFT) {
									current.r.enter = !current.r.enter;
									current.r.saveData(Region.RegionData.FLAGS);
									current.load();
									player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								} else if (event.getClick() == ClickType.RIGHT) {
									RegionFlags.ENTER.sendBypassPermissionToPlayer(player, current.r.getRWorld().getRWorldName(), current.r.getRegionName());
								}
								break;

							case 21:
								current.r.explosions = !current.r.explosions;
								current.r.saveData(Region.RegionData.FLAGS);
								current.load();
								player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								break;

							case 22:
								if (event.getClick() == ClickType.LEFT) {
									current.r.hunger =! current.r.hunger;
									current.r.saveData(Region.RegionData.FLAGS);
									current.load();
									player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								} else if (event.getClick() == ClickType.RIGHT) {
									RegionFlags.HUNGER.sendBypassPermissionToPlayer(player, current.r.getRWorld().getRWorldName(), current.r.getRegionName());
								}
								break;

							case 23:
								if (event.getClick() == ClickType.LEFT) {
									current.r.itemDrop = !current.r.itemDrop;
									current.r.saveData(Region.RegionData.FLAGS);
									current.load();
									player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								} else if (event.getClick() == ClickType.RIGHT) {
									RegionFlags.ITEM_DROP.sendBypassPermissionToPlayer(player, current.r.getRWorld().getRWorldName(), current.r.getRegionName());
								}
								break;

							case 24:
								if (event.getClick() == ClickType.LEFT) {
									current.r.itemPickup = !current.r.itemPickup;
									current.r.saveData(Region.RegionData.FLAGS);
									current.load();
									player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								} else if (event.getClick() == ClickType.RIGHT) {
									RegionFlags.ITEM_PICKUP.sendBypassPermissionToPlayer(player, current.r.getRWorld().getRWorldName(), current.r.getRegionName());
								}
								break;

							case 30:
								if (event.getClick() == ClickType.LEFT) {
									current.r.takeDamage = !current.r.takeDamage;
									current.r.saveData(Region.RegionData.FLAGS);
									current.load();
									player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								} else if (event.getClick() == ClickType.RIGHT) {
									RegionFlags.TAKE_DAMAGE.sendBypassPermissionToPlayer(player, current.r.getRWorld().getRWorldName(), current.r.getRegionName());
								}
								break;

							case 31:
								if (event.getClick() == ClickType.LEFT) {
									current.r.noChat = !current.r.noChat;
									current.r.saveData(Region.RegionData.FLAGS);
									current.load();
									player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								} else if (event.getClick() == ClickType.RIGHT) {
									RegionFlags.NO_CHAT.sendBypassPermissionToPlayer(player, current.r.getRWorld().getRWorldName(), current.r.getRegionName());
								}
								break;

							case 32:
								new PlayerInput(player, input -> {
									if (!StringUtils.isNumeric(input))
									{
										Text.send(player, TranslatableLine.INPUT_NOT_NUMBER.get());
										FlagSelectorGUI wv = new FlagSelectorGUI(player, current.r, current.rr);
										wv.openInventory(player);
										return;
									}

									current.r.setPriority(Integer.valueOf(input));
									current.r.saveData(Region.RegionData.SETTINGS);
									Text.send(player, TranslatableLine.PRIORITY_CHANGED.setV1(ReplacableVar.INPUT.eq(Text.color(input))).get());
									new BukkitRunnable() {
										public void run() {
											FlagSelectorGUI wv = new FlagSelectorGUI(player, current.r, current.rr);
											wv.openInventory(player);
										}
									}.runTaskLater(current.rr.getPlugin(), 2);
								}, input -> {
									FlagSelectorGUI wv = new FlagSelectorGUI(player, current.r, current.rr);
									wv.openInventory(player);
								});
								player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								break;
						}
					}
				}
			}

			@EventHandler
			public void onClose(InventoryCloseEvent e) {
				if (e.getPlayer() instanceof Player) {
					if (e.getInventory() == null) {
						return;
					}
					Player p = (Player) e.getPlayer();
					UUID uuid = p.getUniqueId();
					if (inventories.containsKey(uuid)) {
						inventories.get(uuid).unregister();
					}
				}
			}
		};
	}

	public Inventory getInventory() {
		return inv;
	}

	private void register() {
		inventories.put(this.uuid, this);
	}

	private void unregister() {
		inventories.remove(this.uuid);
	}
}