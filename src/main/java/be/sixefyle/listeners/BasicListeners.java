package be.sixefyle.listeners;

import be.sixefyle.UGPlayer;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.enums.ComponentColor;
import be.sixefyle.items.ItemAction;
import be.sixefyle.items.ItemCategory;
import be.sixefyle.items.UGItem;
import be.sixefyle.utils.HologramUtils;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.User;
import com.jeff_media.armorequipevent.ArmorEquipEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BasicListeners implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        UGPlayer ugPlayer;
        if (UGPlayer.playerMap.get(player.getUniqueId()) == null) {
            ugPlayer = new UGPlayer(e.getPlayer());
            ugPlayer.respawn();
            for (ItemStack content : player.getInventory().getContents()) {
                if(content == null) continue;
                UGItem ugItem = UGItem.getFromItemStack(content);
                if(ugItem == null) continue;

                ugItem.updateLore();
                ugItem.updateConditionLore(ugPlayer);
            }
            ugPlayer.setupStatsFromEquippedItems();

            //Wait 10 ticks to be sure that all before connecting stuff can be do
            Bukkit.getScheduler().runTaskLater(UnlimitedGrind.getInstance(), () -> {
                ugPlayer.setConnecting(false);
            }, 10);
        }
    }

    @EventHandler
    public void changeBlockDropLoc(BlockDropItemEvent e){
        Player player = e.getPlayer();
        for (Item item : e.getItems()){
            item.teleport(player);
        }
    }

    @EventHandler
    public void onEntitySpawn(CreatureSpawnEvent e){
        if(e.isCancelled()) return;
        if(e.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER)) return;

        LivingEntity damageable = e.getEntity();
        @NotNull Optional<Island> island = IridiumSkyblockAPI.getInstance().getIslandViaLocation(damageable.getLocation());
        if(island.isEmpty()) return;

        UGPlayer ugPlayer;
        double maxPower = 0;
        for (User member : island.get().getMembers()) {
            if(member.getPlayer() == null) continue;

            ugPlayer = UGPlayer.GetUGPlayer(member.getPlayer());
            if(maxPower < ugPlayer.getWearedPower()){
                maxPower = ugPlayer.getWearedPower();
            }
        }

        double newHealth = damageable.getMaxHealth()+(damageable.getMaxHealth()*(maxPower/50))*(Math.pow(maxPower,.78)/100+1); //TODO: magic number

        damageable.setMaxHealth(newHealth);
        damageable.setHealth(newHealth);

        HologramUtils.createEntInfoFollow(damageable);

        damageable.setCustomNameVisible(false);
        damageable.setMetadata("power", new FixedMetadataValue(UnlimitedGrind.getInstance(), maxPower));
        damageable.setMaximumNoDamageTicks(3);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        Player player = e.getPlayer();
        UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
        for (Attribute value : Attribute.values()) {
            if(player.getAttribute(value) == null) continue;
            for (AttributeModifier modifier : player.getAttribute(value).getModifiers()) {
                player.getAttribute(value).removeModifier(modifier);
            }
        }

        ugPlayer.leaveGroup();
        UGPlayer.RemoveUGPlayer(player);
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent e){
        Player player = e.getPlayer();
        ItemStack item = e.getItemDrop().getItemStack();
        UGItem ugItem = UGItem.getFromItemStack(item);

        if(ugItem == null) return;
        if(ugItem.getItemCategories().equals(ItemCategory.ARMOR)) return;

        ugItem.createRarityParticle(e.getItemDrop());
        UGPlayer.GetUGPlayer(player).updateStatsFromItem(null, ugItem, ItemAction.DROP);
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent e){
        if(e.getEntity() instanceof Player player){
            ItemStack itemStack = e.getItem().getItemStack();
            UGItem ugItem = UGItem.getFromItemStack(itemStack);

            if(ugItem == null) return;
            if(ugItem.getItemCategories().equals(ItemCategory.ARMOR)) return;

            Bukkit.getScheduler().runTaskLater(UnlimitedGrind.getInstance(), () -> {
                if(player.getInventory().getItemInMainHand().equals(itemStack)){
                    UGPlayer.GetUGPlayer(player).updateStatsFromItem(ugItem, null, ItemAction.PICKUP);
                }
            }, 1);

            ugItem.updateConditionLore(UGPlayer.GetUGPlayer(player));
        }
    }

    @EventHandler
    public void onItemEnchant(EnchantItemEvent e){
        UGItem ugItem = UGItem.getFromItemStack(e.getItem());
        if(ugItem == null) return;
        Bukkit.getScheduler().runTaskLater(UnlimitedGrind.getInstance(), ugItem::updateLore, 1);
    }

    @EventHandler
    public void onPlayerRegenHealth(EntityRegainHealthEvent e){
        if(e.getEntity() instanceof Player player){
            UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
            if(e.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.SATIATED)){
                ugPlayer.regenHealth(.01 * ugPlayer.getMaxHealth());
            } else {
                ugPlayer.regenHealth(e.getAmount());
            }

            e.setCancelled(true);
        }
    }

