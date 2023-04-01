package be.sixefyle.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GuiManager implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent e){
        Inventory inventory = e.getInventory();

        if(inventory.getHolder() != null && inventory.getHolder() instanceof BlockGui blockGui){
            blockGui.onInventoryClick(e);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDrag(InventoryDragEvent e){
        Inventory inventory = e.getInventory();

        if(inventory.getHolder() != null && inventory.getHolder() instanceof BlockGui blockGui){
            blockGui.onDragItem(e);
        }
    }

    @EventHandler
    public void onCloseInventory(InventoryCloseEvent e){
        Inventory inventory = e.getInventory();
        Player player = (Player) e.getPlayer();
        if(inventory.getHolder() != null && inventory.getHolder() instanceof BlockGui blockGui){
            ItemMeta itemMeta;
            for (ItemStack itemStack : inventory) {
                if(itemStack == null) continue;
                itemMeta = itemStack.getItemMeta();

                if(itemMeta != null
                        && itemMeta.displayName() != null
                        && itemMeta.displayName().equals(Component.text(" "))) continue;

                player.getInventory().addItem(itemStack);
            }
        }
    }
}
