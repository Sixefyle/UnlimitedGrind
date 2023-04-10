package be.sixefyle.gui;

import be.sixefyle.UGPlayer;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.arena.ArenaMap;
import be.sixefyle.arena.pve.ArenaManager;
import be.sixefyle.enums.ComponentColor;
import be.sixefyle.utils.PlaceholderUtils;
import com.iridium.iridiumcore.dependencies.iridiumcolorapi.IridiumColorAPI;
import com.iridium.iridiumcore.utils.InventoryUtils;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.IslandBank;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ArenaGui extends UGGui {

    private UGPlayer ugPlayer;
    private double currentPower;
    private int startingWave = 1;
    private ArenaMap arenaMap;

    public ArenaGui(UGPlayer ugPlayer) {
        super(18, "Arena");

        this.ugPlayer = ugPlayer;
        currentPower = ugPlayer.getMaxPower() - ugPlayer.getMaxPower() % 100;
        arenaMap = ArenaMap.ICE;
    }

    @Override
    public void onInventoryClick(InventoryClickEvent e) {
        FileConfiguration config = UnlimitedGrind.getInstance().getConfig();
        Material itemType = e.getCurrentItem().getType();
        String beginPath = "lang.arena.gui.";
        String errorMessage = null;
        Player player = (Player) e.getWhoClicked();

        Material[] icons = {
                Material.getMaterial(config.getString(beginPath + "powerUpgrade.material")),
                Material.getMaterial(config.getString(beginPath + "startButton.material")),
                Material.getMaterial(config.getString(beginPath + "mapChange.material")),
                Material.getMaterial(config.getString(beginPath + "startingWave.material")),
        };

        boolean isShiftClick = e.getClick().isShiftClick();
        if(itemType.equals(icons[0])) { // Power upgrade
            if(isShiftClick){
                if (e.isLeftClick()){
                    addPower(-50);
                } else if(e.isRightClick()){
                    addPower(-500);
                }
            } else {
                if (e.isLeftClick()){
                    addPower(50);
                } else if(e.isRightClick()){
                    addPower(500);
                }
            }
        } else if(itemType.equals(icons[1])) { // start button
            if(ugPlayer.getUgIsland().getIsland().isPresent()){
                Island island = ugPlayer.getUgIsland().getIsland().get();
                double crystalCost = ArenaManager.getCrystalReward(currentPower, startingWave == 1 ? 1 : startingWave + 10);
                if(island.getCrystals() >= crystalCost){
                    IslandBank islandBank = IridiumSkyblock.getInstance().getIslandManager().getIslandBank(island, IridiumSkyblock.getInstance().getBankItems().crystalsBankItem);
                    islandBank.setNumber(islandBank.getNumber() - crystalCost);
                    ugPlayer.joinArena(arenaMap, currentPower, startingWave);
                } else {
                    player.sendMessage(Component.text("You don't have enough crystals to start this arena!")
                            .color(ComponentColor.ERROR.getColor()));
                }
            } else {
                player.sendMessage(Component.text("You need to have an island to start an arena!")
                        .color(ComponentColor.ERROR.getColor()));
            }

        } else if(itemType.equals(icons[3])) { // starting wave
            if(isShiftClick) {
                startingWave = Math.max(startingWave - 5, 1);
            } else {
                startingWave += 5;
            }
        }
    }

    @Override
    public void addContent(Inventory inventory) {
        inventory.clear();
        InventoryUtils.fillInventory(inventory, IridiumSkyblock.getInstance().getInventories().upgradesGUI.background);
        FileConfiguration config = UnlimitedGrind.getInstance().getConfig();

        Set<String> keys = config.getConfigurationSection("lang.arena.gui").getKeys(false);
        String path;
        List<String> configLore;
        ItemStack item;
        ItemMeta itemM;
        String result;
        String itemName;
        for (String key : keys){
            path = "lang.arena.gui." + key;
            configLore = config.getStringList(path + ".lore");
            Material material = Material.getMaterial(config.getString(path + ".material"));

            if(material == null) continue;

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
                result = PlaceholderUtils.replace(this, line);
                lore.add(Component.text(result)); //TODO: hex color support
            }

            itemM.lore(lore);
            item.setItemMeta(itemM);
            inventory.setItem(config.getInt(path + ".pos"), item);
        }
    }

    public void addPower(double power){
        this.currentPower = Math.max(0, this.currentPower + power);
    }

    public UGPlayer getUgPlayer() {
        return ugPlayer;
    }

    public double getCurrentPower() {
        return currentPower;
    }

    public int getStartingWave() {
        return startingWave;
    }
}
