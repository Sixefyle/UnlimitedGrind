package be.sixefyle.items.passifs.interfaces;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public interface OnMeleeHit {
    void doDamage(EntityDamageByEntityEvent e, Player player);
}
