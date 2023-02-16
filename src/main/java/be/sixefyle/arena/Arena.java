package be.sixefyle.arena;

import be.sixefyle.UnlimitedGrind;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.List;

public enum Arena {
    TEST("test",
            Arrays.asList(new Location(null, 25, 65, 15)),
            Arrays.asList(new Location(null, 10, 65, 10))),
    ICE("ice",
            Arrays.asList(new Location(null, -78, 79, 53)),
            Arrays.asList(new Location(null, -99, 80, 77))),
    ;

    private String schematicName;
    private List<Location> creatureSpawnLocs;
    private List<Location> playerSpawnLocs;

    Arena(String schematicName, List<Location> creatureSpawnLocs, List<Location> playerSpawnLocs) {
        this.schematicName = schematicName;
        this.creatureSpawnLocs = creatureSpawnLocs;
        this.playerSpawnLocs = playerSpawnLocs;
    }

    public String getSchematicName() {
        return schematicName;
    }

    public List<Location> getCreatureSpawnLocs() {
        return creatureSpawnLocs;
    }

    public List<Location> getPlayerSpawnLocs() {
        return playerSpawnLocs;
    }
}
