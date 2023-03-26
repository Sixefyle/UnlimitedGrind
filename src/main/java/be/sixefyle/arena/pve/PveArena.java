package be.sixefyle.arena.pve;

import be.sixefyle.UGPlayer;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.arena.ArenaMap;
import be.sixefyle.arena.BaseArena;
import be.sixefyle.arena.WorldManager;
import be.sixefyle.group.Group;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PveArena extends BaseArena {

    //TODO: transfer var to BaseArena
    private final List<Location> creatureSpawnLocations;
    private final List<Location> playerSpawnLocations;
    private UGPlayer owner;
    private Group group;
    private UUID worldUUID;
    private ArenaManager arenaManager;

    public PveArena(UGPlayer owner, ArenaMap arena) {
        super(arena);
        this.creatureSpawnLocations = arena.getCreatureSpawnLocs();
        this.playerSpawnLocations = arena.getCreatureSpawnLocs();
        this.owner = owner;
        this.worldUUID = owner.getPlayer().getUniqueId();
    }

    public PveArena(Group group, ArenaMap arena) {
        super(arena);
        this.creatureSpawnLocations = arena.getCreatureSpawnLocs();
        this.playerSpawnLocations = arena.getCreatureSpawnLocs();
        this.group = group;
        this.owner = group.getOwner();
        this.worldUUID = owner.getPlayer().getUniqueId();
    }

    @Override
    public void join(double power){
        Player player = owner.getPlayer();
        UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
        if(WorldManager.createVoidAndTeleport(player, group, getArena())) {
            arenaManager = new ArenaManager(getArena(), player.getWorld(), Arrays.asList(ugPlayer));
            arenaManager.setArenaPower(power);
            arenaManager.startGame();

            player.setMetadata("arenaWorld", new FixedMetadataValue(UnlimitedGrind.getInstance(), player.getWorld()));
        }
    }

    @Override
    public UUID getWorldUUID() {
        return worldUUID;
    }

    @Override
    public String getWorldName(){
        return "arena_" + worldUUID;
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
