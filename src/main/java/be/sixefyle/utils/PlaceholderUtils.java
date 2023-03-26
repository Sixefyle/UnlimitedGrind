package be.sixefyle.utils;

import be.sixefyle.UGSpawner;
import be.sixefyle.UGPlayer;
import be.sixefyle.enums.Symbols;
import be.sixefyle.gui.ArenaGui;
import be.sixefyle.items.passifs.ItemPassif;
import be.sixefyle.items.UGItem;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Damageable;

import java.util.Iterator;
import java.util.Locale;

public class PlaceholderUtils {
    public static String replace(String s){
        s = s.replaceAll("_", " ");
        s = IridiumColorAPI.process(s);
        return s;
    }

    public static String replace(UGSpawner betterSpawner, String s){
        double power = betterSpawner.getPower();
        s = s.replaceAll("%power%", String.format(Locale.ENGLISH, "%,.0f", power));
        s = s.replaceAll("%fPower%", NumberUtils.format(power));
        s = s.replaceAll("%amount%", NumberUtils.format(betterSpawner.getStackAmount()));
        s = s.replaceAll("%maxAmount%", NumberUtils.format(betterSpawner.getMaxStackAmount()));

        s = s.replaceAll("%silence%", betterSpawner.isSilence() ? "&aEnable" : "&cDisable");

        s = s.replaceAll("%powerUpgradeCost%", NumberUtils.format(UGSpawner.getUgradePrice(power, 10)));
        s = s.replaceAll("%powerUpgradeCost100%", NumberUtils.format(UGSpawner.getUgradePrice(power, 100)));
        s = s.replaceAll("%powerUpgradeCost1000%", NumberUtils.format(UGSpawner.getUgradePrice(power, 1000)));

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

        String prefix = ugItem.getPrefix();
        s = s.replaceAll("%prefix%", prefix != null ? prefix + " " : "");

        String suffix = ugItem.getSuffix();
        s = s.replaceAll("%suffix%", suffix != null ? suffix : "");

        String name = ugItem.getName();
        if(name != null) {
            s = s.replaceAll("%name%", name);
        } else {
            s = s.replaceAll("%name%", StringUtils.capitalize(ugItem.asItemStack().getType().toString().toLowerCase()));
        }

        return replace(s);
    }

    public static String replace(UGItem ugItem, UGPlayer ugPlayer, String s){
        if(ugItem.getPower() > ugPlayer.getMaxWearablePower()){
            s = s.replaceAll("%condition%", Symbols.DANGER.get() + "You need " + NumberUtils.format(ugPlayer.neededPower(ugItem.getPower())) + " more max power to equip this!");
        } else {
            s = s.replaceAll("%condition%", "");
        }
        s = replace(ugItem, s);
        return replace(s);
    }

    public static String replace(ItemPassif passif, boolean isItemMythic, String s){
        s = s.replaceAll("%strength%", String.format(Locale.ENGLISH, "%,.0f",
                passif.getReadableStrength() + (isItemMythic ? passif.getReadableMythicBonus() : 0)));

        return replace(s);
    }

    public static String replace(ArenaGui arenaGui, String s){
        double power = arenaGui.getCurrentPower();
        UGPlayer ugPlayer = arenaGui.getUgPlayer();


        s = s.replaceAll("%power%", String.format(Locale.ENGLISH, "%,.0f", power));
        s = s.replaceAll("%creatureHealth%", String.format(Locale.ENGLISH, "%,.0f", 100+((power/50)*(Math.pow(power,.78)/100+1)) * 100)); // TODO: create class to store this to one place
        s = s.replaceAll("%creatureDamage%", String.format(Locale.ENGLISH, "%,.0f", 100+(power/80) * 100));

        if(ugPlayer.hasGroup()){
            StringBuilder stringBuilder = new StringBuilder("&7Group: ");

            Iterator<UGPlayer> iterator = ugPlayer.getGroup().getMembers().iterator();

            while (iterator.hasNext()){
                stringBuilder.append(ChatColor.GREEN).append(iterator.next().getPlayer().getName());
                if(iterator.hasNext()){
                    stringBuilder.append(", ");
                }
            }
            s = s.replaceAll("%group%", stringBuilder.toString());
        } else {
            s = s.replaceAll("%group%", "");
        }

        return replace(s);
    }
}
