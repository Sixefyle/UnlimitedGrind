package be.sixefyle.arena.pve;

import be.sixefyle.UGPlayer;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.entity.boss.UGBoss;
import be.sixefyle.enums.ComponentColor;
import be.sixefyle.event.UgPlayerDieEvent;
import be.sixefyle.items.ItemManager;
import be.sixefyle.items.UGItem;
import com.jeff_media.armorequipevent.ArmorEquipEvent;
import net.kyori.adventure.text.Component;
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
        if(entity.hasMetadata("arenaWorld")){

            if(entity.hasMetadata("parent")){
                e.getDrops().clear();
                e.setDroppedExp(0);
                return;
            }

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

            if(entity.hasMetadata("ugBoss")){
                UGBoss ugBoss = (UGBoss) entity.getMetadata("ugBoss").get(0).value();
                ugBoss.onDie();
            }
        }
    }

    @EventHandler
    public void onPlayerDie(UgPlayerDieEvent e){
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
        if(entity.hasMetadata("arenaWorld")){
            if(e.getDamager() instanceof Mob){
                e.setCancelled(true);
            } else if(e.getDamager() instanceof Player player && entity instanceof Mob creature){
                creature.setTarget(player);
            }
        }
    }

    @EventHandler
    public void onCreatureChangeTarget(EntityTargetEvent e){
        if(e.getEntity().hasMetadata("arenaWorld") && e.getTarget() instanceof Mob){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreatureTransform(EntityTransformEvent e){
        if(e.getEntity().hasMetadata("arenaWorld")){
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

    @EventHandler
    public void onEquipArmorInArena(ArmorEquipEvent e){
        Player player = e.getPlayer();
        UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
        if(ugPlayer.isInArena()){
            e.setCancelled(true);
            player.sendMessage(Component.text("You can't change your armor in arena!").color(ComponentColor.ERROR.getColor()));
        }
    }

    @EventHandler
    public void onMobAggroChange(EntityTargetLivingEntityEvent e){
        Entity entity = e.getEntity();
        Entity target = e.getTarget();
        if(entity.hasMetadata("arenaWorld") && e.getTarget() == null && target instanceof Player player){
            if(player.getGameMode().equals(GameMode.ADVENTURE) || player.getGameMode().equals(GameMode.SURVIVAL)){
                ArenaManager arenaManager = ArenaManager.getArenaManagers().get(entity.getWorld());
                e.setTarget(arenaManager.getWave().getNearestPlayer(entity.getLocation()).getPlayer());
            }
        } else {
            e.setCancelled(true);
        }
    }
}
