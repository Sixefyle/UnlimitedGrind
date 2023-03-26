package be.sixefyle.utils;

import java.text.DecimalFormat;
import java.util.Locale;

public class NumberUtils {
    public static double getRandomNumber(double min, double max) {
        return Math.random() * (max - min) + min;
    }

    public static String format(double number){
        char[] suffix = {' ', 'k', 'M', 'B', 'T', 'P', 'E'};
        int value = (int) Math.floor(Math.log10(number));
        int base = value / 3;

        if (value >= 4 && base < suffix.length) {
            return String.format(Locale.ENGLISH, "%,.1f" + suffix[base], number / Math.pow(10, base * 3));
        } else {
            return String.format(Locale.ENGLISH, "%,.0f", number);
        }
    }
}
