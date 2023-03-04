package be.sixefyle.arena.pve;

import be.sixefyle.UGPlayer;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.items.ItemManager;
import be.sixefyle.items.UGItem;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerQuitEvent;

public class PveArenaListener implements Listener {

    @EventHandler
    public void onEntityDie(EntityDeathEvent e){
        Entity entity = e.getEntity();
        if(entity.hasMetadata("world") && entity.hasMetadata("power") && entity.hasMetadata("wave")){
            ArenaManager arenaManager = ArenaManager.getArenaManagers().get(entity.getWorld());
            arenaManager.getWave().getAliveCreatures().remove(entity);
            e.getDrops().clear();

            FileConfiguration config = UnlimitedGrind.getInstance().getConfig();
            double rareDropChance = config.getDouble("pve.arena.rareDropChance") +
                    (config.getDouble("pve.arena.perWaveRareDropChanceIncrease") * entity.getMetadata("wave").get(0).asInt());

            if(Math.random() <= rareDropChance){
                UGItem rareItem = ItemManager.generateRandomItem(entity.getMetadata("power").get(0).asDouble());

                Item item = entity.getWorld().dropItemNaturally(entity.getLocation(), rareItem.getItem());
                rareItem.createRarityParticle(item);
            }
        }
    }

    @EventHandler
    public void onPlayerDie(PlayerDeathEvent e){
        Player player = e.getPlayer();
        if(player.hasMetadata("arenaWorld")){
            e.setCancelled(true);
            //UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
            ArenaManager arenaManager = ArenaManager.getArenaManagers().get(player.getWorld());
            //arenaManager.getUgPlayers().remove(ugPlayer);
            arenaManager.reducePlayerAlive();
            player.setGameMode(GameMode.SPECTATOR);
            //player.spigot().respawn();
            //ugPlayer.leavePveArena();

            if(arenaManager.getPlayerAlive() <= 0){
                arenaManager.stopGame();
            }
        }
    }

    @EventHandler
    public void onCreatureTakeDamage(EntityDamageByEntityEvent e){
        Entity entity = e.getEntity();
        if(entity.hasMetadata("world")){
            if(e.getDamager() instanceof Mob){
                e.setCancelled(true);
            } else if(e.getDamager() instanceof Player player && entity instanceof Mob creature){
                creature.setTarget(player);
            }
        }
    }

    @EventHandler
    public void onCreatureChangeTarget(EntityTargetEvent e){
        if(e.getEntity().hasMetadata("world") && e.getTarget() instanceof Mob){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreatureTransform(EntityTransformEvent e){
        if(e.getEntity().hasMetadata("world")){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        Player player = e.getPlayer();
        if(player.hasMetadata("arenaWorld")) {
            ArenaManager arenaManager = ArenaManager.getArenaManagers().get(player.getWorld());
            if(arenaManager != null){
                arenaManager.reducePlayerAlive();
                arenaManager.getParticipants().remove(UGPlayer.GetUGPlayer(player));

                if(arenaManager.getPlayerAlive() <= 0){
                    arenaManager.stopGame();
                }
            }
        }
    }
}
