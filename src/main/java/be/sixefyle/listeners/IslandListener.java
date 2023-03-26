package be.sixefyle.listeners;

import be.sixefyle.UGIsland;
import be.sixefyle.UGPlayer;
import be.sixefyle.UnlimitedGrind;
import com.iridium.iridiumskyblock.api.IslandCreateEvent;
import com.iridium.iridiumskyblock.api.IslandDeleteEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class IslandListener implements Listener {

    @EventHandler
    public void onCreateIsland(IslandCreateEvent e){
        if(e.getUser().getPlayer() == null) return;
        Bukkit.getScheduler().runTaskLater(UnlimitedGrind.getInstance(), () -> {
            UGPlayer ugPlayer = UGPlayer.GetUGPlayer(e.getUser().getPlayer());
            ugPlayer.setUgIsland(new UGIsland(e.getUser().getIsland()));
            ugPlayer.initScoreboard();
        }, 5);
    }

    @EventHandler
    public void onDeleteIsland(IslandDeleteEvent e){
        if(e.getUser() == null || e.getUser().getPlayer() == null) return;
        UGPlayer ugPlayer = UGPlayer.GetUGPlayer(e.getUser().getPlayer());
        ugPlayer.setUgIsland(null);
        ugPlayer.initScoreboard();
    }
}
