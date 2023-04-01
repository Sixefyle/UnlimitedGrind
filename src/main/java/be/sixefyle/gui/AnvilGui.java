package be.sixefyle.gui;

import be.sixefyle.UnlimitedGrind;
import be.sixefyle.event.PrepareAnvilCraftEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class AnvilGui extends BlockGui {

    private final int inputSlot = 19;
    private final int outputSlot = 25;

    private final List<Integer> combinaisonSlots = List.of(12, 21, 30);
    private final List<Integer> resultPreviewStateSlots = List.of(22, 23, 24);

    public AnvilGui() {
        super(45, "Anvil");
        addContent();
    }

    private void callEvent(Inventory inventory){
        Bukkit.getScheduler().runTaskLater(UnlimitedGrind.getInstance(), () -> {
            List<ItemStack> combinaisonsItems = combinaisonSlots.stream()
                    .map(inventory::getItem)
                    .collect(Collectors.toList());

            Bukkit.getPluginManager().callEvent(
                    new PrepareAnvilCraftEvent(inventory, this, inputSlot, outputSlot, combinaisonSlots,
                            inventory.getItem(inputSlot), inventory.getItem(outputSlot), combinaisonsItems));
        }, 1);
    }

    @Override
    public void onInventoryClick(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();
        int clickedSlot = e.getSlot();
        Inventory inventory = getInventory();

        if(clickedSlot == outputSlot) {
            e.setCancelled(true);
            return;
        }

        if(clickedItem != null){
            if(clickedItem.hasItemMeta()){
                ItemMeta itemMeta = clickedItem.getItemMeta();
                if(itemMeta.displayName().equals(Component.text(" "))){
                    e.setCancelled(true);
                }
            }
        }
        Inventory clickedIventory = e.getClickedInventory();
        if(clickedIventory != null && clickedIventory.equals(inventory)){
            callEvent(inventory);
        }
    }

    @Override
    public void onDragItem(InventoryDragEvent e) {
        Inventory inventory = getInventory();
        callEvent(inventory);
    }

    @Override
    public void addContent() {
        Inventory inventory = getInventory();

        ItemStack empty = new ItemStack(Material.AIR);
        for (Integer combinaisonSlot : combinaisonSlots) {
            inventory.setItem(combinaisonSlot, empty);
        }

        setResultPreviewState(false);

        inventory.setItem(inputSlot, empty);
        inventory.setItem(outputSlot, empty);
    }

    public void setResultPreviewState(boolean ok){
        ItemStack redPane = new ItemStack(ok ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE, 1);
        ItemMeta redPaneMeta = redPane.getItemMeta();
        redPaneMeta.displayName(Component.text(" "));
        redPane.setItemMeta(redPaneMeta);
        for (Integer resultPreviewStateSlot : resultPreviewStateSlots) {
            getInventory().setItem(resultPreviewStateSlot, redPane);
        }
    }

    public int getInputSlot() {
        return inputSlot;
    }

    public int getOutputSlot() {
        return outputSlot;
    }

    public List<Integer> getCombinaisonSlots() {
        return combinaisonSlots;
    }
}
