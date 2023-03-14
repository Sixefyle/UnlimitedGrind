package be.sixefyle.items;

import be.sixefyle.UGPlayer;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.enums.ComponentColor;
import be.sixefyle.enums.Stats;
import be.sixefyle.items.passifs.ItemPassif;
import be.sixefyle.items.passifs.Passif;
import be.sixefyle.utils.NumberUtils;
import be.sixefyle.utils.PlaceholderUtils;
import com.google.common.collect.Multimap;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Item;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class UGItem {
    private ItemStack item;
    private String name;
    private String prefix;
    private String suffix;
    private final double power;
    private double critDamage;
    private final Rarity rarity;
    private List<Passif> passifList = new ArrayList<>();
    private HashMap<Stats, Double> statsMap = new HashMap<>();

    public UGItem(ItemStack itemStack, Rarity rarity, double power, int[] passifIDs, HashMap<Stats, Double> statsMap) {
        this.power = power;
        this.item = itemStack;
        this.rarity = rarity;

        Passif passif;
        for (int id : passifIDs) {
            passif = Passif.getByID(id);
            if(passif != null){
                passifList.add(passif);
            }
        }

        this.statsMap = statsMap;
    }

    public UGItem(Material itemType, Rarity rarity, String name, String prefix, String suffix, List<String> lore, double power, List<Passif> passifList) {
        this.power = power;
        this.passifList = passifList;
        this.rarity = rarity;
        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;

        createItem(itemType, lore);
    }

    public void createItem(Material material, List<String> lore){
        if(item == null){
            item = new ItemStack(material);
        }
        ItemMeta itemMeta = item.getItemMeta();

        String nameLine = UnlimitedGrind.getInstance().getConfig().getString("lang.item.name");
        itemMeta.displayName(Component.text(PlaceholderUtils.replace(this, nameLine)).color(rarity.getColor()).decoration(TextDecoration.ITALIC, false));

        itemMeta.getPersistentDataContainer().set(
                new NamespacedKey(UnlimitedGrind.getInstance(), "power"), PersistentDataType.DOUBLE, power);
        itemMeta.getPersistentDataContainer().set(
                new NamespacedKey(UnlimitedGrind.getInstance(), "rarity"), PersistentDataType.STRING, rarity.name());

        int[] passifIds = new int[passifList.size()];
        for (int i = 0; i < passifList.size(); i++) {
            passifIds[i] = passifList.get(i).getId();
        }
        itemMeta.getPersistentDataContainer().set(
                new NamespacedKey(UnlimitedGrind.getInstance(), "passifIdArray"), PersistentDataType.INTEGER_ARRAY, passifIds);

        DropTable dropTableItem = DropTable.valueOf(item.getType().name());
        ItemCategory itemCategory = dropTableItem.getItemCategory();
        if(itemCategory.equals(ItemCategory.MELEE)) {
            double weapDamage = Math.pow(getPower(), UnlimitedGrind.getInstance().getConfig().getDouble("power.efficiencyDamage"));
            addStats(Stats.ATTACK_DAMAGE, weapDamage, itemMeta, dropTableItem.getSlot());

            double weapAttackSpeed = NumberUtils.getRandomNumber(Stats.ATTACK_SPEED.getMin(), Stats.ATTACK_SPEED.getMax());
            addStats(Stats.ATTACK_SPEED, weapAttackSpeed, itemMeta, dropTableItem.getSlot());

            if(dropTableItem.getBonusPrimaryStat() != null){
                Stats bonusPrimaryStat = dropTableItem.getBonusPrimaryStat();
                double bonusPrimaryStatValue = NumberUtils.getRandomNumber(bonusPrimaryStat.getMin(), bonusPrimaryStat.getMax());
                addStats(bonusPrimaryStat, bonusPrimaryStatValue, itemMeta, dropTableItem.getSlot());
            }

            List<Stats> itemStatsList = new ArrayList<>() {{
                add(Stats.BONUS_ATTACK_DAMAGE);
                add(Stats.BONUS_ATTACK_SPEED);
                add(Stats.ATTACK_KNOCKBACK);
                add(Stats.MOVEMENT_SPEED);
                add(Stats.SWEEPING_RANGE);
                add(Stats.SWEEPING_DAMAGE);
                add(Stats.BONUS_CRITICAL_DAMAGE);
                add(Stats.BONUS_CRITICAL_CHANCE);
            }};

            addRandomStats(itemStatsList, itemMeta, dropTableItem.getSlot());

        } else if(itemCategory.equals(ItemCategory.ARMOR) || itemCategory.equals(ItemCategory.SHIELD)) {
            double armorValue = NumberUtils.getRandomNumber(Math.pow(getPower(), 0.6912), Math.pow(getPower(), 0.7012));
            armorValue = Double.min(armorValue, Stats.ARMOR.getMax());
            addStats(Stats.ARMOR, armorValue, itemMeta, dropTableItem.getSlot());

            double bonusHealth = NumberUtils.getRandomNumber(Math.pow(getPower(), 1.04956), Math.pow(getPower(), 1.05956));
            addStats(Stats.HEALTH, bonusHealth, itemMeta, dropTableItem.getSlot());

            if(dropTableItem.getBonusPrimaryStat() != null){
                Stats bonusPrimaryStat = dropTableItem.getBonusPrimaryStat();
                double bonusPrimaryStatValue = NumberUtils.getRandomNumber(bonusPrimaryStat.getMin(), bonusPrimaryStat.getMax());
                addStats(bonusPrimaryStat, bonusPrimaryStatValue, itemMeta, dropTableItem.getSlot());
            }

            List<Stats> itemStatsList = new ArrayList<>() {{
                add(Stats.ARMOR_TOUGHNESS);
                add(Stats.BONUS_ATTACK_DAMAGE);
                add(Stats.BONUS_ATTACK_SPEED);
                add(Stats.KNOCKBACK_RESISTANCE);
                add(Stats.MOVEMENT_SPEED);
                add(Stats.BONUS_CRITICAL_CHANCE);
                add(Stats.BONUS_CRITICAL_DAMAGE);
                add(Stats.MELEE_DAMAGE_REDUCTION);
                add(Stats.RANGE_DAMAGE_REDUCTION);
            }};

            addRandomStats(itemStatsList, itemMeta, dropTableItem.getSlot());
        }
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        setupLore(lore, itemMeta);
        item.setItemMeta(itemMeta);
    }

    public void updateLore(){
        ItemMeta itemMeta = item.getItemMeta();
        setupLore(null, itemMeta);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(itemMeta);
    }

    public void updateConditionLore(UGPlayer ugPlayer){
        ItemMeta itemMeta = item.getItemMeta();
        String powerLine = UnlimitedGrind.getInstance().getConfig().getString("lang.item.condition");
        List<Component> lore = itemMeta.lore();
        lore.set(1, Component.text(PlaceholderUtils.replace(this, ugPlayer, powerLine)).color(ComponentColor.ERROR.getColor()));
        itemMeta.lore(lore);
        item.setItemMeta(itemMeta);
    }

    private void setupLore(List<String> lore, ItemMeta itemMeta){
        List<Component> loreComp = new ArrayList<>();
        setupPowerLore(loreComp);

        loreComp.add(Component.text(""));
        setupStatsLore(loreComp, itemMeta);
        setupPassifLore(loreComp);

        if(lore != null){
        loreComp.add(Component.text(""));
            for (String s : lore) {
                loreComp.add(Component.text(PlaceholderUtils.replace(this, s)).color(ComponentColor.LORE.getColor()));
            }
        }
        itemMeta.lore(loreComp);
    }

    private void setupPowerLore(List<Component> loreComp){
        String powerLine = UnlimitedGrind.getInstance().getConfig().getString("lang.item.power");
        loreComp.add(Component.text(PlaceholderUtils.replace(this, powerLine)));
    }

    private void setupStatsLore(List<Component> loreComp, ItemMeta itemMeta){
        Multimap<Attribute, AttributeModifier> attributes = itemMeta.getAttributeModifiers();
        if(attributes == null || attributes.isEmpty()) return;

        HashMap<Stats, Double> primaryStats = new HashMap<>();
        HashMap<Stats, Double> secondaryStats = new HashMap<>();
        statsMap.forEach((itemStat, value) -> {
            if(itemStat.isPrimary()){
                primaryStats.put(itemStat, value);
            } else {
                secondaryStats.put(itemStat, value);
            }
        });

        loreComp.add(Component.text("◆ Primary").color(ComponentColor.GOLD.getColor()).decoration(TextDecoration.ITALIC, false));

        DropTable dropTableItem = DropTable.valueOf(item.getType().name());
        primaryStats.forEach((itemStat, value) -> {
            Component attributeComp = Component.empty();
            attributeComp = attributeComp
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("  ◆ ").color(ComponentColor.GOLD.getColor()))
                    .append(getStatsValueLien(value, itemStat.isPercent()))
                    .color(itemStat.equals(dropTableItem.getBonusPrimaryStat()) ? ComponentColor.ITEM_SPECIAL_STAT.getColor() : ComponentColor.NEUTRAL.getColor())
                    .append(Component.text(itemStat.getDisplayName()));
            loreComp.add(attributeComp);
        });

        if(secondaryStats.size() > 0) {
            loreComp.add(Component.text(" "));
            loreComp.add(Component.text("◇ Secondary").color(ComponentColor.GOLD.getColor()).decoration(TextDecoration.ITALIC, false));
            secondaryStats.forEach((itemStat, value) -> {
                Component attributeComp = Component.empty();
                attributeComp = attributeComp
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text("  ◇ ").color(ComponentColor.GOLD.getColor()))
                        .append(getStatsValueLien(value, itemStat.isPercent()))
                        .color(itemStat.equals(dropTableItem.getBonusPrimaryStat()) ? ComponentColor.ITEM_SPECIAL_STAT.getColor() : ComponentColor.NEUTRAL.getColor())
                        .append(Component.text(itemStat.getDisplayName()));
                loreComp.add(attributeComp);
            });
        }
    }

    private Component getStatsValueLien(Double value, boolean isPercent){
        Component component = Component.empty();
        component = component
                .color(value > 0 ? ComponentColor.FINE.getColor() : ComponentColor.ERROR.getColor())
                .append(Component.text(isPercent ? String.format(Locale.ENGLISH, "%.2f", value * 100) : String.format(Locale.ENGLISH, "%,.1f", value)))
                .append(isPercent ? Component.text("%") : Component.empty())
                .append(Component.text(" "));

        return component;
    }

    private void setupPassifLore(List<Component> loreComp){
        boolean isMythic = rarity.equals(Rarity.MYTHIC);
        if(passifList.isEmpty()) return;
        loreComp.add(Component.text(""));
        ItemPassif itemPassif;
        for (Passif passif : passifList) {
            itemPassif = passif.getItemPassif();
            loreComp.add(Component.text(IridiumColorAPI.process(itemPassif.getName())));
            for (String s : itemPassif.getDescription()) {
                loreComp.add(Component.text("   " + PlaceholderUtils.replace(itemPassif, isMythic, s)));
            }
        }
    }

    private void addStats(Stats itemStat, double value, ItemMeta itemMeta, EquipmentSlot slot){
        AttributeModifier randomArmorAttribute;
        if(statsMap.containsKey(itemStat)) return;

        if(itemStat.isAttribute()){
            randomArmorAttribute = new AttributeModifier(UUID.randomUUID(),
                    "random.attribute." + itemStat.toString().toLowerCase(),
                    Math.random() * value,
                    itemStat.isPercent() ? AttributeModifier.Operation.MULTIPLY_SCALAR_1 : AttributeModifier.Operation.ADD_NUMBER,
                    slot);

            itemMeta.addAttributeModifier((Attribute) itemStat.getStats(), randomArmorAttribute);
        } else {
            NamespacedKey key = new NamespacedKey(UnlimitedGrind.getInstance(), (String) itemStat.getStats());
            itemMeta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, value);
        }

        this.statsMap.put(itemStat, value);
    }

    private void addRandomStats(List<Stats> itemStatsType, ItemMeta itemMeta, EquipmentSlot slot){
        Random random = new Random();
        int index;
        Stats itemStat;
        double value;
        for (int i = 0; i < rarity.getBonusStatsAmount(); i++) {
            index = random.nextInt(itemStatsType.size());
            itemStat = itemStatsType.get(index);
            value = NumberUtils.getRandomNumber(itemStat.getMin(), itemStat.getMax());

            addStats(itemStat, value, itemMeta, slot);
            itemStatsType.remove(index);
        }
    }

    public ItemStack getItem() {
        return item;
    }

    public double getPower() {
        return power;
    }

    public List<Passif> getPassifList() {
        return passifList;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public double getCritDamage() {
        return critDamage;
    }

    public HashMap<Stats, Double> getStatsMap() {
        return statsMap;
    }

    public void createRarityParticle(Item item){
        new BukkitRunnable() {
            Location loc;
            Location particleLoc;
            Particle.DustOptions dustOptions;
            @Override
            public void run() {
                if(item == null || item.isDead()){
                    cancel();
                    return;
                }
                loc = item.getLocation();

                dustOptions = new Particle.DustOptions(
                        Color.fromBGR(rarity.getColor().blue(), rarity.getColor().green(), rarity.getColor().red()), .5f);
                particleLoc = loc.clone();
                particleLoc.add(NumberUtils.getRandomNumber(-.3,.3),NumberUtils.getRandomNumber(.1,.5), NumberUtils.getRandomNumber(-.3,.3)).getWorld().spawnParticle(Particle.REDSTONE, particleLoc, 0, 0, 0, 0, .1, dustOptions);
            }
        }.runTaskTimer(UnlimitedGrind.getInstance(), 0, 6 - getRarity().getBonusStatsAmount());
    }

    public static boolean isMythic(ItemStack item){
        ItemMeta itemMeta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(UnlimitedGrind.getInstance(), "rarity");
        if(item.getItemMeta().getPersistentDataContainer().has(key)){
            return itemMeta.getPersistentDataContainer().get(key, PersistentDataType.STRING).equals(Rarity.MYTHIC.name());
        }
        return false;
    }

    public static UGItem getFromItemStack(ItemStack item){
        if(item == null) return  null;
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta == null) return null;

        NamespacedKey powerKey = new NamespacedKey(UnlimitedGrind.getInstance(), "power");
        if(itemMeta.getPersistentDataContainer().has(powerKey)) {
            NamespacedKey passifKey = new NamespacedKey(UnlimitedGrind.getInstance(), "passifIdArray");
            NamespacedKey rarityKey = new NamespacedKey(UnlimitedGrind.getInstance(), "rarity");
            double power = itemMeta.getPersistentDataContainer().get(powerKey, PersistentDataType.DOUBLE);
            String rarity = itemMeta.getPersistentDataContainer().get(rarityKey, PersistentDataType.STRING);
            int[] passifs = itemMeta.getPersistentDataContainer().get(passifKey, PersistentDataType.INTEGER_ARRAY);

            HashMap<Stats, Double> statsMap = new HashMap<>();
            NamespacedKey key;
            double attributeValue;
            boolean isSameAttributeType;
            for (Stats stat : Stats.values()) {
                if(!stat.isAttribute()){
                    key = new NamespacedKey(UnlimitedGrind.getInstance(), (String) stat.getStats());
                    if(itemMeta.getPersistentDataContainer().has(key)) {
                        statsMap.put(stat, itemMeta.getPersistentDataContainer().get(key, PersistentDataType.DOUBLE));
                    }
                } else {
                    Attribute attribute = (Attribute) stat.getStats();
                    if(itemMeta.hasAttributeModifiers()){
                        if(itemMeta.getAttributeModifiers(attribute) != null){
                            attributeValue = 0;
                            isSameAttributeType = false;
                            for (AttributeModifier attributeModifier : itemMeta.getAttributeModifiers(attribute)) {
                                if((attributeModifier.getOperation().equals(AttributeModifier.Operation.ADD_NUMBER) && stat.isPrimary())
                                || (attributeModifier.getOperation().equals(AttributeModifier.Operation.MULTIPLY_SCALAR_1) && !stat.isPrimary())) {
                                    isSameAttributeType = true;
                                    attributeValue += attributeModifier.getAmount();
                                }
                            }
                            if(isSameAttributeType){
                                statsMap.put(stat, attributeValue);
                            }
                        }
                    }
                }
            }

            return new UGItem(item, Rarity.valueOf(rarity), power, passifs, statsMap);
        }
        return null;
    }

}
