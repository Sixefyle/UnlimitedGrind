package be.sixefyle.utils;

import be.sixefyle.UnlimitedGrind;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.line.TextHologramLine;
import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HologramUtils {

    public static int index = 0;

    public static void createTimed(Location loc, List<String> text, int ticks){
        Hologram hologram = UnlimitedGrind.getHolographicApi().createHologram(loc);
        for(String line : text){
            hologram.getLines().appendText(line);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                hologram.delete();
                cancel();
            }
        }.runTaskLater(UnlimitedGrind.getInstance(), ticks);
    }

    public static void createTimed(Location loc, String line, int ticks){
        createTimed(loc, new ArrayList<>(Collections.singleton(line)), ticks);
    }

    public static void createEntInfoFollow(Damageable entityToFollow){
        final Hologram hologram = UnlimitedGrind.getHolographicApi().createHologram(entityToFollow.getLocation());
        String powerLine = UnlimitedGrind.getInstance().getConfig().getString("creature.power");
        String healthLine = UnlimitedGrind.getInstance().getConfig().getString("creature.health");
        String amountLine = UnlimitedGrind.getInstance().getConfig().getString("creature.amount");
        TextHologramLine holoHPowerLine = hologram.getLines().appendText(powerLine);
        TextHologramLine holoHealthLine = hologram.getLines().appendText(healthLine);
        TextHologramLine holoAmountLine = hologram.getLines().appendText(amountLine);

        new BukkitRunnable() {
            Location entLoc = entityToFollow.getLocation().clone();
            @Override
            public void run() {
                if(entityToFollow.isDead()) {
                    cancel();
                    hologram.delete();
                    return;
                }
                entLoc = entityToFollow.getLocation().clone();
                hologram.setPosition(entLoc.add(0,entityToFollow.getHeight() + .8,0));
                holoHPowerLine.setText(PlaceholderUtils.replace(entityToFollow, powerLine));
                holoHealthLine.setText(PlaceholderUtils.replace(entityToFollow, healthLine));
                holoAmountLine.setText(PlaceholderUtils.replace(entityToFollow, amountLine));
            }
        }.runTaskTimer(UnlimitedGrind.getInstance(), 0, 1);
    }
}
