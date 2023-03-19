package be.sixefyle.items;


import net.kyori.adventure.text.format.TextColor;

import java.util.Random;

public enum Rarity {
    COMMON(TextColor.color(213, 213, 213), "Common", 55.0, 0),
    MAGIC(TextColor.color(62, 123, 255), "Magic", 30.0, 1),
    RARE(TextColor.color(246, 255, 37), "Rare", 14.0, 2),
    LEGENDARY(TextColor.color(255, 163, 64), "Legendary", 0.95, 3),
    MYTHIC(TextColor.color(70, 242, 255), "Mythic", 0.05, 4),
    ;

    private final TextColor color;
    private final String name;
    private final double dropWeight;
    private final int bonusAttributeAmount;

    Rarity(TextColor color, String name, double dropWeight, int bonusAttributeAmount) {
        this.color = color;
        this.name = name;
        this.dropWeight = dropWeight;
        this.bonusAttributeAmount = bonusAttributeAmount;
    }

    public double getDropWeight() {
        return dropWeight;
    }

    public TextColor getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public int getBonusStatsAmount() {
        return bonusAttributeAmount;
    }

    public static double getWeightTotal(){
        double total = 0;
        for (Rarity value : Rarity.values()) {
            total += value.getDropWeight();
        }
        return total;
    }

    public static Rarity getRandomRarity(){
        Random random = new Random();
        double randomNum = random.nextDouble(getWeightTotal());
        double currentWeightSumm = 0;
        for(Rarity currentRarity: Rarity.values()) {
            if (randomNum > currentWeightSumm &&
                    randomNum <= (currentWeightSumm + currentRarity.getDropWeight())) {
                return currentRarity;
            }
            currentWeightSumm += currentRarity.getDropWeight();
        }

        return COMMON;
    }
}
