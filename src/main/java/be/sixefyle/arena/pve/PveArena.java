package be.sixefyle.arena.pve;

import be.sixefyle.UnlimitedGrind;
import be.sixefyle.arena.Arena;
import be.sixefyle.arena.BaseArena;
import be.sixefyle.arena.WorldManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Arrays;
import java.util.List;

public class PveArena extends BaseArena {

    private final List<Location> creatureSpawnLocations;
    private final List<Location> playerSpawnLocations;
    private Player owner;
    private ArenaManager arenaManager;

    public PveArena(Player owner, Arena arena) {
        super(arena);
        this.creatureSpawnLocations = arena.getCreatureSpawnLocs();
        this.playerSpawnLocations = arena.getCreatureSpawnLocs();
        this.owner = owner;
    }

    public void join(double power){
        if(WorldManager.createVoidAndTeleport(owner, getArena())) {
            arenaManager = new ArenaManager(getArena(), owner.getWorld(), Arrays.asList(owner));
            arenaManager.setArenaPower(power);
            arenaManager.startGame();

            owner.setMetadata("arenaWorld", new FixedMetadataValue(UnlimitedGrind.getInstance(), owner.getWorld()));
        }
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public List<Location> getCreatureSpawnLocations() {
        return creatureSpawnLocations;
    }

    public List<Location> getPlayerSpawnLocations() {
        return playerSpawnLocations;
    }
}
