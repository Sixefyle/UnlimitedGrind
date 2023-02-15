package be.sixefyle.commands;

import be.sixefyle.arena.Arena;
import be.sixefyle.arena.pve.PveArena;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.InputMismatchException;

public class ArenaCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length >= 1){
            try{
                Arena arena = Arena.values()[(int) (Math.random() * Arena.values().length)];
                new PveArena((Player) commandSender, arena).join(Double.parseDouble(args[0]));
            } catch (InputMismatchException ignore) { }
        }

        return true;
    }
}
