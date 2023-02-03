package be.sixefyle.commands;

import be.sixefyle.UGPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.InputMismatchException;
import java.util.Locale;

public class PowerCommand implements @Nullable CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        try{
            if (args.length == 0){
                UGPlayer ugPlayer = UGPlayer.GetUGPlayer((Player) commandSender);
                commandSender.sendMessage("Power:" + String.format(Locale.ENGLISH, "%,.0f", ugPlayer.getPower()));
            }
            if(args.length == 3){
                if(args[0].equals("set")){
                    UGPlayer ugPlayer = UGPlayer.GetUGPlayer(Bukkit.getPlayer(args[1]));
                    ugPlayer.setPower(Double.parseDouble(args[2]));
                }
            }
        }catch (InputMismatchException e){
            return false;
        }
        return true;
    }
}
