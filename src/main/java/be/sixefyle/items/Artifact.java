package be.sixefyle.items;

import net.kyori.adventure.text.format.TextColor;

import java.util.Arrays;
import java.util.Random;

public enum Artifact {
    NORMAL(null, "", 100, 1),
    ANCIENT(TextColor.color(178, 91, 2), "Ancient", 50, 1.1),
    PRIMALIST(TextColor.color(119, 61, 255), "Primalist", 0.5, 1.15),
    FORGED_BY_GODS(TextColor.color(215, 196, 0), "Forged By Gods", 0.01, 1.25),
    ;

    private final TextColor color;
    private final String name;
    private final double dropWeight;
    private final double attributeBonusPercentage;

    Artifact(TextColor color, String name, double dropWeight, double attributeBonusPercentage) {
        this.color = color;
        this.name = name;
        this.dropWeight = dropWeight;
        this.attributeBonusPercentage = attributeBonusPercentage;
    }

    public TextColor getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public double getDropWeight() {
        return dropWeight;
    }

    public double getAttributeBonusPercentage() {
        return attributeBonusPercentage;
    }

    private static final Random random = new Random();
    private static final double weightTotal = Arrays.stream(Artifact.values()).mapToDouble(Artifact::getDropWeight).sum();

    public static Artifact getRandomArtifact() {
        double randomNum = random.nextDouble(weightTotal);
        int currentWeightSum = 0;
        for (Artifact currentArtifact : Artifact.values()) {
            currentWeightSum += currentArtifact.getDropWeight();
            if (randomNum < currentWeightSum) {
                return currentArtifact;
            }
        }
        return null;
    }
}
