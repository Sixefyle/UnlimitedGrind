package be.sixefyle;

import com.iridium.iridiumskyblock.database.Island;
import org.bukkit.Location;

import java.util.*;

public class UGIsland {

    private static Map<Optional<Island>, UGIsland> islands = new HashMap<>();
    private List<Location> generatorsLoc = new ArrayList<>();
    private Optional<Island> island;

    public UGIsland(Optional<Island> island) {
        islands.put(island, this);
        this.island = island;
    }

    public void addGenerator(Location loc){
        generatorsLoc.add(loc);
    }

    public void removeGenerator(Location loc){
        generatorsLoc.remove(loc);
    }

    public List<Location> getGeneratorsLoc() {
        return generatorsLoc;
    }

    public static UGIsland getIsland(Optional<Island> island) {
        return islands.get(island);
    }

    public Optional<Island> getIsland() {
        return island;
    }
}
