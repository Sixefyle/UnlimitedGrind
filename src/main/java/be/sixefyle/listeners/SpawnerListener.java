package be.sixefyle.listeners;

import be.sixefyle.BetterSpawner;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.gui.SpawnerGui;
import be.sixefyle.utils.HologramUtils;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.database.Island;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Collection;
import java.util.Optional;

public class SpawnerListener implements Listener {

    @EventHandler
    public void onClickOnSpawner(PlayerInteractEvent e){
        Block block = e.getClickedBlock();
        if(block == null) return;

        Player player = e.getPlayer();
        if(player.isSneaking()) return;

        if(block.getType().equals(Material.SPAWNER) && e.getAction().isRightClick()){
            CreatureSpawner spawner = (CreatureSpawner) block.getState();
            if(player.getInventory().getItemInMainHand().getType().name().contains("_SPAWN_EGG") &&
                    (!spawner.getSpawnedType().isSpawnable() ||
                            (spawner.hasMetadata("amount") &&
                                    spawner.getMetadata("amount").get(0).asInt() == 1))) return;

            e.setCancelled(true);
            player.openInventory(new SpawnerGui(spawner).getInventory());
        }
    }

    @EventHandler
    public void onBreakSpawner(BlockBreakEvent e){
        Block block = e.getBlock();
        if(block.getType().equals(Material.SPAWNER)){
            BetterSpawner.getSpawners().remove(block.getLocation());
        }
    }

    @EventHandler
    public void onPlaceSpawner(BlockPlaceEvent e){
        Block block = e.getBlock();
        if(block.getType().equals(Material.SPAWNER)){
            Player player = e.getPlayer();
            Optional<Island> island = IridiumSkyblockAPI.getInstance().getUser(player).getIsland();

            if(island.isPresent()) {
                BetterSpawner spawner = new BetterSpawner(EntityType.ZOMBIE, island.get(), block.getLocation());
            }
        }
    }

    @EventHandler
    public void onCreatureSpawn(SpawnerSpawnEvent e){
        e.setCancelled(true);
        CreatureSpawner spawner = e.getSpawner();

        if(spawner.hasMetadata("amount")){
            int amount = spawner.getMetadata("amount").get(0).asInt();
            spawnEntity(spawner, amount);
        }
    }

    private void spawnEntity(CreatureSpawner spawner, int amountToAdd){
        if(spawner == null) return;

        if(spawner.hasMetadata("power")) {
            double spawnerPower = spawner.getMetadata("power").get(0).asDouble();
            Damageable nearestCreature = getNearestCreature(spawner.getSpawnedType(), spawner.getLocation(), spawnerPower,10);
            if (nearestCreature == null) {
                Damageable ent = (Damageable) spawner.getWorld().spawnEntity(spawner.getLocation(), spawner.getSpawnedType());
                double newHealth = ent.getMaxHealth() +
                        Math.pow(spawnerPower, UnlimitedGrind.getInstance().getConfig().getDouble("power.efficiency"));

                ent.setMaxHealth(newHealth);
                ent.setHealth(newHealth);
                ent.setMetadata("power", new FixedMetadataValue(UnlimitedGrind.getInstance(), spawnerPower));

                if(spawner.hasMetadata("silenceMode")){
                    ent.setSilent(spawner.getMetadata("silenceMode").get(0).asBoolean());
                }
                HologramUtils.createEntInfoFollow(ent);

                ent.setCustomNameVisible(false);
                ent.setMetadata("amount", new FixedMetadataValue(UnlimitedGrind.getInstance(), amountToAdd));
                ((LivingEntity) ent).setMaximumNoDamageTicks(2);

            } else if(nearestCreature.hasMetadata("amount")){
                int amount = nearestCreature.getMetadata("amount").get(0).asInt();
                nearestCreature.setMetadata("amount",
                        new FixedMetadataValue(UnlimitedGrind.getInstance(), amount + amountToAdd));
            }
        }
    }

    public Damageable getNearestCreature(EntityType type, Location loc, double power, int radius){
        Collection<LivingEntity> ents = loc.getNearbyLivingEntities(radius);
        double entPower;
        for (Entity ent : ents) {
            if(ent.hasMetadata("power")){
                entPower = ent.getMetadata("power").get(0).asDouble();
                if(ent.getType().equals(type) && power == entPower && ent instanceof Damageable damageable){
                    return damageable;
                }
            }
        }
        return null;
    }
}
