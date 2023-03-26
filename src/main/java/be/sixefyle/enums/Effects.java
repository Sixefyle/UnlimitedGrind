package be.sixefyle.enums;

public enum Effects {
    ABSORPTION(.1, true, Stats.HEALTH, true),
    REGENERATION(.01, true, true, true),
    HEAL(.15, true, true, true),
    POISON(.01, true, true, false),
    FIRE(.07, true, true, false),
    INCREASE_DAMAGE(.15, true, Stats.ATTACK_DAMAGE, true),
    WEAKNESS(.22, true, Stats.ATTACK_DAMAGE, false),
    ;

    double strength;
    boolean isPercentage;
    Stats affectedStats;
    boolean isBuff;
    boolean doDirectHealthDamage = false;

    Effects(double strength, boolean isPercentage, Stats affectedStats, boolean isBuff) {
        this.strength = strength;
        this.isPercentage = isPercentage;
        this.affectedStats = affectedStats;
        this.isBuff = isBuff;
    }

    Effects(double strength, boolean isPercentage, boolean doDirectHealthDamage, boolean isBuff) {
        this.strength = strength;
        this.isPercentage = isPercentage;
        this.doDirectHealthDamage = doDirectHealthDamage;
        this.isBuff = isBuff;
    }

    public boolean isBuff() {
        return isBuff;
    }

    public boolean isDirectHealthDamage() {
        return doDirectHealthDamage;
    }

    public double getStrength() {
        return strength;
    }

    public Stats getAffectedStats() {
        return affectedStats;
    }

    public boolean isPercentage() {
        return isPercentage;
    }
}
