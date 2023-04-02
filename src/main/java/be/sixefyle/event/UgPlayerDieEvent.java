package be.sixefyle.event;

import be.sixefyle.UGPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class UgPlayerDieEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled = false;

    private UGPlayer ugPlayer;
    private Entity killer;
    private Location loc;

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public UgPlayerDieEvent(UGPlayer ugPlayer, Entity killer, Location loc) {
        this.ugPlayer = ugPlayer;
        this.killer = killer;
        this.loc = loc;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }

    public UGPlayer getUgPlayer() {
        return ugPlayer;
    }

    public Entity getKiller() {
        return killer;
    }

    public Location getLoc() {
        return loc;
    }
}
