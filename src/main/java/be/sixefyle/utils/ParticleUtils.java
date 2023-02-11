package be.sixefyle.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class ParticleUtils {

    public static void drawBeam(Location loc1, Location loc2, Particle type){
        Location particleLoc = loc1.clone();
        World world = loc1.getWorld();

        Vector dir = loc1.toVector().subtract(loc2.toVector()).multiply(-1);
        final Vector vecOffset = dir.clone().multiply(0.05);
        int maxIteration = 100;
        int iteration = 0;
        while(!(Math.round(particleLoc.getBlockX()) == Math.round(loc2.getBlockX()) &&
                Math.round(particleLoc.getBlockY()) == Math.round(loc2.getBlockY()) &&
                Math.round(particleLoc.getBlockZ()) == Math.round(loc2.getBlockZ())) &&
                iteration++ < maxIteration)
        {
            particleLoc.add(vecOffset);
            world.spawnParticle(type, particleLoc, 0);
        }
    }
}
