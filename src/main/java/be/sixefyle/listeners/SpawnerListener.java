package be.sixefyle.listeners;

import be.sixefyle.UGSpawner;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.gui.SpawnerGui;
import be.sixefyle.utils.HologramUtils;
import be.sixefyle.utils.StringUtils;
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
import org.bukkit.inventory.ItemStack;
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
        boolean isSneaking = player.isSneaking();

        if(block.getType().equals(Material.SPAWNER) && e.getAction().isRightClick()){
            CreatureSpawner spawner = (CreatureSpawner) block.getState();
            UGSpawner betterSpawner = UGSpawner.getBetterSpawner(spawner.getLocation());
            ItemStack handItem = player.getInventory().getItemInMainHand();

            boolean isSpawnEgg = handItem.getType().name().contains("_SPAWN_EGG");
            if(isSpawnEgg){
                String spawnEggType = handItem.getType().name().split("_")[0];
                boolean isSameEntityType = spawner.getSpawnedType().name().contains(spawnEggType);


                if(!spawner.getSpawnedType().isSpawnable() || (betterSpawner.getStackAmount() == 1 && !isSameEntityType)) return;

                if(isSameEntityType) {
                    e.setCancelled(true);
                    int eggToUse = 1;
                    if(isSneaking){
                       eggToUse = Math.min(handItem.getAmount(), 64);
                       if(betterSpawner.getStackAmount() + eggToUse > betterSpawner.getMaxStackAmount()){
                           eggToUse = betterSpawner.getMaxStackAmount() - betterSpawner.getStackAmount();
                       }
                    }
                    if(betterSpawner.addStackAmount(eggToUse)){
                        handItem.setAmount(handItem.getAmount() - eggToUse);
                    }
                }
            } else {
                if(isSneaking) return;

                e.setCancelled(true);
                player.openInventory(new SpawnerGui(spawner).getInventory());
            }
        }
    }

    @EventHandler
    public void onBreakSpawner(BlockBreakEvent e){
        Block block = e.getBlock();
        if(block.getType().equals(Material.SPAWNER)){
            UGSpawner ugSpawner = UGSpawner.getSpawners().get(block.getLocation());
            ugSpawner.pickup(e.getPlayer());
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

                    new UGSpawner(maxAmount, amount, maxStackUpgradeLevel, stackUpgradeLevel,
                            power, isSilence, rareDropChance, block.getLocation(), island.get(), entityType);
                } else {
                    new UGSpawner(EntityType.PIG, island.get(), block.getLocation());
                }
            }
        }
    }

    @EventHandler
    public void onCreatureSpawn(SpawnerSpawnEvent e){
        e.setCancelled(true);
        CreatureSpawner spawner = e.getSpawner();
        UGSpawner betterSpawner = UGSpawner.getBetterSpawner(spawner.getLocation());
        if(betterSpawner != null){
            spawnEntity(spawner, e.getLocation(), betterSpawner.getStackAmount());
        }
    }

    private void spawnEntity(CreatureSpawner spawner, Location loc, int amountToAdd){
        if(spawner == null) return;
        UGSpawner betterSpawner = UGSpawner.getBetterSpawner(spawner.getLocation());
        if(betterSpawner == null) return;

        double spawnerPower = betterSpawner.getPower();
        Damageable nearestCreature = getNearestCreature(spawner.getSpawnedType(), spawner.getLocation(), spawnerPower,10);
        if (nearestCreature == null) {
            Damageable ent = (Damageable) spawner.getWorld().spawnEntity(loc, spawner.getSpawnedType());
            double newHealth = ent.getMaxHealth()+(ent.getMaxHealth()*(spawnerPower/50))*(Math.pow(spawnerPower,.78)/100+1);

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
