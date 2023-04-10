package be.sixefyle.entity;

import be.sixefyle.UnlimitedGrind;
import be.sixefyle.utils.HologramUtils;
import fr.skytasul.glowingentities.GlowingEntities;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class UGEntity {
    private final LivingEntity entity;
    private final World world;
    private final Location location;

    private double power;
    private double maxHealth;
    private double attackDamage;
    private final EntityType entityType;
    private final Map<String, FixedMetadataValue> metadataMap;
    private ChatColor glowingColor;

    public UGEntity(double power, Location location, EntityType creatureType, ChatColor glowingColor) {
        this.power = power;
        this.entityType = creatureType;
        this.glowingColor = glowingColor;
        this.location = location;
        this.world = location.getWorld();
        this.metadataMap = new HashMap<>();

        this.entity = (LivingEntity) world.spawnEntity(location, EntityType.valueOf(creatureType.name()));

        this.maxHealth = this.entity.getMaxHealth()+(this.entity.getMaxHealth()*(power/50))*(Math.pow(power,.78)/100+1);
        this.attackDamage = (power/80 + 1);

        this.entity.setMaxHealth(maxHealth);
        this.entity.setHealth(maxHealth);

        this.entity.setCustomNameVisible(false);
        this.entity.setMaximumNoDamageTicks(10);

        HologramUtils.createEntInfoFollow(this.entity);
        registerMetadata("ugEntity", this);
    }

    public void registerMetadata(String id, Object value){
        FixedMetadataValue fixedMetadataValue = new FixedMetadataValue(UnlimitedGrind.getInstance(), value);
        metadataMap.put(id, fixedMetadataValue);
        entity.setMetadata(id, fixedMetadataValue);
    }

    public boolean hasMetadata(String id){
        return metadataMap.containsKey(id);
    }

    public void updateMetadata(String id, Object newValue){
        if(hasMetadata(id)){
            Plugin plugin = UnlimitedGrind.getInstance();
            entity.removeMetadata(id, plugin);
            metadataMap.remove(id);
            registerMetadata(id, newValue);
        }
    }

    public Object getMetadata(String id){
        if(hasMetadata(id)){
            return metadataMap.get(id);
        }
        return null;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public World getWorld() {
        return world;
    }

    public Location getLocation() {
        return location;
    }

    public double getPower() {
        return power;
    }

    public double getHealth() {
        return entity.getHealth();
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
        entity.setMaxHealth(maxHealth);
        entity.setHealth(maxHealth);
    }

    public double getFinalDamage(double baseDamage) {
        return baseDamage + baseDamage * attackDamage;
    }

    public double getAttackDamage() {
        return attackDamage;
    }

    public void setAttackDamage(double attackDamage) {
        this.attackDamage = attackDamage;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public Map<String, FixedMetadataValue> getMetadataMap() {
        return metadataMap;
    }

    public ChatColor getGlowingColor() {
        return glowingColor;
    }

    public void setGlowingColor(ChatColor glowingColor) {
        this.glowingColor = glowingColor;
    }

    public static UGEntity getUGEntity(Entity entity){
        if(!entity.hasMetadata("ugEntity")) return null;
        return (UGEntity) entity.getMetadata("ugEntity").get(0).value();
    }
}
