package be.sixefyle.arena.pve;

import be.sixefyle.UGPlayer;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.arena.Arena;
import be.sixefyle.arena.WorldManager;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ArenaManager {
    private final Arena arena;
    private Wave wave;
    private int currentWave;
    private int minCreature;
    private int maxCreature;
    private int bossWave;
    private double score;
    private double arenaPower;
    private double powerToAddEachWave;
    private BukkitTask bukkitTask;
    private World world;
    private List<UGPlayer> ugPlayers = new ArrayList<>();
    private int playerAlive;

    private static HashMap<World, ArenaManager> arenaManagers = new HashMap<>();

    public ArenaManager(Arena arena, World world, List<UGPlayer> players, int minCreature, int maxCreature) {
        this(arena, world, players);
        this.minCreature = minCreature;
        this.maxCreature = maxCreature;
    }

    public ArenaManager(Arena arena, World world, List<UGPlayer> players) {
        currentWave = 1;
        bossWave = 5;
        minCreature = 5;
        maxCreature = 30;
        this.arena = arena;
        this.world = world;
        this.ugPlayers.addAll(players);
        playerAlive = players.size();

        arenaManagers.put(world, this);
    }

    public void startGame(){
        wave = new Wave(minCreature, arena.getCreatureSpawnLocs(), world, ugPlayers);
        wave.start(arenaPower, currentWave);

        bukkitTask = new BukkitRunnable() {
            int newAmount = minCreature;
            @Override
            public void run() {
                if(wave.isEnd()){
                    wave.setCreatureToSpawn(newAmount);
                    wave.start(arenaPower, currentWave);

                    if(++currentWave % bossWave == 0){
                        wave.spawnBoss();
                        newAmount = Math.min(minCreature += 2, maxCreature);
                        addArenaPower();
                    }
                }
            }
        }.runTaskTimer(UnlimitedGrind.getInstance(), 100, 60);
    }

    public void stopGame(){
        wave.end();
        bukkitTask.cancel();

        for (UGPlayer ugPlayer : ugPlayers) {
            ugPlayer.getPlayer().sendMessage(Component.text("Finito gg!"));
        }

        WorldManager.deleteWorld(world);
    }

    public BukkitTask getBukkitTask() {
        return bukkitTask;
    }

    public void setArenaPower(double arenaPower) {
        this.arenaPower = arenaPower;
        this.powerToAddEachWave = arenaPower * 0.075;
    }

    public void addArenaPower(){
        this.arenaPower += powerToAddEachWave;
    }

    public Arena getArena() {
        return arena;
    }

    public Wave getWave() {
        return wave;
    }

    public World getWorld() {
        return world;
    }

    public List<UGPlayer> getUgPlayers() {
        return ugPlayers;
    }

    public static HashMap<World, ArenaManager> getArenaManagers() {
        return arenaManagers;
    }

    public int getPlayerAlive() {
        return playerAlive;
    }

    public void reducePlayerAlive(){
        playerAlive--;
    }
}