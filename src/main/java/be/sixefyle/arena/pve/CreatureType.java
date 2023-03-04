package be.sixefyle.arena.pve;

public enum CreatureType {
    ZOMBIFIED_PIGLIN(0),
    SPIDER(0),
    ZOMBIE(0),
    ZOMBIE_VILLAGER(0),
    DROWNED(5),
    SKELETON(5),
    SLIME(5),
    HUSK(10),
    PILLAGER(10),
    ENDERMAN(10),
    CAVE_SPIDER(10),
    WITHER_SKELETON(15),
    MAGMA_CUBE(15),
    SILVERFISH(15),
    BLAZE(20),
    WITCH(20),
    PIGLIN_BRUTE(25),
    EVOKER(30),
    VINDICATOR(30),
    ILLUSIONER(40),
    RAVAGER(40),
    HOGLIN(40),
    ELDER_GUARDIAN(50),
    ;

    int minWave;

    CreatureType(int minWave) {
        this.minWave = minWave;
    }

    public int getMinWave() {
        return minWave;
    }
}


