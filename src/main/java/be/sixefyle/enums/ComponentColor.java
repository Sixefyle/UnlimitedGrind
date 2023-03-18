package be.sixefyle.enums;

import net.kyori.adventure.text.format.TextColor;

public enum ComponentColor {
    ERROR(TextColor.color(200, 58, 43)),
    FINE(TextColor.color(72, 200, 51)),
    WARNING(TextColor.color(249, 255, 43)),
    GOLD(TextColor.color(255, 208, 72)),
    NEUTRAL(TextColor.color(171, 168, 168)),
    LORE(TextColor.color(146, 99, 13)),
    ITEM_SPECIAL_STAT(TextColor.color(172, 29, 164)),
    ARMOR(TextColor.color(34, 141, 241)),
    ;

    TextColor color;

    ComponentColor(TextColor color) {
        this.color = color;
    }

    public TextColor getColor() {
        return color;
    }
}
