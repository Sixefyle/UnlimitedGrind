package be.sixefyle.gui;

import be.sixefyle.UGSpawner;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.utils.PlaceholderUtils;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import com.iridium.iridiumcore.dependencies.xseries.XMaterial;
import com.iridium.iridiumcore.utils.InventoryUtils;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.configs.inventories.NoItemGUI;
import com.iridium.iridiumskyblock.gui.GUI;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;


public class SpawnerGui extends UGGui {

    private CreatureSpawner spawner;
    private UGSpawner betterSpawner;

    public SpawnerGui(CreatureSpawner spawner) {
        super(27, "Spawner");

        this.spawner = spawner;
        this.betterSpawner = UGSpawner.getBetterSpawner(spawner.getLocation());
    }

    @Override
    public void onInventoryClick(InventoryClickEvent e) {
        if(e.getSlot() < getInventory().getSize()) {
            if(!spawner.getSpawnedType().isSpawnable()) {
                e.setCancelled(true);
                return;
            }

            Player player = (Player) e.getViewers().get(0);
            FileConfiguration config = UnlimitedGrind.getInstance().getConfig();
            Material itemType = e.getCurrentItem().getType();
            String beginPath = "lang.spawner.gui.";
            String errorMessage = null;

            Material[] icons = {
                    Material.getMaterial(config.getString(beginPath + "powerUpgrade.material")),
                    Material.getMaterial(spawner.getSpawnedType().name().toUpperCase() + "_SPAWN_EGG"),
                    Material.getMaterial(config.getString(beginPath + "spawnerSilence.material")),
                    Material.getMaterial(config.getString(beginPath + "spawnerSpeed.material")),
                    Material.getMaterial(config.getString(beginPath + "stackUpgrade.material")),
                    Material.getMaterial(config.getString(beginPath + "rareLootUpgrade.material")),
                    Material.getMaterial(config.getString(beginPath + "pickup.material"))
            };

            if(itemType.equals(icons[0])) { // Power upgrade
                if(e.isShiftClick()){
                    betterSpawner.addPower(1000);
                } else if(e.isRightClick()) {
                    betterSpawner.addPower(100);
                } else if(e.isLeftClick()) {
                    betterSpawner.addPower(10);
                }
            } else if(itemType.equals(icons[1])) { // Spawner stacks
                Material eggType = Material.getMaterial(spawner.getSpawnedType().name().toUpperCase() + "_SPAWN_EGG");
                XMaterial eggXType = XMaterial.matchXMaterial(eggType);
                Inventory playerInventory = e.getViewers().get(0).getInventory();
                int amount = InventoryUtils.getAmount(playerInventory, eggXType);

                if(amount > 0){
                    if(betterSpawner.addStackAmount()) {
                        InventoryUtils.removeAmount(playerInventory, eggXType, 1);
                    } else {
                        errorMessage = config.getString("lang.spawner.error.maxStackReached");
                    }
                } else {
                    errorMessage = config.getString("lang.spawner.error.noEggs");
                }
            } else if(itemType.equals(icons[2])) { // Silence mode
                betterSpawner.invertSilenceMode();
            } else if(itemType.equals(icons[3])) { // Speed upgrade
                if(e.isLeftClick()) {
                    if(spawner.getMaxSpawnDelay() > 40){
                        spawner.setMaxSpawnDelay(spawner.getMaxSpawnDelay() - 5);
                        spawner.update();
                    } else {
                        errorMessage = config.getString("lang.spawner.error.minSpeedReached");
                    }
                }else if(e.isRightClick()) {
                    if(spawner.getMinSpawnDelay() > 40) {
                        spawner.setMinSpawnDelay(spawner.getMinSpawnDelay() - 5);
                        spawner.update();
                    } else {
                        errorMessage = config.getString("lang.spawner.error.minSpeedReached");
                    }
                }
            } else if(itemType.equals(icons[4])) { // stack upgrade
                if(betterSpawner.getStackUpgradeLevel() < betterSpawner.getMaxStackUpgradeLevel()){
                    betterSpawner.addStackUpgradeLevel();
                } else {
                    errorMessage = config.getString("lang.spawner.error.maxStackUpgradeReached");
                }
            } else if(itemType.equals(icons[5])) { // rare loot upgrade
                if(!betterSpawner.addRareDropChance()) {
                    errorMessage = config.getString("lang.spawner.error.maxRareDropChance");
                }
            } else if(itemType.equals(icons[6])) { // pickup
                betterSpawner.pickup(player);
            }

            if(errorMessage != null && !errorMessage.isEmpty()){
                player.sendMessage(PlaceholderUtils.replace(betterSpawner, errorMessage));
            }
        }
    }

    @Override
    public void addContent(Inventory inventory) {
        inventory.clear();
        InventoryUtils.fillInventory(inventory, IridiumSkyblock.getInstance().getInventories().upgradesGUI.background);
        FileConfiguration config = UnlimitedGrind.getInstance().getConfig();

        Set<String> keys = config.getConfigurationSection("lang.spawner.gui").getKeys(false);
        String path;
        List<String> configLore;
        ItemStack item;
        ItemMeta itemM;
        String result;
        String itemName;
        for (String key : keys){
            path = "lang.spawner.gui." + key;
            configLore = config.getStringList(path + ".lore");
            Material material = Material.getMaterial(config.getString(path + ".material"));

            if(material == null) continue;

            if(material.equals(Material.EGG)){
                material = Material.getMaterial(spawner.getSpawnedType().name().toUpperCase() + "_SPAWN_EGG");
            }

            item = new ItemStack(material);
            itemM = item.getItemMeta();

            itemM.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemM.addItemFlags(ItemFlag.HIDE_DYE);
            itemM.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemM.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);

            itemName = config.getString(path + ".name");
            itemM.displayName(Component.text(IridiumColorAPI.process(itemName)));

            List<Component> lore = new ArrayList<>();
            for (String line : configLore) {
                result = PlaceholderUtils.replace(betterSpawner, line);
                lore.add(Component.text(result)); //TODO: hex color support
            }

            itemM.lore(lore);
            item.setItemMeta(itemM);
            inventory.setItem(config.getInt(path + ".pos"), item);
        }
    }
}
