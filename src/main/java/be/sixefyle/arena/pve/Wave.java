package be.sixefyle.arena.pve;

import be.sixefyle.UGPlayer;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.utils.HologramUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;

public class Wave {

    private int creatureToSpawn;
    private List<Location> spawnLocs;
    private List<Damageable> aliveCreatures;
    private World world;
    private List<UGPlayer> players;

    public Wave(int creatureToSpawn, List<Location> spawnLocs, World world, List<UGPlayer> players) {
        this.creatureToSpawn = creatureToSpawn;
        this.spawnLocs = spawnLocs;
        this.aliveCreatures = new ArrayList<>();
        this.world = world;
        this.players = players;
    }

    public void setCreatureToSpawnAmount(int creatureToSpawn) {
        this.creatureToSpawn = creatureToSpawn;
    }

    public boolean isEnd(){
        return aliveCreatures.size() <= 2;
    }

    public void end(){
        for (Damageable creature : aliveCreatures) {
            creature.remove();
        }
    }

    public void start(double power, int currentWave){
        Location locToSpawn;
        EntityType entityType;
        int random;
        CreatureType[] creatureTypes = CreatureType.values();
        Damageable currentEntity;
        UGPlayer nearestPlayer = players.get(0);
        double nearestDistance = 9999;
        double distance;
        for (int i = 0; i < creatureToSpawn; i++) {
            locToSpawn = spawnLocs.get((int) (Math.random() * spawnLocs.size()));
            locToSpawn.setWorld(world);
            for (UGPlayer player : players) {
                distance = player.getPlayer().getLocation().distance(locToSpawn);
                if(distance < nearestDistance){
                    nearestDistance = distance;
                    nearestPlayer = player;
                }
            }

            random = (int) (Math.random() * creatureTypes.length);
            entityType = EntityType.valueOf(creatureTypes[random].name());

            while(currentWave < creatureTypes[random].minWave){
                random = (int) (Math.random() * creatureTypes.length);
                entityType = EntityType.valueOf(creatureTypes[random].name());
            }

            currentEntity = (Damageable) world.spawnEntity(locToSpawn, entityType);
            currentEntity.setMetadata("power", new FixedMetadataValue(UnlimitedGrind.getInstance(), power));
            currentEntity.setMetadata("world", new FixedMetadataValue(UnlimitedGrind.getInstance(), world));
            currentEntity.setMetadata("wave", new FixedMetadataValue(UnlimitedGrind.getInstance(), currentWave));

            //TODO: magic number
            double newHealth =
                    currentEntity.getMaxHealth()+(currentEntity.getMaxHealth()*(power/20000))*(Math.pow(power,.78)/100+1);

            currentEntity.setMaxHealth(newHealth);
            currentEntity.setHealth(newHealth);

            HologramUtils.createEntInfoFollow(currentEntity);

            currentEntity.setCustomNameVisible(false);
            ((LivingEntity) currentEntity).setMaximumNoDamageTicks(10);

            if(currentEntity instanceof Monster monster) {
                monster.setTarget(nearestPlayer.getPlayer());
            }
            addGlowToEntity(currentEntity, ChatColor.WHITE);

            aliveCreatures.add(currentEntity);
        }
    }

    public void spawnBoss(){
        System.out.println("BOSS!!");
    }

    public void addGlowToEntity(Entity entity, ChatColor color){
        for (UGPlayer ugPlayer : players) {
            try {
                UnlimitedGrind.getGlowingEntities().setGlowing(entity, ugPlayer.getPlayer(), color);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Damageable> getAliveCreatures() {
        return aliveCreatures;
    }
}
