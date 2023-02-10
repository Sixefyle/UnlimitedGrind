package be.sixefyle.items.passifs.interfaces;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public interface OnReceiveDamage {
    void onGetDamage(EntityDamageEvent e, Player player, ItemStack armor);
}
