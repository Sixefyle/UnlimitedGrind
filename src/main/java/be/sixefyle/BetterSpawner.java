package be.sixefyle;

import com.iridium.iridiumskyblock.database.Island;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;

public class BetterSpawner {
    public static HashMap<Location, BetterSpawner> spawners = new HashMap<>();

    private int maxStackAmount = 100;
    private int stackAmount;
    private int maxStackUpgradeLevel = 90;
    private int stackUpgradeLevel;
    private EntityType entityType;
    private double power;
    private Island island;
    private boolean isSilence;
    private double rareDropChance;

    private CreatureSpawner spawner;

    public BetterSpawner(EntityType entityType, Island island, Location loc) {
        this.stackAmount = 1;
        this.entityType = entityType;
        this.power = 0.0;
        this.island = island;
        this.isSilence = false;
        this.rareDropChance = 0.0;
        this.stackUpgradeLevel = 0;

        spawner = initSpawner(loc);
        spawners.put(loc, this);
    }

    public BetterSpawner(int maxStackAmount, int stackAmount, int maxStackUpgradeLevel, int stackUpgradeLevel, double power, boolean isSilence, double rareDropChance) {
        this.maxStackAmount = maxStackAmount;
        this.stackAmount = stackAmount;
        this.maxStackUpgradeLevel = maxStackUpgradeLevel;
        this.stackUpgradeLevel = stackUpgradeLevel;
        this.power = power;
        this.isSilence = isSilence;
        this.rareDropChance = rareDropChance;
    }

    public CreatureSpawner initSpawner(Location loc){
        CreatureSpawner spawner = (CreatureSpawner) loc.getBlock().getState();
        spawner.setSpawnedType(entityType);
        spawner.setMetadata("power", new FixedMetadataValue(UnlimitedGrind.getInstance(), power));
        spawner.setMetadata("amount", new FixedMetadataValue(UnlimitedGrind.getInstance(), stackAmount));
        spawner.setMetadata("silenceMode", new FixedMetadataValue(UnlimitedGrind.getInstance(), false));
        spawner.update();
        return spawner;
    }

    public void addPower(double power){
        this.power += power;
        spawner.setMetadata("power", new FixedMetadataValue(UnlimitedGrind.getInstance(), this.power));
    }

    public int getStackAmount() {
        return stackAmount;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public double getPower() {
        return power;
    }

    public Island getIsland() {
        return island;
    }

    public CreatureSpawner getSpawner() {
        return spawner;
    }

    public boolean isSilence() {
        return isSilence;
    }

    public void setSilenceMode(boolean silence) {
        isSilence = silence;
        spawner.setMetadata("silenceMode", new FixedMetadataValue(UnlimitedGrind.getInstance(), silence));
    }

    public void invertSilenceMode() {
        setSilenceMode(!isSilence);
    }

    public int getMaxStackAmount() {
        return maxStackAmount;
    }

    public void setMaxStackAmount(int maxStackAmount) {
        this.maxStackAmount = maxStackAmount;
        spawner.setMetadata("maxAmount", new FixedMetadataValue(UnlimitedGrind.getInstance(), maxStackAmount));
    }

    public void addMaxStackAmount(int amount){
        setMaxStackAmount(getMaxStackAmount() + (amount * 10));
    }

    public boolean canAddStackAmount() {
        return stackAmount < maxStackAmount;
    }

    public boolean addStackAmount(int amount) {
        if(canAddStackAmount()){
            stackAmount += amount;
            spawner.setMetadata("amount", new FixedMetadataValue(UnlimitedGrind.getInstance(), stackAmount));
            return true;
        }
        return false;
    }

    public boolean addStackAmount() {
        return addStackAmount(1);
    }

    public double getRareDropChance() {
        return rareDropChance;
    }

    public void addStackUpgradeLevel(int amount){
        this.stackUpgradeLevel += amount;
        spawner.setMetadata("stackUpgradeLevel", new FixedMetadataValue(UnlimitedGrind.getInstance(), this.stackUpgradeLevel));
        addMaxStackAmount(amount);
    }

    public void addStackUpgradeLevel(){
        addStackUpgradeLevel(1);
    }

    public int getStackUpgradeLevel() {
        return stackUpgradeLevel;
    }

    public int getMaxStackUpgradeLevel() {
        return maxStackUpgradeLevel;
    }

    public double getUgradePrice(int upgradeAmount) {
        double price = getPower() * 0.344;
        for (int i = 0; i < upgradeAmount / 10 ; i++) {
            price += 100;
        }
        return price;
    }

    public void remove(){
        spawners.remove(spawner.getLocation());
        spawner.getLocation().getBlock().setType(Material.AIR);
    }


    public static double getUgradePrice(double currentPower, int upgradeAmount) {
        int base = 50;
        double growRate = UnlimitedGrind.getInstance().getConfig().getDouble("power.currencyConvertion");
        double price = 10; //TODO: price

        return price;
    }

    public static BetterSpawner getBetterSpawner(Location loc){
        return spawners.get(loc);
    }

    public static HashMap<Location, BetterSpawner> getSpawners() {
        return spawners;
    }
}
