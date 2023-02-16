package be.sixefyle.utils;

import org.bukkit.ChatColor;
import org.bukkit.Color;

public class ColorUtils {
    public static Color convertChatColorToColor(ChatColor color){
        Color returnColor = Color.BLACK;
        switch (color) {
            case WHITE -> returnColor = Color.WHITE;
            case BLUE, DARK_BLUE -> returnColor = Color.BLUE;
            case GOLD -> returnColor = Color.ORANGE;
            case GRAY, DARK_GRAY -> returnColor = Color.GRAY;
            case GREEN, DARK_GREEN -> returnColor = Color.GREEN;
            case RED, DARK_RED -> returnColor = Color.RED;
            case AQUA, DARK_AQUA -> returnColor = Color.AQUA;
            case YELLOW -> returnColor = Color.YELLOW;
            case LIGHT_PURPLE, DARK_PURPLE -> returnColor = Color.PURPLE;
        }

        return returnColor;
    }
}
