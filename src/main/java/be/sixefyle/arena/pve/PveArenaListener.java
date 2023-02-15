package be.sixefyle.arena.pve;

import be.sixefyle.UnlimitedGrind;
import be.sixefyle.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.persistence.PersistentDataType;

public class PveArenaListener implements Listener {

    @EventHandler
    public void onEntityDie(EntityDeathEvent e){
        Entity entity = e.getEntity();
        if(entity.hasMetadata("world")){
            ArenaManager arenaManager = ArenaManager.getArenaManagers().get(entity.getWorld());
            arenaManager.getWave().getCreatures().remove(entity);
            e.getDrops().clear();
        }
    }

    @EventHandler
    public void onPlayerDie(PlayerDeathEvent e){
        Player player = e.getPlayer();
        if(player.hasMetadata("arenaWorld")){
            e.setCancelled(true);
            ArenaManager arenaManager = ArenaManager.getArenaManagers().get(player.getWorld());
            arenaManager.getPlayers().remove(player);

            player.spigot().respawn();
//            if(player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation())) {
//                player.removeMetadata("arenaWorld", UnlimitedGrind.getInstance());
//            }

            if(arenaManager.getPlayers().size() <= 0){
                arenaManager.stopGame();
            }
        }
    }

    @EventHandler
    public void onCreatureTakeDamage(EntityDamageByEntityEvent e){
        Entity entity = e.getEntity();
        if(entity.hasMetadata("world") && e.getDamager() instanceof Player player && entity instanceof Monster creature){
            creature.setTarget(player);
        }
    }

    @EventHandler
    public void onCreatureChangeTarget(EntityTargetEvent e){
        if(e.getEntity().hasMetadata("world") && e.getTarget() instanceof Monster){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreatureTransform(EntityTransformEvent e){
        if(e.getEntity().hasMetadata("world")){
            e.setCancelled(true);
        }
    }
}
