package be.sixefyle.event;

import be.sixefyle.gui.AnvilGui;
import org.bukkit.Material;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PrepareAnvilCraftEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;

    private final Inventory inventory;
    private final AnvilGui anvilGui;

    private final int inputSlot;
    private final int outputSlot;
    private final List<Integer> combinaisonSlots;

    private final ItemStack inputItem;
    private ItemStack outputItem;
    private final List<ItemStack> combinaisonItems;


    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public PrepareAnvilCraftEvent(Inventory inventory, AnvilGui anvilGui, int inputSlot, int outputSlot, List<Integer> combinaisonSlots, ItemStack inputItem, ItemStack outputItem, List<ItemStack> combinaisonItems) {
        this.inventory = inventory;
        this.anvilGui = anvilGui;
        this.inputSlot = inputSlot;
        this.outputSlot = outputSlot;
        this.combinaisonSlots = combinaisonSlots;
        this.inputItem = inputItem;
        this.outputItem = outputItem;
        this.combinaisonItems = combinaisonItems;
        this.isCancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }

    public Inventory getInventory() {
        return inventory;
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

    public ItemStack getInputItem() {
        return inputItem;
    }

    public List<ItemStack> getCombinaisonItems() {
        return combinaisonItems;
    }

    public ItemStack getOutputItem() {
        return outputItem;
    }

    public AnvilGui getAnvilGui() {
        return anvilGui;
    }

    public void setOutputItem(ItemStack outputItem) {
        inventory.setItem(outputSlot, outputItem);
        this.outputItem = outputItem;

        anvilGui.setResultPreviewState(outputItem != null && (!outputItem.equals(anvilGui.getEmptyOutputItem()) && !outputItem.getType().equals(Material.AIR)));
    }
}
