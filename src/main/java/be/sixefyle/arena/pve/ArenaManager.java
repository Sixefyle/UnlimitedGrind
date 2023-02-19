package be.sixefyle.arena.pve;

import be.sixefyle.UGPlayer;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.arena.Arena;
import be.sixefyle.arena.WorldManager;
import be.sixefyle.utils.NumberUtils;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.IslandBank;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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

    public void respawnPlayer(){
        Player player;
        Location loc;
        for (UGPlayer ugPlayer : ugPlayers) {
            player = ugPlayer.getPlayer();
            if(player.getGameMode().equals(GameMode.SPECTATOR)){
                loc = arena.getPlayerSpawnLocs().get(0).clone();
                loc.setWorld(world);
                ugPlayer.getPlayer().teleport(loc);
            }
        }
    }

    public void startGame(){
        wave = new Wave(minCreature, arena.getCreatureSpawnLocs(), world, ugPlayers);
        wave.start(arenaPower, currentWave);

        bukkitTask = new BukkitRunnable() {
            int newAmount = minCreature;
            @Override
            public void run() {
                if(wave.isEnd()){
                    respawnPlayer();

                    wave.setCreatureToSpawnAmount(newAmount);
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

        IslandBank islandBank;
        Optional<Island> island;
        double crystalGain;
        for (UGPlayer ugPlayer : ugPlayers) {
            ugPlayer.leavePveArena();

            island = IridiumSkyblockAPI.getInstance().getUser(ugPlayer.getPlayer()).getIsland();
            if(island.isEmpty()) continue;
            crystalGain = Math.pow(currentWave, 1.14)-1; //TODO: magic number
            islandBank = IridiumSkyblock.getInstance().getIslandManager().getIslandBank(island.get(), IridiumSkyblock.getInstance().getBankItems().crystalsBankItem);
            islandBank.setNumber(islandBank.getNumber() + crystalGain);
            ugPlayer.getPlayer().sendMessage(Component.text("You got " + NumberUtils.format(crystalGain) + " crystals!"));
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