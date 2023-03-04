package be.sixefyle.enums;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;

public enum AttributesName {
    GENERIC_ATTACK_DAMAGE(Attribute.GENERIC_ATTACK_DAMAGE, "Attack Damage"),
    GENERIC_ATTACK_SPEED(Attribute.GENERIC_ATTACK_SPEED, "Attack Speed"),
    GENERIC_ARMOR(Attribute.GENERIC_ARMOR, "Armor"),
    GENERIC_MAX_HEALTH(Attribute.GENERIC_MAX_HEALTH, "Health"),
    GENERIC_ARMOR_TOUGHNESS(Attribute.GENERIC_ARMOR_TOUGHNESS, "Armor Toughness"),
    GENERIC_ATTACK_KNOCKBACK(Attribute.GENERIC_ATTACK_KNOCKBACK, "Attack Knockback"),
    GENERIC_FLYING_SPEED(Attribute.GENERIC_FLYING_SPEED, "Flying Speed"),
    GENERIC_FOLLOW_RANGE(Attribute.GENERIC_FOLLOW_RANGE, "Follow Range"),
    GENERIC_KNOCKBACK_RESISTANCE(Attribute.GENERIC_KNOCKBACK_RESISTANCE, "Knockback Resistance"),
    GENERIC_LUCK(Attribute.GENERIC_LUCK, "Luck"),
    GENERIC_MOVEMENT_SPEED(Attribute.GENERIC_MOVEMENT_SPEED, "Movement Speed"),
    HORSE_JUMP_STRENGTH(Attribute.HORSE_JUMP_STRENGTH, "Horse Jump Strength"),
    ;

    Attribute attribute;
    String name;

    AttributesName(Attribute attribute, String name) {
        this.attribute = attribute;
        this.name = name;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public String getName() {
        return name;
    }

    public static AttributesName getByAttribute(Attribute attribute){
        for (AttributesName value : AttributesName.values()) {
            if(attribute.equals(value.attribute)){
                return value;
            }
        }
        return null;
    }

    public static boolean isPrimary(AttributeModifier attributeModifier){
        return attributeModifier.getOperation().equals(AttributeModifier.Operation.ADD_NUMBER);
    }
}
