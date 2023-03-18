package be.sixefyle.listeners;

import be.sixefyle.UGPlayer;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.enums.ComponentColor;
import be.sixefyle.items.UGItem;
import be.sixefyle.utils.HologramUtils;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.User;
import com.jeff_media.armorequipevent.ArmorEquipEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

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
    public void onEntitySpawn(EntitySpawnEvent e){
        if(e.getEntity() instanceof Damageable damageable) {
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

            double newHealth = damageable.getMaxHealth()+(damageable.getMaxHealth()*(maxPower/20000))*(Math.pow(maxPower,.78)/100+1); //TODO: magic number

            damageable.setMaxHealth(newHealth);
            damageable.setHealth(newHealth);

            HologramUtils.createEntInfoFollow(damageable);

            damageable.setCustomNameVisible(false);
            damageable.setMetadata("power", new FixedMetadataValue(UnlimitedGrind.getInstance(), maxPower));
            ((LivingEntity) damageable).setMaximumNoDamageTicks(3);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        Player player = e.getPlayer();
        for (Attribute value : Attribute.values()) {
            if(player.getAttribute(value) == null) continue;
            for (AttributeModifier modifier : player.getAttribute(value).getModifiers()) {
                player.getAttribute(value).removeModifier(modifier);
            }
        }
        UGPlayer.RemoveUGPlayer(player);
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent e){
        Player player = e.getPlayer();
        ItemStack item = e.getItemDrop().getItemStack();
        UGItem ugItem = UGItem.getFromItemStack(item);
        if(ugItem != null){
            ugItem.createRarityParticle(e.getItemDrop());
            UGPlayer.GetUGPlayer(player).updateStatsFromItem(null, ugItem);
        }
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent e){
        if(e.getEntity() instanceof Player player){
            ItemStack itemStack = e.getItem().getItemStack();
            UGItem ugItem = UGItem.getFromItemStack(itemStack);

            Bukkit.getScheduler().runTaskLater(UnlimitedGrind.getInstance(), () -> {
                if(player.getInventory().getItemInMainHand().equals(itemStack)){
                    UGPlayer.GetUGPlayer(player).updateStatsFromItem(ugItem, null);
                }
            }, 1);

            if(ugItem == null) return;
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
            ugPlayer.regenHealth(e.getAmount());

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
            ugPlayer.updateWearedPower();
            ugPlayer.updateStatsFromItem(newItem, oldItem);
        } else {
            e.setCancelled(true);
            player.sendMessage(
                    Component.text(UnlimitedGrind.getInstance().getConfig().getString("lang.item.error.notEnoughPower"))
                            .color(ComponentColor.ERROR.getColor()));
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
            ugPlayer.updateStatsFromItem(ugNewItem, ugOldItem);

            ugPlayer.updateWearedPower();
            ugPlayer.setHealthFromStat();
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
    public void onDrinkPotion(EntityPotionEffectEvent e){

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
}
