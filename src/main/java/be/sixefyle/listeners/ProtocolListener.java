package be.sixefyle.listeners;

import be.sixefyle.UnlimitedGrind;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Particle;
import org.bukkit.plugin.Plugin;

public class ProtocolListener {
    ProtocolManager protocolManager;
    Plugin plugin = UnlimitedGrind.getInstance();

    public ProtocolListener(ProtocolManager protocolManager) {
        this.protocolManager = protocolManager;
        init();
    }

    private void init(){
        heartDamageParticle();
    }

    private void heartDamageParticle(){
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.WORLD_PARTICLES) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                if (event.getPacketType() != PacketType.Play.Server.WORLD_PARTICLES)
                    return;

                if (packet.getNewParticles().read(0).getParticle() == Particle.DAMAGE_INDICATOR)
                    event.setCancelled(true);
            }
        });
    }
}
