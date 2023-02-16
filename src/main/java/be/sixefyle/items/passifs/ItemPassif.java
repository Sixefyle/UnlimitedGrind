package be.sixefyle.items.passifs;

import be.sixefyle.utils.PlaceholderUtils;

import java.util.List;

public abstract class ItemPassif {

    private List<String> description;
    private String name;
    private String itemPrefixName;

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

    public List<String> getDescription() {
        return description;
    }

    public String getName() {
        return name;
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

    public double getMythicBonus() {
        return mythicBonus;
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

