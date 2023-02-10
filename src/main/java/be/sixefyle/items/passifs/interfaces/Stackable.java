package be.sixefyle.items.passifs.interfaces;

import com.iridium.iridiumcore.dependencies.nbtapi.NBTItem;
import org.bukkit.metadata.Metadatable;

public interface Stackable {
    void addStack(NBTItem item, double amount);
    void resetStack(NBTItem item);
}
