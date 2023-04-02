package be.sixefyle.listeners;

import be.sixefyle.enums.ComponentColor;
import be.sixefyle.event.AnvilCraftEvent;
import be.sixefyle.event.PrepareAnvilCraftEvent;
import be.sixefyle.gui.AnvilGui;
import be.sixefyle.items.ItemRepairTable;
import be.sixefyle.items.UGItem;
import be.sixefyle.utils.ComponentUtils;
import be.sixefyle.utils.ItemStackUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPrepareAnvilCraft(PrepareAnvilCraftEvent e){
        boolean isCombinaisonEmpty = e.getCombinaisonItems().stream().allMatch(Objects::isNull);

        if(e.getInputItem() == null || isCombinaisonEmpty) {
            e.setOutputItem(e.getAnvilGui().getEmptyOutputItem());
        }
    }

    @EventHandler
    public void showRequiredRepairComponent(PrepareAnvilCraftEvent e){
        ItemStack item = e.getInputItem();
        AnvilGui anvilGui = e.getAnvilGui();
        int slot = anvilGui.getRepairPreviewSlot();

        if(item == null) {
            e.getInventory().setItem(slot, anvilGui.getRepairPreviewItem());
            return;
        }

        try {
            if(item.getType().name().split("_").length <= 1) return;

            String itemType = item.getType().name().split("_")[0];
            ItemRepairTable itemRepairTable = ItemRepairTable.valueOf(itemType);

            ItemStack requiredItem = ItemStackUtils.createItem(itemRepairTable.getComponent(),
                    Component.text(" "),
                    List.of(
                            ComponentUtils.createComponent("Needed item to repair: " + itemRepairTable.getComponent(), ComponentColor.NEUTRAL),
                            ComponentUtils.createComponent("Repair power: ", ComponentColor.NEUTRAL)
                                    .append(ComponentUtils.createComponent(String.valueOf(itemRepairTable.getRepairPower()), ComponentColor.FINE))
                    ));
            e.getInventory().setItem(slot, requiredItem);
        } catch (IllegalArgumentException ignore) {}
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPrepareRepairItem(PrepareAnvilCraftEvent e){
        if(e.isCancelled()) return;

        AnvilGui anvilGui = e.getAnvilGui();
        ItemStack item = e.getInputItem();
        List<ItemStack> combinaisonsItem = e.getCombinaisonItems();

        UGItem ugItem = UGItem.getFromItemStack(item);
        if(ugItem != null){
            String itemType = item.getType().name().split("_")[0];
            try{
                ItemRepairTable itemRepairTable = ItemRepairTable.valueOf(itemType);
                ItemStack repairedItem = item.clone();
                Damageable itemMeta = (Damageable) repairedItem.getItemMeta();
                ItemStack itemStack;

                int toRepair = itemMeta.getDamage();
                int itemAmount = 0;
                int repairPower = 0;
                for (int i = 0; i < combinaisonsItem.size(); i++) {
                    itemStack = combinaisonsItem.get(i);
                    if(itemStack == null) continue;

                    if(itemStack.getType().equals(itemRepairTable.getComponent())){
                        itemAmount += Math.min(Math.min(toRepair / itemRepairTable.getRepairPower(), 64), itemStack.getAmount());
                        repairPower += itemRepairTable.getRepairPower() * itemAmount;
                        toRepair -= repairPower;
                        anvilGui.getUsedItemsSlots().put(anvilGui.getCombinaisonSlots().get(i), itemAmount);
                    }
                }

                if(repairPower > 0){
                    itemMeta.setDamage(itemMeta.getDamage() - repairPower);
                    repairedItem.setItemMeta(itemMeta);
                    e.setOutputItem(repairedItem);
                }
            }catch (IllegalArgumentException ignore) {}
        }
    }

    @EventHandler
    public void onPrepareEnchantItem(PrepareAnvilCraftEvent e){
        if(e.isCancelled()) return;

        AnvilGui anvilGui = e.getAnvilGui();
        ItemStack item = !e.getOutputItem().equals(anvilGui.getEmptyOutputItem()) ? e.getOutputItem() : e.getInputItem();
        if(item == null) return;
        ItemStack outputItem = item.clone();

        boolean isEnchantedBook = item.getType().equals(Material.ENCHANTED_BOOK);

        EnchantmentStorageMeta outputEnchants =
                isEnchantedBook ? (EnchantmentStorageMeta) outputItem.getItemMeta() : null;

        List<ItemStack> combinaisonsItems = e.getCombinaisonItems();

        EnchantmentStorageMeta enchantmentMeta;
        ItemStack combinaisonItem;
        for (int i = 0; i < combinaisonsItems.size(); i++) {
            combinaisonItem = combinaisonsItems.get(i);
            if(combinaisonItem == null) continue;
            if(!combinaisonItem.getType().equals(Material.ENCHANTED_BOOK)) continue;

            enchantmentMeta = (EnchantmentStorageMeta) combinaisonItem.getItemMeta();
            if(enchantmentMeta.hasStoredEnchants()){
                int finalI = i;
                enchantmentMeta.getStoredEnchants().forEach((enchantment, level) -> {
                    if(enchantment.canEnchantItem(outputItem)){
                        if(outputItem.getEnchantmentLevel(enchantment) == level && level + 1 < enchantment.getMaxLevel()){
                            outputItem.addEnchantment(enchantment, level + 1);
                            anvilGui.getUsedItemsSlots().put(e.getCombinaisonSlots().get(finalI), 1);
                        } else if(!outputItem.containsEnchantment(enchantment)) {
                            outputItem.addEnchantment(enchantment, level);
                            anvilGui.getUsedItemsSlots().put(e.getCombinaisonSlots().get(finalI), 1);
                        }
                    } else if(isEnchantedBook){
                        if(outputEnchants.getStoredEnchantLevel(enchantment) == level && level + 1 < enchantment.getMaxLevel()){
                            outputEnchants.addStoredEnchant(enchantment, level + 1, false);
                            anvilGui.getUsedItemsSlots().put(e.getCombinaisonSlots().get(finalI), 1);
                        } else if(!outputEnchants.hasStoredEnchant(enchantment)) {
                            outputEnchants.addStoredEnchant(enchantment, level, false);
                            anvilGui.getUsedItemsSlots().put(e.getCombinaisonSlots().get(finalI), 1);
                        }
                    }
                });
            }
        }

        if(isEnchantedBook && outputEnchants != null){
            outputItem.setItemMeta(outputEnchants);
        }

        if(!item.equals(outputItem)){
            UGItem ugItem = UGItem.getFromItemStack(outputItem);
            if(ugItem != null)
                ugItem.updateLore();

            e.setOutputItem(outputItem);
        }
    }

    @EventHandler
    public void onAnvilCraft(AnvilCraftEvent e){
        Location loc = e.getPlayer().getLocation();

        AnvilGui anvilGui = e.getAnvilGui();

        anvilGui.getUsedItemsSlots().put(anvilGui.getInputSlot(), 1);

        anvilGui.getUsedItemsSlots().forEach((slot, amount) -> {
            Inventory inventory = e.getInventory();
            ItemStack item = inventory.getItem(slot);
            if(item == null) return;

            item.setAmount(item.getAmount() - amount);
            inventory.setItem(slot, item);
        });

        loc.getWorld().playSound(loc, Sound.BLOCK_ANVIL_USE, 1, 1);
        e.getPlayer().getInventory().addItem(e.getOutputItem());
    }
}
