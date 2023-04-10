package be.sixefyle.entity.boss;

import be.sixefyle.UnlimitedGrind;
import be.sixefyle.arena.pve.ArenaManager;
import be.sixefyle.entity.UGEntity;
import be.sixefyle.items.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

public abstract class UGBoss extends UGEntity {
    private BukkitTask bossTask;
    private double nextPatternTime = 20;
    private double patternTime = nextPatternTime;
    private Random random = new Random();
    private String name;
    private ArenaManager arenaManager;

    public UGBoss(double power, Location location, String name, EntityType creatureType, ChatColor glowingColor) {
        super(power, location, creatureType, glowingColor);
        this.name = name;

        this.arenaManager = ArenaManager.getArenaManagers().get(location.getWorld());

        initBossTask();

        registerMetadata("ugBoss", this);
        registerMetadata("wave", 0);
    }

    private void initBossTask(){
        bossTask = new BukkitRunnable() {
            @Override
            public void run() {
                if(getPatternTime() <= 0) {
                    startPattern();
                    patternTime = getNextPatternTime();
                }
                patternTime -= 20;
            }
        }.runTaskTimer(UnlimitedGrind.getInstance(), 0, 20);
    }

    public abstract void startPattern();

    public void onDie() {
        arenaManager.setBossWave(false);
        bossTask.cancel();

        Location loc = getEntity().getLocation();
        for (int i = 0; i < 3; i++) {
            loc.getWorld().dropItemNaturally(loc, ItemManager.generateRandomItem(getPower()).asItemStack());
        }
    }

    public double getPatternTime() {
        return patternTime;
    }

    public double getNextPatternTime() {
        return nextPatternTime;
    }

    public void setNextPatternTime(double nextPatternTime) {
        this.nextPatternTime = nextPatternTime;
    }

    public Random getRandom() {
        return random;
    }

    public String getName() {
        return name;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }
}
