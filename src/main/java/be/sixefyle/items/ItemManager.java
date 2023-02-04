package be.sixefyle.items;

import be.sixefyle.UnlimitedGrind;
import be.sixefyle.items.passifs.OnHit;
import be.sixefyle.items.passifs.Passif;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ItemManager implements Listener {
    @EventHandler
    public void onDoDamage(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        if(damager instanceof Player player){
            ItemStack item = player.getInventory().getItemInMainHand();
            ItemMeta itemMeta = item.getItemMeta();
            NamespacedKey passifsKey = new NamespacedKey(UnlimitedGrind.getInstance(), "passifIdArray");
            if(itemMeta.getPersistentDataContainer().has(passifsKey)) {
                int[] passifIDs = itemMeta.getPersistentDataContainer().get(passifsKey, PersistentDataType.INTEGER_ARRAY);

                for (int passifID : passifIDs) {
                    if(Passif.getByID(passifID).getPassif() instanceof OnHit onHitPassif){
                        onHitPassif.doDamage(e);
                    }
                }
            }
        }
    }
}
