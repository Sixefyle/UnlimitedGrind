package be.sixefyle.items.passifs.armor;

import be.sixefyle.UnlimitedGrind;
import be.sixefyle.items.UGItem;
import be.sixefyle.items.passifs.ItemPassif;
import be.sixefyle.items.passifs.Passif;
import be.sixefyle.items.passifs.interfaces.OnReceiveDamage;
import be.sixefyle.utils.HologramUtils;
import be.sixefyle.utils.NumberUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ThunderStormPassif extends ItemPassif implements OnReceiveDamage {

    private final double chance;
    private final int radius;
    public ThunderStormPassif() {
        super(UnlimitedGrind.getInstance().getConfig().getString("itemPassif.thunderStorm.name"),
                UnlimitedGrind.getInstance().getConfig().getStringList("itemPassif.thunderStorm.description"),
                UnlimitedGrind.getInstance().getConfig().getDouble("itemPassif.thunderStorm.strength"),
                true,
                .5);

        this.chance = 0.075;
        this.radius = 10;
    }

    @Override
    public void onGetDamage(EntityDamageEvent e, Player player, ItemStack armor) {
        if(player.isBlocking()) return;

        if(Math.random() <= chance){
            NamespacedKey powerKey = new NamespacedKey(UnlimitedGrind.getInstance(), "power");
            if(!armor.getItemMeta().getPersistentDataContainer().has(powerKey)) return;

            double armorPower = armor.getItemMeta().getPersistentDataContainer().get(powerKey, PersistentDataType.DOUBLE);
            double damage = armorPower * (1 + getStrength() + getMythicBonus(UGItem.isMythic(armor)));

            List<LivingEntity> livingEntityList = player.getLocation().getNearbyLivingEntities(radius)
                    .stream()
                    .filter(ent -> ent instanceof Monster)
                    .filter(ent -> !(ent instanceof Player))
                    .toList();

            for (LivingEntity nearbyLivingEntity : livingEntityList) {
                Bukkit.getScheduler().runTaskLater(UnlimitedGrind.getInstance(), () -> {
                    nearbyLivingEntity.getLocation().getWorld().strikeLightningEffect(nearbyLivingEntity.getLocation());
                    nearbyLivingEntity.damage(damage);
                    HologramUtils.createDamageIndicator(nearbyLivingEntity.getLocation(), NumberUtils.format(damage), ChatColor.AQUA);
                }, (long) (Math.random() * 10));
            }
        }
    }
}
