package be.sixefyle.items.passifs;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

public class DoubleDamagePassif extends ItemPassif implements OnHit, OnProjectilHit{

    public DoubleDamagePassif() {
        super("description of double power", "name");
    }

    @Override
    public void doDamage(EntityDamageByEntityEvent e) {
        e.setDamage(e.getFinalDamage() * 2);
        Bukkit.broadcast(Component.text(e.getDamage()));
    }

    @Override
    public void onHit(ProjectileHitEvent e) {

    }
}
