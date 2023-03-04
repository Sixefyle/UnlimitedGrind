package be.sixefyle.items;

import be.sixefyle.UGPlayer;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.enums.AttributesName;
import be.sixefyle.enums.ComponentColor;
import be.sixefyle.items.passifs.ItemPassif;
import be.sixefyle.items.passifs.Passif;
import be.sixefyle.utils.NumberUtils;
import be.sixefyle.utils.PlaceholderUtils;
import com.google.common.collect.Multimap;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
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

import java.text.DecimalFormat;
import java.util.*;

public class UGItem {
    private ItemStack item;
    private String name;
    private String prefix;
    private String suffix;
    private final double power;
    private final Rarity rarity;
    private List<Passif> passifList = new ArrayList<>();

    public UGItem(ItemStack itemStack, Rarity rarity, double power, int[] passifIDs) {
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
        ItemCategory dropTableItemCategory = dropTableItem.getItemCategory();
        if(dropTableItemCategory.equals(ItemCategory.MELEE)) {
            double weapDamage = Math.pow(getPower(), UnlimitedGrind.getInstance().getConfig().getDouble("power.efficiencyDamage"));

            AttributeModifier damage = new AttributeModifier(UUID.randomUUID(),
                    "generic.attack_damage", weapDamage, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
            itemMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, damage);

            AttributeModifier attack_speed = new AttributeModifier(UUID.randomUUID(),
                    "generic.attack_speed", NumberUtils.getRandomNumber(-3, 3), AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
            itemMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, attack_speed);

            HashMap<Attribute, Double> attributeList = new HashMap<>() {{
                put(Attribute.GENERIC_ATTACK_DAMAGE, .25);
                put(Attribute.GENERIC_ATTACK_SPEED, .5);
                put(Attribute.GENERIC_KNOCKBACK_RESISTANCE, .1);
                put(Attribute.GENERIC_ATTACK_KNOCKBACK, .2);
                put(Attribute.GENERIC_MOVEMENT_SPEED, .1);
            }};
            addRandomAttributes(attributeList, itemMeta, dropTableItem.getSlot());

        } else if(dropTableItemCategory.equals(ItemCategory.ARMOR) || dropTableItemCategory.equals(ItemCategory.SHIELD)) {
            boolean isShield = dropTableItemCategory.equals(ItemCategory.SHIELD);
            double armorResistance = NumberUtils.getRandomNumber(Math.pow(getPower(), 0.6912), Math.pow(getPower(), 0.7012));
            armorResistance = Double.min(armorResistance, 60000);

            AttributeModifier armorAttribute = new AttributeModifier(UUID.randomUUID(),
                    "generic.armor", armorResistance, AttributeModifier.Operation.ADD_NUMBER, dropTableItem.getSlot());
            itemMeta.addAttributeModifier(Attribute.GENERIC_ARMOR, armorAttribute);

            double bonusHealth = NumberUtils.getRandomNumber(Math.pow(getPower(), 1.04956), Math.pow(getPower(), 1.05956));
            itemMeta.getPersistentDataContainer().set(
                    new NamespacedKey(UnlimitedGrind.getInstance(), "bonusHealth"), PersistentDataType.DOUBLE, bonusHealth);

            HashMap<Attribute, Double> attributeList = new HashMap<>() {{
               put(Attribute.GENERIC_ARMOR_TOUGHNESS, .25);
               put(Attribute.GENERIC_ATTACK_SPEED, .05);
               put(Attribute.GENERIC_MOVEMENT_SPEED, isShield ? .03 : .05);
               put(Attribute.GENERIC_KNOCKBACK_RESISTANCE, isShield ? 1 : .1);
               put(Attribute.GENERIC_ATTACK_DAMAGE, isShield ? .1 : .2);
            }};
            addRandomAttributes(attributeList, itemMeta, dropTableItem.getSlot());
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
        if(lore != null){
            for (String s : lore) {
                loreComp.add(Component.text(PlaceholderUtils.replace(this, s)));
            }
        }
        loreComp.add(Component.text(""));

        setupStatsLore(loreComp, itemMeta);
        setupPassifLore(loreComp);

        itemMeta.lore(loreComp);
    }

    private void setupPowerLore(List<Component> loreComp){
        String powerLine = UnlimitedGrind.getInstance().getConfig().getString("lang.item.power");
        loreComp.add(Component.text(PlaceholderUtils.replace(this, powerLine)));
    }

    private void setupStatsLore(List<Component> loreComp, ItemMeta itemMeta){
        Multimap<Attribute, AttributeModifier> attributes = itemMeta.getAttributeModifiers();
        if(attributes == null || attributes.isEmpty()) return;

        HashMap<Attribute, AttributeModifier> primaryAttribute = new HashMap<>();
        HashMap<Attribute, AttributeModifier> secondaryAttribute = new HashMap<>();
        attributes.forEach((attribute, attributeModifier) -> {
            if(AttributesName.isPrimary(attributeModifier)){
                primaryAttribute.put(attribute, attributeModifier);
            } else {
                secondaryAttribute.put(attribute, attributeModifier);
            }
        });

        loreComp.add(Component.text("◆ Primary").color(ComponentColor.GOLD.getColor()).decoration(TextDecoration.ITALIC, false));
        primaryAttribute.forEach((attribute, attributeModifier) -> {
            Component attributeComp = Component.empty();
            attributeComp = attributeComp
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("    ◆ ")).color(ComponentColor.GOLD.getColor())
                    .append(getAttributeModifierLine(attributeModifier))
                    .color(ComponentColor.NEUTRAL.getColor())
                    .append(Component.text(AttributesName.getByAttribute(attribute).getName()));
            loreComp.add(attributeComp);
        });

        if(secondaryAttribute.size() > 0) {
            loreComp.add(Component.text(" "));
            loreComp.add(Component.text("◇ Secondary").color(ComponentColor.GOLD.getColor()).decoration(TextDecoration.ITALIC, false));
            secondaryAttribute.forEach((attribute, attributeModifier) -> {
                Component attributeComp = Component.empty();
                attributeComp = attributeComp
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text("    ◇ ")).color(ComponentColor.GOLD.getColor())
                        .append(getAttributeModifierLine(attributeModifier))
                        .color(ComponentColor.NEUTRAL.getColor())
                        .append(Component.text(AttributesName.getByAttribute(attribute).getName()));
                loreComp.add(attributeComp);
            });
        }
    }

    private Component getAttributeModifierLine(AttributeModifier attributeModifier){
        Component component = Component.empty();
        double amount = attributeModifier.getAmount();
        boolean isPercentage = attributeModifier.getOperation().equals(AttributeModifier.Operation.MULTIPLY_SCALAR_1);
        component = component
                .color(amount > 0 ? ComponentColor.FINE.getColor() : ComponentColor.ERROR.getColor())
                .append(Component.text(isPercentage ? String.format("%.2f", amount * 100) : String.format(Locale.ENGLISH, "%,.1f", amount)))
                .append(isPercentage ? Component.text("%") : Component.empty())
                .append(Component.text(" "));

        return component;
    }

    private void setupPassifLore(List<Component> loreComp){
        boolean isMythic = rarity.equals(Rarity.MYTHIC);
        ItemPassif itemPassif;
        for (Passif passif : passifList) {
            itemPassif = passif.getItemPassif();
            loreComp.add(Component.text(IridiumColorAPI.process(itemPassif.getName() + ": ")));
            for (String s : itemPassif.getDescription()) {
                loreComp.add(Component.text("   " + PlaceholderUtils.replace(itemPassif, isMythic, s)));
            }
        }
    }

    private void addRandomAttributes(HashMap<Attribute, Double> attributeMap, ItemMeta itemMeta, EquipmentSlot slot){
        AttributeModifier randomArmorAttribute;
        Random random = new Random();
        Set<Attribute> attributeSet = attributeMap.keySet();
        List<Attribute> attributeList = new ArrayList<>(attributeSet);
        Attribute key;
        Double value;
        int index;
        for (int i = 0; i < rarity.getBonusAttributeAmount(); i++) {
            index = random.nextInt(attributeList.size());
            key = attributeList.get(index);
            value = attributeMap.get(key);

            randomArmorAttribute = new AttributeModifier(UUID.randomUUID(),
                    "random.attribute." + i, Math.random() * value, AttributeModifier.Operation.MULTIPLY_SCALAR_1, slot);

            itemMeta.addAttributeModifier(key, randomArmorAttribute);

            attributeList.remove(key);
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
        }.runTaskTimer(UnlimitedGrind.getInstance(), 0, 6 - getRarity().getBonusAttributeAmount());
    }

    public static boolean isMythic(ItemStack item){
        ItemMeta itemMeta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(UnlimitedGrind.getInstance(), "rarity");
        if(item.getItemMeta().getPersistentDataContainer().has(key)){
            return itemMeta.getPersistentDataContainer().get(key, PersistentDataType.STRING).equals(Rarity.MYTHIC.name());
        }
        return false;
    }

    public static void updateItemStackLore(ItemStack item){
        if(item == null) return;

        ItemMeta itemMeta = item.getItemMeta();
        UGItem ugItem = UGItem.getFromItemStack(item);

        if(ugItem == null) return;

        List<Component> loreComp = itemMeta.lore();

        boolean isMythic = ugItem.getRarity().equals(Rarity.MYTHIC);

        ItemPassif itemPassif;
        int passifLineIndex = 2;
        int descLineIndex = 3;
        for (Passif passif : ugItem.getPassifList()) {
            itemPassif = passif.getItemPassif();
            loreComp.set(passifLineIndex, Component.text(IridiumColorAPI.process(itemPassif.getName() + ": ")));
            for (String s : itemPassif.getDescription()) {
                loreComp.set(descLineIndex++, Component.text("   " + PlaceholderUtils.replace(itemPassif, isMythic, s)));
            }
        }

        itemMeta.lore(loreComp);
        item.setItemMeta(itemMeta);
    }

    public static UGItem getFromItemStack(ItemStack item){
        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta == null) return null;

        NamespacedKey powerKey = new NamespacedKey(UnlimitedGrind.getInstance(), "power");
        if(itemMeta.getPersistentDataContainer().has(powerKey)) {
            NamespacedKey passifKey = new NamespacedKey(UnlimitedGrind.getInstance(), "passifIdArray");
            NamespacedKey rarityKey = new NamespacedKey(UnlimitedGrind.getInstance(), "rarity");
            double power = itemMeta.getPersistentDataContainer().get(powerKey, PersistentDataType.DOUBLE);
            String rarity = itemMeta.getPersistentDataContainer().get(rarityKey, PersistentDataType.STRING);
            int[] passifs = itemMeta.getPersistentDataContainer().get(passifKey, PersistentDataType.INTEGER_ARRAY);

            return new UGItem(item, Rarity.valueOf(rarity), power, passifs);
        }
        return null;
    }

}
