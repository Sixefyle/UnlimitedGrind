package be.sixefyle.listeners;

import be.sixefyle.UGIsland;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.utils.NumberUtils;
import com.iridium.iridiumcore.dependencies.xseries.XMaterial;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.upgrades.OresUpgrade;
import com.iridium.iridiumskyblock.utils.RandomAccessList;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;


public class BlockGeneratorListener implements Listener {
    @EventHandler
    public void onBlockForm(BlockFormEvent e){
        Optional<Island> island = IridiumSkyblockAPI.getInstance().getIslandViaLocation(e.getBlock().getLocation());
        UGIsland ugIsland = UGIsland.getIsland(island);
        Location loc = e.getBlock().getLocation();

        if(!ugIsland.getGeneratorsLoc().contains(loc)){
            ugIsland.addGenerator(loc);
        } else {
            e.setCancelled(true);
        }
    }

    private static final Map<Integer, RandomAccessList<XMaterial>> normalOreLevels = new HashMap();
    private static final Map<Integer, RandomAccessList<XMaterial>> netherOreLevels = new HashMap();
    public static void generateOrePossibilities() {
        Iterator var0 = IridiumSkyblock.getInstance().getUpgrades().oresUpgrade.upgrades.entrySet().iterator();

        while(var0.hasNext()) {
            Map.Entry<Integer, OresUpgrade> oreUpgrade = (Map.Entry)var0.next();
            normalOreLevels.put(oreUpgrade.getKey(), new RandomAccessList((oreUpgrade.getValue()).ores));
            netherOreLevels.put(oreUpgrade.getKey(), new RandomAccessList((oreUpgrade.getValue()).netherOres));
        }
    }

    private final int[] generatorSpeed = {20,15,10,5,3};
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        Location loc = e.getBlock().getLocation();
        Optional<Island> island = IridiumSkyblockAPI.getInstance().getIslandViaLocation(loc);
        if(island.isPresent() && UGIsland.getIsland(island).getGeneratorsLoc().contains(loc)){
            Block block = e.getBlock();
            if(isStillGenerator(block)) {
                int genSpeedLevel = IridiumSkyblockAPI.getInstance().getIslandUpgrade(island.get(), "GeneratorSpeedUpgrade").getLevel();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        generateBlock(block.getLocation());
                    }
                }.runTaskLater(UnlimitedGrind.getInstance(), generatorSpeed[genSpeedLevel-1]);
            } else {
                UGIsland.getIsland(island).removeGenerator(block.getLocation());
            }
        }
    }

    public boolean isStillGenerator(Block block){
        BlockFace[] faces = {BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.UP};
        boolean hasLava = false;
        boolean hasWater = false;
        for (BlockFace face : faces) {
            if(hasType(Material.LAVA, face, block)) {
                hasLava = true;
            } else if(hasType(Material.WATER, face, block)) {
                hasWater = true;
            }
        }
        return hasLava && hasWater;
    }

    private boolean hasType(Material type, BlockFace face, Block block){
        if (block.getRelative(face).getType().equals(type)) {
            return true;
        }
        return false;
    }

    public void generateBlock(Location loc){
        generateOrePossibilities();// TODO: need to try to remove this for opti
        if (IridiumSkyblockAPI.getInstance().isIslandWorld(loc.getWorld())) {
            XMaterial newMaterial = XMaterial.matchXMaterial(loc.getBlock().getType());
            Optional<Island> island = IridiumSkyblock.getInstance().getIslandManager().getIslandViaLocation(loc.getBlock().getLocation());
            if (island.isPresent()) {
                int upgradeLevel = IridiumSkyblock.getInstance().getIslandManager().getIslandUpgrade(island.get(), "generator").getLevel();
                RandomAccessList<XMaterial> randomMaterialList = newMaterial == XMaterial.BASALT ? netherOreLevels.get(upgradeLevel) : normalOreLevels.get(upgradeLevel);
                if (randomMaterialList == null) {
                    return;
                }

                Optional<XMaterial> xMaterialOptional = randomMaterialList.nextElement();
                if (!xMaterialOptional.isPresent()) {
                    return;
                }

                Material material = xMaterialOptional.get().parseMaterial();
                if (material == Material.COBBLESTONE && newMaterial == XMaterial.STONE) {
                    material = Material.STONE;
                }

                if (material != null) {
                    loc.getBlock().setType(material);
                    loc.getWorld().playSound(loc.toCenterLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1, 2);

                    Location particleLoc;
                    for (int i = 0; i < 3; i++) {
                        particleLoc = loc.toCenterLocation().clone();
                        particleLoc.add(NumberUtils.getRandomNumber(-.4,.4),.6, NumberUtils.getRandomNumber(-.4,.4)).getWorld().spawnParticle(Particle.SMOKE_LARGE, particleLoc, 0, 0, 0, 0, .1);
                    }
                }
            }
        }
    }
}
