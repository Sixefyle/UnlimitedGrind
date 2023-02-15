package be.sixefyle.arena.pve;

import be.sixefyle.UnlimitedGrind;
import be.sixefyle.arena.Arena;
import be.sixefyle.arena.WorldManager;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
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
    private List<Player> players = new ArrayList<>();
    private Player owner;

    private static HashMap<World, ArenaManager> arenaManagers = new HashMap<>();

    public ArenaManager(Arena arena, World world, List<Player> players, int minCreature, int maxCreature) {
        this(arena, world, players);
        this.minCreature = minCreature;
        this.maxCreature = maxCreature;
    }

    public ArenaManager(Arena arena, World world, List<Player> players) {
        currentWave = 1;
        bossWave = 5;
        minCreature = 5;
        maxCreature = 30;
        this.arena = arena;
        this.world = world;
        this.players.addAll(players);
        this.owner = players.get(0);

        arenaManagers.put(world, this);
    }

    public void startGame(){
        wave = new Wave(minCreature, arena.getCreatureSpawnLocs(), world, players);
        wave.start(arenaPower);

        bukkitTask = new BukkitRunnable() {
            int newAmount;
            @Override
            public void run() {
                if(wave.isEnd()){
                    newAmount = Math.min(minCreature + 2, maxCreature);
                    wave.setCreatureToSpawn(newAmount);
                    wave.start(arenaPower);

                    if(++currentWave % bossWave == 0){
                        wave.spawnBoss();
                        addArenaPower();
                    }
                }
            }
        }.runTaskTimer(UnlimitedGrind.getInstance(), 100, 60);
    }

    public void stopGame(){
        wave.end();
        bukkitTask.cancel();

        WorldManager.deleteWorldArena(owner);
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

    public List<Player> getPlayers() {
        return players;
    }

    public static HashMap<World, ArenaManager> getArenaManagers() {
        return arenaManagers;
    }
}