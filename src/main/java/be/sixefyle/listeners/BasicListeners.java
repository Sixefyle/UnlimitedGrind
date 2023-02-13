package be.sixefyle.listeners;

import be.sixefyle.UGPlayer;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.items.passifs.Passif;
import be.sixefyle.items.passifs.melee.LifeConversion;
import be.sixefyle.utils.HologramUtils;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.User;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BasicListeners implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        if (UGPlayer.playerMap.get(player) == null) {
            new UGPlayer(e.getPlayer());
        }
    }

    @EventHandler
    public void changeBlockDropLoc(BlockDropItemEvent e){
        Player player = e.getPlayer();
        for (Item item : e.getItems()){
            item.teleport(player);
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e){
        if(e.getEntity() instanceof Damageable damageable) {
            @NotNull Optional<Island> island = IridiumSkyblockAPI.getInstance().getIslandViaLocation(damageable.getLocation());
            if(island.isEmpty()) return;

            UGPlayer ugPlayer;
            double maxPower = 0;
            for (User member : island.get().getMembers()) {
                if(member.getPlayer() == null) continue;

                ugPlayer = UGPlayer.GetUGPlayer(member.getPlayer());
                if(maxPower < ugPlayer.getPower()){
                    maxPower = ugPlayer.getPower();
                }
            }

            double newHealth = damageable.getMaxHealth() +
                    Math.pow(maxPower, 1.29912);

            damageable.setMaxHealth(newHealth);
            damageable.setHealth(newHealth);

            HologramUtils.createEntInfoFollow(damageable);

            damageable.setCustomNameVisible(false);
            damageable.setMetadata("power", new FixedMetadataValue(UnlimitedGrind.getInstance(), maxPower));
            ((LivingEntity) damageable).setMaximumNoDamageTicks(3);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        Player player = e.getPlayer();
        for (Attribute value : Attribute.values()) {
            if(player.getAttribute(value) == null) continue;
            for (AttributeModifier modifier : player.getAttribute(value).getModifiers()) {
                player.getAttribute(value).removeModifier(modifier);
            }
        }
    }
}
