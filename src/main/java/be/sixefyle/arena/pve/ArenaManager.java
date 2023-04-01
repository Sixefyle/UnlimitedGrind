package be.sixefyle.arena.pve;

import be.sixefyle.UGPlayer;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.arena.ArenaMap;
import be.sixefyle.arena.WorldManager;
import be.sixefyle.group.Group;
import be.sixefyle.utils.NumberUtils;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.IslandBank;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ArenaManager {
    private int currentWave = 1;
    private final int bossWave = 5;
    private final PveArena pveArena;
    private final ArenaMap arenaMap;
    private final Group group;
    private final BossBar bossBar;
    private Wave wave;
    private double score;
    private double arenaPower;
    private double powerToAddEachBoss;
    private BukkitTask bukkitTask;
    private final World world;
    private int playerAlive;

    private static final HashMap<World, ArenaManager> arenaManagers = new HashMap<>();

    public ArenaManager(PveArena arena, World world, Group group) {
        this.pveArena = arena;
        this.arenaMap = pveArena.getArenaMap();
        this.world = world;
        this.group = group;
        playerAlive = group.getMembers().size();
        bossBar = Bukkit.createBossBar("Arena", BarColor.RED, BarStyle.SEGMENTED_10, BarFlag.CREATE_FOG);

        arenaManagers.put(world, this);
    }

    public void respawnPlayers(){
        Player player;
        for (UGPlayer ugPlayer : group.getMembers()) {
            player = ugPlayer.getPlayer();
            if(player.getGameMode().equals(GameMode.SPECTATOR)){
                pveArena.spawnPlayerToArena(ugPlayer);
                player.setGameMode(GameMode.ADVENTURE);
            }
        }
        setPlayerAlive(group.getMembers().size());
    }

    public void updateBossBar(){
        getBossBar().setTitle("Wave " + getCurrentWave() + " (" + getWave().getAliveCreatures().size() + " Creatures Alives)");
        getBossBar().setProgress((double) getWave().getAliveCreatures().size() / getWave().getTotalWaveCreatureAmount());
    }

    public void startGame(){
        wave = new Wave(arenaMap.getMinCreature(), arenaMap.getCreatureSpawnLocs(), world, group);
        wave.start(arenaPower, currentWave);

        for (UGPlayer ugPlayer : group.getMembers()) {
            bossBar.addPlayer(ugPlayer.getPlayer());
        }
        updateBossBar();

        bukkitTask = new BukkitRunnable() {
            int newAmount = arenaMap.getMinCreature();
            @Override
            public void run() {
                if(wave.isEnd()){
                    respawnPlayers();

                    wave.setCreatureToSpawnAmount(newAmount);
                    wave.start(arenaPower, currentWave);

                    if(++currentWave % bossWave == 0){
                        wave.spawnBoss();
                        newAmount = Math.min(newAmount + arenaMap.getCreatureToAddPerBossWave(), arenaMap.getMaxCreature());
                        addArenaPower();
                    }
                    updateBossBar();
                }
            }
        }.runTaskTimer(UnlimitedGrind.getInstance(), 100, 60);
    }

    public void sendRewards(UGPlayer ugPlayer){
        IslandBank islandBank;
        Optional<Island> island;
        double crystalGain;

        island = IridiumSkyblockAPI.getInstance().getUser(ugPlayer.getPlayer()).getIsland();
        if(island.isEmpty()) return;
        crystalGain = Math.pow(currentWave, 1.14)-1; //TODO: magic number
        islandBank = IridiumSkyblock.getInstance().getIslandManager().getIslandBank(island.get(), IridiumSkyblock.getInstance().getBankItems().crystalsBankItem);
        islandBank.setNumber(islandBank.getNumber() + crystalGain);
        ugPlayer.sendMessageComponents(
                List.of(Component.text("§e§lThe arena as ended !"),
                        Component.text("§7You got §a" + NumberUtils.format(crystalGain) + "§7 crystals!"))
        );
    }

    public void stopGame(){
        wave.end();
        bukkitTask.cancel();
        bossBar.removeAll();

        for (UGPlayer ugPlayer : group.getMembers()) {
            ugPlayer.leaveArena();
        }

        WorldManager.deleteWorld(world);
    }



    public BukkitTask getBukkitTask() {
        return bukkitTask;
    }

    public void setArenaPower(double arenaPower) {
        this.arenaPower = arenaPower;
        this.powerToAddEachBoss = 10 + (arenaPower * 0.075);
    }

    public void addArenaPower(){
        this.arenaPower += powerToAddEachBoss;
    }

    public ArenaMap getArenaMap() {
        return arenaMap;
    }

    public Wave getWave() {
        return wave;
    }

    public World getWorld() {
        return world;
    }

    public Group getGroup() {
        return group;
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

    public void setPlayerAlive(int playerAlive) {
        this.playerAlive = playerAlive;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public double getScore() {
        return score;
    }

    public double getArenaPower() {
        return arenaPower;
    }
}