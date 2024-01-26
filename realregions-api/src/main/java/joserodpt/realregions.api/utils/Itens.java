package joserodpt.realregions.api.utils;

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

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class Itens {

    public static ItemStack createItem(Material material, int quantidade, String nome) {
        ItemStack item = new ItemStack(material, quantidade);
        ItemMeta meta = item.getItemMeta();
        if (nome != null) {
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