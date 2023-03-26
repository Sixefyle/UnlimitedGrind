package be.sixefyle.listeners;

import be.sixefyle.UGPlayer;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.enums.Effects;
import be.sixefyle.enums.Symbols;
import be.sixefyle.items.UGItem;
import be.sixefyle.utils.NumberUtils;
import be.sixefyle.utils.HologramUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class CombatListener implements Listener {
    private final double powerEfficiency = UnlimitedGrind.getInstance().getConfig().getDouble("power.efficiencyDamage");

//    @EventHandler //TODO: Passif sur les item débloqué avec le power
//    public void onPlayerAttack(EntityDamageByEntityEvent e){
//        if(e.getDamager() instanceof Player player){
//            UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
//
//            double newDamage = e.getFinalDamage() + (e.getFinalDamage() * powerEfficiency * ugPlayer.getPower());
//
//            e.setDamage(newDamage);
//        }
//    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerAttack(EntityDamageByEntityEvent e){
        // If item is broke
        if(e.getDamager() instanceof Player player){
            ItemStack item = player.getInventory().getItemInMainHand();
            if(item.getItemMeta() != null){
                int itemDamage = ((org.bukkit.inventory.meta.Damageable)item.getItemMeta()).getDamage();
                if(itemDamage + 1 >= item.getType().getMaxDurability()){
                    e.setDamage(1);
                }
            }
            UGItem ugItem = UGItem.getFromItemStack(item);
            if(ugItem != null){
                UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
                if(!ugPlayer.canEquipItem(ugItem)){
                    e.setDamage(1);
                }
            }
        }
        if(e.getFinalDamage() <= 0) return;

        Location damageIndicatorLoc = e.getEntity().getLocation().clone();

        ChatColor color = ChatColor.WHITE;
        if(e.getDamager() instanceof Player)
            color = e.isCritical() ? ChatColor.GOLD : ChatColor.WHITE;
        if(e.getEntity() instanceof Player)
            color = ChatColor.RED;

        HologramUtils.createDamageIndicator(damageIndicatorLoc, (e.isCritical() ? ChatColor.BOLD + Symbols.CRITICS.get() : "") +
                NumberUtils.format(e.getFinalDamage()), color);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityAttack(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Damageable ent && ent.hasMetadata("power")) {
            double power = ent.getMetadata("power").get(0).asDouble();
            double newDamage = e.getDamage() + e.getDamage() * (power/80 + 1);

            e.setDamage(newDamage);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTakeDamageByEntity(EntityDamageByEntityEvent e){
        if(e.getEntity() instanceof Player player){
            UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
            double damage = e.getDamage() * ugPlayer.getDamageReductionPercentage();
            ugPlayer.takeDamage(damage);
            e.setDamage(0);
        }
    }

    @EventHandler
    public void onPlayerTakeDamage(EntityDamageEvent e){
        if(e.getEntity() instanceof Player player){
            UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
            EntityDamageEvent.DamageCause cause = e.getCause();

            if(cause.equals(EntityDamageEvent.DamageCause.FIRE_TICK) || cause.equals(EntityDamageEvent.DamageCause.FIRE)){
                ugPlayer.takeDamage((ugPlayer.getMaxHealth() * Effects.FIRE.getStrength()) * ugPlayer.getDamageReductionPercentage());
                e.setDamage(0);
            }

            if(!cause.equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)){
                ugPlayer.takeDamage(e.getDamage());
                e.setDamage(0);
            }
            ugPlayer.updateActionBarStats();
        }
    }

    @EventHandler
    public void onEntityDie(EntityDeathEvent e){
        Damageable entity = e.getEntity();
        if(entity.hasMetadata("amount")){
            int amount = entity.getMetadata("amount").get(0).asInt();
            if(amount > 1) {
                e.setCancelled(true);
                for (ItemStack drop : e.getDrops()) {
                    entity.getWorld().dropItemNaturally(entity.getLocation(), drop);
                }
            }
            entity.setMetadata("amount", new FixedMetadataValue(UnlimitedGrind.getInstance(), amount - 1));
        }
    }

    @EventHandler
    public void onItemBreak(PlayerItemDamageEvent e){
        int itemDamage = ((org.bukkit.inventory.meta.Damageable)e.getItem().getItemMeta()).getDamage();
        if(itemDamage + e.getDamage() >= e.getItem().getType().getMaxDurability()){
            e.setCancelled(true);
        }
    }
}
