package be.sixefyle.items.passifs.melee;

import be.sixefyle.UnlimitedGrind;
import be.sixefyle.items.UGItem;
import be.sixefyle.items.passifs.ItemPassif;
import be.sixefyle.items.passifs.interfaces.OnMeleeHit;
import be.sixefyle.items.passifs.interfaces.Stackable;
import be.sixefyle.utils.HologramUtils;
import be.sixefyle.utils.NumberUtils;
import be.sixefyle.utils.ParticleUtils;
import com.iridium.iridiumcore.dependencies.nbtapi.NBTItem;
import org.bukkit.*;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeadlyLink extends ItemPassif implements OnMeleeHit, Stackable {
    private HashMap<Player, List<LivingEntity>> links;
    private int linkTime = 200;
    private double sharedDamagePercent = .55;

    private String totalSharedDamageKey = "totalMitigatedDamage";

    public DeadlyLink() {
        super(UnlimitedGrind.getInstance().getConfig().getString("itemPassif.deadlyLink.name"),
                UnlimitedGrind.getInstance().getConfig().getStringList("itemPassif.deadlyLink.description"),
                UnlimitedGrind.getInstance().getConfig().getDouble("itemPassif.deadlyLink.strength"),
                false,
                2);
        links = new HashMap<>();
    }


    private void createLinkParticle(Player player){
        List<LivingEntity> livingEntities = links.get(player);
        Damageable startEntity = livingEntities.get(0);
        livingEntities.remove(0);
        new BukkitRunnable() {
            @Override
            public void run() {
                if(!links.containsKey(player)) cancel();
                for (LivingEntity livingEntity : livingEntities) {
                    ParticleUtils.drawBeam(
                            startEntity.getLocation().toCenterLocation(),
                            livingEntity.getLocation().toCenterLocation(),
                            Particle.CRIT
                    );
                }
            }
        }.runTaskTimer(UnlimitedGrind.getInstance(), 0, 0);
    }

    private void createLinkTimer(Player player){
        createLinkParticle(player);
        ItemStack playerWeapon = player.getInventory().getItemInMainHand();
        Bukkit.getScheduler().runTaskLater(UnlimitedGrind.getInstance(), () -> {
            NBTItem nbtWeapon = new NBTItem(playerWeapon);
            double damage = nbtWeapon.getDouble(totalSharedDamageKey);
            for (Damageable damageable : links.get(player)) {
                damageable.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, damageable.getLocation(), 1);//TODO: firework explosion
                for (LivingEntity nearbyLivingEntity : damageable.getWorld().getNearbyLivingEntities(damageable.getLocation(), 3)) {
                    if(nearbyLivingEntity.equals(damageable)) continue;

                    nearbyLivingEntity.damage(damage);
                    HologramUtils.createDamageIndicator(nearbyLivingEntity.getLocation(), NumberUtils.format(damage), ChatColor.AQUA);
                }
            }
            links.remove(player);
            resetStack(nbtWeapon);
            nbtWeapon.applyNBT(playerWeapon);
        }, linkTime);
    }

    @Override
    public void doDamage(EntityDamageByEntityEvent e, Player player) {
        double mitigatedDamage = 0;
        LivingEntity targetEntity = (LivingEntity) e.getEntity();
        if(links.containsKey(player)){
            List<LivingEntity> linkedEntities = links.get(player);

            mitigatedDamage = e.getFinalDamage() * sharedDamagePercent;

            if(linkedEntities.contains(targetEntity)) {
                for (Damageable damageable : linkedEntities) {
                    if(damageable.equals(targetEntity)) continue;

                    damageable.damage(mitigatedDamage);
                    HologramUtils.createDamageIndicator(damageable.getLocation(), NumberUtils.format(mitigatedDamage), ChatColor.AQUA);
                }
            }
        } else {
            ItemStack weapon = player.getInventory().getItemInMainHand();
            List<LivingEntity> livingEntities = targetEntity.getLocation().getNearbyLivingEntities(5)
                    .stream()
                    .filter(ent -> ent instanceof Monster)
                    .toList();

            LivingEntity currentEntity;
            int i = 0;
            int playerAmount = 0;
            while(i < getStrength() + getMythicBonus(UGItem.isMythic(weapon))){
                if(i >= livingEntities.size() - playerAmount) break;
                currentEntity = livingEntities.get(i);
                if(currentEntity instanceof Player) {
                    playerAmount++;
                    continue;
                }
                livingEntities.add(currentEntity);
                i++;
            }

            links.put(player, livingEntities);
            createLinkTimer(player);
        }

        ItemStack playerWeapon = player.getInventory().getItemInMainHand();
        NBTItem nbtWeapon = new NBTItem(playerWeapon);
        if(!nbtWeapon.hasKey(totalSharedDamageKey)){
            resetStack(nbtWeapon);
        }
        addStack(nbtWeapon, mitigatedDamage);
        nbtWeapon.applyNBT(playerWeapon);
    }

    @Override
    public void addStack(NBTItem item, double amount) {
        double newVal = item.getDouble(totalSharedDamageKey) + amount;
        item.setDouble(totalSharedDamageKey, newVal);
    }

    @Override
    public void resetStack(NBTItem item) {
        item.setDouble(totalSharedDamageKey, 0.0);
    }
}
