package be.sixefyle.commands;

import be.sixefyle.UGPlayer;
import be.sixefyle.UnlimitedGrind;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(commandSender instanceof Player sender){
            if(!sender.isOp()) return false;
        }

//        Plugin plugin = Bukkit.getPluginManager().getPlugin("UnlimitedGrind");
//        Bukkit.getPluginManager().disablePlugin(plugin);
//        Bukkit.getPluginManager().enablePlugin(plugin);

        return true;
    }
}
