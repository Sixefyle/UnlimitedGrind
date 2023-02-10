package be.sixefyle.listeners;

import be.sixefyle.BetterSpawner;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.gui.SpawnerGui;
import be.sixefyle.utils.HologramUtils;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.database.Island;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;

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
            BetterSpawner betterSpawner = BetterSpawner.getBetterSpawner(spawner.getLocation());
            if(player.getInventory().getItemInMainHand().getType().name().contains("_SPAWN_EGG") &&
                    (!spawner.getSpawnedType().isSpawnable() || betterSpawner.getStackAmount() == 1)) return;

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

    private <T,Z> Z getPersistantContainer(ItemMeta itemMeta, String id, PersistentDataType<T, Z> type) {
        Z value = null;
        NamespacedKey key = new NamespacedKey(UnlimitedGrind.getInstance(), id);
        if(itemMeta.getPersistentDataContainer().has(key, type)) {
            value = itemMeta.getPersistentDataContainer().get(key, type);
        }
        return value;
    }

    @EventHandler
    public void onPlaceSpawner(BlockPlaceEvent e){
        Block block = e.getBlock();

        if(block.getType().equals(Material.SPAWNER)){
            Player player = e.getPlayer();
            Optional<Island> island = IridiumSkyblockAPI.getInstance().getUser(player).getIsland();

            if(island.isPresent()) {
                ItemMeta itemMeta = e.getItemInHand().getItemMeta();
                if(getPersistantContainer(itemMeta, "power", PersistentDataType.DOUBLE) != null){
                    double power = getPersistantContainer(itemMeta, "power", PersistentDataType.DOUBLE);
                    int amount = getPersistantContainer(itemMeta, "amount", PersistentDataType.INTEGER);
                    int maxAmount = getPersistantContainer(itemMeta, "maxAmount", PersistentDataType.INTEGER);
                    int stackUpgradeLevel = getPersistantContainer(itemMeta, "stackUpgradeLevel", PersistentDataType.INTEGER);
                    int maxStackUpgradeLevel = getPersistantContainer(itemMeta, "maxStackUpgradeLevel", PersistentDataType.INTEGER);
                    double rareDropChance = getPersistantContainer(itemMeta, "rareDropChance", PersistentDataType.DOUBLE);
                    boolean isSilence = getPersistantContainer(itemMeta, "silence", PersistentDataType.BYTE) == 1;
                    EntityType entityType = EntityType.valueOf(getPersistantContainer(itemMeta, "entityType", PersistentDataType.STRING));

                    new BetterSpawner(maxAmount, amount, maxStackUpgradeLevel, stackUpgradeLevel,
                            power, isSilence, rareDropChance, block.getLocation(), island.get(), entityType);
                } else {
                    new BetterSpawner(EntityType.ZOMBIE, island.get(), block.getLocation());
                }
            }
        }
    }

    @EventHandler
    public void onCreatureSpawn(SpawnerSpawnEvent e){
        e.setCancelled(true);
        CreatureSpawner spawner = e.getSpawner();
        BetterSpawner betterSpawner = BetterSpawner.getBetterSpawner(spawner.getLocation());
        if(betterSpawner != null){
            spawnEntity(spawner, e.getLocation(), betterSpawner.getStackAmount());
        }
    }

    private void spawnEntity(CreatureSpawner spawner, Location loc, int amountToAdd){
        if(spawner == null) return;
        BetterSpawner betterSpawner = BetterSpawner.getBetterSpawner(spawner.getLocation());
        if(betterSpawner == null) return;

        double spawnerPower = betterSpawner.getPower();
        Damageable nearestCreature = getNearestCreature(spawner.getSpawnedType(), spawner.getLocation(), spawnerPower,10);
        if (nearestCreature == null) {
            Damageable ent = (Damageable) spawner.getWorld().spawnEntity(loc, spawner.getSpawnedType());
            double newHealth = ent.getMaxHealth() +
                    Math.pow(spawnerPower, 1.29912);

            ent.setMaxHealth(newHealth);
            ent.setHealth(newHealth);
            ent.setSilent(betterSpawner.isSilence());

            HologramUtils.createEntInfoFollowAmount(ent);

            ent.setCustomNameVisible(false);
            ent.setMetadata("power", new FixedMetadataValue(UnlimitedGrind.getInstance(), spawnerPower));
            ent.setMetadata("amount", new FixedMetadataValue(UnlimitedGrind.getInstance(), amountToAdd));
            ((LivingEntity) ent).setMaximumNoDamageTicks(3);

        } else if(nearestCreature.hasMetadata("amount")){
            int amount = nearestCreature.getMetadata("amount").get(0).asInt();
            nearestCreature.setMetadata("amount",
                    new FixedMetadataValue(UnlimitedGrind.getInstance(), amount + amountToAdd));
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