//    @EventHandler
//    public void onEquipArmor(PlayerArmorChangeEvent e){
//        UGPlayer ugPlayer = UGPlayer.GetUGPlayer(e.getPlayer());
//        if(ugPlayer == null) return;
//        UGItem newItem = UGItem.getFromItemStack(e.getNewItem());
//        UGItem oldItem = UGItem.getFromItemStack(e.getOldItem());
//        ugPlayer.updateStatsFromItem(newItem, oldItem);
//
//        ugPlayer.updateWearedPower();
//        ugPlayer.updateBonusHealthFromStats();
//    }

    @EventHandler
    public void onPlayerDie(PlayerDeathEvent e){
        e.setCancelled(true);
    }

    @EventHandler
    public void onTakeItemInHand(PlayerItemHeldEvent e){
        Player player = e.getPlayer();
        ItemStack newItemStack = player.getInventory().getItem(e.getNewSlot());
        ItemStack oldItemStack = player.getInventory().getItem(e.getPreviousSlot());

        UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
        UGItem newItem = UGItem.getFromItemStack(newItemStack);
        UGItem oldItem = UGItem.getFromItemStack(oldItemStack);

        if(ugPlayer.canEquipItem(newItem)){
            ugPlayer.updateWearedPower(newItem, oldItem, ItemAction.HOLD);
            ugPlayer.updateStatsFromItem(newItem, oldItem, ItemAction.HOLD);
        } else if(newItem.getItemCategories().equals(ItemCategory.MELEE) || newItem.getItemCategories().equals(ItemCategory.DISTANCE)) {
            e.setCancelled(true);
            player.sendMessage(
                    Component.text(UnlimitedGrind.getInstance().getConfig().getString("lang.item.error.notEnoughPower"))
                            .color(ComponentColor.ERROR.getColor()));
        }
    }

    private boolean equipOffHand(Player player, ItemStack newItemStack, ItemStack oldItemStack){
        UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
        UGItem newItem = UGItem.getFromItemStack(newItemStack);
        UGItem oldItem = UGItem.getFromItemStack(oldItemStack);

        if(ugPlayer.canEquipItem(newItem)){
            ugPlayer.updateWearedPower(newItem, oldItem, ItemAction.HOLD_OFF_HAND);
            ugPlayer.updateStatsFromItem(newItem, oldItem, ItemAction.HOLD_OFF_HAND);
        } else {
            player.sendMessage(
                    Component.text(UnlimitedGrind.getInstance().getConfig().getString("lang.item.error.notEnoughPower"))
                            .color(ComponentColor.ERROR.getColor()));
            return true;
        }
        return false;
    }

    @EventHandler
    public void handItemSwap(PlayerSwapHandItemsEvent e){
        e.setCancelled(equipOffHand(e.getPlayer(), e.getOffHandItem(), e.getMainHandItem()));
    }

    @EventHandler
    public void onOffHandEquip(InventoryClickEvent e){
        int slot = e.getSlot();
        ItemStack currentItem = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();
        Inventory inv = e.getInventory();

        if(slot == 40){
            e.setCancelled(equipOffHand(player, e.getCursor(), currentItem));
        }

        if(e.isShiftClick() && currentItem != null && currentItem.getType() == Material.SHIELD){
            e.setCancelled(equipOffHand(player, currentItem, inv.getItem(40)));
        }
    }


    @EventHandler
    public void onOffHandEquipDrag(InventoryDragEvent e){
        for (Integer inventorySlot : e.getInventorySlots()) {
            if(inventorySlot == 40){
                e.setCancelled(equipOffHand((Player) e.getWhoClicked(), e.getOldCursor(), e.getCursor()));
            }
        }
    }

    @EventHandler
    public void onEquipArmor(PlayerArmorChangeEvent e){
        UGPlayer ugPlayer = UGPlayer.GetUGPlayer(e.getPlayer());
        if(ugPlayer.isConnecting()){
            return;
        }

        ItemStack newItem = e.getNewItem();
        ItemStack oldItem = e.getOldItem();

        UGItem ugNewItem = UGItem.getFromItemStack(newItem);
        UGItem ugOldItem = UGItem.getFromItemStack(oldItem);

        if(ugPlayer.canEquipItem(ugNewItem)) {
            ugPlayer.updateStatsFromItem(ugNewItem, ugOldItem, ItemAction.EQUIP);
            ugPlayer.updateEquippedWearedPower();

            Player player = e.getPlayer();

            Bukkit.getScheduler().runTaskLater(UnlimitedGrind.getInstance(), () -> {
                for (AttributeModifier modifier : player.getAttribute(Attribute.GENERIC_ARMOR).getModifiers()) {
                    player.getAttribute(Attribute.GENERIC_ARMOR).removeModifier(modifier);
                }
            },1);
        }
    }

    @EventHandler
    public void onTryEquipArmor(ArmorEquipEvent e){
        ItemStack newItem = e.getNewArmorPiece();
        if(newItem == null) return;

        UGItem ugNewItem = UGItem.getFromItemStack(newItem);
        if(ugNewItem == null) return;

        UGPlayer ugPlayer = UGPlayer.GetUGPlayer(e.getPlayer());

        if(!ugPlayer.canEquipItem(ugNewItem)){
            e.setCancelled(true);
            e.getPlayer().sendMessage(
                    Component.text(UnlimitedGrind.getInstance().getConfig().getString("lang.item.error.notEnoughPower"))
                            .color(ComponentColor.ERROR.getColor()));
        }
    }

    @EventHandler
    public void onBlockDispenseArmor(BlockDispenseArmorEvent e){
        e.setCancelled(true);
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent e){
        e.setCancelled(true);
        e.setFoodLevel(20);
    }

    @EventHandler
    public void updateItemOnOpenInventory(InventoryOpenEvent e){
        UGItem ugItem;
        for (ItemStack content : e.getInventory().getContents()) {
            if(content == null) continue;
            ugItem = UGItem.getFromItemStack(content);
            if(ugItem == null) continue;

            ugItem.updateLore();
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if(e.getClickedInventory() == null) return;

        if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            ItemStack item = e.getCurrentItem();
            if(item == null) return;
            if(e.getClickedInventory().equals(player.getInventory())){
                if(player.getInventory().equals(e.getClickedInventory())) return;

                UGItem ugItem = UGItem.getFromItemStack(item);
                if(ugItem == null) return;

                ugItem.updateLore();
            } else {
                UGItem ugItem = UGItem.getFromItemStack(item);
                if(ugItem == null) return;

                ugItem.updateConditionLore(UGPlayer.GetUGPlayer(player));
            }
        } else if(e.getAction() == InventoryAction.PLACE_ALL) {
            ItemStack item = e.getCursor();
            if(item == null) return;

            if(e.getClickedInventory().equals(player.getInventory())){
                UGItem ugItem = UGItem.getFromItemStack(item);
                if(ugItem == null) return;

                ugItem.updateConditionLore(UGPlayer.GetUGPlayer(player));
            } else {
                UGItem ugItem = UGItem.getFromItemStack(item);
                if(ugItem == null) return;

                ugItem.updateLore();
            }
        }
    }

    private Enchantment getRandomEnchant(ItemStack item, List<Enchantment> excludedEnchants){
        excludedEnchants.add(Enchantment.MENDING);

        Enchantment enchant;
        @NotNull Enchantment[] values = Enchantment.values();
        do {
            enchant = values[(int) (Math.random() * values.length)];
        }while (!enchant.canEnchantItem(item) && !excludedEnchants.contains(enchant));
        return enchant;
    }

    @EventHandler
    public void changeEnchantLevelCost(PrepareItemEnchantEvent e){
        for (EnchantmentOffer offer : e.getOffers()) {
            if(offer == null) continue;
            if(offer.getEnchantment().equals(Enchantment.KNOCKBACK) ||
            offer.getEnchantment().equals(Enchantment.ARROW_KNOCKBACK)) {
                offer.setEnchantment(getRandomEnchant(e.getItem(), List.of(Enchantment.KNOCKBACK, Enchantment.ARROW_KNOCKBACK)));
            }

            offer.setCost(offer.getCost() * 2);
        }
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent e){
        if(e.isCancelled()) return;

        Player player = e.getEnchanter();
        int levelCost = e.getExpLevelCost();
        int baseLevelCost = e.whichButton() + 1;

        Map<Enchantment, Integer> enchantsToAdd = e.getEnchantsToAdd();
        if(enchantsToAdd.containsKey(Enchantment.KNOCKBACK) || enchantsToAdd.containsKey(Enchantment.ARROW_KNOCKBACK)){
            enchantsToAdd.remove(Enchantment.KNOCKBACK);
            enchantsToAdd.remove(Enchantment.ARROW_KNOCKBACK);
            Enchantment enchantToReplace = getRandomEnchant(e.getItem(), List.of(Enchantment.KNOCKBACK, Enchantment.ARROW_KNOCKBACK));

            enchantsToAdd.put(enchantToReplace, 1);
        }
        player.giveExpLevels(-levelCost + baseLevelCost);
    }
}
