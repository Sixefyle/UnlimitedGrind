package be.sixefyle.arena.pve;

import be.sixefyle.UGPlayer;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.arena.ArenaMap;
import be.sixefyle.arena.WorldManager;
import be.sixefyle.entity.boss.UGBoss;
import be.sixefyle.group.Group;
import be.sixefyle.utils.NumberUtils;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.IslandBank;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
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
    private int creatureToSpawn;
    private double score;
    private double arenaPower;
    private double powerToAddEachBoss;
    private BukkitTask bukkitTask;
    private final World world;
    private int playerAlive;
    private boolean isBossWave;

    private final int defaultTimeBeforeStartWave = 100;
    private int timeBeforeStartWave = defaultTimeBeforeStartWave;

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
        if(isBossWave()) {
            try {
                Damageable boss = wave.getAliveCreatures().get(0);

                getBossBar().setTitle(((UGBoss) boss.getMetadata("ugBoss").get(0).value()).getName());
                getBossBar().setColor(BarColor.PURPLE);
                getBossBar().setProgress(boss.getHealth() / boss.getMaxHealth());
                getBossBar().setStyle(BarStyle.SEGMENTED_20);
            } catch (IndexOutOfBoundsException ignore) {}
        }
        else if(!wave.isEnd()){
            getBossBar().setTitle("Wave " + getCurrentWave() + " (" + getWave().getAliveCreatures().size() + " Creatures Alives)");
            getBossBar().setColor(BarColor.RED);
            getBossBar().setProgress((double) getWave().getAliveCreatures().size() / getWave().getTotalWaveCreatureAmount());
            getBossBar().setStyle(BarStyle.SEGMENTED_10);
        } else {
            getBossBar().setTitle("Starting next wave in " + timeBeforeStartWave/20);
            getBossBar().setColor(BarColor.YELLOW);
            getBossBar().setProgress((double) timeBeforeStartWave / defaultTimeBeforeStartWave);
            getBossBar().setStyle(BarStyle.SOLID);
        }
    }

    private void waitAndStartNexWave(){
        new BukkitRunnable() {
            @Override
            public void run() {
                if (timeBeforeStartWave-- <= 0) {
                    cancel();
                    timeBeforeStartWave = defaultTimeBeforeStartWave;
                    setBossWave(++currentWave % bossWave == 0);

                    respawnPlayers();

                    if (isBossWave()) {
                        setCreatureToSpawn(Math.min(getCreatureToSpawn() + arenaMap.getCreatureToAddPerBossWave(), arenaMap.getMaxCreature()));
                        addArenaPower();
                        wave.spawnBoss(arenaPower);
                    } else {
                        wave.setCreatureToSpawnAmount(getCreatureToSpawn());
                        wave.start(arenaPower, currentWave);
                    }
                }
                updateBossBar();
            }
        }.runTaskTimer(UnlimitedGrind.getInstance(), 0, 1);
    }

    public void startGame(){
        setCreatureToSpawn(Math.min(arenaMap.getMinCreature() +
                ((currentWave / bossWave) * arenaMap.getCreatureToAddPerBossWave()), arenaMap.getMaxCreature()));

        wave = new Wave(getCreatureToSpawn(), arenaMap.getCreatureSpawnLocs(), world, group);
        wave.start(arenaPower + ((currentWave / bossWave) * powerToAddEachBoss), currentWave);

        for (UGPlayer ugPlayer : group.getMembers()) {
            bossBar.addPlayer(ugPlayer.getPlayer());
        }
        updateBossBar();

        bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                if(wave.isEnd() && timeBeforeStartWave == defaultTimeBeforeStartWave){
                    waitAndStartNexWave();
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
        crystalGain = ArenaManager.getCrystalReward(arenaPower, currentWave); //TODO: magic number
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

    public int getCreatureToSpawn() {
        return creatureToSpawn;
    }

    public void setCreatureToSpawn(int creatureToSpawn) {
        this.creatureToSpawn = creatureToSpawn;
    }

    public void setCurrentWave(int currentWave) {
        this.currentWave = currentWave;
    }

    public static double getCrystalReward(double power, int wave){
        return (power * (wave - 1)) / 85;
    }

    public boolean isBossWave() {
        return isBossWave;
    }

    public void setBossWave(boolean bossWave) {
        isBossWave = bossWave;
    }
}