package be.sixefyle.listeners;

import be.sixefyle.event.PrepareAnvilCraftEvent;
import be.sixefyle.gui.AnvilGui;
import be.sixefyle.items.ItemRepairTable;
import be.sixefyle.items.UGItem;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.List;

public class AnvilListener implements Listener {

    @EventHandler
    public void onClickOnAnvil(PlayerInteractEvent e){
        Block block = e.getClickedBlock();
        if(block == null) return;

        if(block.getType().equals(Material.ANVIL)){
            e.setCancelled(true);
            e.getPlayer().openInventory(new AnvilGui().getInventory());
        }
    }

    @EventHandler
    public void onPrepareAnvilCraft(PrepareAnvilCraftEvent e){
        e.setOutputItem(new ItemStack(Material.AIR));
        ItemStack item = e.getInputItem();
        List<ItemStack> combinaisonsItem = e.getCombinaisonItems();

        UGItem ugItem = UGItem.getFromItemStack(item);
        if(ugItem != null){
            String itemType = item.getType().name().split("_")[0];
            try{
                ItemRepairTable itemRepairTable = ItemRepairTable.valueOf(itemType);
                for (ItemStack itemStack : combinaisonsItem) {
                    if(itemStack == null) continue;
                    if(itemStack.getType().equals(itemRepairTable.getComponent())){
                        ItemStack repairedItem = item.clone();
                        Damageable itemMeta = (Damageable) item.getItemMeta();
                        itemMeta.setDamage(itemMeta.getDamage() - itemRepairTable.getRepairPower());
                        repairedItem.setItemMeta(itemMeta);
                        e.setOutputItem(repairedItem);
                    }
                }
            }catch (IllegalArgumentException ignore) {}
        }
    }
}
