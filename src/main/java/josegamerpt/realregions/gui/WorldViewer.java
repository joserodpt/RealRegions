package josegamerpt.realregions.gui;

import java.util.*;

import josegamerpt.realregions.RealRegions;
import josegamerpt.realregions.classes.PickType;
import josegamerpt.realregions.classes.RWorld;
import josegamerpt.realregions.managers.WorldManager;
import josegamerpt.realregions.utils.Itens;
import josegamerpt.realregions.utils.Pagination;
import josegamerpt.realregions.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
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

public class WorldViewer {

	private static Map<UUID, WorldViewer> inventories = new HashMap<>();
	private Inventory inv;

	static ItemStack placeholder = Itens.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "&7Worlds");
	static ItemStack next = Itens.createItem(Material.GREEN_STAINED_GLASS, 1, "&aNext",
			Collections.singletonList("&fClick here to go to the next page."));
	static ItemStack back = Itens.createItem(Material.YELLOW_STAINED_GLASS, 1, "&6Back",
			Collections.singletonList("&fClick here to go back to the next page."));
	static ItemStack close = Itens.createItem(Material.OAK_DOOR, 1, "&cClose",
			Collections.singletonList("&fClick here to close this menu."));

	private UUID uuid;
	private ArrayList<RWorld> worlds;
	private HashMap<Integer, RWorld> display = new HashMap<>();

	int pageNumber = 0;
	Pagination<RWorld> p;

	public WorldViewer(Player as) {
		this.uuid = as.getUniqueId();
		inv = Bukkit.getServer().createInventory(null, 54, Text.color("&8Real&eRegions &8| &aWorlds"));

		load();

		this.register();
	}

	public void load() {
		worlds = WorldManager.getWorlds();

		p = new Pagination<>(21, worlds);
		fillChest(p.getPage(pageNumber));
	}

	public void fillChest(List<RWorld> items) {

		inv.clear();
		display.clear();

		for (int i = 10; i < 35; i++) {
			if (i != 18 && i != 27 && i != 17 && i != 26) {
				if (items.size() != 0) {
					RWorld wi = items.get(0);
					inv.setItem(i, wi.getItem());
					display.put(i, wi);
					items.remove(0);
				} else {
                    inv.setItem(i, placeholder);
                }
			}
		}

		inv.setItem(51, next);
		inv.setItem(47, back);
		inv.setItem(49, close);
	}

	public void openInventory(Player target) {
		Inventory inv = getInventory();
		Inventory openTop = target.getOpenInventory().getTopInventory();
		if (openTop != null && openTop.getType().name().equalsIgnoreCase(inv.getType().name())) {
			openTop.setContents(inv.getContents());
		} else {
			target.openInventory(inv);
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
						WorldViewer current = inventories.get(uuid);
						if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
							return;
						}

						e.setCancelled(true);

						if (e.getRawSlot() == 49) {
							p.closeInventory();
						}

						if (e.getRawSlot() == 51) {
							nextPage(current);
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
						}
						if (e.getRawSlot() == 47) {
							backPage(current);
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
						}

						if (current.display.containsKey(e.getRawSlot())) {

							RWorld a = current.display.get(e.getRawSlot());
							switch (e.getClick())
							{
								case RIGHT:
									a.teleport(p, false);
									break;
                                case MIDDLE:
                                    p.closeInventory();
                                    new BukkitRunnable()
                                    {
                                        public void run()
                                        {
                                            MaterialPicker mp = new MaterialPicker(a, p, PickType.ICON_WORLD);
                                            mp.openInventory(p);
                                        }
                                    }.runTaskLater(RealRegions.getPL(), 2);
                                    break;
								default:
									p.closeInventory();
                                    new BukkitRunnable()
                                    {
                                        public void run()
                                        {
                                            WorldGUI v = new WorldGUI(p, a);
                                            v.openInventory(p);
                                        }
                                    }.runTaskLater(RealRegions.getPL(), 2);
									break;
							}
						}
					}
				}
			}

			private void backPage(WorldViewer asd) {
				if (asd.p.exists(asd.pageNumber - 1)) {
					asd.pageNumber--;
				}

				asd.fillChest(asd.p.getPage(asd.pageNumber));
			}

			private void nextPage(WorldViewer asd) {
				if (asd.p.exists(asd.pageNumber + 1)) {
					asd.pageNumber++;
				}

				asd.fillChest(asd.p.getPage(asd.pageNumber));
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