package be.sixefyle.items;

import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;

public enum DropTable {
    WOODEN_SWORD(Material.WOODEN_SWORD, ItemCategory.MELEE, EquipmentSlot.HAND),
    STONE_SWORD(Material.STONE_SWORD, ItemCategory.MELEE, EquipmentSlot.HAND),
    IRON_SWORD(Material.IRON_SWORD, ItemCategory.MELEE, EquipmentSlot.HAND),
    GOLDEN_SWORD(Material.GOLDEN_SWORD, ItemCategory.MELEE, EquipmentSlot.HAND),
    DIAMOND_SWORD(Material.DIAMOND_SWORD, ItemCategory.MELEE, EquipmentSlot.HAND),

    LEATHER_BOOTS(Material.LEATHER_BOOTS, ItemCategory.ARMOR, EquipmentSlot.FEET),
    LEATHER_LEGGINGS(Material.LEATHER_LEGGINGS, ItemCategory.ARMOR, EquipmentSlot.LEGS),
    LEATHER_CHESTPLATE(Material.LEATHER_CHESTPLATE, ItemCategory.ARMOR, EquipmentSlot.CHEST),
    LEATHER_HELMET(Material.LEATHER_HELMET, ItemCategory.ARMOR, EquipmentSlot.HEAD),

    CHAINMAIL_BOOTS(Material.CHAINMAIL_BOOTS, ItemCategory.ARMOR, EquipmentSlot.FEET),
    CHAINMAIL_LEGGINGS(Material.CHAINMAIL_LEGGINGS, ItemCategory.ARMOR, EquipmentSlot.LEGS),
    CHAINMAIL_CHESTPLATE(Material.CHAINMAIL_CHESTPLATE, ItemCategory.ARMOR, EquipmentSlot.CHEST),
    CHAINMAIL_HELMET(Material.CHAINMAIL_HELMET, ItemCategory.ARMOR, EquipmentSlot.HEAD),

    IRON_BOOTS(Material.IRON_BOOTS, ItemCategory.ARMOR, EquipmentSlot.FEET),
    IRON_LEGGINGS(Material.IRON_LEGGINGS, ItemCategory.ARMOR, EquipmentSlot.LEGS),
    IRON_CHESTPLATE(Material.IRON_CHESTPLATE, ItemCategory.ARMOR, EquipmentSlot.CHEST),
    IRON_HELMET(Material.IRON_HELMET, ItemCategory.ARMOR, EquipmentSlot.HEAD),

    GOLDEN_BOOTS(Material.GOLDEN_BOOTS, ItemCategory.ARMOR, EquipmentSlot.FEET),
    GOLDEN_LEGGINGS(Material.GOLDEN_LEGGINGS, ItemCategory.ARMOR, EquipmentSlot.LEGS),
    GOLDEN_CHESTPLATE(Material.GOLDEN_CHESTPLATE, ItemCategory.ARMOR, EquipmentSlot.CHEST),
    GOLDEN_HELMET(Material.GOLDEN_HELMET, ItemCategory.ARMOR, EquipmentSlot.HEAD),

    DIAMOND_BOOTS(Material.DIAMOND_BOOTS, ItemCategory.ARMOR, EquipmentSlot.FEET),
    DIAMOND_LEGGINGS(Material.DIAMOND_LEGGINGS, ItemCategory.ARMOR, EquipmentSlot.LEGS),
    DIAMOND_CHESTPLATE(Material.DIAMOND_CHESTPLATE, ItemCategory.ARMOR, EquipmentSlot.CHEST),
    DIAMOND_HELMET(Material.DIAMOND_HELMET, ItemCategory.ARMOR, EquipmentSlot.HEAD),

    BOW(Material.BOW, ItemCategory.DISTANCE, EquipmentSlot.HAND),
    CROSSBOW(Material.CROSSBOW, ItemCategory.DISTANCE, EquipmentSlot.HAND),
    SHIELD(Material.SHIELD, ItemCategory.SHIELD, EquipmentSlot.OFF_HAND),
    ;

    final Material material;
    final ItemCategory itemCategory;
    EquipmentSlot slot;

    DropTable(Material material, ItemCategory itemCategory) {
        this.material = material;
        this.itemCategory = itemCategory;
    }

    DropTable(Material material, ItemCategory itemCategory, EquipmentSlot slot) {
        this.material = material;
        this.itemCategory = itemCategory;
        this.slot = slot;
    }

    public Material getMaterial() {
        return material;
    }

    public ItemCategory getItemCategory() {
        return itemCategory;
    }

    public EquipmentSlot getSlot() {
        return slot;
    }
}
