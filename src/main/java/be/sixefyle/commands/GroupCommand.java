package be.sixefyle.commands;

import be.sixefyle.UGPlayer;
import be.sixefyle.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GroupCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        UGPlayer ugSender = UGPlayer.GetUGPlayer((Player) commandSender);
        String subcommand = args[0];
        if(args.length >= 2) {
            Group group;
            if(subcommand.equals("invite")){
                String targetName = args[1];
                Player target = Bukkit.getPlayer(targetName);
                if(target == null){
                    commandSender.sendMessage("§cThis player is not connected!");
                }

                if(ugSender.getGroup() == null){
                    group = ugSender.setGroup(new Group(ugSender));
                } else {
                    group = ugSender.getGroup();
                }
                if(group.getMembers().contains(target)){
                    commandSender.sendMessage("§cThis player is already in your group!");
                } else {
                    group.askPlayerToJoin(UGPlayer.GetUGPlayer(target));
                }
            } else if(subcommand.equals("accept")) {
                group = Group.getByOwnerName(args[1]);
                if(group != null && group.addPlayer(ugSender)){
                    commandSender.sendMessage("§aYou have joined the group!");
                }
            } else if(subcommand.equals("decline")) {
                group = Group.getByOwnerName(args[1]);
                if(group != null && group.removePendingInvite(ugSender)){
                    commandSender.sendMessage("§eYou declined the invitation!");
                }
            }
        }

        if(subcommand.equals("leave")){
            if(ugSender.leaveGroup()){
                commandSender.sendMessage("§eYou left your group!");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        List<String> completions = new ArrayList<>();
        if(args.length == 1){
            completions.add("invite");
            completions.add("accept");
            completions.add("decline");
            completions.add("leave");
        }

        return completions;
    }
}
