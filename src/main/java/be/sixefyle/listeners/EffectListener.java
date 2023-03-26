package be.sixefyle.listeners;

import be.sixefyle.UGPlayer;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.enums.ComponentColor;
import be.sixefyle.enums.Effects;
import be.sixefyle.enums.Stats;
import net.kyori.adventure.text.Component;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class EffectListener implements Listener {

    private static void addPlayerStat(UGPlayer ugPlayer, double value, double strength, Effects consumable, boolean shouldAdd){
        if(shouldAdd){
            ugPlayer.addStatValue(consumable.getAffectedStats(),
                    consumable.isPercentage() ?
                            value * strength :
                            value + strength);
        } else {
            ugPlayer.addStatValue(consumable.getAffectedStats(),
                    consumable.isPercentage() ?
                            (value / (strength + 1)) - value :
                            value - strength);
        }
    }

    @EventHandler
    public void onTakePotion(EntityPotionEffectEvent e){
        if(e.getEntity() instanceof Player player){
            try{
                PotionEffect potionEffect;
                if(e.getOldEffect() == null) {
                    potionEffect = e.getNewEffect();
                } else {
                    potionEffect = e.getOldEffect();
                }
                UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
                EntityPotionEffectEvent.Action action = e.getAction();

                if(ugPlayer.getActivesPotionsMap().containsKey(potionEffect.getType()) && !action.equals(EntityPotionEffectEvent.Action.REMOVED)){
                    player.sendMessage(Component.text("You need to wait the end of this potion before taking another!")
                            .color(ComponentColor.ERROR.getColor()));
                    e.setCancelled(true);
                    return;
                }

                Effects consumable = Effects.valueOf(potionEffect.getType().getName());
                Stats stat = consumable.getAffectedStats();

                int amplifier = potionEffect.getAmplifier();
                double finalStrength = consumable.getStrength() * (amplifier + 1);
                boolean isBuff = consumable.isBuff();


                if(!consumable.isDirectHealthDamage()){
                    if(stat.isAttribute()){
                        if(action.equals(EntityPotionEffectEvent.Action.ADDED)){
                            AttributeModifier modifier = new AttributeModifier(
                                    "potion."+consumable,
                                    isBuff ? finalStrength : -finalStrength,
                                    consumable.isPercentage() ? AttributeModifier.Operation.MULTIPLY_SCALAR_1 : AttributeModifier.Operation.ADD_NUMBER);
                            player.getAttribute((Attribute) stat.getStats()).addModifier(modifier);
                        } else if(action.equals(EntityPotionEffectEvent.Action.REMOVED)){
                            for (AttributeModifier modifier : player.getAttribute((Attribute) stat.getStats()).getModifiers()) {
                                if(modifier.getName().equals("potion."+consumable)){
                                  player.getAttribute((Attribute) stat.getStats()).removeModifier(modifier);
                                }
                            }
                        }
                    } else {
                        double statValue = ugPlayer.getStatValue(stat);
                        if(action.equals(EntityPotionEffectEvent.Action.ADDED)){
                            addPlayerStat(ugPlayer, statValue, finalStrength, consumable, isBuff);
                        } else if (action.equals(EntityPotionEffectEvent.Action.REMOVED)) {
                            addPlayerStat(ugPlayer, statValue, finalStrength, consumable, !isBuff);
                        }
                        if(stat.equals(Stats.HEALTH)) {
                            ugPlayer.updatePlayerHeartBar();
                        }
                    }
                } else {
                    BukkitTask bukkitTask = new BukkitRunnable() {
                        int timeLeft = potionEffect.getDuration();
                        @Override
                        public void run() {
                            timeLeft -= 10;
                            if (timeLeft <= 0) {
                                cancel();
                                ugPlayer.getActivesPotionsMap().remove(potionEffect.getType());
                            }

                            if (isBuff) {
                                ugPlayer.regenHealth(ugPlayer.getHealth() * finalStrength);
                            } else {
                                ugPlayer.takeDamage(ugPlayer.getHealth() * finalStrength);
                            }
                        }
                    }.runTaskTimer(UnlimitedGrind.getInstance(), 0, 10);

                    ugPlayer.getActivesPotionsMap().putIfAbsent(potionEffect.getType(), bukkitTask);
                }
            }catch (IllegalArgumentException ignore){ }
        }
    }
}
