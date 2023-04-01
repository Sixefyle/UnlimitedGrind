package be.sixefyle.items;

import be.sixefyle.enums.Stats;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;

public enum DropTable {
    WOODEN_SWORD(Material.WOODEN_SWORD, ItemCategory.MELEE, EquipmentSlot.HAND),
    STONE_SWORD(Material.STONE_SWORD, ItemCategory.MELEE, EquipmentSlot.HAND),
    IRON_SWORD(Material.IRON_SWORD, ItemCategory.MELEE, EquipmentSlot.HAND),
    GOLDEN_SWORD(Material.GOLDEN_SWORD, ItemCategory.MELEE, EquipmentSlot.HAND, Stats.CRITICAL_CHANCE),
    DIAMOND_SWORD(Material.DIAMOND_SWORD, ItemCategory.MELEE, EquipmentSlot.HAND, Stats.SWEEPING_DAMAGE),
    NETHERITE_SWORD(Material.NETHERITE_SWORD, ItemCategory.MELEE, EquipmentSlot.HAND, false),

    WOODEN_AXE(Material.WOODEN_AXE, ItemCategory.MELEE, EquipmentSlot.HAND),
    STONE_AXE(Material.STONE_AXE, ItemCategory.MELEE, EquipmentSlot.HAND),
    IRON_AXE(Material.IRON_AXE, ItemCategory.MELEE, EquipmentSlot.HAND),
    GOLDEN_AXE(Material.GOLDEN_AXE, ItemCategory.MELEE, EquipmentSlot.HAND, Stats.CRITICAL_DAMAGE),
    DIAMOND_AXE(Material.DIAMOND_AXE, ItemCategory.MELEE, EquipmentSlot.HAND, Stats.LIFE_STEAL),
    NETHERITE_AXE(Material.NETHERITE_AXE, ItemCategory.MELEE, EquipmentSlot.HAND, false),

    LEATHER_BOOTS(Material.LEATHER_BOOTS, ItemCategory.ARMOR, EquipmentSlot.FEET, Stats.CRITICAL_CHANCE),
    LEATHER_LEGGINGS(Material.LEATHER_LEGGINGS, ItemCategory.ARMOR, EquipmentSlot.LEGS, Stats.CRITICAL_CHANCE),
    LEATHER_CHESTPLATE(Material.LEATHER_CHESTPLATE, ItemCategory.ARMOR, EquipmentSlot.CHEST, Stats.CRITICAL_CHANCE),
    LEATHER_HELMET(Material.LEATHER_HELMET, ItemCategory.ARMOR, EquipmentSlot.HEAD, Stats.CRITICAL_CHANCE),

    CHAINMAIL_BOOTS(Material.CHAINMAIL_BOOTS, ItemCategory.ARMOR, EquipmentSlot.FEET, Stats.RANGE_DAMAGE_REDUCTION),
    CHAINMAIL_LEGGINGS(Material.CHAINMAIL_LEGGINGS, ItemCategory.ARMOR, EquipmentSlot.LEGS, Stats.RANGE_DAMAGE_REDUCTION),
    CHAINMAIL_CHESTPLATE(Material.CHAINMAIL_CHESTPLATE, ItemCategory.ARMOR, EquipmentSlot.CHEST, Stats.RANGE_DAMAGE_REDUCTION),
    CHAINMAIL_HELMET(Material.CHAINMAIL_HELMET, ItemCategory.ARMOR, EquipmentSlot.HEAD, Stats.RANGE_DAMAGE_REDUCTION),

    IRON_BOOTS(Material.IRON_BOOTS, ItemCategory.ARMOR, EquipmentSlot.FEET, Stats.MELEE_DAMAGE_REDUCTION),
    IRON_LEGGINGS(Material.IRON_LEGGINGS, ItemCategory.ARMOR, EquipmentSlot.LEGS, Stats.MELEE_DAMAGE_REDUCTION),
    IRON_CHESTPLATE(Material.IRON_CHESTPLATE, ItemCategory.ARMOR, EquipmentSlot.CHEST, Stats.MELEE_DAMAGE_REDUCTION),
    IRON_HELMET(Material.IRON_HELMET, ItemCategory.ARMOR, EquipmentSlot.HEAD, Stats.MELEE_DAMAGE_REDUCTION),

