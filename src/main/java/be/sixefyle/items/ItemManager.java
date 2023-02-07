package be.sixefyle.items;

import be.sixefyle.UnlimitedGrind;
import be.sixefyle.items.passifs.interfaces.OnMeleeHit;
import be.sixefyle.items.passifs.Passif;
import be.sixefyle.items.passifs.interfaces.OnReceiveDamage;
import be.sixefyle.utils.NumberUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ItemManager implements Listener {
    public static UGItem generateRandomItem(double power){
        double minPower = power - (power % 100);
        double maxPower = minPower + 300;
        double itemPower = NumberUtils.getRandomNumber(minPower, maxPower);

        DropTable itemType = DropTable.values()[(int) (Math.random() * DropTable.values().length)];
        Rarity rarity = Rarity.getRandomRarity();
        List<Passif> itemPassifList = new ArrayList<>();
        if(rarity.equals(Rarity.LEGENDARY) || rarity.equals(Rarity.MYTHIC)){
            Passif passif = Passif.values()[(int) (Math.random() * Passif.values().length)];
            ItemCategory passifCategory = passif.getItemCategory();
            ItemCategory itemCategory = itemType.getItemCategory();

            while(!(passifCategory.equals(itemCategory) ||
                    (itemCategory.equals(ItemCategory.MELEE) && passifCategory.equals(ItemCategory.MELEE_DISTANCE)) ||
                    (itemCategory.equals(ItemCategory.DISTANCE) && passifCategory.equals(ItemCategory.MELEE_DISTANCE)))) {
                passif = Passif.values()[(int) (Math.random() * Passif.values().length)];
                passifCategory = passif.getItemCategory();
            }

            itemPassifList.add(passif);
        }

        return new UGItem(itemType.getMaterial(), rarity, null, null, itemPower, itemPassifList);
    }

    @EventHandler
    public void onDoDamage(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        if(damager instanceof Player player){
            ItemStack item = player.getInventory().getItemInMainHand();
            ItemMeta itemMeta = item.getItemMeta();
            NamespacedKey passifsKey = new NamespacedKey(UnlimitedGrind.getInstance(), "passifIdArray");
            if(itemMeta != null && itemMeta.getPersistentDataContainer().has(passifsKey)) {
                int[] passifIDs = itemMeta.getPersistentDataContainer().get(passifsKey, PersistentDataType.INTEGER_ARRAY);

                for (int passifID : passifIDs) {
                    if(Passif.getByID(passifID).getItemPassif() instanceof OnMeleeHit onHitPassif){
                        onHitPassif.doDamage(e);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onReceiveDamage(EntityDamageEvent e){
        Entity entity = e.getEntity();
        if(entity instanceof Player player){
            ItemStack[] item = player.getInventory().getArmorContents();
            for (ItemStack itemStack : item) {
                ItemMeta itemMeta = itemStack.getItemMeta();
                NamespacedKey passifsKey = new NamespacedKey(UnlimitedGrind.getInstance(), "passifIdArray");
                if(itemMeta != null && itemMeta.getPersistentDataContainer().has(passifsKey)) {
                    int[] passifIDs = itemMeta.getPersistentDataContainer().get(passifsKey, PersistentDataType.INTEGER_ARRAY);

                    for (int passifID : passifIDs) {
                        if(Passif.getByID(passifID).getItemPassif() instanceof OnReceiveDamage onReceiveDamage){
                            onReceiveDamage.onGetDamage(e);
                        }
                    }
                }
            }
        }
    }
}
