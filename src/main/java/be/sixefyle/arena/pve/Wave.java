package be.sixefyle.arena.pve;

import be.sixefyle.UnlimitedGrind;
import be.sixefyle.utils.HologramUtils;
import fr.skytasul.glowingentities.GlowingEntities;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Wave {

    private int creatureToSpawn;
    private List<Location> spawnLocs;
    private List<Damageable> creatures;
    private World world;
    private List<Player> players;

    public Wave(int creatureToSpawn, List<Location> spawnLocs, World world, List<Player> players) {
        this.creatureToSpawn = creatureToSpawn;
        this.spawnLocs = spawnLocs;
        this.creatures = new ArrayList<>();
        this.world = world;
        this.players = players;
    }

    public void setCreatureToSpawn(int creatureToSpawn) {
        this.creatureToSpawn = creatureToSpawn;
    }

    public boolean isEnd(){
        return creatures.size() <= 2;
    }

    public void end(){
        for (Damageable creature : creatures) {
            creature.remove();
        }
    }

    public void start(double power){
        Location locToSpawn;
        EntityType entityType;
        int random;
        CreatureType[] creatureTypes = CreatureType.values();
        Damageable currentEntity;
        Player nearestPlayer = players.get(0);
        double nearestDistance = 9999;
        double distance;
        for (int i = 0; i < creatureToSpawn; i++) {
            locToSpawn = spawnLocs.get((int) (Math.random() * spawnLocs.size()));
            locToSpawn.setWorld(world);
            for (Player player : players) {
                distance = player.getLocation().distance(locToSpawn);
                if(distance < nearestDistance){
                    nearestDistance = distance;
                    nearestPlayer = player;
                }
            }

            random = (int) (Math.random() * creatureTypes.length);
            entityType = EntityType.valueOf(creatureTypes[random].name());
            currentEntity = (Damageable) world.spawnEntity(locToSpawn, entityType);

            currentEntity.setMetadata("power", new FixedMetadataValue(UnlimitedGrind.getInstance(), power));
            currentEntity.setMetadata("world", new FixedMetadataValue(UnlimitedGrind.getInstance(), world));

            double newHealth =  currentEntity.getMaxHealth() + currentEntity.getMaxHealth() *
                    Math.pow(power, 1.02912);//TODO: magic number

            currentEntity.setMaxHealth(newHealth);
            currentEntity.setHealth(newHealth);

            HologramUtils.createEntInfoFollow(currentEntity);

            currentEntity.setCustomNameVisible(false);
            ((LivingEntity) currentEntity).setMaximumNoDamageTicks(10);

            if(currentEntity instanceof Monster monster) {
                monster.setTarget(nearestPlayer);
            }

            creatures.add(currentEntity);
        }
    }

    public void spawnBoss(){
        System.out.println("BOSS!!");
    }

    public void addGlowToPlayer(){

    }

    public List<Damageable> getCreatures() {
        return creatures;
    }
}
