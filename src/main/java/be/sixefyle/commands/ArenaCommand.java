package be.sixefyle.commands;

import be.sixefyle.UGPlayer;
import be.sixefyle.arena.Arena;
import be.sixefyle.arena.pve.PveArena;
import be.sixefyle.gui.ArenaGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.InputMismatchException;

public class ArenaCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length == 0){
            Player player = (Player) commandSender;
            player.openInventory(new ArenaGui(UGPlayer.GetUGPlayer(player)).getInventory());
        } else {
            try{
                Arena arena;
                try{
                    arena = Arena.valueOf(args[1].toUpperCase());
                } catch (ArrayIndexOutOfBoundsException e){
                    arena = Arena.values()[(int) (Math.random() * Arena.values().length)];
                }
                UGPlayer ugPlayer = UGPlayer.GetUGPlayer((Player) commandSender);
                ugPlayer.joinPveArena(arena, Double.parseDouble(args[0]));

            } catch (InputMismatchException ignore) { }
        }

        return true;
    }
}
