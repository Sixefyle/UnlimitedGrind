package be.sixefyle.items;

import org.bukkit.Material;

public enum ItemRepairTable {
    WOOD(Material.OAK_PLANKS, 10),
    STONE(Material.ANDESITE, 18),
    GOLDEN(Material.GOLD_ORE, 7),
    IRON(Material.IRON_BLOCK, 25),
    CHAINMAIL(Material.IRON_INGOT, 11),
    DIAMOND(Material.DEEPSLATE_DIAMOND_ORE, 45),
    NETHERITE(Material.NETHERITE_INGOT, 25),
    ;

    private final Material component;
    private final int repairPower;

    ItemRepairTable(Material component, int repairPower) {
        this.component = component;
        this.repairPower = repairPower;
    }

    public Material getComponent() {
        return component;
    }

    public int getRepairPower() {
        return repairPower;
    }
}
