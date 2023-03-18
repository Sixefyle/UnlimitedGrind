package be.sixefyle.enums;

import org.bukkit.attribute.Attribute;

public enum Stats {
    ATTACK_DAMAGE(Attribute.GENERIC_ATTACK_DAMAGE, "Attack Damage", true, 1, Double.MAX_VALUE, true, false),
    ATTACK_SPEED(Attribute.GENERIC_ATTACK_SPEED,"Attack Speed", true, -3, 3, true, false),
    ARMOR(Attribute.GENERIC_ARMOR,"Armor", true, 1, 60000, true, false),
    HEALTH("bonusHealth","Health", false, 1, Double.MAX_VALUE, true, false),
    VITALITY("vitality","Vitality", false, 1, Double.MAX_VALUE, true, false),
    STRENGTH("strength","Strength", false, 1, Double.MAX_VALUE, true, false),
    CRITICAL_DAMAGE("critDamage","Critical Damage", false, .2, .5, true, true),
    CRITICAL_CHANCE("critChance","Critical Chance", false, .03, .08, true, true),
    LIFE_STEAL("lifeSteal", "Life Steal",false, .01, .03, true, true),

    BONUS_STRENGTH("bonusStrength","Strength", false, 1, 150, false, false),
    BONUS_CRITICAL_CHANCE("bonusCritChance","Critical Chance", false, .01, .05, false, true),
    BONUS_CRITICAL_DAMAGE("bonusCritDamage","Critical Damage", false, .1, .25, false, true),
    SWEEPING_RANGE("sweepRange", "Sweep Range",false, .05, .1, false, true),
    SWEEPING_DAMAGE("sweepDamage", "Sweep Damage",false, .01, 1.1, false, true),

    MELEE_DAMAGE_REDUCTION("meleeDamageReduction", "Melee Damage Reduction",false, .01, .05, false, true),
    RANGE_DAMAGE_REDUCTION("rangeDamageReduction", "Range Damage Reduction",false, .01, .05, false, true),

    BONUS_ATTACK_DAMAGE(Attribute.GENERIC_ATTACK_DAMAGE, "Attack Damage", true, 0, .25, false, true),
    BONUS_ATTACK_SPEED(Attribute.GENERIC_ATTACK_SPEED,"Attack Speed", true, 0, .25, false, true),
    ATTACK_KNOCKBACK(Attribute.GENERIC_ATTACK_KNOCKBACK,"Attack Knockback", true, 0, .25, false, true),
    MOVEMENT_SPEED(Attribute.GENERIC_MOVEMENT_SPEED,"Movement Speed", true, 0, .25, false, true),
    ARMOR_TOUGHNESS(Attribute.GENERIC_ARMOR_TOUGHNESS,"Armor Toughness", true, 0, .25, false, true),
    KNOCKBACK_RESISTANCE(Attribute.GENERIC_KNOCKBACK_RESISTANCE,"Knockback Resistance", true, 0, .25, false, true),
    ;

    Object stats;
    String displayName;
    boolean isPercent;
    boolean isAttribute;
    boolean isPrimaryStat;
    double min;
    double max;

    Stats(Object stats, String displayName, boolean isAttribute, double min, double max, boolean isPrimaryStat, boolean isPercent) {
        this.stats = stats;
        this.displayName = displayName;
        this.isAttribute = isAttribute;
        this.min = min;
        this.max = max;
        this.isPrimaryStat = isPrimaryStat;
        this.isPercent = isPercent;
    }


    public Object getStats() {
        return stats;
    }

    public boolean isAttribute() {
        return isAttribute;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public boolean isPrimary() {
        return isPrimaryStat;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isPercent() {
        return isPercent;
    }
}
