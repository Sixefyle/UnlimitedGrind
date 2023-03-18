package be.sixefyle;

import be.sixefyle.arena.Arena;
import be.sixefyle.arena.BaseArena;
import be.sixefyle.arena.pve.PveArena;
import be.sixefyle.enums.ComponentColor;
import be.sixefyle.enums.Stats;
import be.sixefyle.enums.Symbols;
import be.sixefyle.exception.PlayerNotExist;
import be.sixefyle.group.Group;
import be.sixefyle.items.UGItem;
import be.sixefyle.utils.NumberUtils;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.database.Island;
import josegamerpt.realscoreboard.api.RealScoreboardAPI;
import josegamerpt.realscoreboard.api.scoreboard.RScoreboard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class UGPlayer {
    public static HashMap<UUID, UGPlayer> playerMap = new HashMap<>();
    private Player player;
    private UGIsland ugIsland;
    private RScoreboard scoreboard;
    private BaseArena arena;
    private Group group;
    private BukkitTask actionBarTask;
    private BukkitTask scoreboardTask;

    public static UGPlayer GetUGPlayer(Player player){
        return playerMap.get(player.getUniqueId());
    }
    public static void RemoveUGPlayer(Player player) {
        playerMap.get(player.getUniqueId()).remove();
        playerMap.remove(player.getUniqueId());
    }

    private void remove(){
        actionBarTask.cancel();
        scoreboardTask.cancel();
        leavePveArena();
        leaveGroup();
    }

    private int level;
    private int experience;
    private double wearedPower;
    private double maxPower;
    private final double baseHealth = 20;
    private double maxHealth;
    private double currentHealth;
    private boolean isHealthLocked;
    private boolean isConnecting;

    private HashMap<Stats, Double> statsMap = new HashMap<>();

    public UGPlayer(Player player) {
        if(playerMap.containsKey(player.getUniqueId())) return;
        this.player = player;

        Optional<Island> island = IridiumSkyblockAPI.getInstance().getUser(player).getIsland();
        if(island.isPresent()) {
            ugIsland = new UGIsland(island);
        }

        try {
            getPlayerData();
        } catch (PlayerNotExist e) {
            wearedPower = 0;
            maxPower = 0;
            level = 1;
            experience = 0;
            isHealthLocked = false;

            statsMap.put(Stats.CRITICAL_CHANCE, 0.05);
            statsMap.put(Stats.CRITICAL_DAMAGE, 1.4);
            statsMap.put(Stats.LIFE_STEAL, 0.0);
            statsMap.put(Stats.HEALTH, 0.0);
            statsMap.put(Stats.STRENGTH, 0.0);
            statsMap.put(Stats.VITALITY, 0.0);
            statsMap.put(Stats.SWEEPING_RANGE, 1.0);
            statsMap.put(Stats.SWEEPING_DAMAGE, 1.0);
            statsMap.put(Stats.MELEE_DAMAGE_REDUCTION, 1.0);
            statsMap.put(Stats.RANGE_DAMAGE_REDUCTION, 1.0);
            statsMap.put(Stats.BONUS_CRITICAL_CHANCE, 0.0);
            statsMap.put(Stats.BONUS_CRITICAL_DAMAGE, 0.0);
        }
        isConnecting = true;

        initScoreboard();
        updateScoreboardLine();
        startActionBarRefresh();
        playerMap.putIfAbsent(player.getUniqueId(), this);
    }

    public void initScoreboard(){
        Island island = ugIsland.getIsland().get();
        scoreboard = RealScoreboardAPI.getInstance().getScoreboardManager().getScoreboard(player);
        scoreboard.getLines().clear();

        List<String> lines = new ArrayList<>() {{
            add("         #{f79f07}&l⭐#{f0aa32} Rank " + island.getRank() + " #{f79f07}&l⭐");
            add("");
            add("#{e23f22}" + Symbols.PLAYER.get() + "#{e25822} Player Informations:" );
            add("  #{e23f22}▸#{E6EED6} Items Power &c" + NumberUtils.format(wearedPower) + Symbols.POWER.get());
            add("  #{e23f22}▸#{E6EED6} Max Power &c" + NumberUtils.format(maxPower) + Symbols.POWER.get());
            add("  #{e23f22}▸#{E6EED6} Money &e" + NumberUtils.format(UnlimitedGrind.getEconomy().getBalance(player)) + Symbols.COIN.get());
            add("");
            add("#{48bff0}" + Symbols.ISLAND.get() + "#{87CEEB} Island Informations:");
            add("  #{48bff0}▸#{E4F0D0} Value - " + NumberUtils.format(island.getValue()));
            add("  #{48bff0}▸#{E4F0D0} Level - " + NumberUtils.format(island.getLevel()));
            add("  #{48bff0}▸#{E4F0D0} Bank");
            add("    #{48bff0}▹#{E4F0D0} Money - " + island.getMoney());
            add("    #{48bff0}▹#{E4F0D0} Crystals - " + island.getCrystals());
        }};

        scoreboard.getLines().addAll(lines);
    }

    public void updateScoreboardLine(){
        scoreboardTask = Bukkit.getScheduler().runTaskTimer(UnlimitedGrind.getInstance(), () -> {
            Island island = ugIsland.getIsland().get();
            scoreboard.getLines().set(0, "         #{f79f07}&l⭐#{f0aa32} Rank " + ugIsland.getIsland().get().getRank() + " #{f79f07}&l⭐");
            scoreboard.getLines().set(3, "  #{e23f22}▸#{E6EED6} Items Power &c" + NumberUtils.format(wearedPower) + Symbols.POWER.get());
            scoreboard.getLines().set(4, "  #{e23f22}▸#{E6EED6} Max Power &c" + NumberUtils.format(maxPower) + Symbols.POWER.get());
            scoreboard.getLines().set(5, "  #{e23f22}▸#{E6EED6} Money &e" + NumberUtils.format(UnlimitedGrind.getEconomy().getBalance(player)) + Symbols.COIN.get());
            scoreboard.getLines().set(8, "  #{48bff0}▸#{E4F0D0} Value - " + NumberUtils.format(island.getValue()));
            scoreboard.getLines().set(9, "  #{48bff0}▸#{E4F0D0} Level - " + NumberUtils.format(island.getLevel()));
            scoreboard.getLines().set(11, "    #{48bff0}▹#{E4F0D0} Money - " + island.getMoney() + Symbols.COIN.get());
            scoreboard.getLines().set(12, "    #{48bff0}▹#{E4F0D0} Crystals - " + island.getCrystals() + Symbols.CRYSTALS.get());
        }, 100, 100);
    }

    public void startActionBarRefresh(){
        actionBarTask = new BukkitRunnable() {
            @Override
            public void run() {
                if(!player.isOnline()) {
                    cancel();
                    return;
                }
                Component component = Component.empty()
                                .append(Component.text(Symbols.HEALTH.get() + " " + String.format("%.0f", getHealth()) + "/" + NumberUtils.format(getMaxHealth()) + " ")
                                                .color(getHealthColor()))
                                .append(Component.text( Symbols.ARMOR.get() + " " + String.format("%.0f", getArmorValue()) + " (" + String.format("%.1f", getDamageReduction()*100) + "%)")
                                                .color(ComponentColor.ARMOR.getColor()));


                player.sendActionBar(component);
            }
        }.runTaskTimer(UnlimitedGrind.getInstance(), 20, 20);
    }

    public TextColor getHealthColor(){
        double healthPercentage = getHealthPercentage();
        return TextColor.color(
                (int) (healthPercentage * 67) + (int) ((1-healthPercentage) * 229),
                (int) (healthPercentage * 255) + (int) ((1-healthPercentage) * 90),
                (int) (healthPercentage * 93) + (int) ((1-healthPercentage) * 30)
        );
    }

    public void addPower(int power) {
        this.wearedPower += power;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isConnecting() {
        return isConnecting;
    }

    public void setConnecting(boolean connecting) {
        isConnecting = connecting;
    }

    public int getLevel() {
        return level;
    }

    public int getExperience() {
        return experience;
    }

    public double getWearedPower() {
        return wearedPower;
    }

    public void setWearedPower(double wearedPower) {
        this.wearedPower = wearedPower;
    }

    public void updateWearedPower(){
        UGItem ugItem;
        double tempPower = 0;
        for (ItemStack content : getEquippedItems()) {
            if(content == null) continue;
            ugItem = UGItem.getFromItemStack(content);
            if(ugItem == null) continue;
            tempPower += ugItem.getPower();
        }
        wearedPower = tempPower / getEquippedItems().size();
        if(wearedPower > maxPower){
            maxPower = wearedPower;
            updateAllItemsPowerConditionLore();
        }
    }

    public double getMaxPower() {
        return maxPower;
    }

    public double getMaxWearablePower(){
        return maxPower + 1000;
    }

    public double neededPower(double itemPower){
        return itemPower - getMaxWearablePower();
    }

    public boolean canEquipItem(UGItem ugItem){
        if(ugItem == null) return true;
        return getMaxWearablePower() >= ugItem.getPower();
    }

    public void updateAllItemsPowerConditionLore(){
        UGItem ugItem;
        for (ItemStack content : player.getInventory().getContents()) {
            if(content == null) continue;
            ugItem = UGItem.getFromItemStack(content);
            if(ugItem == null) continue;

            ugItem.updateConditionLore(this);
        }
    }

    public void getPlayerData() throws PlayerNotExist {

        throw new PlayerNotExist();
    }

    public double getArmorValue() {
        double armor = 0;
        ItemMeta itemMeta;
        for (ItemStack itemStack : getArmorAndOffHand()) {
            if(itemStack == null) continue;

            itemMeta = itemStack.getItemMeta();
            if(itemMeta == null) continue;
            if(itemMeta.hasAttributeModifiers() && itemMeta.getAttributeModifiers(Attribute.GENERIC_ARMOR) != null){
                for (AttributeModifier attributeModifier : itemMeta.getAttributeModifiers(Attribute.GENERIC_ARMOR)) {
                    armor += attributeModifier.getAmount();
                }
            }
        }
        return armor;
    }

    public double getDamageReduction(){
        double playerArmor = getArmorValue();

        return (playerArmor / (playerArmor + 15000));
    }

    public void setHealthFromStat(){
        setMaxHealth(baseHealth + (getStatValue(Stats.VITALITY) * 3) + getStatValue(Stats.HEALTH));
        if(currentHealth > maxHealth){
            setHealth(maxHealth);
        }
        updatePlayerHeartBar();
    }

    public void updatePlayerHeartBar(){
        player.setHealth(Math.max(1, (currentHealth / maxHealth) * 20));
    }

    public void setHealth(double health){
        currentHealth = Math.min(health, maxHealth);
        if(currentHealth <= 0){
            kill();
        }
        updatePlayerHeartBar();
    }

    public void setMaxHealth(double maxHealth){
        if(!isHealthLocked()){
            this.maxHealth = Math.max(1, maxHealth);
            if(currentHealth > maxHealth){
                setHealth(maxHealth);
            }
        }
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public double getHealth() {
        return currentHealth;
    }

    public double getHealthPercentage(){
        return currentHealth / maxHealth;
    }

    public boolean isHealthLocked() {
        return isHealthLocked;
    }

    public void setHealthLocked(boolean healthLocked) {
        isHealthLocked = healthLocked;
    }

    public void kill(){
        setHealth(getMaxHealth());
        Bukkit.getServer().getPluginManager().callEvent(new PlayerDeathEvent(player, new ArrayList<>(), 0, 0, ""));
        respawn();
    }

    public void respawn(){
        if(ugIsland.getIsland().isPresent()) {
            Island island = ugIsland.getIsland().get();
            player.teleport(island.getHome());
        } else {
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        }
    }

    public void regenHealth(double heath){
        setHealth(currentHealth + heath);
    }

    public void takeDamage(double damage){
        setHealth(currentHealth - damage);
    }

    public Group getGroup() {
        return group;
    }

    public Group setGroup(Group group) {
        return this.group = group;
    }

    public boolean hasGroup(){
        return group != null;
    }

    public boolean leaveGroup(){
        if(group == null) return false;

        group.removePlayer(this);
        group = null;
        return true;
    }

    public List<ItemStack> getArmorAndOffHand(){
        List<ItemStack> armor = new ArrayList<>(Arrays.asList(player.getInventory().getArmorContents()));
        armor.add(player.getInventory().getItemInOffHand());

        return armor;
    }

    public List<ItemStack> getEquippedItems(){
        List<ItemStack> items = getArmorAndOffHand();
        items.add(player.getInventory().getItemInMainHand());
        return items;
    }

    public void shouldAllowArmorChange(boolean bool){
        ItemStack[] armor = player.getInventory().getArmorContents();

        if(bool){
            for (ItemStack armorContent : armor) {
                if(armorContent == null) continue;
                armorContent.removeEnchantment(Enchantment.BINDING_CURSE);
            }
        } else {
            for (ItemStack armorContent : armor) {
                if(armorContent == null) continue;
                armorContent.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
            }
        }
    }

    public void setupStatsFromEquippedItems(){
        UGItem ugItem;
        for (ItemStack item : getEquippedItems()) {
            if(item == null) continue;
            ugItem = UGItem.getFromItemStack(item);
            if(ugItem == null) continue;
            ugItem.getStatsMap().forEach(this::addStatValue);
        }
        double healthPerc = player.getHealth()/player.getHealthScale();
        setHealthFromStat();
        setHealth(healthPerc * getMaxHealth());
        updateWearedPower();
    }

    public HashMap<Stats, Double> getStatsMap() {
        return statsMap;
    }

    public void updateStatsFromItem(UGItem newItem, UGItem oldItem){
        if(newItem != null) {
            newItem.getStatsMap().forEach((stat, value) -> {
                if(statsMap.containsKey(stat)){
                    addStatValue(stat, value);
                }
            });
        }
        if(oldItem != null){
            oldItem.getStatsMap().forEach((stat, value) -> {
                if(statsMap.containsKey(stat)){
                    addStatValue(stat, -value);
                }
            });
        }
    }

    public double getStatValue(Stats stat) {
        return Math.round(statsMap.get(stat));
    }

    public void setStatValue(Stats stat, double value) {
        statsMap.replace(stat, value);
    }

    public void addStatValue(Stats stat, double toAdd){
        if(statsMap.containsKey(stat)){
            setStatValue(stat, statsMap.get(stat) + toAdd);
        }
    }

    public void joinPveArena(Arena arena, double power){
        if(hasGroup()){
            this.arena = new PveArena(getGroup(), arena);
        } else {
            this.arena = new PveArena(this, arena);
        }
        this.arena.join(power);
        shouldAllowArmorChange(false);
        player.setGameMode(GameMode.ADVENTURE);
    }


    public void leavePveArena(){
        this.arena = null;
        shouldAllowArmorChange(true);
        player.setGameMode(GameMode.SURVIVAL);
        respawn();
    }

    public BaseArena getArena() {
        return arena;
    }

    public boolean isInArena(){
        return arena != null;
    }

    public void sendMessageComponents(List<Component> components){
        int spaceToDo = 2;
        Component messageToSend = Component.empty();
        for (int i = 0; i < spaceToDo ; i++) {
            messageToSend = messageToSend.append(Component.newline());
        }
        for (Component component : components) {
            messageToSend = messageToSend.append(component).append(Component.newline());
        }
        for (int i = 0; i < spaceToDo-1; i++) {
            messageToSend = messageToSend.append(Component.newline());
        }

        getPlayer().sendMessage(messageToSend);
    }

    public void sendMessageComponents(Component component){
        sendMessageComponents(List.of(component));
    }
}
