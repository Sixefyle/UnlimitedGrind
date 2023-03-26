package be.sixefyle.event;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class OnSmithingCraftEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;

    ItemStack result;
    List<HumanEntity> viewers;
    InventoryAction action;

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public OnSmithingCraftEvent(ItemStack result, List<HumanEntity> viewers, InventoryAction action) {
        this.result = result;
        this.viewers = viewers;
        this.action = action;
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

    public ItemStack getResult() {
        return result;
    }

    public void setResult(ItemStack result) {
        this.result = result;
    }

    public List<HumanEntity> getViewers() {
        return viewers;
    }

    public InventoryAction getAction() {
        return action;
    }
}
