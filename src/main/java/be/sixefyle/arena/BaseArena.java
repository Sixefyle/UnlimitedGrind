package be.sixefyle.arena;

import java.util.UUID;

public abstract class BaseArena {

    private ArenaMap arena;

    public BaseArena(ArenaMap arena) {
        this.arena = arena;
    }

    public ArenaMap getArena() {
        return arena;
    }

    public abstract void join(double power);

    public abstract UUID getWorldUUID();

    public abstract String getWorldName();
}
