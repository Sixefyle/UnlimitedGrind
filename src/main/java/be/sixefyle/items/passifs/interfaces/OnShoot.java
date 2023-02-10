package be.sixefyle.items.passifs.interfaces;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;

public interface OnShoot {
    void doShoot(EntityShootBowEvent e, Player player);
}
