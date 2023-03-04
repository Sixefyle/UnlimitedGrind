package be.sixefyle.listeners;

import be.sixefyle.UGPlayer;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.items.UGItem;
import be.sixefyle.utils.HologramUtils;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.User;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BasicListeners implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        UGPlayer ugPlayer;
        if (UGPlayer.playerMap.get(player.getUniqueId()) == null) {
            ugPlayer = new UGPlayer(e.getPlayer());
            ugPlayer.respawn();
            for (ItemStack content : player.getInventory().getContents()) {
                if(content == null) continue;
                UGItem ugItem = UGItem.getFromItemStack(content);
                if(ugItem == null) return;

                ugItem.updateLore();
                ugItem.updateConditionLore(ugPlayer);
            }
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

            double newHealth = damageable.getMaxHealth() +
                    Math.pow(maxPower, 1.02112);

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
        ItemStack item = e.getItemDrop().getItemStack();
        UGItem ugItem = UGItem.getFromItemStack(item);
        if(ugItem != null){
            ugItem.createRarityParticle(e.getItemDrop());
        }
    }

    @EventHandler
    public void onPlayerRegenHealth(EntityRegainHealthEvent e){
        if(e.getEntity() instanceof Player player){
            UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
            ugPlayer.regenHealth(e.getAmount());

            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEquipArmor(PlayerArmorChangeEvent e){
        UGPlayer ugPlayer = UGPlayer.GetUGPlayer(e.getPlayer());
        if(ugPlayer == null) return;

        ugPlayer.updateBonusHealthFromArmor();
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

        if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            ItemStack item = e.getCurrentItem();
            if(item == null) return;
            if(e.getClickedInventory().equals(player.getInventory())){
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

            System.out.println(e.getClickedInventory().equals(player.getInventory()));
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
