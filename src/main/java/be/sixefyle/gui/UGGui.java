package be.sixefyle.gui;

import com.iridium.iridiumskyblock.configs.inventories.NoItemGUI;
import com.iridium.iridiumskyblock.gui.GUI;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


public class UGGui extends GUI {
    public UGGui(int size, String name){
        super(new NoItemGUI(size, name, null));
    }

    @Override
    public void onInventoryClick(InventoryClickEvent inventoryClickEvent) { }

    @Override
    public void addContent(Inventory inventory) { }


}
