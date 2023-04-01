package be.sixefyle.arena.pve;

import be.sixefyle.UGPlayer;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.event.OnUgPlayerDieEvent;
import be.sixefyle.items.ItemManager;
import be.sixefyle.items.UGItem;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

            arenaManager.updateBossBar();

            FileConfiguration config = UnlimitedGrind.getInstance().getConfig();
            double rareDropChance = config.getDouble("pve.arena.rareDropChance") +
                    (config.getDouble("pve.arena.perWaveRareDropChanceIncrease") * entity.getMetadata("wave").get(0).asInt());
            rareDropChance = Math.max(rareDropChance, .25);

            if(Math.random() <= rareDropChance){
                UGItem rareItem = ItemManager.generateRandomItem(arenaManager.getArenaPower());

                Item item = entity.getWorld().dropItemNaturally(entity.getLocation(), rareItem.asItemStack());
                rareItem.createRarityParticle(item);
            }
        }
    }

    @EventHandler
    public void onPlayerDie(OnUgPlayerDieEvent e){
        UGPlayer ugPlayer = e.getUgPlayer();
        Player player = ugPlayer.getPlayer();
        if(ugPlayer.isInArena()){
            e.setCancelled(true);
            ArenaManager arenaManager = ArenaManager.getArenaManagers().get(player.getWorld());
            arenaManager.reducePlayerAlive();
            player.setGameMode(GameMode.SPECTATOR);

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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent e){
        Player player = e.getPlayer();
        UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
        if(ugPlayer == null) return;

        if(ugPlayer.isInArena()) {
            ArenaManager arenaManager = ArenaManager.getArenaManagers().get(player.getWorld());
            if(arenaManager != null){
                arenaManager.reducePlayerAlive();

                if(arenaManager.getPlayerAlive() <= 0){
                    arenaManager.stopGame();
                }
            }
        }
    }
}
