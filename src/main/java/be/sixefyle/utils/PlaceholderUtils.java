package be.sixefyle.utils;

import be.sixefyle.BetterSpawner;
import be.sixefyle.items.passifs.ItemPassif;
import be.sixefyle.items.UGItem;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Damageable;

import java.util.Locale;

public class PlaceholderUtils {
    public static String replace(BetterSpawner betterSpawner, String s){
        double power = betterSpawner.getPower();
        s = s.replaceAll("%power%", String.format(Locale.ENGLISH, "%,.0f", power));
        s = s.replaceAll("%fPower%", NumberUtils.format(power));
        s = s.replaceAll("%amount%", NumberUtils.format(betterSpawner.getStackAmount()));
        s = s.replaceAll("%maxAmount%", NumberUtils.format(betterSpawner.getMaxStackAmount()));

        s = s.replaceAll("%silence%", betterSpawner.isSilence() ? "&aEnable" : "&cDisable");

        s = s.replaceAll("%powerUpgradeCost%", NumberUtils.format(BetterSpawner.getUgradePrice(power, 10)));
        s = s.replaceAll("%powerUpgradeCost100%", NumberUtils.format(BetterSpawner.getUgradePrice(power, 100)));
        s = s.replaceAll("%powerUpgradeCost1000%", NumberUtils.format(BetterSpawner.getUgradePrice(power, 1000)));

        s = s.replaceAll("%minTime%", String.valueOf(betterSpawner.getSpawner().getMinSpawnDelay()));
        s = s.replaceAll("%maxTime%", String.valueOf(betterSpawner.getSpawner().getMaxSpawnDelay()));

        s = s.replaceAll("%rareDropChance%", String.valueOf(betterSpawner.getRareDropChance()));

        s = s.replaceAll("%stackUpgradeLevel%", String.valueOf(betterSpawner.getStackUpgradeLevel()));
        s = s.replaceAll("%maxStackUpgradeLevel%", String.valueOf(betterSpawner.getMaxStackUpgradeLevel()));

        s = s.replaceAll("%mobType%", StringUtils.capitalize(betterSpawner.getSpawner().getSpawnedType().name().toLowerCase()));

        s = s.replaceAll("_", " ");
        s = IridiumColorAPI.process(s);
        return s;
    }

    public static String replace(Damageable entity, String s){
        s = s.replaceAll("%currentHealth%", NumberUtils.format(entity.getHealth()));
        s = s.replaceAll("%maxHealth%", NumberUtils.format(entity.getMaxHealth()));
        if(entity.hasMetadata("amount"))
            s = s.replaceAll("%amount%", NumberUtils.format(entity.getMetadata("amount").get(0).asInt()));
        if(entity.hasMetadata("power"))
            s = s.replaceAll("%power%", NumberUtils.format(entity.getMetadata("power").get(0).asDouble()));

        s = IridiumColorAPI.process(s);
        return s;
    }

    public static String replace(UGItem ugItem, String s){
        s = s.replaceAll("%power%", NumberUtils.format(ugItem.getPower()));

        s = IridiumColorAPI.process(s);
        return s;
    }

    public static String replace(ItemPassif passif, String s){
        s = IridiumColorAPI.process(s);
        return s;
    }
}
