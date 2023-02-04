package be.sixefyle.items.passifs;

import org.bukkit.event.entity.EntityDamageByEntityEvent;

public interface OnHit {
    void doDamage(EntityDamageByEntityEvent e);
}
