package be.sixefyle.utils;

import be.sixefyle.BetterSpawner;
import be.sixefyle.items.passifs.ItemPassif;
import be.sixefyle.items.UGItem;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Damageable;

import java.util.Locale;

public class PlaceholderUtils {
    public static String replace(String s){
        s = s.replaceAll("_", " ");
        s = IridiumColorAPI.process(s);
        return s;
    }

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

        return replace(s);
    }

    public static String replace(Damageable entity, String s){
        s = s.replaceAll("%currentHealth%", NumberUtils.format(entity.getHealth()));
        s = s.replaceAll("%maxHealth%", NumberUtils.format(entity.getMaxHealth()));
        if(entity.hasMetadata("amount"))
            s = s.replaceAll("%amount%", NumberUtils.format(entity.getMetadata("amount").get(0).asInt()));
        if(entity.hasMetadata("power"))
            s = s.replaceAll("%power%", NumberUtils.format(entity.getMetadata("power").get(0).asDouble()));

        return replace(s);
    }

    public static String replace(UGItem ugItem, String s){
        s = s.replaceAll("%power%", String.format(Locale.ENGLISH, "%,.0f", ugItem.getPower()));
        s = s.replaceAll("%rarity%", ugItem.getRarity().getColor() + ugItem.getRarity().getName());

        String name = ugItem.getName();
        if(name != null) {
            s = s.replaceAll("%name%", name);
        } else {
            s = s.replaceAll("%name%", StringUtils.capitalize(ugItem.getItem().getType().toString().toLowerCase()));
        }

        return replace(s);
    }

    public static String replace(ItemPassif passif, boolean isItemMythic, String s){
        s = s.replaceAll("%strength%", String.format(Locale.ENGLISH, "%,.0f",
                passif.getReadableStrength() + (isItemMythic ? passif.getReadableMythicBonus() : 0)));

        return replace(s);
    }
}
