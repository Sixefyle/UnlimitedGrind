package be.sixefyle.arena;

import be.sixefyle.UGPlayer;
import be.sixefyle.group.Group;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.*;
public class WorldManager {

    private static boolean unzipWorldAndRename(ArenaMap arena, String worldName){
        String fileZip = Bukkit.getWorldContainer().getAbsolutePath().replaceFirst("\\.", "") + "arenas\\" + arena.getSchematicName() + ".zip";
        String destDir = Bukkit.getWorldContainer().getAbsolutePath().replaceFirst("\\.", "") + "\\" + worldName;
        try {
            ZipFile zipFile = new ZipFile(fileZip);
            zipFile.extractAll(destDir);
            return true;
        } catch (ZipException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean createVoidAndTeleport(Player owner, Group group, ArenaMap arena){
        String worldName = "arena_" + owner.getUniqueId();
        if(unzipWorldAndRename(arena, worldName)){
            WorldCreator worldCreator = new WorldCreator(worldName);
            worldCreator.generator(new EmptyWorld());
            worldCreator.createWorld();
            World world = Bukkit.getWorld(worldName);

            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            world.setGameRule(GameRule.MOB_GRIEFING, false);
            world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
            world.setGameRule(GameRule.DO_FIRE_TICK, false);
            world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);

            world.setTime(13000);

            Location loc = arena.getPlayerSpawnLocs().get((int) (Math.random() * arena.getPlayerSpawnLocs().size())).clone();
            loc.setWorld(world);

            if(group != null) {
                for (UGPlayer member : group.getMembers()) {
                    member.getPlayer().teleport(loc);
                }
            } else {
                owner.teleport(loc);
            }
            return true;
        }
        return false;
    }

    public static boolean createVoidAndTeleport(Player owner, ArenaMap arena){
        return createVoidAndTeleport(owner, null, arena);
    }

//    public static boolean createArena(Location loc, String schematicName){
//        File arenaSchematic = new File(schematicName);
//        ClipboardFormat format = ClipboardFormats.findByFile(arenaSchematic);
//        try (ClipboardReader reader = format.getReader(new FileInputStream(arenaSchematic))) {
//            Clipboard clipboard = reader.read();
//            com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(loc.getWorld());
//            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1)) {
//                Operation operation = new ClipboardHolder(clipboard)
//                        .createPaste(editSession)
//                        .to(BlockVector3.at(7,64,7))
//                        .ignoreAirBlocks(true)
//                        .build();
//                Operations.complete(operation);
//            } catch (WorldEditException e) {
//                e.printStackTrace();
//                return false;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//
//        return true;
//    }

    public static void deleteWorld(World world){
        Bukkit.unloadWorld(world, false);
        File worldFolder = new File(world.getWorldFolder().getAbsolutePath());
        try {
            FileUtils.deleteDirectory(worldFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
