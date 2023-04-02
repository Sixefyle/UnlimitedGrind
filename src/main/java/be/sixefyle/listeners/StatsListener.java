package be.sixefyle.listeners;

import be.sixefyle.UGPlayer;
import be.sixefyle.enums.Stats;
import be.sixefyle.event.PostDamageEvent;
import be.sixefyle.items.UGItem;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class StatsListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void doPlayerAttack(EntityDamageByEntityEvent e){
        if(e.isCancelled()) return;

        Entity attacker = e.getDamager();
        Entity damaged = e.getEntity();
        double finalDamage = e.getFinalDamage();
        if(e.isCritical()){
            finalDamage /= 1.5;
        }
        boolean isCrit = false;
        if(e.getDamager() instanceof Player player){
            UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
            double critChance = ugPlayer.getStatValue(Stats.CRITICAL_CHANCE) + ugPlayer.getStatValue(Stats.BONUS_CRITICAL_CHANCE);
            isCrit = Math.random() <= critChance;

            if(isCrit){
                double critDamageValue = ugPlayer.getStatValue(Stats.CRITICAL_DAMAGE) + ugPlayer.getStatValue(Stats.BONUS_CRITICAL_DAMAGE);
                Location location = damaged.getLocation().clone();
                double entityHeight = damaged.getHeight();
                location = location.add(0, entityHeight/2, 0);
                World world = player.getWorld();
                world.spawnParticle(Particle.CRIT, location, 10);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 1);
                finalDamage *= critDamageValue;
            }

            ItemStack item = player.getInventory().getItemInMainHand();
            if(item.getItemMeta() != null){
                int itemDamage = ((org.bukkit.inventory.meta.Damageable)item.getItemMeta()).getDamage();
                if(itemDamage + 1 >= item.getType().getMaxDurability()){
                    finalDamage = 1;
                }
            }
            UGItem ugItem = UGItem.getFromItemStack(item);
            if(ugItem != null){
                if(!ugPlayer.canEquipItem(ugItem)){
                    finalDamage = 1;
                }
            }
        }
        System.out.println("StatsListener.doPlayerAttack");
        System.out.println("finalDamage = " + finalDamage);
        Bukkit.getPluginManager().callEvent(new PostDamageEvent(finalDamage, attacker, damaged, isCrit));
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void doLifeSteal(EntityDamageByEntityEvent e){
        if(e.isCancelled()) return;

        if(e.isCritical() && e.getDamager() instanceof Player player){
            UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
            ugPlayer.regenHealth(e.getFinalDamage() * ugPlayer.getStatValue(Stats.LIFE_STEAL));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void doStrengthDamage(EntityDamageByEntityEvent e){
        if(e.isCancelled()) return;

        if(e.getDamager() instanceof Player player){
            UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
            e.setDamage(e.getFinalDamage() * ((ugPlayer.getStatValue(Stats.STRENGTH) / 200) + 1));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void doDamageReduction(EntityDamageByEntityEvent e){
        if(e.isCancelled()) return;

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
        if(e.isCancelled()) return;

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
