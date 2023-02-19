package be.sixefyle.enums;

import net.kyori.adventure.text.format.TextColor;

public enum ComponentColor {
    ERROR(TextColor.color(200, 58, 43)),
    FINE(TextColor.color(72, 200, 51)),
    WARNING(TextColor.color(249, 255, 43)),
    ;

    TextColor color;

    ComponentColor(TextColor color) {
        this.color = color;
    }

    public TextColor getColor() {
        return color;
    }
}
