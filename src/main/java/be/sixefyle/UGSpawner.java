package be.sixefyle;

import be.sixefyle.utils.PlaceholderUtils;
import com.iridium.iridiumskyblock.database.Island;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.line.TextHologramLine;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UGSpawner {
    public static HashMap<Location, UGSpawner> spawners = new HashMap<>();

    private int maxStackAmount = 100;
    private int stackAmount;
    private int maxStackUpgradeLevel = 90;
    private int stackUpgradeLevel;
    private EntityType entityType;
    private double power;
    private Island island;
    private boolean isSilence;
    private double rareDropChance;
    private double maxRareDropChance = 5.0;

    private CreatureSpawner spawner;
    private Hologram hologramTitle;

    private void init(Location loc){
        initSpawner(loc);
        initHologramTitle();
        spawners.put(loc, this);
    }

    public UGSpawner(EntityType entityType, Island island, Location loc) {
        this.stackAmount = 1;
        this.entityType = entityType;
        this.power = 0.0;
        this.island = island;
        this.isSilence = false;
        this.rareDropChance = 0.0;
        this.stackUpgradeLevel = 0;

        init(loc);
    }

    public UGSpawner(int maxStackAmount, int stackAmount, int maxStackUpgradeLevel, int stackUpgradeLevel, double power, boolean isSilence, double rareDropChance, Location loc, Island island, EntityType entityType) {
        this.maxStackAmount = maxStackAmount;
        this.stackAmount = stackAmount;
        this.maxStackUpgradeLevel = maxStackUpgradeLevel;
        this.stackUpgradeLevel = stackUpgradeLevel;
        this.power = power;
        this.isSilence = isSilence;
        this.rareDropChance = rareDropChance;
        this.island = island;
        this.entityType = entityType;

        init(loc);
    }

    public void initSpawner(Location loc){
        spawner = (CreatureSpawner) loc.getBlock().getState();
        if(entityType != null){
            spawner.setSpawnedType(entityType);
        }
        spawner.setRequiredPlayerRange(50);
        spawner.update();
    }

    public void initHologramTitle(){
        Location loc = spawner.getLocation().toCenterLocation().clone();
        FileConfiguration config = UnlimitedGrind.getInstance().getConfig();
        loc = loc.add(0,1,0);
        hologramTitle = UnlimitedGrind.getHolographicApi().createHologram(loc);
        UGSpawner betterSpawner = this;

        String firstLine = config.getString("spawner.title.typeAndPower");
        String secondLine = config.getString("spawner.title.amount");
        TextHologramLine typeAndPowerLine = hologramTitle.getLines().appendText(firstLine);
        TextHologramLine amountLine = hologramTitle.getLines().appendText(secondLine);


        new BukkitRunnable() {
            @Override
            public void run() {
                if(!spawner.getBlock().getType().equals(Material.SPAWNER)) {
                    hologramTitle.delete();
                    cancel();
                    return;
                }
                typeAndPowerLine.setText(PlaceholderUtils.replace(betterSpawner, firstLine));
                amountLine.setText(PlaceholderUtils.replace(betterSpawner, secondLine));
            }
        }.runTaskTimer(UnlimitedGrind.getInstance(), 0, 100);
    }

    public void addPower(double power){
        this.power += power;
    }

    public int getStackAmount() {
        return stackAmount;
    }

    private EntityType getEntityType() {
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
    }

    public void invertSilenceMode() {
        setSilenceMode(!isSilence);
    }

    public int getMaxStackAmount() {
        return maxStackAmount;
    }

    public void setMaxStackAmount(int maxStackAmount) {
        this.maxStackAmount = maxStackAmount;
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

    public boolean canAddDropChance(){
        return rareDropChance < maxRareDropChance;
    }

    public boolean addRareDropChance(){
        if(canAddDropChance()) {
            rareDropChance += .1;
            return true;
        }
        return false;
    }

    public void addStackUpgradeLevel(int amount){
        this.stackUpgradeLevel += amount;
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
        hologramTitle.delete();
        spawner.getLocation().getBlock().setType(Material.AIR);
    }

    protected <T, Z> void createNewPersistentDataContainer(ItemMeta itemMeta, String id, PersistentDataType<T, Z> type, Z value){
        itemMeta.getPersistentDataContainer().set(new NamespacedKey(UnlimitedGrind.getInstance(), id), type, value);
    }

    public void pickup(Player player){
        FileConfiguration config = UnlimitedGrind.getInstance().getConfig();
        ItemStack pickupSpawner = new ItemStack(Material.SPAWNER, 1);
        ItemMeta pickupSpawnerMeta = pickupSpawner.getItemMeta();

        pickupSpawnerMeta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);

        String name = PlaceholderUtils.replace(this, config.getString("lang.spawner.gui.pickedUp.name"));
        pickupSpawnerMeta.displayName(Component.text(name));

        List<Component> lore = new ArrayList<>();
        String result;
        for (String line : config.getStringList("lang.spawner.gui.pickedUp.lore")) {
            result = PlaceholderUtils.replace(this, line);
            lore.add(Component.text(result));
        }
        pickupSpawnerMeta.lore(lore);

        createNewPersistentDataContainer(pickupSpawnerMeta, "power", PersistentDataType.DOUBLE, getPower());
        createNewPersistentDataContainer(pickupSpawnerMeta, "amount", PersistentDataType.INTEGER, getStackAmount());
        createNewPersistentDataContainer(pickupSpawnerMeta, "maxAmount", PersistentDataType.INTEGER, getMaxStackAmount());
        createNewPersistentDataContainer(pickupSpawnerMeta, "stackUpgradeLevel", PersistentDataType.INTEGER, getStackUpgradeLevel());
        createNewPersistentDataContainer(pickupSpawnerMeta, "maxStackUpgradeLevel", PersistentDataType.INTEGER, getMaxStackUpgradeLevel());
        createNewPersistentDataContainer(pickupSpawnerMeta, "rareDropChance", PersistentDataType.DOUBLE, getRareDropChance());
        createNewPersistentDataContainer(pickupSpawnerMeta, "silence", PersistentDataType.BYTE, (byte) (isSilence() ?  1 : 0));
        createNewPersistentDataContainer(pickupSpawnerMeta, "entityType", PersistentDataType.STRING, getSpawner().getSpawnedType().name());

        pickupSpawner.setItemMeta(pickupSpawnerMeta);

        player.getInventory().addItem(pickupSpawner);
        this.remove();
        player.closeInventory();
    }

    public static double getUgradePrice(double currentPower, int upgradeAmount) {
        int base = 50;
        double growRate = UnlimitedGrind.getInstance().getConfig().getDouble("power.currencyConvertion");
        double price = 0; //TODO: price

        return price;
    }

    public static UGSpawner getBetterSpawner(Location loc){
        return spawners.get(loc);
    }

    public static HashMap<Location, UGSpawner> getSpawners() {
        return spawners;
    }

}
