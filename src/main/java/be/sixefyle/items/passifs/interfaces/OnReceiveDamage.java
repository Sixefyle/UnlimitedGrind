package be.sixefyle.items.passifs.interfaces;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public interface OnReceiveDamage {
    void onGetDamage(EntityDamageEvent e);
}
