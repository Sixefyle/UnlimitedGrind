package be.sixefyle.gui;

import be.sixefyle.UnlimitedGrind;
import be.sixefyle.enums.ComponentColor;
import be.sixefyle.event.AnvilCraftEvent;
import be.sixefyle.event.PrepareAnvilCraftEvent;
import be.sixefyle.utils.ComponentUtils;
import be.sixefyle.utils.ItemStackUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnvilGui extends BlockGui {

    private final List<Integer> combinaisonSlots = List.of(12, 21, 30);
    private final List<Integer> resultPreviewStateSlots = List.of(24, 26, 16, 34);

    private final int repairPreviewSlot;
    private final ItemStack repairPreviewItem;
    private final ItemStack emptyOutputItem;

    private final Map<Integer, Integer> usedItemsSlots = new HashMap<>();

    public AnvilGui() {
        super(45, "Anvil");
        setInputSlot(19);
        setOutputSlot(25);

        repairPreviewSlot = getInputSlot() + 9;
        repairPreviewItem = ItemStackUtils.createItem(Material.GREEN_STAINED_GLASS_PANE,
                Component.text(" "),
                List.of(
                        ComponentUtils.createComponent("Place an item to see which item can repair it!", ComponentColor.NEUTRAL)
                ));

        emptyOutputItem = ItemStackUtils.createItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, Component.text(" "), null);

        addContent();
    }

    private void callPrepareEvent(Inventory inventory){
        Bukkit.getScheduler().runTaskLater(UnlimitedGrind.getInstance(), () -> {
            getUsedItemsSlots().clear();
            List<ItemStack> combinaisonsItems = combinaisonSlots.stream()
                    .map(inventory::getItem)
                    .collect(Collectors.toList());

            Bukkit.getPluginManager().callEvent(
                    new PrepareAnvilCraftEvent(inventory, this, getInputSlot(), getOutputSlot(), combinaisonSlots,
                            inventory.getItem(getInputSlot()), inventory.getItem(getOutputSlot()), combinaisonsItems));
        }, 1);
    }

    public void callCraftEvent(Inventory inventory, Player player){
        Bukkit.getScheduler().runTaskLater(UnlimitedGrind.getInstance(), () -> {
            List<ItemStack> combinaisonsItems = combinaisonSlots.stream()
                    .map(inventory::getItem)
                    .collect(Collectors.toList());

            Bukkit.getPluginManager().callEvent(
                    new AnvilCraftEvent(inventory, this, player, getInputSlot(), getOutputSlot(), combinaisonSlots,
                            inventory.getItem(getInputSlot()), inventory.getItem(getOutputSlot()), combinaisonsItems));
        }, 1);
    }

    @Override
    public void onInventoryClick(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();
        int clickedSlot = e.getRawSlot();
        Inventory inventory = getInventory();

        if(clickedItem != null && !clickedItem.equals(emptyOutputItem) && clickedSlot == getOutputSlot()) {
            e.setCancelled(true);
            callCraftEvent(inventory, (Player) e.getWhoClicked());
            callPrepareEvent(inventory);
            return;
        }

        if(clickedItem != null){
            if(clickedItem.hasItemMeta()){
                ItemMeta itemMeta = clickedItem.getItemMeta();
                if(itemMeta.displayName() != null && itemMeta.displayName().equals(Component.text(" "))){
                    e.setCancelled(true);
                }
            }
        }
        Inventory clickedInventory = e.getClickedInventory();
        if(clickedInventory != null && clickedInventory.equals(inventory)){
            callPrepareEvent(inventory);
        }
    }

    @Override
    public void onDragItem(InventoryDragEvent e) {
        Inventory inventory = getInventory();
        callPrepareEvent(inventory);
    }

    @Override
    public void addContent() {
        Inventory inventory = getInventory();

        ItemStack empty = new ItemStack(Material.AIR);
        for (Integer combinaisonSlot : combinaisonSlots) {
            inventory.setItem(combinaisonSlot, empty);
        }

        setResultPreviewState(false);

        inventory.setItem(getInputSlot(), empty);
        inventory.setItem(getOutputSlot(), emptyOutputItem);
        inventory.setItem(getRepairPreviewSlot(), getRepairPreviewItem());
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

    public List<Integer> getCombinaisonSlots() {
        return combinaisonSlots;
    }

    public Map<Integer, Integer> getUsedItemsSlots() {
        return usedItemsSlots;
    }

    public int getRepairPreviewSlot() {
        return repairPreviewSlot;
    }

    public ItemStack getRepairPreviewItem() {
        return repairPreviewItem;
    }

    public ItemStack getEmptyOutputItem() {
        return emptyOutputItem;
    }
}
