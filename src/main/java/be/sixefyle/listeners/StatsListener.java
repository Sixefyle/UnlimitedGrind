package be.sixefyle.listeners;

import be.sixefyle.UGPlayer;
import be.sixefyle.enums.Stats;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class StatsListener implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void doCriticalDamage(EntityDamageByEntityEvent e){
        if(e.isCritical() && e.getDamager() instanceof Player player){
            UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
            double critDamageValue = ugPlayer.getStatValue(Stats.CRITICAL_DAMAGE) + ugPlayer.getStatValue(Stats.BONUS_CRITICAL_DAMAGE);
            e.setDamage(e.getFinalDamage() * critDamageValue);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void doLifeSteal(EntityDamageByEntityEvent e){
        if(e.isCritical() && e.getDamager() instanceof Player player){
            UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
            ugPlayer.regenHealth(e.getFinalDamage() * ugPlayer.getStatValue(Stats.LIFE_STEAL));
        }
    }
}
