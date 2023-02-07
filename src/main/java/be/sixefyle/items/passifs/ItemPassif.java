package be.sixefyle.items.passifs;

import be.sixefyle.utils.PlaceholderUtils;

import java.util.List;

public abstract class ItemPassif {

    private List<String> description;
    private String name;

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

    public List<String> getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public double getStrength() {
        if(isStrenghtPercentage) {
            return (strength - 1) * 100;
        }
        return strength;
    }

    public double getMythicBonus() {
        if(isStrenghtPercentage) {
            return mythicBonus * 100;
        }
        return mythicBonus;
    }
}

