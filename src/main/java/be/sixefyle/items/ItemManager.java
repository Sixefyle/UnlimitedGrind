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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ItemManager implements Listener {

    public static boolean canAddPassif(Passif passif, Rarity rarity, ItemCategory passifCategory, ItemCategory itemCategory){
        if(passif.getRequiredRarity() == null || passif.getRequiredRarity().equals(rarity)){
            if(passifCategory.equals(itemCategory) ||
                    (itemCategory.equals(ItemCategory.MELEE) && passifCategory.equals(ItemCategory.MELEE_DISTANCE)) ||
                    (itemCategory.equals(ItemCategory.DISTANCE) && passifCategory.equals(ItemCategory.MELEE_DISTANCE))) {
                return true;
            }
        }
        return false;
    }

    public static UGItem generateRandomItem(ItemCategory itemCategory, double power){
        double minPower = power - (power % 100);
        double maxPower = minPower + 300;
        double itemPower = NumberUtils.getRandomNumber(minPower, maxPower);

        DropTable itemType = DropTable.values()[(int) (Math.random() * DropTable.values().length)];
        ItemCategory currentItemCategory = itemType.getItemCategory();
        while(!currentItemCategory.equals(itemCategory)){
            itemType = DropTable.values()[(int) (Math.random() * DropTable.values().length)];
            currentItemCategory = itemType.getItemCategory();
        }

        Rarity rarity = Rarity.getRandomRarity();
        List<Passif> itemPassifList = new ArrayList<>();
        if(rarity.equals(Rarity.LEGENDARY) || rarity.equals(Rarity.MYTHIC)){
            Passif passif = Passif.values()[(int) (Math.random() * Passif.values().length)];
            ItemCategory currentPassifCategory = passif.getItemCategory();

            while(!canAddPassif(passif, rarity, currentPassifCategory, currentItemCategory)) {
                passif = Passif.values()[(int) (Math.random() * Passif.values().length)];
                currentPassifCategory = passif.getItemCategory();
            }

            itemPassifList.add(passif);
        }

        return new UGItem(itemType.getMaterial(), rarity, null, null, itemPower, itemPassifList);
    }

    public static UGItem generateRandomItem(double power){
        DropTable itemType = DropTable.values()[(int) (Math.random() * DropTable.values().length)];
        ItemCategory itemCategory = itemType.getItemCategory();
        return generateRandomItem(itemCategory, power);
    }

    @EventHandler(priority = EventPriority.LOW)
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
                        onHitPassif.doDamage(e, player);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onReceiveDamage(EntityDamageEvent e){
        Entity entity = e.getEntity();
        if(entity instanceof Player player){
            ItemStack[] item = new ItemStack[5];
            item[0] = player.getInventory().getHelmet();
            item[1] = player.getInventory().getChestplate();
            item[2] = player.getInventory().getLeggings();
            item[3] = player.getInventory().getBoots();
            item[4] = player.getInventory().getItemInOffHand();
            for (ItemStack itemStack : item) {
                if(itemStack == null) continue;

                ItemMeta itemMeta = itemStack.getItemMeta();
                NamespacedKey passifsKey = new NamespacedKey(UnlimitedGrind.getInstance(), "passifIdArray");
                if(itemMeta != null && itemMeta.getPersistentDataContainer().has(passifsKey)) {
                    int[] passifIDs = itemMeta.getPersistentDataContainer().get(passifsKey, PersistentDataType.INTEGER_ARRAY);
                    for (int passifID : passifIDs) {
                        if(Passif.getByID(passifID).getItemPassif() instanceof OnReceiveDamage onReceiveDamage){
                            onReceiveDamage.onGetDamage(e, player, itemStack);
                        }
                    }
                }
            }
        }
    }
}
