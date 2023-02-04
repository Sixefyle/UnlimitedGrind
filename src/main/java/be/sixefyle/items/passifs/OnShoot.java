package be.sixefyle.items.passifs;

import org.bukkit.event.entity.EntityShootBowEvent;

public interface OnShoot {
    void doShoot(EntityShootBowEvent e);
}
