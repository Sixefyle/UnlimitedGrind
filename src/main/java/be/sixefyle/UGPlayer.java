package be.sixefyle;

import be.sixefyle.arena.ArenaMap;
import be.sixefyle.arena.BaseArena;
import be.sixefyle.arena.pve.PveArena;
import be.sixefyle.enums.ComponentColor;
import be.sixefyle.enums.Stats;
import be.sixefyle.enums.Symbols;
import be.sixefyle.event.UgPlayerDieEvent;
import be.sixefyle.exception.PlayerNotExist;
import be.sixefyle.group.Group;
import be.sixefyle.items.*;
import be.sixefyle.utils.NumberUtils;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.database.Island;
import fr.mrmicky.fastboard.FastBoard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class UGPlayer {
    public final static HashMap<UUID, UGPlayer> playerMap = new HashMap<>();

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
        activesPotionsMap.forEach((potionEffectType, bukkitTask) -> {
            bukkitTask.cancel();
        });
        leaveArena();
        leaveGroup();
    }

    private Player player;
    private UGIsland ugIsland;
    private FastBoard scoreboard;
    private BaseArena arena;
    private Group group;
    private BukkitTask actionBarTask;
    private BukkitTask scoreboardTask;

    private int level;
    private int experience;
    private double wearedPower;
    private double maxPower;
    private final double baseHealth = 20;
    private double currentHealth;
    private boolean isHealthLocked;
    private boolean isConnecting;
    private final HashMap<PotionEffectType, BukkitTask> activesPotionsMap = new HashMap<>();

    private final HashMap<Stats, Double> statsMap = new HashMap<>();

    public UGPlayer(Player player) {
        try{
            if(playerMap.containsKey(player.getUniqueId())) return;
            this.player = player;

            Optional<Island> island = IridiumSkyblockAPI.getInstance().getUser(player).getIsland();
            if(island.isPresent()) {
                ugIsland = new UGIsland(island);
            }

            try {
                getPlayerData();
            } catch (PlayerNotExist e) {
                wearedPower = 1;
                maxPower = 1;
                level = 1;
                experience = 0;
                isHealthLocked = false;

                statsMap.put(Stats.CRITICAL_CHANCE, 0.05);
                statsMap.put(Stats.CRITICAL_DAMAGE, 1.4);
                statsMap.put(Stats.LIFE_STEAL, 0.0);
                statsMap.put(Stats.HEALTH, baseHealth);
                statsMap.put(Stats.ARMOR, 0.0);
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
        } catch (Exception e){
            player.kick(Component.text("An error occurred on loading player info, please try again")
                    .color(ComponentColor.ERROR.getColor()));
            e.printStackTrace();
        }
    }

    public void initScoreboard(){
        Island island = null;
        if(ugIsland != null && ugIsland.getIsland().isPresent()){
            island = ugIsland.getIsland().get();
        }

        scoreboard = new FastBoard(player);
        scoreboard.updateTitle("test");

        Island finalIsland = island;
        List<String> lines = new ArrayList<>() {{
            if(finalIsland != null){
                add(ChatColor.of("#f79f07") +"         §l⭐"+ ChatColor.of("#f0aa32") +" Rank " + finalIsland.getRank() + ChatColor.of("#f79f07") + " §l⭐");
                add("");
            }
            add(ChatColor.of("#e23f22") + Symbols.PLAYER.get() + ChatColor.of("#e25822") + " Player Informations:" );
            add(ChatColor.of("#e23f22") + "  ▸"+ ChatColor.of("#E6EED6") + " Items Power §c" + NumberUtils.format(wearedPower) + Symbols.POWER.get());
            add(ChatColor.of("#e23f22") + "  ▸"+ ChatColor.of("#E6EED6") + " Max Power §c" + NumberUtils.format(maxPower) + Symbols.POWER.get());
            add(ChatColor.of("#e23f22") + "  ▸"+ ChatColor.of("#E6EED6") + " Money §e" + NumberUtils.format(UnlimitedGrind.getEconomy().getBalance(player)) + Symbols.COIN.get());
            add("");
            if(finalIsland != null){
                add(ChatColor.of("#48bff0") + Symbols.ISLAND.get() + ChatColor.of("#87CEEB") + " Island Informations:");
                add(ChatColor.of("#48bff0") + "  ▸" + ChatColor.of("#E4F0D0") + " Value - " + NumberUtils.format(finalIsland.getValue()));
                add(ChatColor.of("#48bff0") + "  ▸" + ChatColor.of("#E4F0D0") + " Level - " + NumberUtils.format(finalIsland.getLevel()));
                add(ChatColor.of("#48bff0") + "  ▸" + ChatColor.of("#E4F0D0") + " Bank");
                add(ChatColor.of("#48bff0") + "    ▹" + ChatColor.of("#E4F0D0") + " Money - " + finalIsland.getMoney());
                add(ChatColor.of("#48bff0") + "    ▹" + ChatColor.of("#E4F0D0") + " Crystals - " + finalIsland.getCrystals());
            } else {
                add(ChatColor.of("#48bff0") + " ▸" + ChatColor.of("#E4F0D0") + " Do " + ChatColor.of("#48bff0") + "/is create" + ChatColor.of("#E4F0D0") + " to start your journey! " + ChatColor.of("#48bff0") + "◂");
            }
        }};

        scoreboard.updateLines(lines);
    }

    public void updateScoreboardLine(){
        scoreboardTask = Bukkit.getScheduler().runTaskTimer(UnlimitedGrind.getInstance(), () -> {
            updateScoreboardPower();
            if(ugIsland != null && ugIsland.getIsland().isPresent()){
                Island island = ugIsland.getIsland().get();
                scoreboard.updateLine(0, ChatColor.of("#f79f07") +"         §l⭐"+ ChatColor.of("#f0aa32") +" Rank " + island.getRank() + ChatColor.of("#f79f07") + " §l⭐");
                scoreboard.updateLine(8, ChatColor.of("#48bff0") + "  ▸" + ChatColor.of("#E4F0D0") + " Value - " + NumberUtils.format(island.getValue()));
                scoreboard.updateLine(9, ChatColor.of("#48bff0") + "  ▸" + ChatColor.of("#E4F0D0") + " Level - " + NumberUtils.format(island.getLevel()));
                scoreboard.updateLine(11, ChatColor.of("#48bff0") + "    ▹" + ChatColor.of("#E4F0D0") + " Money - " + island.getMoney());
                scoreboard.updateLine(12, ChatColor.of("#48bff0") + "    ▹" + ChatColor.of("#E4F0D0") + " Crystals - " + island.getCrystals());
            }
            scoreboard.updateLine(ugIsland == null ? 3 : 5, ChatColor.of("#e23f22") + "  ▸"+ ChatColor.of("#E6EED6") + " Money §e" + NumberUtils.format(UnlimitedGrind.getEconomy().getBalance(player)) + Symbols.COIN.get());
        }, 100, 100);
    }

    public void updateScoreboardPower(){
        int index = ugIsland == null ? 1 : 3;
        scoreboard.updateLine(index++, ChatColor.of("#e23f22") + "  ▸"+ ChatColor.of("#E6EED6") + " Items Power §c" + NumberUtils.format(getWearedPower()) + Symbols.POWER.get());
        scoreboard.updateLine(index, ChatColor.of("#e23f22") + "  ▸"+ ChatColor.of("#E6EED6") + " Items Power §c" + NumberUtils.format(getMaxPower()) + Symbols.POWER.get());
    }

    public void updateActionBarStats(){
        player.sendActionBar(Component.empty()
                .append(Component.text(Symbols.HEALTH.get() + " " + String.format("%.0f", getHealth()) + "/" + NumberUtils.format(getMaxHealth()) + " ")
                        .color(getHealthColor()))
                .append(Component.text( Symbols.ARMOR.get() + " " + String.format("%.0f", getStatValue(Stats.ARMOR)) + " (" + String.format("%.1f", getDamageReduction()*100) + "%)")
                        .color(ComponentColor.ARMOR.getColor())));
    }

    public void startActionBarRefresh(){
        actionBarTask = new BukkitRunnable() {
            @Override
            public void run() {
                if(!player.isOnline()) {
                    cancel();
                    return;
                }

                updateActionBarStats();
            }
        }.runTaskTimer(UnlimitedGrind.getInstance(), 20, 20);
    }

    public TextColor getHealthColor(){
        double healthPercentage = getHealthPercentage();
        return TextColor.color(
                (int) (healthPercentage * 67) + (int) ((1-healthPercentage) * 243),
                (int) (healthPercentage * 255) + (int) ((1-healthPercentage) * 23),
                (int) (healthPercentage * 93) + (int) ((1-healthPercentage) * 18)
        );
    }

    public void addPower(double power) {
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
        this.wearedPower = wearedPower + 1;
    }

    public void updateEquippedWearedPower(){
        UGItem ugItem;
        double tempPower = 0;
        for (ItemStack content : getEquippedItems()) {
            if(content == null) continue;
            ugItem = UGItem.getFromItemStack(content);
            if(ugItem == null) continue;
            tempPower += ugItem.getPower();
        }
        setWearedPower(tempPower / getEquippedItems().size());
        if(getWearedPower() > getMaxPower()){
            setMaxPower(getWearedPower());
        }
        updateScoreboardPower();
    }

    public void updateWearedPower(UGItem newItem, UGItem oldItem, ItemAction action){
        if(ItemAction.shouldAffectStats(newItem, action)){
            addPower(newItem.getPower() / 6);
            if(getWearedPower() > getMaxPower()){
                setMaxPower(getWearedPower());
            }
        }
        if(ItemAction.shouldAffectStats(oldItem, action)){
            addPower(-(oldItem.getPower() / 6));
        }
        updateScoreboardPower();
    }

    public double getMaxPower() {
        return maxPower;
    }

    public void setMaxPower(double maxPower) {
        this.maxPower = maxPower;
        updateAllItemsPowerConditionLore();
    }

    public double getMaxWearablePower(){
        return maxPower + ItemManager.MAX_OFFSET_POWER;
    }

    public double neededPower(double itemPower){
        return itemPower - getMaxWearablePower();
    }

    public boolean canEquipItem(UGItem ugItem){
        if(ugItem == null) return true;

        double maxWearable = getMaxWearablePower();
        Rarity itemRarity = ugItem.getRarity();

        switch (itemRarity){
            case LEGENDARY -> maxWearable += ItemManager.MAX_OFFSET_POWER / 2.0;
            case MYTHIC -> maxWearable += ItemManager.MAX_OFFSET_POWER;
        }

        return maxWearable >= ugItem.getPower();
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

//    public double getArmorValue() {
//        double armor = 0;
//        ItemMeta itemMeta;
//        for (ItemStack itemStack : getArmorAndOffHand()) {
//            if(itemStack == null) continue;
//
//            itemMeta = itemStack.getItemMeta();
//            if(itemMeta == null) continue;
//            if(itemMeta.hasAttributeModifiers() && itemMeta.getAttributeModifiers(Attribute.GENERIC_ARMOR) != null){
//                for (AttributeModifier attributeModifier : itemMeta.getAttributeModifiers(Attribute.GENERIC_ARMOR)) {
//                    armor += attributeModifier.getAmount();
//                }
//            }
//        }
//        return armor;
//    }

    public double getDamageReduction(){
        double playerArmor = getStatValue(Stats.ARMOR);

        return (playerArmor / (playerArmor + 15000));
    }

    public double getDamageReductionPercentage(){
        return 1 - getDamageReduction();
    }

    public void setHealthFromStat(){
        double maxHealth = baseHealth + getStatValue(Stats.VITALITY) * 3;
        setMaxHealth(maxHealth);
        if(currentHealth > maxHealth){
            setHealth(maxHealth);
        }
        updatePlayerHeartBar();
    }

    public void updatePlayerHeartBar(){
        player.setHealth(Math.max(1, (currentHealth / getMaxHealth()) * 20));
    }

    public void setHealth(double health){
        double maxHealth = getMaxHealth();
        currentHealth = Math.min(health, maxHealth);
        if(currentHealth <= 0){
            kill();
        }
        if(currentHealth > 0 && currentHealth <= maxHealth){
            updatePlayerHeartBar();
        }
    }

    public void setMaxHealth(double maxHealth){
        if(!isHealthLocked()){
            double clampedMaxHealth = Math.max(1, maxHealth);
            setStatValue(Stats.HEALTH, clampedMaxHealth);
            if(currentHealth > clampedMaxHealth){
                setHealth(clampedMaxHealth);
            }
        }
    }

    public double getMaxHealth() {
        return getStatValue(Stats.HEALTH);
    }

    public double getHealth() {
        return currentHealth;
    }

    public double getHealthPercentage(){
        return currentHealth / getMaxHealth();
    }

    public boolean isHealthLocked() {
        return isHealthLocked;
    }

    public void setHealthLocked(boolean healthLocked) {
        isHealthLocked = healthLocked;
    }

    public void kill(){
        setHealth(getMaxHealth());
        player.getActivePotionEffects().clear();
        player.setFireTicks(0);
        Bukkit.getServer().getPluginManager().callEvent(new UgPlayerDieEvent(this, null, player.getLocation()));
    }

    public void respawn(){
        if(ugIsland != null && ugIsland.getIsland().isPresent()) {
            Island island = ugIsland.getIsland().get();
            player.teleport(island.getHome());
        } else {
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        }
    }

    public UGIsland getUgIsland() {
        return ugIsland;
    }

    public void setUgIsland(UGIsland ugIsland) {
        this.ugIsland = ugIsland;
    }

    public void regenHealth(double health){
        double toRegen = health;
        if(player.getFireTicks() > 0) {
            toRegen *= .5;
        }
        setHealth(currentHealth + toRegen);
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

        if(isInArena()){
            leaveArena();
        }

        group.removePlayer(this);
        group = null;
        return true;
    }

    public List<ItemStack> getArmorAndOffHand(){
        List<ItemStack> armor = new ArrayList<>(Arrays.asList(player.getInventory().getArmorContents()));

        ItemStack handItem = player.getInventory().getItemInMainHand();
        UGItem ugItem = UGItem.getFromItemStack(handItem);
        if(ugItem != null && ugItem.getItemCategories().equals(ItemCategory.SHIELD) && canEquipItem(ugItem)){
            armor.add(player.getInventory().getItemInMainHand());
        }

        armor.add(player.getInventory().getItemInOffHand());
        return armor;
    }

    public List<ItemStack> getEquippedItems(){
        List<ItemStack> items = getArmorAndOffHand();

        ItemStack handItem = player.getInventory().getItemInMainHand();
        UGItem ugItem = UGItem.getFromItemStack(handItem);
        if(ugItem != null && !ugItem.getItemCategories().equals(ItemCategory.ARMOR) && canEquipItem(ugItem)){
            items.add(player.getInventory().getItemInMainHand());
        }
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
        updateEquippedWearedPower();
    }

    public HashMap<Stats, Double> getStatsMap() {
        return statsMap;
    }

    public void updateStatsFromItem(UGItem newItem, UGItem oldItem, ItemAction action){
        if(ItemAction.shouldAffectStats(newItem, action)) {
            newItem.getStatsMap().forEach((stat, value) -> {
                if(statsMap.containsKey(stat)){
                    addStatValue(stat, value);
                }
            });
        }
        if(ItemAction.shouldAffectStats(oldItem, action) && canEquipItem(oldItem)){
            oldItem.getStatsMap().forEach((stat, value) -> {
                if(statsMap.containsKey(stat)){
                    addStatValue(stat, -value);
                }
            });
        }
        setHealthFromStat();
    }

    public double getStatValue(Stats stat) {
        double value = statsMap.get(stat);
        return value >= 1 ? Math.round(statsMap.get(stat)) : value;
    }

    public void setStatValue(Stats stat, double value) {
        statsMap.replace(stat, value);
    }

    public void addStatValue(Stats stat, double toAdd){
        if(statsMap.containsKey(stat)){
            setStatValue(stat, statsMap.get(stat) + toAdd);
        }
    }

    public void joinArena(ArenaMap arenaMap, double power){
        if(hasGroup() && !getGroup().getOwner().equals(this)) return;
        if(isInArena()) return;

        if(hasGroup()){
            this.arena = new PveArena(getGroup(), arenaMap);
        } else {
            this.arena = new PveArena(this, arenaMap);
        }
        this.arena.setupArena(power);
        shouldAllowArmorChange(false);
        player.setGameMode(GameMode.ADVENTURE);
    }

    public void setArena(BaseArena arena) {
        this.arena = arena;
    }

    public void leaveArena(){
        if(!isInArena()) return;

        PveArena pveArena = (PveArena) arena;
        pveArena.quit(this);

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

    public HashMap<PotionEffectType, BukkitTask> getActivesPotionsMap() {
        return activesPotionsMap;
    }

    public void sendMessageComponents(List<Component> components){
        final int spaceToDo = 2;
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
