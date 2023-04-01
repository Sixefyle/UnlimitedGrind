package be.sixefyle.items.passifs;

import be.sixefyle.utils.PlaceholderUtils;

import java.util.List;

public abstract class ItemPassif {

    private List<String> description;
    private String name;
    private String itemPrefixName;
    private List<String> lore;

    private boolean isStrenghtPercentage;
    private double strength;
    private double mythicBonus;

    public ItemPassif(String name, List<String> description, double strength, boolean isStrenghtPercentage, double mythicBonus) {
        this.description = description;
        this.name = name;
        this.strength = strength;
        this.mythicBonus = mythicBonus;
        this.isStrenghtPercentage = isStrenghtPercentage;
    }

    public ItemPassif(String name, String itemPrefixName, List<String> description, double strength, boolean isStrenghtPercentage, double mythicBonus) {
        this.description = description;
        this.name = name;
        this.itemPrefixName = itemPrefixName;
        this.strength = strength;
        this.mythicBonus = mythicBonus;
        this.isStrenghtPercentage = isStrenghtPercentage;
    }

    public ItemPassif(String name, String itemPrefixName, List<String> description, List<String> lore, double strength, boolean isStrenghtPercentage, double mythicBonus) {
        this.description = description;
        this.name = name;
        this.itemPrefixName = itemPrefixName;
        this.strength = strength;
        this.mythicBonus = mythicBonus;
        this.isStrenghtPercentage = isStrenghtPercentage;
        this.lore = lore;
    }
    public List<String> getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public double getStrength() {
        return strength;
    }

    public double getReadableStrength(){
        if(isStrenghtPercentage) {
            return strength * 100;
        }
        return strength;
    }

    public double getMythicBonus(boolean isMythic) {
        return isMythic ? mythicBonus : 0;
    }

    public double getReadableMythicBonus(){
        if(isStrenghtPercentage) {
            return mythicBonus * 100;
        }
        return mythicBonus;
    }

    public String getItemPrefixName() {
        return itemPrefixName;
    }
}

