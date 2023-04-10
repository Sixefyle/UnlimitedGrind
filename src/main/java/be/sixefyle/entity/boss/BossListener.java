package be.sixefyle.entity.boss;

import be.sixefyle.arena.pve.ArenaManager;
import be.sixefyle.entity.UGEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;


public class BossListener implements Listener {

    @EventHandler
    public void onBossTakeDamage(EntityDamageByEntityEvent e){
        Entity entity = e.getEntity();
        if(entity.hasMetadata("ugBoss")){
            if(entity.hasMetadata("canTakeDirectDamage")
                    && !entity.getMetadata("canTakeDirectDamage").get(0).asBoolean()){
                e.setCancelled(true);
            }
            UGBoss boss = (UGBoss) entity.getMetadata("ugBoss").get(0).value();
            if(boss == null) return;

            if(boss instanceof BossPhase bossPhase){
                bossPhase.onBossTakeDamage(e);
            }
            ArenaManager.getArenaManagers().get(boss.getWorld()).updateBossBar();
        }
    }

    @EventHandler
    public void onMinionTakeDamage(EntityDamageByEntityEvent e){
        Entity entity = e.getEntity();
        if(entity.hasMetadata("ugEntity") && entity.hasMetadata("parent")){
            UGEntity parent = (UGEntity) entity.getMetadata("parent").get(0).value();
            if(parent instanceof MinionCreator minionCreator){
                minionCreator.onMinionTakeDamage(e, parent);
            }
        }
    }
}
