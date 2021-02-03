package josegamerpt.realregions.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class Itens {

	public static ItemStack createItem(Material material, int quantidade, String nome) {
		ItemStack item = new ItemStack(material, quantidade);
		ItemMeta meta = item.getItemMeta();
		if (nome == null) {
		} else {
			meta.setDisplayName(Text.color(nome));
		}
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack createItem(Material material, int quantidade, String nome, List<String> desc) {
		ItemStack item = new ItemStack(material, quantidade);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', nome));
		meta.setLore(Text.color(desc));
		item.setItemMeta(meta);
		return item;
	}

}