package be.sixefyle.items.passifs.melee;

import be.sixefyle.UnlimitedGrind;
import be.sixefyle.items.UGItem;
import be.sixefyle.items.passifs.ItemPassif;
import be.sixefyle.items.passifs.interfaces.OnMeleeHit;
import be.sixefyle.utils.HologramUtils;
import be.sixefyle.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class ExplodePassif extends ItemPassif implements OnMeleeHit {
    public ExplodePassif() {
        super(UnlimitedGrind.getInstance().getConfig().getString("itemPassif.explosion.name"),
                UnlimitedGrind.getInstance().getConfig().getStringList("itemPassif.explosion.description"),
                UnlimitedGrind.getInstance().getConfig().getDouble("itemPassif.explosion.strength"),
                true,
                0.2);
    }

    @Override
    public void doDamage(EntityDamageByEntityEvent e, Player player) {
        Location loc = e.getEntity().getLocation();
        loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc,1);

        double damage = e.getDamage() * getStrength();
        for (LivingEntity nearbyLivingEntity : loc.getNearbyLivingEntities(5)) {
            if(nearbyLivingEntity.equals(player) || nearbyLivingEntity.equals(e.getEntity())) continue;

            nearbyLivingEntity.damage(damage);
            HologramUtils.createDamageIndicator(nearbyLivingEntity.getLocation(), NumberUtils.format(damage), ChatColor.AQUA);
        }
    }
}
