package be.sixefyle.listeners;

import be.sixefyle.UGPlayer;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.utils.NumberUtils;
import be.sixefyle.utils.HologramUtils;
import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class CombatListener implements Listener {
    private final double powerEfficiency = UnlimitedGrind.getInstance().getConfig().getDouble("power.efficiencyDamage");

    @EventHandler //TODO: Passif sur les item débloqué avec le power
    public void onPlayerAttack(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Player player){
            UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
            double newDamage = e.getFinalDamage() + (e.getFinalDamage() * powerEfficiency * ugPlayer.getPower());

            e.setDamage(newDamage);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void createFloatingDamage(EntityDamageByEntityEvent e){
        Location damageIndicatorLoc = e.getEntity().getLocation().clone();
        damageIndicatorLoc.add(0,2,0);
        HologramUtils.createTimed(damageIndicatorLoc, NumberUtils.format(e.getDamage()), 60);
    }

    @EventHandler
    public void onEntityAttack(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Damageable ent && ent.hasMetadata("power")) {
            double newDamage = e.getDamage() + (ent.getMetadata("power").get(0).asDouble() * powerEfficiency);
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
}
