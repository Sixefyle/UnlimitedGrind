package be.sixefyle.entity.boss;

import org.bukkit.event.entity.EntityDamageByEntityEvent;

public interface BossPhase {
    void onBossTakeDamage(EntityDamageByEntityEvent e);
}
