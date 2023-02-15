package be.sixefyle.arena;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.*;
public class WorldManager {

    private static boolean unzipWorldAndRename(Arena arena, String worldName){
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

    public static boolean createVoidAndTeleport(Player player, Arena arena){
        String worldName = String.valueOf(player.getUniqueId());
        if(unzipWorldAndRename(arena, worldName)){
            WorldCreator worldCreator = new WorldCreator(worldName);
            worldCreator.createWorld();
            World world = Bukkit.getWorld(worldName);
            Location loc = arena.getPlayerSpawnLocs().get((int) (Math.random() * arena.getPlayerSpawnLocs().size()));
            loc.setWorld(world);
            player.teleport(loc);
            return true;
        }
        return false;
    }

    public static boolean createArena(Location loc, String schematicName){
        File arenaSchematic = new File(schematicName);
        ClipboardFormat format = ClipboardFormats.findByFile(arenaSchematic);
        try (ClipboardReader reader = format.getReader(new FileInputStream(arenaSchematic))) {
            Clipboard clipboard = reader.read();
            com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(loc.getWorld());
            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1)) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(7,64,7))
                        .ignoreAirBlocks(true)
                        .build();
                Operations.complete(operation);
            } catch (WorldEditException e) {
                e.printStackTrace();
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static void deleteWorldArena(Player owner){
        World world = Bukkit.getWorld(owner.getUniqueId().toString());
        if(world == null) return;
        Bukkit.unloadWorld(world, false);

        File worldFolder = new File(world.getWorldFolder().getAbsolutePath());
        try {
            FileUtils.deleteDirectory(worldFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
