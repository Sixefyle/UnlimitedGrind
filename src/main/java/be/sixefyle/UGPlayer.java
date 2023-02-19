package be.sixefyle;

import be.sixefyle.arena.Arena;
import be.sixefyle.arena.BaseArena;
import be.sixefyle.arena.pve.PveArena;
import be.sixefyle.enums.Symbols;
import be.sixefyle.exception.PlayerNotExist;
import be.sixefyle.group.Group;
import be.sixefyle.utils.NumberUtils;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import com.iridium.iridiumcore.DefaultFontInfo;
import com.iridium.iridiumcore.IridiumCore;
import com.iridium.iridiumcore.utils.StringUtils;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.database.Island;
import josegamerpt.realscoreboard.api.RealScoreboardAPI;
import josegamerpt.realscoreboard.api.scoreboard.RScoreboard;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static be.sixefyle.UnlimitedGrind.getInstance;

public class UGPlayer {
    public static HashMap<UUID, UGPlayer> playerMap = new HashMap<>();
    private Player player;
    private UGIsland ugIsland;
    private RScoreboard scoreboard;
    private BaseArena arena;
    private Group group;

    public static UGPlayer GetUGPlayer(Player player){
        return playerMap.get(player.getUniqueId());
    }

    private int level;
    private int experience;
    private double power;

    public UGPlayer(Player player) {
        if(playerMap.containsKey(player.getUniqueId())) return;
        this.player = player;

        Optional<Island> island = IridiumSkyblockAPI.getInstance().getUser(player).getIsland();
        if(island.isPresent()) {
            ugIsland = new UGIsland(island);
        }

        try {
            getPlayerData();
        } catch (PlayerNotExist e) {
            power = 150000;
            level = 1;
            experience = 0;
        }

        initScoreboard();
        updateScoreboardLine();
        playerMap.putIfAbsent(player.getUniqueId(), this);
    }

    public void initScoreboard(){
        Island island = ugIsland.getIsland().get();
        scoreboard = RealScoreboardAPI.getInstance().getScoreboardManager().getScoreboard(player);
        scoreboard.getLines().clear();

        List<String> lines = new ArrayList<>() {{
            add("         #{f79f07}&l⭐#{f0aa32} Rank " + island.getRank() + " #{f79f07}&l⭐");
            add("");
            add("#{e23f22}" + Symbols.PLAYER.get() + "#{e25822} Player Informations:" );
            add("  #{e23f22}▸#{E6EED6} Power &c" + NumberUtils.format(power) + Symbols.POWER.get());
            add("  #{e23f22}▸#{E6EED6} Money &e" + NumberUtils.format(UnlimitedGrind.getEconomy().getBalance(player)) + Symbols.COIN.get());
            add("  #{e23f22}▸#{E6EED6} Crystals&r &a" + NumberUtils.format(island.getCrystals()));
            add("");
            add("#{48bff0}" + Symbols.ISLAND.get() + "#{87CEEB} Island Informations:");
            add("  #{48bff0}▸#{E4F0D0} Value - " + NumberUtils.format(island.getValue()));
            add("  #{48bff0}▸#{E4F0D0} Level - " + NumberUtils.format(island.getLevel()));
            add("  #{48bff0}▸#{E4F0D0} Bank");
            add("    #{48bff0}▹#{E4F0D0} Money - " + island.getMoney());
            add("    #{48bff0}▹#{E4F0D0} Crystals - " + island.getCrystals());
        }};

        scoreboard.getLines().addAll(lines);
    }

    public void updateScoreboardLine(){
        Island island = ugIsland.getIsland().get();
        Bukkit.getScheduler().runTaskTimer(UnlimitedGrind.getInstance(), () -> {
            scoreboard.getLines().set(0,"         #{f79f07}&l⭐#{f0aa32} Rank " + ugIsland.getIsland().get().getRank() + " #{f79f07}&l⭐");
            scoreboard.getLines().set(3,"  #{e23f22}▸#{E6EED6} Power &c" + NumberUtils.format(power) + Symbols.POWER.get());
            scoreboard.getLines().set(4,"  #{e23f22}▸#{E6EED6} Money &e" + NumberUtils.format(UnlimitedGrind.getEconomy().getBalance(player)) + Symbols.COIN.get());
            scoreboard.getLines().set(8,"  #{48bff0}▸#{E4F0D0} Value - " + NumberUtils.format(island.getValue()));
            scoreboard.getLines().set(9,"  #{48bff0}▸#{E4F0D0} Level - " + NumberUtils.format(island.getLevel()));
            scoreboard.getLines().set(11,"    #{48bff0}▹#{E4F0D0} Money - " + island.getMoney() + Symbols.COIN.get());
            scoreboard.getLines().set(12,"    #{48bff0}▹#{E4F0D0} Crystals - " + island.getCrystals() + Symbols.CRYSTALS.get());
        }, 100, 100);
    }

    public void addPower(int power) {
        this.power += power;
    }

    public Player getPlayer() {
        return player;
    }

    public int getLevel() {
        return level;
    }

    public int getExperience() {
        return experience;
    }

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }

    public void getPlayerData() throws PlayerNotExist {

        throw new PlayerNotExist();
    }

    public Group getGroup() {
        return group;
    }

    public Group setGroup(Group group) {
        return this.group = group;
    }

    public boolean leaveGroup(){
        if(group == null) return false;

        group.removePlayer(this);
        group = null;
        return true;
    }

    public void joinPveArena(Arena arena, double power){
        this.arena = new PveArena(this, arena);
        this.arena.join(power);
        player.setGameMode(GameMode.ADVENTURE);
    }

    public void leavePveArena(){
        this.arena = null;
        player.setGameMode(GameMode.SURVIVAL);
        player.spigot().respawn();
    }

    public BaseArena getArena() {
        return arena;
    }

    public void sendMessageComponents(List<Component> components){
        int spaceToDo = 2;
        Component messageToSend = Component.empty();
        for (int i = 0; i < spaceToDo ; i++) {
            messageToSend = messageToSend.append(Component.newline());
        }
        for (Component component : components) {
            messageToSend = messageToSend.append(component).append(Component.newline());
        }
        for (int i = 0; i < spaceToDo-1; i++) {
            messageToSend = messageToSend.append(Component.newline());
        }

        getPlayer().sendMessage(messageToSend);
    }

    public void sendMessageComponents(Component component){
        sendMessageComponents(List.of(component));
    }
}
