package be.sixefyle.arena;

import java.util.UUID;

public abstract class BaseArena {

    private Arena arena;

    public BaseArena(Arena arena) {
        this.arena = arena;
    }

    public Arena getArena() {
        return arena;
    }

    public abstract void join(double power);

    public abstract UUID getWorldUUID();

    public abstract String getWorldName();
}
