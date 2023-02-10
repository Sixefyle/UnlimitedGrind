package be.sixefyle.items.passifs.interfaces;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;

public interface OnProjectilHit {
    void onHit(ProjectileHitEvent e, Player player);
}
