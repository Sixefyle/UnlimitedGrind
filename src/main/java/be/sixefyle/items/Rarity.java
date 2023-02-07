package be.sixefyle.items;

import net.md_5.bungee.api.ChatColor;

import java.util.Random;

public enum Rarity {
    COMMON(ChatColor.WHITE, "Common", 55.0, 0),
    MAGIC(ChatColor.BLUE, "Magic", 30.0, 1),
    RARE(ChatColor.YELLOW, "Rare", 14.0, 2),
    LEGENDARY(ChatColor.GOLD, "Legendary", 0.95, 3),
    MYTHIC(ChatColor.AQUA, "Mythic", 1005, 5),
    ;

    private final ChatColor color;
    private final String name;
    private final double dropWeight;
    private final int bonusAttributeAmount;

    Rarity(ChatColor color, String name, double dropWeight, int bonusAttributeAmount) {
        this.color = color;
        this.name = name;
        this.dropWeight = dropWeight;
        this.bonusAttributeAmount = bonusAttributeAmount;
    }

    public double getDropWeight() {
        return dropWeight;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public int getBonusAttributeAmount() {
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
