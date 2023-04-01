package be.sixefyle.event;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class OnPostDamageEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private double damage;
    private Entity attacker;
    private Entity damaged;
    private boolean isCrit;

    public OnPostDamageEvent(double damage, Entity attacker, Entity damaged, boolean isCrit) {
        this.damage = damage;
        this.attacker = attacker;
        this.damaged = damaged;
        this.isCrit = isCrit;
    }

    public double getDamage() {
        return damage;
    }

    public Entity getAttacker() {
        return attacker;
    }

    public Entity getTarget() {
        return damaged;
    }

    public boolean isCrit() {
        return isCrit;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
