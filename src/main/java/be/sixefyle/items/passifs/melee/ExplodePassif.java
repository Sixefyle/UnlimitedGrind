package be.sixefyle.items.passifs.melee;

import be.sixefyle.UnlimitedGrind;
import be.sixefyle.items.UGItem;
import be.sixefyle.items.passifs.ItemPassif;
import be.sixefyle.items.passifs.interfaces.OnMeleeHit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class ExplodePassif extends ItemPassif implements OnMeleeHit {
    public ExplodePassif() {
        super(UnlimitedGrind.getInstance().getConfig().getString("itemPassif.explosion.name"),
                UnlimitedGrind.getInstance().getConfig().getStringList("itemPassif.explosion.lore"),
                UnlimitedGrind.getInstance().getConfig().getDouble("itemPassif.explosion.strength"),
                true,
                0.2);
    }

    @Override
    public void doDamage(EntityDamageByEntityEvent e) {
        Location loc = e.getEntity().getLocation();
        loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc,1);

        if(e.getDamager() instanceof Player player){
            ItemStack item = player.getInventory().getItemInMainHand();
            double mythicBonusDamage = UGItem.isMythic(item) ? 0 : getMythicBonus();
            for (LivingEntity nearbyLivingEntity : loc.getNearbyLivingEntities(5)) {
                nearbyLivingEntity.damage(e.getDamage() * getStrength() + mythicBonusDamage);
            }
        }
    }
}
