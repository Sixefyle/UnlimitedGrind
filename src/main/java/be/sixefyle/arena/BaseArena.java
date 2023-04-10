package be.sixefyle.arena;

import be.sixefyle.UGPlayer;
import org.bukkit.Location;

import java.util.List;
import java.util.UUID;

public abstract class BaseArena {

    private final ArenaMap arenaMap;
    private final List<Location> creatureSpawnLocations;
    private final List<Location> playerSpawnLocations;
    private final UGPlayer owner;
    private final UUID worldUUID;

    public BaseArena(ArenaMap arenaMap, List<Location> creatureSpawnLocations, List<Location> playerSpawnLocations, UGPlayer owner, UUID worldUUID) {
        this.arenaMap = arenaMap;
        this.creatureSpawnLocations = creatureSpawnLocations;
        this.playerSpawnLocations = playerSpawnLocations;
        this.owner = owner;
        this.worldUUID = worldUUID;
    }

    public abstract void setupArena(double power, int startingWave);

    public String getWorldName(){
        return "arena_" + getWorldUUID();
    }
    public ArenaMap getArenaMap() {
        return arenaMap;
    }

    public List<Location> getCreatureSpawnLocations() {
        return creatureSpawnLocations;
    }

    public List<Location> getPlayerSpawnLocations() {
        return playerSpawnLocations;
    }

    public UGPlayer getOwner() {
        return owner;
    }

    public UUID getWorldUUID() {
        return worldUUID;
    }
}
