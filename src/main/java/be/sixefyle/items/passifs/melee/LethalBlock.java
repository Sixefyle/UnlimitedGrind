package be.sixefyle.items.passifs.melee;

import be.sixefyle.UnlimitedGrind;
import be.sixefyle.items.passifs.ItemPassif;
import be.sixefyle.items.passifs.interfaces.OnReceiveDamage;
import be.sixefyle.items.passifs.interfaces.Stackable;
import be.sixefyle.utils.HologramUtils;
import be.sixefyle.utils.NumberUtils;
import com.iridium.iridiumcore.dependencies.nbtapi.NBTItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LethalBlock extends ItemPassif implements OnReceiveDamage, Stackable {

    private List<Player> checkingPlayers;

    private static String blockNBTKey = "blockState";
    private static String damageBlockedNBTKey = "damageBlocked";

    public LethalBlock() {
        super(UnlimitedGrind.getInstance().getConfig().getString("itemPassif.lethalBlock.name"),
                UnlimitedGrind.getInstance().getConfig().getString("itemPassif.lethalBlock.itemPrefixName"),
                UnlimitedGrind.getInstance().getConfig().getStringList("itemPassif.lethalBlock.description"),
                UnlimitedGrind.getInstance().getConfig().getDouble("itemPassif.lethalBlock.strength"),
                true,
                0.01);

        checkingPlayers = new ArrayList<>();
    }

    public void addStack(NBTItem armorNBT, double amount){
        double oldValue = armorNBT.getDouble(damageBlockedNBTKey);
        double newValue = oldValue + amount;
        armorNBT.setDouble(damageBlockedNBTKey, newValue);
    }

    public void resetStack(NBTItem armorNBT){
        armorNBT.setDouble(damageBlockedNBTKey, 0.0);
    }

    public void checkIfPlayerBlock(Player player, ItemStack armor){
        if(checkingPlayers.contains(player)) return;

        checkingPlayers.add(player);

        new BukkitRunnable() {
            double damage;
            NBTItem nbtArmor;
            @Override
            public void run() {
                if(!player.isBlocking()){
                    //do unstack damage
                    player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, player.getLocation(), 1);
                    nbtArmor = new NBTItem(armor);
                    damage = nbtArmor.getDouble(damageBlockedNBTKey);
                    List<LivingEntity> livingEntityList = player.getLocation().getNearbyLivingEntities(3)
                            .stream()
                            .filter(ent -> ent instanceof Monster)
                            .toList();
                    for (LivingEntity nearbyLivingEntity : livingEntityList) {
                        if(nearbyLivingEntity.equals(player)) continue;
                        nearbyLivingEntity.damage(damage);
                        HologramUtils.createDamageIndicator(nearbyLivingEntity.getLocation(), NumberUtils.format(damage), ChatColor.AQUA);
                    }
                    resetStack(nbtArmor);
                    checkingPlayers.remove(player);
                    nbtArmor.applyNBT(armor);
                    cancel();
                }
            }
        }.runTaskTimer(UnlimitedGrind.getInstance(), 0, 2);
    }

    @Override
    public void onGetDamage(EntityDamageEvent e, Player player, ItemStack armor) {
        boolean isBlocking = player.isBlocking();

        NBTItem nbtArmor = new NBTItem(armor);
        
        if(!nbtArmor.hasKey(blockNBTKey) || nbtArmor.getBoolean(blockNBTKey) != isBlocking) {
            nbtArmor.setBoolean(blockNBTKey, isBlocking);
        }

        if(!isBlocking) return;

        if(!nbtArmor.hasKey(damageBlockedNBTKey)) {
            resetStack(nbtArmor);
        }
        addStack(nbtArmor, e.getFinalDamage() * getStrength());
        checkIfPlayerBlock(player, armor);
        nbtArmor.applyNBT(armor);
    }
}
