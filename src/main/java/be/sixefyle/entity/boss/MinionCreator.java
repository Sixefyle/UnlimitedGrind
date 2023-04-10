package be.sixefyle.entity.boss;

import be.sixefyle.entity.UGEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public interface MinionCreator {
    void onMinionTakeDamage(EntityDamageByEntityEvent e, UGEntity parent);
}