    GOLDEN_BOOTS(Material.GOLDEN_BOOTS, ItemCategory.ARMOR, EquipmentSlot.FEET, Stats.CRITICAL_DAMAGE),
    GOLDEN_LEGGINGS(Material.GOLDEN_LEGGINGS, ItemCategory.ARMOR, EquipmentSlot.LEGS, Stats.CRITICAL_DAMAGE),
    GOLDEN_CHESTPLATE(Material.GOLDEN_CHESTPLATE, ItemCategory.ARMOR, EquipmentSlot.CHEST, Stats.CRITICAL_DAMAGE),
    GOLDEN_HELMET(Material.GOLDEN_HELMET, ItemCategory.ARMOR, EquipmentSlot.HEAD, Stats.CRITICAL_DAMAGE),

    DIAMOND_BOOTS(Material.DIAMOND_BOOTS, ItemCategory.ARMOR, EquipmentSlot.FEET, Stats.LIFE_STEAL),
    DIAMOND_LEGGINGS(Material.DIAMOND_LEGGINGS, ItemCategory.ARMOR, EquipmentSlot.LEGS, Stats.LIFE_STEAL),
    DIAMOND_CHESTPLATE(Material.DIAMOND_CHESTPLATE, ItemCategory.ARMOR, EquipmentSlot.CHEST, Stats.LIFE_STEAL),
    DIAMOND_HELMET(Material.DIAMOND_HELMET, ItemCategory.ARMOR, EquipmentSlot.HEAD, Stats.LIFE_STEAL),

    NETHERITE_BOOTS(Material.NETHERITE_BOOTS, ItemCategory.ARMOR, EquipmentSlot.FEET, false),
    NETHERITE_LEGGINGS(Material.NETHERITE_LEGGINGS, ItemCategory.ARMOR, EquipmentSlot.LEGS, false),
    NETHERITE_CHESTPLATE(Material.NETHERITE_CHESTPLATE, ItemCategory.ARMOR, EquipmentSlot.CHEST, false),
    NETHERITE_HELMET(Material.NETHERITE_HELMET, ItemCategory.ARMOR, EquipmentSlot.HEAD, false),

    BOW(Material.BOW, ItemCategory.DISTANCE, EquipmentSlot.HAND, Stats.MOVEMENT_SPEED),
    CROSSBOW(Material.CROSSBOW, ItemCategory.DISTANCE, EquipmentSlot.HAND, Stats.CRITICAL_DAMAGE),
    SHIELD(Material.SHIELD, ItemCategory.SHIELD, EquipmentSlot.OFF_HAND, Stats.KNOCKBACK_RESISTANCE),
    ;

    final Material material;
    final ItemCategory itemCategory;
    EquipmentSlot slot;
    Stats bonusPrimaryStat;
    boolean canBeLoot;

    DropTable(Material material, ItemCategory itemCategory, EquipmentSlot slot, boolean canBeLoot) {
        this.material = material;
        this.itemCategory = itemCategory;
        this.slot = slot;
        this.canBeLoot = canBeLoot;
    }

    DropTable(Material material, ItemCategory itemCategory, EquipmentSlot slot) {
        this.material = material;
        this.itemCategory = itemCategory;
        this.slot = slot;
        canBeLoot = true;
    }

    DropTable(Material material, ItemCategory itemCategory, EquipmentSlot slot, Stats bonusPrimaryStat) {
        this.material = material;
        this.itemCategory = itemCategory;
        this.slot = slot;
        this.bonusPrimaryStat = bonusPrimaryStat;
        canBeLoot = true;
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

    public Stats getBonusPrimaryStat() {
        return bonusPrimaryStat;
    }

    public boolean canBeLoot() {
        return canBeLoot;
    }
}
