package be.sixefyle.entity.boss;

import be.sixefyle.arena.pve.Wave;
import be.sixefyle.entity.UGEntity;
import be.sixefyle.utils.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayList;
import java.util.List;

public class DarkInfester extends UGBoss implements MinionCreator, BossPhase {

    int actualPattern = -1;
    List<UGEntity> spawnCreatures = new ArrayList<>();
    List<EntityType> entityTypeList = new ArrayList<>();

    public DarkInfester(double power, Location location, String name) {
        super(power, location, name, EntityType.WARDEN, ChatColor.DARK_PURPLE);

        entityTypeList.add(EntityType.ZOMBIE);
        entityTypeList.add(EntityType.SKELETON);

        setMaxHealth(getMaxHealth() * 2);
        setAttackDamage(getAttackDamage() * 1.25);
    }

    @Override
    public void startPattern() {
        actualPattern = getRandom().nextInt(2);
        switch (actualPattern){
            case 1 -> setNextPatternTime(creatureSpawn());
        }
    }

    @Override
    public void onDie() {
        super.onDie();
        for (UGEntity spawnCreature : spawnCreatures) {
            spawnCreature.getEntity().remove();
        }
    }

    private int creatureSpawn(){
        if(spawnCreatures.size() >= 50) return 20 * 5;

        UGEntity ugEntity;
        Wave wave = getArenaManager().getWave();
        Location loc;
        for (int i = 0; i < NumberUtils.getRandomNumber(4, 6); i++) {
            loc = this.getEntity().getLocation().clone();
            loc.add(Math.random() * 5, 0, Math.random() * 5);
            Block block = loc.getWorld().getHighestBlockAt(loc);
            loc.getWorld().strikeLightningEffect(block.getLocation());

            ugEntity = new UGEntity(getPower(), block.getLocation(),
                    entityTypeList.get((int) (Math.random() * entityTypeList.size())), ChatColor.GOLD);
            ugEntity.registerMetadata("parent", this);
            ugEntity.registerMetadata("arenaWorld", getWorld());
            ugEntity.setMaxHealth(getMaxHealth() * .055);

            spawnCreatures.add(ugEntity);
            wave.addGlowToEntity(ugEntity);
        }

        return 20 * 10;
    }

    @Override
    public void onMinionTakeDamage(EntityDamageByEntityEvent e, UGEntity parent) {
        if(e.getEntity() instanceof LivingEntity minion){
            double damage = Math.min(e.getFinalDamage(), minion.getHealth());
            parent.getEntity().damage(damage);
            getArenaManager().updateBossBar();
        }
    }

    @Override
    public void onBossTakeDamage(EntityDamageByEntityEvent e) {
        if(e.isCancelled()) return;
        double damage = e.getFinalDamage();
        double health = getEntity().getHealth();
        double maxHealth = getMaxHealth();
        double newPerc = (health - damage) / maxHealth;

        if(newPerc <= .75){
            getEntity().setHealth(maxHealth * .75);
            registerMetadata("canTakeDirectDamage", false);

            setGlowingColor(ChatColor.GRAY);
            getArenaManager().getWave().addGlowToEntity(this);
        }
    }
}
