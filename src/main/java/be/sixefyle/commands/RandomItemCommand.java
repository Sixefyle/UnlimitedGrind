package be.sixefyle.commands;

import be.sixefyle.items.ItemCategory;
import be.sixefyle.items.ItemManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RandomItemCommand implements @Nullable CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if(commandSender instanceof Player player){
            if(player.isOp()){
                if(args.length == 1) {
                    player.getInventory().addItem(ItemManager.generateRandomItem(Double.parseDouble(args[0])).getItem());
                } else if(args.length == 2){
                    player.getInventory().addItem(ItemManager.generateRandomItem(ItemCategory.valueOf(args[1]),
                            Double.parseDouble(args[0])).getItem());
                }
            } else return false;
        }
        return true;
    }
}
