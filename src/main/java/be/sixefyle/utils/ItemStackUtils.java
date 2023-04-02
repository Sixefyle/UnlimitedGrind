package be.sixefyle.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemStackUtils {

    public static ItemStack createItem(Material type, Component name, List<Component> lore){
        ItemStack item = new ItemStack(type);
        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.displayName(name);
        itemMeta.lore(lore);

        item.setItemMeta(itemMeta);
        return item;
    }
}
