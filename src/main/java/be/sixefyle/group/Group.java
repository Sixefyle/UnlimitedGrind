package be.sixefyle.group;

import be.sixefyle.UGPlayer;
import be.sixefyle.enums.ComponentColor;
import be.sixefyle.utils.ComponentUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private static final int MAX_SIZE = 4;
    private List<UGPlayer> members;
    private List<UGPlayer> pendingInvit;

    public Group(UGPlayer owner){
        members = new ArrayList<>();
        pendingInvit = new ArrayList<>();
        members.add(owner);
    }

    public Group(List<UGPlayer> ugPlayersList) {
        this.members = ugPlayersList;
    }

    public List<UGPlayer> getMembers() {
        return members;
    }

    public UGPlayer getOwner(){
        return members.get(0);
    }

    public boolean isOwner(UGPlayer ugPlayer){
        return members.get(0).equals(ugPlayer);
    }

    public boolean addPlayer(UGPlayer ugPlayer){
        if(members.size() > MAX_SIZE) {
            return false;
        }
        if(ugPlayer.getGroup() != null){
            Component youNeedToLeaveComponent = Component.text("You need to leave your group first before join another group!");
            Component leaveComponent = ComponentUtils.createText("Click to leave your current group", "Click to leave your current group")
                    .color(ComponentColor.WARNING.getColor())
                    .decorate(TextDecoration.BOLD)
                    .clickEvent(ClickEvent.runCommand("/group leave"));

            ugPlayer.sendMessageComponents(List.of(youNeedToLeaveComponent, leaveComponent));
            return false;
        }

        if(pendingInvit.contains(ugPlayer)){
            ugPlayer.setGroup(this);
            members.add(ugPlayer);
            pendingInvit.remove(ugPlayer);

            sendMessageToGroup(Component.text(ugPlayer.getPlayer().getName() + " has join the group!"));
            return true;
        }
        return false;
    }

    public void removePlayer(UGPlayer ugPlayer){
        if(members.contains(ugPlayer)){
            members.remove(ugPlayer);
            sendMessageToGroup(Component.text(ugPlayer.getPlayer().getName() + " has left the group!"));
        }
    }

    public void sendMessageToGroup(Component line){
        if(members.size() <= 0) return;
        for (UGPlayer member : members) {
            member.getPlayer().sendMessage(line);
        }
    }

    private void addPlayerPendingInvit(UGPlayer ugPlayer){
        pendingInvit.add(ugPlayer);
    }

    public List<String> getGroupMemberName(){
        List<String> membersName = new ArrayList<>();
        for (UGPlayer member : members) {
            membersName.add(member.getPlayer().getName());
        }
        return membersName;
    }

    public void askPlayerToJoin(UGPlayer ugPlayer){
        if(members.size() > MAX_SIZE) {
            return;
        }

        addPlayerPendingInvit(ugPlayer);

        TextComponent ownerName = (TextComponent) getOwner().getPlayer().displayName();

        Component inviteComponent = getOwner().getPlayer().name();
        Component askComponent = ComponentUtils.createText(" has invited you to join his group !", getGroupMemberName());
        inviteComponent = inviteComponent.append(askComponent);

        Component acceptComponent =  ComponentUtils.createText("Accept", "Click to accept invitation")
                .color(ComponentColor.FINE.getColor())
                .decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/group accept " + ownerName.content()));

        Component declineComponent = ComponentUtils.createText("Decline", "Click to refuse invitation")
                .color(ComponentColor.ERROR.getColor())
                .decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/group decline " +  ownerName.content()));

        acceptComponent = acceptComponent.append(ComponentUtils.createSeparator("   ")).append(declineComponent);

        ugPlayer.sendMessageComponents(List.of(inviteComponent, acceptComponent));
    }

    public boolean removePendingInvite(UGPlayer ugPlayer){
        return pendingInvit.remove(ugPlayer);
    }

    public static Group getByOwnerName(String playerName){
        try{
            UGPlayer ugPlayer = UGPlayer.GetUGPlayer(Bukkit.getPlayer(playerName));
            return ugPlayer.getGroup();
        } catch (Exception ignore) { }
        return null;
    }
}
