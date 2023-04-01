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
            List.of(new Location(null, -78, 79, 53)), 10, 50, 7),
    ;

    private final String schematicName;
    private final List<Location> creatureSpawnLocs;
    private final List<Location> playerSpawnLocs;
    private final int minCreature;
    private final int maxCreature;
    private final int creatureToAddPerBossWave;

    ArenaMap(String schematicName, List<Location> creatureSpawnLocs, List<Location> playerSpawnLocs, int minCreature, int maxCreature, int creatureToAddPerWave) {
        this.schematicName = schematicName;
        this.creatureSpawnLocs = creatureSpawnLocs;
        this.playerSpawnLocs = playerSpawnLocs;
        this.minCreature = minCreature;
        this.maxCreature = maxCreature;
        this.creatureToAddPerBossWave = creatureToAddPerWave;
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

    public int getMinCreature() {
        return minCreature;
    }

    public int getMaxCreature() {
        return maxCreature;
    }

    public int getCreatureToAddPerBossWave() {
        return creatureToAddPerBossWave;
    }
}
