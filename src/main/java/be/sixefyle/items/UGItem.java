package be.sixefyle.items;

import be.sixefyle.UnlimitedGrind;
import be.sixefyle.items.passifs.ItemPassif;
import be.sixefyle.items.passifs.Passif;
import be.sixefyle.utils.NumberUtils;
import be.sixefyle.utils.PlaceholderUtils;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import com.iridium.iridiumcore.dependencies.nbtapi.NBTItem;
import it.unimi.dsi.fastutil.Hash;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Objective;

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

        initItem(itemType, lore);
    }

    public void initItem(Material material, List<String> lore){
        if(item == null){
            item = new ItemStack(material);
        }
        ItemMeta itemMeta = item.getItemMeta();

        String nameLine = UnlimitedGrind.getInstance().getConfig().getString("lang.item.name");
        itemMeta.displayName(Component.text(PlaceholderUtils.replace(this, nameLine)));

        List<Component> loreComp = new ArrayList<>();

        String powerLine = UnlimitedGrind.getInstance().getConfig().getString("lang.item.power");
        loreComp.add(Component.text(PlaceholderUtils.replace(this, powerLine)));
        if(lore != null){
            for (String s : lore) {
                loreComp.add(Component.text(PlaceholderUtils.replace(this, s)));
            }
        }
        loreComp.add(Component.text(""));

        boolean isMythic = rarity.equals(Rarity.MYTHIC);

        ItemPassif itemPassif;
        for (Passif passif : passifList) {
            itemPassif = passif.getItemPassif();
            loreComp.add(Component.text(IridiumColorAPI.process(itemPassif.getName() + ": ")));
            for (String s : itemPassif.getDescription()) {
                loreComp.add(Component.text("   " + PlaceholderUtils.replace(itemPassif, isMythic, s)));
            }
        }

        itemMeta.lore(loreComp);

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
            double armorResistance = Math.pow(getPower(), 0.4912);

            AttributeModifier armor = new AttributeModifier(UUID.randomUUID(),
                    "generic.armor", armorResistance, AttributeModifier.Operation.ADD_NUMBER, dropTableItem.getSlot());
            itemMeta.addAttributeModifier(Attribute.GENERIC_ARMOR, armor);

            HashMap<Attribute, Double> attributeList = new HashMap<>() {{
               put(Attribute.GENERIC_ARMOR_TOUGHNESS, .25);
               put(Attribute.GENERIC_MAX_HEALTH, isShield ? .35 : .1);
               put(Attribute.GENERIC_MOVEMENT_SPEED, .05);
               put(Attribute.GENERIC_KNOCKBACK_RESISTANCE, isShield ? 1 : .1);
               put(Attribute.GENERIC_ATTACK_DAMAGE, isShield ? .1 : .2);
            }};
            addRandomAttributes(attributeList, itemMeta, dropTableItem.getSlot());
        }

        item.setItemMeta(itemMeta);
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

    public static boolean isMythic(ItemStack item){
        ItemMeta itemMeta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(UnlimitedGrind.getInstance(), "rarity");
        if(item.getItemMeta().getPersistentDataContainer().has(key)){
            return itemMeta.getPersistentDataContainer().get(key, PersistentDataType.STRING).equals(Rarity.MYTHIC.name());
        }
        return false;
    }

    public static void updateItemStackLorePassif(ItemStack item){
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
