package be.sixefyle.arena.pve;

import be.sixefyle.UGPlayer;
import be.sixefyle.arena.ArenaMap;
import be.sixefyle.arena.BaseArena;
import be.sixefyle.arena.WorldManager;
import be.sixefyle.group.Group;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public class PveArena extends BaseArena {

    private Group group;
    private ArenaManager arenaManager;

    public PveArena(UGPlayer owner, ArenaMap arenaMap) {
        super(arenaMap, arenaMap.getCreatureSpawnLocs(), arenaMap.getPlayerSpawnLocs(), owner, UUID.randomUUID());
    }

    public PveArena(Group group, ArenaMap arena) {
        this(group.getOwner(), arena);
        this.group = group;
    }

    @Override
    public void setupArena(double power){
        World world = WorldManager.createArenaMap(getWorldUUID(), getArenaMap());
        if(world != null) {
            if(group == null){
                group = new Group(getOwner());
            }

            arenaManager = new ArenaManager(this, world, group);
            arenaManager.setArenaPower(power);

            for (UGPlayer ugPlayer : group.getMembers()) {
                ugPlayer.setArena(this);
            }
            teleportPlayers();
            arenaManager.startGame();
        }
    }

    public void teleportPlayers() {
        for (UGPlayer member : group.getMembers()) {
            spawnPlayerToArena(member);
        }
    }

    public void spawnPlayerToArena(UGPlayer ugPlayer){
        Location loc = getArenaMap().getPlayerSpawnLocs().get((int) (Math.random() * getArenaMap().getPlayerSpawnLocs().size())).clone();
        loc.setWorld(getArenaManager().getWorld());
        ugPlayer.getPlayer().teleport(loc);
    }

    public void quit(UGPlayer ugPlayer){
        arenaManager.reducePlayerAlive();
        arenaManager.sendRewards(ugPlayer);
        arenaManager.getBossBar().removePlayer(ugPlayer.getPlayer());
        ugPlayer.setArena(null);
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public Group getGroup() {
        return group;
    }
}
