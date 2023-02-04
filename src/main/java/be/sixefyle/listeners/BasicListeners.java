package be.sixefyle.listeners;

import be.sixefyle.UGPlayer;
import be.sixefyle.items.UGItem;
import be.sixefyle.items.passifs.Passif;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;

public class BasicListeners implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        if (UGPlayer.playerMap.get(player) == null) {
            new UGPlayer(e.getPlayer());
        }

        UGItem testItem = new UGItem(Material.IRON_SWORD, "Test Passif", null, 0, new ArrayList<>(){{ add(Passif.DOUBLE_DAMAGE); }});
        player.getInventory().addItem(testItem.getItem());
    }

    @EventHandler
    public void changeBlockDropLoc(BlockDropItemEvent e){
        Player player = e.getPlayer();
        for (Item item : e.getItems()){
            item.teleport(player);
        }
    }
}
