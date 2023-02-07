package be.sixefyle.listeners;

import be.sixefyle.UGPlayer;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.enums.Symbols;
import be.sixefyle.utils.NumberUtils;
import be.sixefyle.utils.HologramUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
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
    public void createFloatingDamage(EntityDamageByEntityEvent e){
        // If item is broke
        if(e.getDamager() instanceof Player player){
            ItemStack item = player.getInventory().getItemInMainHand();
            if(item.getItemMeta() != null){
                int itemDamage = ((org.bukkit.inventory.meta.Damageable)item.getItemMeta()).getDamage();
                if(itemDamage + 1 >= item.getType().getMaxDurability()){
                    e.setDamage(1);
                }
            }
        }

        Location damageIndicatorLoc = e.getEntity().getLocation().clone();
        damageIndicatorLoc.add(
                NumberUtils.getRandomNumber(-0.07, 0.07),
                NumberUtils.getRandomNumber(1.95, 2.05),
                NumberUtils.getRandomNumber(-0.07, 0.07)
        );

        ChatColor color = e.isCritical() ? ChatColor.GOLD : ChatColor.WHITE ;
        HologramUtils.createTimed(damageIndicatorLoc, (e.isCritical() ? Symbols.CRITICS.get() + " " : "") +
                NumberUtils.format(e.getDamage()), color, 15);
    }

    @EventHandler
    public void onEntityAttack(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Damageable ent && ent.hasMetadata("power")) {
            double newDamage = Math.pow(ent.getMetadata("power").get(0).asDouble(),
                    UnlimitedGrind.getInstance().getConfig().getDouble("power.efficiencyDamage"));

            e.setDamage(newDamage);
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
