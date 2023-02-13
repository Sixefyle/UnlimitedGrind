package be.sixefyle.items.passifs.interfaces;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface OnEquip {
    void onEquip(Player player, ItemStack item);
    void onUnequip(Player player, ItemStack item);
}
