package be.sixefyle.arena.pve;

import be.sixefyle.UGPlayer;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.entity.UGEntity;
import be.sixefyle.entity.boss.Bosses;
import be.sixefyle.entity.boss.UGBoss;
import be.sixefyle.group.Group;
import be.sixefyle.utils.HologramUtils;
import fr.skytasul.glowingentities.GlowingEntities;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;

public class Wave {

    private int creatureToSpawn;
    private int totalWaveCreatureAmount;
    private final List<Location> spawnLocs;
    private final List<Damageable> aliveCreatures;
    private final World world;
    private final Group group;

    public Wave(int creatureToSpawn, List<Location> spawnLocs, World world, Group group) {
        this.creatureToSpawn = creatureToSpawn;
        this.spawnLocs = spawnLocs;
        this.aliveCreatures = new ArrayList<>();
        this.world = world;
        this.group = group;
    }

    public void setCreatureToSpawnAmount(int creatureToSpawn) {
        this.creatureToSpawn = creatureToSpawn;
    }

    public boolean isEnd(){
        return aliveCreatures.size() <= 0;
    }

    public void end(){
        for (Damageable creature : aliveCreatures) {
            creature.remove();
        }
        aliveCreatures.clear();
    }

    public UGPlayer getNearestPlayer(Location location){
        if(group.getMembers().size() == 1) {
            return group.getOwner();
        }

        double nearestDistance = 9999;
        double distance;
        UGPlayer nearestPlayer = null;
        for (UGPlayer player : group.getMembers()) {
            if(!world.equals(player.getPlayer().getWorld())) {
                continue;
            }

            distance = player.getPlayer().getLocation().distance(location);
            if(distance < nearestDistance){
                nearestDistance = distance;
                nearestPlayer = player;
            }
        }
        return nearestPlayer;
    }

    public void start(double power, int currentWave){
        Location locToSpawn;
        EntityType entityType;
        int random;
        CreatureType[] creatureTypes = CreatureType.values();
        Damageable currentEntity;
        UGPlayer nearestPlayer;

        for (int i = 0; i < creatureToSpawn; i++) {
            locToSpawn = spawnLocs.get((int) (Math.random() * spawnLocs.size()));
            locToSpawn.setWorld(world);

            nearestPlayer = getNearestPlayer(locToSpawn);

            random = (int) (Math.random() * creatureTypes.length);
            entityType = EntityType.valueOf(creatureTypes[random].name());

            while(currentWave < creatureTypes[random].minWave){
                random = (int) (Math.random() * creatureTypes.length);
                entityType = EntityType.valueOf(creatureTypes[random].name());
            }

            UGEntity ugEntity = new UGEntity(power, locToSpawn, entityType, ChatColor.RED);
            ugEntity.registerMetadata("wave", currentWave);
            ugEntity.registerMetadata("arenaWorld", world);
            currentEntity = ugEntity.getEntity();

            if(currentEntity instanceof Monster monster) {
                monster.setTarget(nearestPlayer.getPlayer());
            }
            addGlowToEntity(ugEntity);

            currentEntity.setPersistent(true);
            aliveCreatures.add(currentEntity);
        }

        totalWaveCreatureAmount = aliveCreatures.size();
    }

    public void spawnBoss(double power){
        Location locToSpawn = spawnLocs.get((int) (Math.random() * spawnLocs.size()));
        locToSpawn.setWorld(world);
        try {
            UGBoss boss = Bosses.getRandomBoss(power, locToSpawn);
            aliveCreatures.add(boss.getEntity());
            addGlowToEntity(boss);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addGlowToEntity(UGEntity ugEntity){
        GlowingEntities glowingEntities = UnlimitedGrind.getInstance().getGlowingEntities();
        for (UGPlayer member : group.getMembers()) {
            try {
                glowingEntities.setGlowing(ugEntity.getEntity(), member.getPlayer(), ugEntity.getGlowingColor());
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Damageable> getAliveCreatures() {
        return aliveCreatures;
    }

    public int getTotalWaveCreatureAmount() {
        return totalWaveCreatureAmount;
    }
}
