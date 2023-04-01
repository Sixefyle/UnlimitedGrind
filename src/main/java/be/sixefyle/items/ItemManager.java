package be.sixefyle.items;

import be.sixefyle.UGPlayer;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.enums.ComponentColor;
import be.sixefyle.event.OnSmithingCraftEvent;
import be.sixefyle.items.passifs.interfaces.OnEquip;
import be.sixefyle.items.passifs.interfaces.OnMeleeHit;
import be.sixefyle.items.passifs.Passif;
import be.sixefyle.items.passifs.interfaces.OnReceiveDamage;
import be.sixefyle.utils.NumberUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ItemManager implements Listener {

    public final static int MAX_POWER = 5000;
    public final static int MAX_OFFSET_POWER = 100;

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

    public static UGItem generateItem(double power, DropTable dropTable, Rarity rarity){
        double minPower = Math.min(power - (power % MAX_OFFSET_POWER), MAX_POWER - MAX_OFFSET_POWER);
        double maxPower = Math.min(minPower + MAX_OFFSET_POWER, MAX_POWER);

        switch (rarity){
            case LEGENDARY -> maxPower += MAX_OFFSET_POWER / 2.0;
            case MYTHIC -> maxPower += MAX_OFFSET_POWER;
        }
        double itemPower = NumberUtils.getRandomNumber(minPower, maxPower);

        List<Passif> itemPassifList = new ArrayList<>();
        String itemPrefix = null;
        if(rarity.equals(Rarity.LEGENDARY) || rarity.equals(Rarity.MYTHIC)){
            Passif passif = Passif.values()[(int) (Math.random() * Passif.values().length)];
            ItemCategory currentPassifCategory = passif.getItemCategory();

            while(!canAddPassif(passif, rarity, currentPassifCategory, dropTable.getItemCategory())) {
                passif = Passif.values()[(int) (Math.random() * Passif.values().length)];
                currentPassifCategory = passif.getItemCategory();
            }
            itemPrefix = passif.getItemPassif().getItemPrefixName();
            itemPassifList.add(passif);
        }

        return new UGItem(dropTable.getMaterial(), rarity, null, itemPrefix, null, null, itemPower, itemPassifList);
    }

    public static UGItem generateRandomItem(ItemCategory itemCategory, double power, Rarity rarity){
        DropTable[] dropTables = DropTable.values();
        DropTable[] lootableItems = Arrays.stream(dropTables)
                .filter(DropTable::canBeLoot)
                .filter(dt -> dt.getItemCategory().equals(itemCategory))
                .toArray(DropTable[]::new);

        DropTable itemType;
        if (lootableItems.length > 0) {
            itemType = lootableItems[ThreadLocalRandom.current().nextInt(lootableItems.length)];
        } else {
            itemType = DropTable.WOODEN_SWORD;
        }
        return generateItem(power, itemType, rarity);
    }

    public static UGItem generateRandomItem(ItemCategory itemCategory, double power){
        Rarity rarity = Rarity.getRandomRarity();
        return generateRandomItem(itemCategory, power, rarity);
    }

    public static UGItem generateRandomItem(double power){
        DropTable itemType = DropTable.values()[(int) (Math.random() * DropTable.values().length)];
        ItemCategory itemCategory = itemType.getItemCategory();
        return generateRandomItem(itemCategory, power);
    }

    public static int[] getItemPassifArray(ItemStack item) {
        if(item == null) return null;
        ItemMeta itemMeta = item.getItemMeta();
        NamespacedKey passifsKey = new NamespacedKey(UnlimitedGrind.getInstance(), "passifIdArray");
        int[] passifIDs = null;
        if(itemMeta != null && itemMeta.getPersistentDataContainer().has(passifsKey)) {
            passifIDs = itemMeta.getPersistentDataContainer().get(passifsKey, PersistentDataType.INTEGER_ARRAY);
        }
        return passifIDs;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDoDamage(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        if(damager instanceof Player player){
            ItemStack item = player.getInventory().getItemInMainHand();
            int[] passifIDs = getItemPassifArray(item);
            if(passifIDs != null){
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
            UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
            for (ItemStack itemStack : ugPlayer.getArmorAndOffHand()) {
                if(itemStack == null) continue;

                int[] passifIDs = getItemPassifArray(itemStack);
                if(passifIDs != null){
                    for (int passifID : passifIDs) {
                        if(Passif.getByID(passifID).getItemPassif() instanceof OnReceiveDamage onReceiveDamage){
                            onReceiveDamage.onGetDamage(e, player, itemStack);
                        }
                    }
                }
            }
        }
    }

    private static void onEquip(int[] passifIDs, Player player, ItemStack item){
        if(passifIDs != null){
            for (int passifID : passifIDs) {
                if(Passif.getByID(passifID).getItemPassif() instanceof OnEquip onEquip){
                    onEquip.onEquip(player, item);
                }
            }
        }
    }

    private static void onUnequip(int[] passifIDs, Player player, ItemStack item){
        if(passifIDs != null){
            for (int passifID : passifIDs) {
                if(Passif.getByID(passifID).getItemPassif() instanceof OnEquip onEquip){
                    onEquip.onUnequip(player, item);
                }
            }
        }
    }

    public static void doOnEquipForEquippedItems(Player player){
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        int[] passifIDs = getItemPassifArray(heldItem);
        onEquip(passifIDs, player, heldItem);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChangeHeldItem(PlayerItemHeldEvent e){
        if(e.isCancelled()) return;

        ItemStack newItem = e.getPlayer().getInventory().getItem(e.getNewSlot());
        ItemStack oldItem = e.getPlayer().getInventory().getItem(e.getPreviousSlot());
        int[] passifIDs = getItemPassifArray(newItem);
        int[] oldItemPassifIDs = getItemPassifArray(oldItem);
        onUnequip(oldItemPassifIDs, e.getPlayer(), newItem);
        onEquip(passifIDs, e.getPlayer(), newItem);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDropItem(PlayerDropItemEvent e){
        ItemStack droppedItem = e.getItemDrop().getItemStack();
        int[] passifIDs = getItemPassifArray(droppedItem);
        onUnequip(passifIDs, e.getPlayer(), droppedItem);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void doItemPassifOnPlayerJoin(PlayerJoinEvent e){
        //fuck it
        Bukkit.getScheduler().runTaskLater(UnlimitedGrind.getInstance(), () -> {
            doOnEquipForEquippedItems(e.getPlayer());
        }, 10);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickupItem(EntityPickupItemEvent e){
        // wait 1 tick to be sure that the player have received the item in his inventory
        Bukkit.getScheduler().runTaskLater(UnlimitedGrind.getInstance(), () -> {
            if(e.getEntity() instanceof Player player){
                ItemStack pickedItem = player.getInventory().getItemInMainHand();
                int[] passifIDs = getItemPassifArray(pickedItem);
                onEquip(passifIDs, player, pickedItem);
            }
        }, 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCraftItem(CraftItemEvent e){
        try{
            if(e.getCurrentItem() == null) return;
            Material craftedItemMaterial = e.getCurrentItem().getType();

            Player player = (Player) e.getWhoClicked();
            UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
            UGItem ugItem = ItemManager.generateItem(ugPlayer.getMaxPower(), DropTable.valueOf(craftedItemMaterial.name()), Rarity.getRandomRarity());

            e.setCurrentItem(ugItem.asItemStack());
        }catch (IllegalArgumentException ignore){}
    }

    @EventHandler
    public void onSmithingCraft(SmithItemEvent e){
        ItemStack result = e.getCurrentItem();
        if(result == null) return;

        UGItem ugItem = UGItem.getFromItemStack(result);
        ugItem.createItem(result.getType(), List.of("We can always count on netherite..."));
        e.setCurrentItem(ugItem.asItemStack());
    }

    @EventHandler
    public void onPrepareSmithin(PrepareSmithingEvent e){
        ItemStack item = e.getResult();
        if(item == null) return;
        UGItem ugItem = UGItem.getFromItemStack(item);
        if(ugItem == null) return;

        ItemMeta itemMeta = item.getItemMeta();
        List<Component> lore = itemMeta.lore();
        if(lore == null) return;

        lore.clear();
        lore.add(Component.text(""));
        lore.add(Component.text("Upgrading to netherite maxes out primary").color(ComponentColor.ARMOR.getColor()));
        lore.add(Component.text("stats and reforge secondary stats").color(ComponentColor.ARMOR.getColor()));
        lore.add(Component.text("but dedicated stat from the original").color(ComponentColor.ARMOR.getColor()));
        lore.add(Component.text("item will be lost.").color(ComponentColor.ARMOR.getColor()));

        itemMeta.lore(lore);
        item.setItemMeta(itemMeta);
    }
}
