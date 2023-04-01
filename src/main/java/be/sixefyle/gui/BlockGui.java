package be.sixefyle.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public abstract class BlockGui implements InventoryHolder{

    private final Inventory inventory;

    public BlockGui(int size, String title) {
        this.inventory = Bukkit.createInventory(this, size, Component.text(title));
        createBackground();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public abstract void addContent();

    public abstract void onInventoryClick(InventoryClickEvent e);

    public abstract void onDragItem(InventoryDragEvent e);

    public void createBackground(){
        ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta paneMeta = pane.getItemMeta();
        paneMeta.displayName(Component.text(" "));
        pane.setItemMeta(paneMeta);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, pane);
        }
    }
}
