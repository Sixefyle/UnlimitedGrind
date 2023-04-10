package be.sixefyle.entity.boss;

import org.bukkit.Location;

import java.lang.reflect.Constructor;
import java.util.Random;

public enum Bosses {
    DARK_INFESTER(DarkInfester.class, "Dark Infester"),

    ;

    final Class<? extends UGBoss> boss;
    final String name;

    Bosses(Class<? extends UGBoss> boss, String name) {
        this.boss = boss;
        this.name = name;
    }

    public Class<? extends UGBoss> getBossClass() {
        return boss;
    }

    public String getName() {
        return name;
    }

    public static UGBoss getRandomBoss(double power, Location spawnLoc) throws Exception {
        Random random = new Random();
        int randomIndex = random.nextInt(Bosses.values().length);
        Bosses boss = Bosses.values()[randomIndex];
        Class<? extends UGBoss> bossClass = boss.getBossClass();
        Constructor<? extends UGBoss> constructor = bossClass.getConstructor(double.class, Location.class, String.class);
        return constructor.newInstance(power, spawnLoc, boss.getName());
    }
}
