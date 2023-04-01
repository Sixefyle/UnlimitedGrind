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
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

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
        Entity entity = e.getEntity();
        Location loc = entity.getLocation();

        if(player.getAttackCooldown() != 1) return;

        loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc,1);

        double damage = e.getFinalDamage() * getStrength();
        List<LivingEntity> livingEntityList = loc.getNearbyLivingEntities(5)
                .stream()
                .filter(ent -> ent instanceof Monster)
                .filter(ent -> !ent.equals(entity))
                .toList();

        for (LivingEntity nearbyLivingEntity : livingEntityList) {
            nearbyLivingEntity.damage(damage);
            HologramUtils.createDamageIndicator(nearbyLivingEntity.getLocation(), NumberUtils.format(damage), ChatColor.AQUA);
        }
    }
}
