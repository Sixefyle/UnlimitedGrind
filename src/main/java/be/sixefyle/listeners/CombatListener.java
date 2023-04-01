package be.sixefyle.listeners;

import be.sixefyle.UGPlayer;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.enums.Effects;
import be.sixefyle.enums.Symbols;
import be.sixefyle.event.OnPostDamageEvent;
import be.sixefyle.event.OnUgPlayerDieEvent;
import be.sixefyle.items.ItemManager;
import be.sixefyle.items.ItemRepairTable;
import be.sixefyle.items.UGItem;
import be.sixefyle.utils.NumberUtils;
import be.sixefyle.utils.HologramUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

public class CombatListener implements Listener {
    @EventHandler
    public void onPlayerAttack(OnPostDamageEvent e){
        if(e.getDamage() <= 0) return;

        Damageable damaged = (Damageable) e.getTarget();
        damaged.damage(e.getDamage());

        Location damageIndicatorLoc = e.getTarget().getLocation().clone();

        ChatColor color = ChatColor.WHITE;
        if(e.getAttacker() instanceof Player)
            color = e.isCrit() ? ChatColor.GOLD : ChatColor.WHITE;
        if(e.getTarget() instanceof Player)
            color = ChatColor.RED;

        HologramUtils.createDamageIndicator(damageIndicatorLoc, (e.isCrit() ? ChatColor.BOLD + Symbols.CRITICS.get() : "") +
                NumberUtils.format(e.getDamage()), color);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityAttack(EntityDamageByEntityEvent e){
        if(e.isCancelled()) return;

        if(e.getDamager() instanceof Damageable ent && ent.hasMetadata("power")) {
            double power = ent.getMetadata("power").get(0).asDouble();
            double newDamage = e.getFinalDamage() + e.getFinalDamage() * (power/80 + 1);

            e.setDamage(newDamage);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTakeDamageByEntity(EntityDamageByEntityEvent e){
        if(e.isCancelled()) return;

        if(e.getEntity() instanceof Player player){
            UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
            double damage = e.getDamage() * ugPlayer.getDamageReductionPercentage();
            ugPlayer.takeDamage(damage);
            e.setDamage(0);
        }
    }

    @EventHandler
    public void onPlayerTakeDamage(EntityDamageEvent e){
        if(e.isCancelled()) return;

        if(e.getEntity() instanceof Player player){
            UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
            EntityDamageEvent.DamageCause cause = e.getCause();

            if(cause.equals(EntityDamageEvent.DamageCause.FIRE_TICK) || cause.equals(EntityDamageEvent.DamageCause.FIRE)){
                ugPlayer.takeDamage((ugPlayer.getMaxHealth() * Effects.FIRE.getStrength()) * ugPlayer.getDamageReductionPercentage());
                e.setDamage(0);
            }

            if(!cause.equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)){
                ugPlayer.takeDamage(e.getFinalDamage());
                e.setDamage(0);
            }
            ugPlayer.updateActionBarStats();
        }
    }

    @EventHandler
    public void onEntityDie(EntityDeathEvent e){
        Damageable entity = e.getEntity();
        if(entity.hasMetadata("amount")){
            int amount = entity.getMetadata("amount").get(0).asInt();
            if(amount > 1) {
                e.setCancelled(true);
                for (ItemStack drop : e.getDrops()) {
                    entity.getWorld().dropItemNaturally(entity.getLocation(), drop);
                }
            }
            if(Math.random() <= .1){//TODO: add this to config
                UGItem rareItem = ItemManager.generateRandomItem(entity.getMetadata("power").get(0).asDouble());

                Item item = entity.getWorld().dropItemNaturally(entity.getLocation(), rareItem.asItemStack());
                rareItem.createRarityParticle(item);
            }
            entity.setMetadata("amount", new FixedMetadataValue(UnlimitedGrind.getInstance(), amount - 1));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDie(OnUgPlayerDieEvent e){
        if(e.isCancelled()) return;

        e.getUgPlayer().respawn();
    }

    @EventHandler
    public void reduceDurabilityWhenIt(EntityDamageByEntityEvent e){
        if(e.isCancelled()) return;

        if(e.getEntity() instanceof Player player){
            UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
            org.bukkit.inventory.meta.Damageable itemDamageable;
            for (ItemStack itemStack : ugPlayer.getArmorAndOffHand()) {
                itemDamageable = (org.bukkit.inventory.meta.Damageable) itemStack.getItemMeta();
                itemDamageable.setDamage(itemDamageable.getDamage() + 1);
            }
        } else if(e.getDamager() instanceof Player player) {
            ItemStack weap = player.getInventory().getItemInMainHand();
            org.bukkit.inventory.meta.Damageable itemDamageable;
            itemDamageable = (org.bukkit.inventory.meta.Damageable) weap.getItemMeta();
            itemDamageable.setDamage(itemDamageable.getDamage() + 1);
        }
    }

    @EventHandler
    public void onItemBreak(PlayerItemDamageEvent e){
        int itemDamage = ((org.bukkit.inventory.meta.Damageable)e.getItem().getItemMeta()).getDamage();
        if(itemDamage + e.getDamage() >= e.getItem().getType().getMaxDurability()){
            e.setCancelled(true);
        }
    }


    @EventHandler
    public void onRepairItem(PrepareAnvilEvent e){
        AnvilInventory anvilInventory = e.getInventory();
        if(anvilInventory.getSecondItem() != null) return;


        ItemStack item = anvilInventory.getFirstItem();
        UGItem ugItem = UGItem.getFromItemStack(item);
        if(ugItem == null) return;

        String itemType = item.getType().name().split("_")[0];

        try{
            ItemRepairTable itemRepairTable = ItemRepairTable.valueOf(itemType);

            anvilInventory.setSecondItem(new ItemStack(Material.DIAMOND, 1));
            anvilInventory.setResult(new ItemStack(Material.NETHERITE_INGOT, 10));

        }catch (IllegalArgumentException ignore) {}
    }
}
