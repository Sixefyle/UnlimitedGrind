package be.sixefyle.commands;

import be.sixefyle.items.ItemCategory;
import be.sixefyle.items.ItemManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RandomItemCommand implements @Nullable CommandExecutor, @Nullable TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if(commandSender instanceof Player player){
            if(player.isOp()){
                if(args.length == 1) {
                    player.getInventory().addItem(ItemManager.generateRandomItem(Double.parseDouble(args[0])).asItemStack());
                } else if(args.length == 2){
                    player.getInventory().addItem(ItemManager.generateRandomItem(ItemCategory.valueOf(args[1]),
                            Double.parseDouble(args[0])).asItemStack());
                }
            } else return false;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        List<String> completions = new ArrayList<>();
        if(args.length == 2){
            for (ItemCategory value : ItemCategory.values()) {
                completions.add(value.name());
            }
        }
        return completions;
    }
}
