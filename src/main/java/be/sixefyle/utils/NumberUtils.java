package be.sixefyle.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

import java.text.DecimalFormat;

public class NumberUtils {
    public static double getRandomNumber(double min, double max) {
        return Math.random() * (max - min) + min;
    }

    public static String format(double number){
        char[] suffix = {' ', 'k', 'M', 'B', 'T', 'P', 'E'};
        int value = (int) Math.floor(Math.log10(number));
        int base = value / 3;

        if (value >= 3 && base < suffix.length) {
            return new DecimalFormat("#0.0").format(number / Math.pow(10, base * 3)) + suffix[base];
        } else {
            return new DecimalFormat("#,##0").format(number);
        }
    }
}
