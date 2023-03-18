package be.sixefyle.listeners;

import be.sixefyle.UGPlayer;
import be.sixefyle.enums.Stats;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

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

    @EventHandler
    public void doStrengthDamage(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Player player){
            UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
            e.setDamage(e.getFinalDamage() * ((ugPlayer.getStatValue(Stats.STRENGTH) / 10000) + 1));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void doDamageReduction(EntityDamageByEntityEvent e){
        if(e.getEntity() instanceof Player player){
            UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
            if(e.getDamager() instanceof Mob){
                e.setDamage(e.getFinalDamage() / ugPlayer.getStatValue(Stats.MELEE_DAMAGE_REDUCTION));
            } else if(e.getDamager() instanceof Arrow){
                e.setDamage(e.getFinalDamage() / ugPlayer.getStatValue(Stats.RANGE_DAMAGE_REDUCTION));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void doSweep(EntityDamageByEntityEvent e){
        if(e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
            if(e.getDamager() instanceof Player player){
                e.setCancelled(true);
                UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);

                double baseSweepRange = 2.75;
                double baseSweepDamage = e.getDamage();

                double finalSweepRange = baseSweepRange * ugPlayer.getStatValue(Stats.SWEEPING_RANGE);
                double finalSweepDamage = baseSweepDamage * ugPlayer.getStatValue(Stats.SWEEPING_DAMAGE);

                for (LivingEntity entity : player.getLocation().getNearbyLivingEntities(finalSweepRange)) {
                    if(entity == e.getEntity()) continue;
                    entity.damage(finalSweepDamage);
                }
            }
        }
    }
}
