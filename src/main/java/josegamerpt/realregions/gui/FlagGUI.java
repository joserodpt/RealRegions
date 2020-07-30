package josegamerpt.realregions.gui;

import josegamerpt.realregions.RealRegions;
import josegamerpt.realregions.classes.Data;
import josegamerpt.realregions.classes.RWorld;
import josegamerpt.realregions.classes.Region;
import josegamerpt.realregions.managers.WorldManager;
import josegamerpt.realregions.utils.Itens;
import josegamerpt.realregions.utils.Pagination;
import josegamerpt.realregions.utils.PlayerInput;
import josegamerpt.realregions.utils.Text;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class FlagGUI {

	private static Map<UUID, FlagGUI> inventories = new HashMap<>();
	private Inventory inv;

	static ItemStack close = Itens.createItem(Material.OAK_DOOR, 1, "&cClose",
			Arrays.asList("&fClick here to close this menu."));

	private UUID uuid;
	private Region r;

	public FlagGUI(Player as, Region r) {
		this.uuid = as.getUniqueId();
		inv = Bukkit.getServer().createInventory(null, 45, Text.color("&8Real&eRegions &8| " + r.getDisplayName()));

		this.r = r;
		load();

		this.register();
	}

	public void load() {
		//row1

		inv.setItem(2, Itens.createItem(Material.DIAMOND_PICKAXE, 1, "&7&lBlock Breaking &r&7[" + getStyle(r.blockbreak) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows block breaking.", "&e&nPermissions",
						"  &aAllow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Block-Breaking.Allow",
						"  &cDisallow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Block-Breaking.Disallow",
						"&e","Click to change the value.")));
		inv.setItem(3, Itens.createItem(Material.GRASS_BLOCK, 1, "&7&lBlock Placing &r&7[" + getStyle(r.blockplace) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows block placing.", "&e&nPermissions",
						"  &aAllow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Block-Placing.Allow",
						"  &cDisallow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Block-Placing.Disallow",
						"&e","Click to change the value.")));
		inv.setItem(4, Itens.createItem(Material.STONE, 1, "&7&lBlock Interaction &r&7[" + getStyle(r.blockinteract) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows Block Interaction.", "&e&nPermissions",
						"  &aAllow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Block-Interaction.Allow",
						"  &cDisallow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Block-Interaction.Disallow",
						"&e","Click to change the value.")));
		inv.setItem(5, Itens.createItem(Material.DIAMOND_SWORD, 1, "&7&lPVP &r&7[" + getStyle(r.pvp) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows PVP (player vs player).", "&e&nPermissions",
						"  &aAllow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".PVP.Allow",
						"  &cDisallow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".PVP.Disallow",
						"&e","Click to change the value.")));
		inv.setItem(6, Itens.createItem(Material.IRON_SWORD, 1, "&7&lPVE &r&7[" + getStyle(r.pve) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows PVE (player vs entity).", "&e&nPermissions",
						"  &aAllow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".PVE.Allow",
						"  &cDisallow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".PVE.Disallow",
						"&e","Click to change the value.")));

		//row2

		inv.setItem(11, Itens.createItem(Material.CRAFTING_TABLE, 1, "&7&lCrafting &r&7[" + getStyle(r.acesscrafting) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows acess to Crafting Tables.", "&e&nPermissions",
						"  &aAllow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Crafting.Allow",
						"  &cDisallow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Crafting.Disallow",
						"&e","Click to change the value.")));
		inv.setItem(12, Itens.createItem(Material.CHEST, 1, "&7&lChests &r&7[" + getStyle(r.acesschests) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows chest interactions.", "&e&nPermissions",
						"  &aAllow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Chests.Allow",
						"  &cDisallow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Chests.Disallow",
						"&e","Click to change the value.")));
		inv.setItem(13, Itens.createItem(Material.HOPPER, 1, "&7&lHoppers &r&7[" + getStyle(r.acesshoppers) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows hopper interactions.", "&e&nPermissions",
						"  &aAllow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Hoppers.Allow",
						"  &cDisallow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Hoppers.Disallow",
						"&e","Click to change the value.")));
		inv.setItem(14, Itens.createItem(Material.ENDER_CHEST, 1, "&7&lContainer Interaction &r&7[" + getStyle(r.containerinteract) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows container interactions.", "&e&nPermissions",
						"  &aAllow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Containers.Allow",
						"  &cDisallow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Containers.Disallow",
						"&e","Click to change the value.")));
		inv.setItem(15, Itens.createItem(Material.CREEPER_SPAWN_EGG, 1, "&7&lEntity Spawning &r&7[" + getStyle(r.entityspawning) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows Entity Spawning.", "&e&nPermissions",
						"  &fNot applicable for Player.",
						"&e","Click to change the value.")));

		//row3

		inv.setItem(20, Itens.createItem(Material.BARRIER, 1, "&7&lEnter &r&7[" + getStyle(r.enter) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows player access to this region.", "&e&nPermissions",
						"  &aAllow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Enter.Allow",
						"  &cDisallow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Enter.Disallow",
						"&e","Click to change the value.")));
		inv.setItem(21, Itens.createItem(Material.TNT, 1, "&7&lExplosions &r&7[" + getStyle(r.explosions) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows explosions.", "&e&nPermissions",
						"  &fNot applicable for Player.",
						"&e","Click to change the value.")));
		inv.setItem(22, Itens.createItem(Material.COOKED_BEEF, 1, "&7&lHunger &r&7[" + getStyle(r.hunger) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows Hunger.", "&e&nPermissions",
						"  &aAllow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Hunger.Allow",
						"  &cDisallow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Hunger.Disallow",
						"&e","Click to change the value.")));
		inv.setItem(23, Itens.createItem(Material.DROPPER, 1, "&7&lItem Drop &r&7[" + getStyle(r.itemdrop) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows item drop.", "&e&nPermissions",
						"  &aAllow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Item-Drop.Allow",
						"  &cDisallow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Item-Drop.Disallow",
						"&e","Click to change the value.")));
		inv.setItem(24, Itens.createItem(Material.HOPPER_MINECART, 1, "&7&lItem Pickup &r&7[" + getStyle(r.itempickup) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows Entity Spawning.", "&e&nPermissions",
						"  &aAllow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Item-Pickup.Allow",
						"  &cDisallow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Item-Pickup.Disallow",
						"&e","Click to change the value.")));

		//row4
		inv.setItem(30, Itens.createItem(Material.FLINT_AND_STEEL, 1, "&7&lTake Damage &r&7[" + getStyle(r.takedamage) + "&7]",
				Arrays.asList("&e&nDescription", "  Allows or Disallows Damage.", "&e&nPermissions",
						"  &aAllow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Damage.Allow",
						"  &cDisallow&f: RealRegions." + r.getWorld().getName() + "." + r.getName() + ".Damage.Disallow",
						"&e","Click to change the value.")));
		inv.setItem(32, Itens.createItem(Material.SUNFLOWER, 1, "&7&lPriority &r&7[&b&l" + r.priority + "&r&7]",
				Arrays.asList("&e&nDescription", "  Region Priority over others.",
						"Click to change the value.")));
		inv.setItem(40, close);
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
			public void onClick(InventoryClickEvent e) {
				HumanEntity clicker = e.getWhoClicked();
				if (clicker instanceof Player) {
					Player p = (Player) clicker;
					if (e.getCurrentItem() == null) {
						return;
					}
					UUID uuid = clicker.getUniqueId();
					if (inventories.containsKey(uuid)) {
						FlagGUI current = inventories.get(uuid);
						if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
							return;
						}

						e.setCancelled(true);


						switch (e.getRawSlot())
						{
							case 40:
								p.closeInventory();
								new BukkitRunnable()
								{
									public void run()
									{
										WorldGUI mp = new WorldGUI(p, current.r.getWorld());
										mp.openInventory(p);
									}
								}.runTaskLater(RealRegions.getPL(), 2);
								break;
							case 2:
								current.r.blockbreak = !current.r.blockbreak;
								current.r.saveData(Data.Region.FLAGS);
								current.load();
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								break;
							case 3:
								current.r.blockplace = !current.r.blockplace;
								current.r.saveData(Data.Region.FLAGS);
								current.load();
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								break;
							case 4:
								current.r.blockinteract = !current.r.blockinteract;
								current.r.saveData(Data.Region.FLAGS);
								current.load();
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								break;
							case 5:
								current.r.pvp = !current.r.pvp;
								current.r.saveData(Data.Region.FLAGS);
								current.load();
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								break;
							case 6:
								current.r.pve = !current.r.pve;
								current.r.saveData(Data.Region.FLAGS);
								current.load();
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								break;
							case 11:
								current.r.acesscrafting = !current.r.acesscrafting;
								current.r.saveData(Data.Region.FLAGS);
								current.load();
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								break;
							case 12:
								current.r.acesschests = !current.r.acesschests;
								current.r.saveData(Data.Region.FLAGS);
								current.load();
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								break;
							case 13:
								current.r.acesshoppers = !current.r.acesshoppers;
								current.r.saveData(Data.Region.FLAGS);
								current.load();
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								break;
							case 14:
								current.r.containerinteract = !current.r.containerinteract;
								current.r.saveData(Data.Region.FLAGS);
								current.load();
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								break;
							case 15:
								current.r.entityspawning = !current.r.entityspawning;
								current.r.saveData(Data.Region.FLAGS);
								current.load();
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								break;
							case 20:
								current.r.enter = !current.r.enter;
								current.r.saveData(Data.Region.FLAGS);
								current.load();
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								break;
							case 21:
								current.r.explosions = !current.r.explosions;
								current.r.saveData(Data.Region.FLAGS);
								current.load();
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								break;
							case 22:
								current.r.hunger = !current.r.hunger;
								current.r.saveData(Data.Region.FLAGS);
								current.load();
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								break;
							case 23:
								current.r.itemdrop = !current.r.itemdrop;
								current.r.saveData(Data.Region.FLAGS);
								current.load();
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								break;
							case 24:
								current.r.itempickup = !current.r.itempickup;
								current.r.saveData(Data.Region.FLAGS);
								current.load();
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								break;
							case 30:
								current.r.takedamage = !current.r.takedamage;
								current.r.saveData(Data.Region.FLAGS);
								current.load();
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
								break;
							case 32:
								new PlayerInput(p, new PlayerInput.InputRunnable() {
									@Override
									public void run(String input) {
										//continue

										if (!StringUtils.isNumeric(input))
										{
											Text.send(p, "&fInput is &cnot &fa number.");
											FlagGUI wv = new FlagGUI(p, current.r);
											wv.openInventory(p);
											return;
										}

										current.r.setPriority(Integer.valueOf(input));
										current.r.saveData(Data.Region.SETTINGS);
										Text.send(p, "&fPriority changed to " + Text.color(input));
										new BukkitRunnable() {
											public void run() {
												FlagGUI wv = new FlagGUI(p, current.r);
												wv.openInventory(p);
											}
										}.runTaskLater(RealRegions.getPL(), 2);
									}
								}, new PlayerInput.InputRunnable() {
									@Override
									public void run(String input) {
										FlagGUI wv = new FlagGUI(p, current.r);
										wv.openInventory(p);
									}
								});
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
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