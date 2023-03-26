package be.sixefyle.arena;

import org.bukkit.Location;

import java.util.List;

public enum ArenaMap {
    ICE("ice",
            List.of(
                    new Location(null, -99, 80, 77),
                    new Location(null, -96, 80, 87),
                    new Location(null, -128, 79, 65),
                    new Location(null, -108, 79, 35),
                    new Location(null, -55, 78, 29),
                    new Location(null, -40, 81, 87),
                    new Location(null, -79, 80, 101)
            ),
            List.of(new Location(null, -78, 79, 53))),
    ;

    private String schematicName;
    private List<Location> creatureSpawnLocs;
    private List<Location> playerSpawnLocs;

    ArenaMap(String schematicName, List<Location> creatureSpawnLocs, List<Location> playerSpawnLocs) {
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
