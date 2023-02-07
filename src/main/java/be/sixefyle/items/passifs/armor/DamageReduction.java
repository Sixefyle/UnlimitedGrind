package be.sixefyle.items.passifs.armor;

import be.sixefyle.UnlimitedGrind;
import be.sixefyle.items.passifs.ItemPassif;
import be.sixefyle.items.passifs.interfaces.OnReceiveDamage;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageReduction extends ItemPassif implements OnReceiveDamage {
    public DamageReduction() {
        super(UnlimitedGrind.getInstance().getConfig().getString("itemPassif.damageReduction.name"),
                UnlimitedGrind.getInstance().getConfig().getStringList("itemPassif.damageReduction.lore"),
                UnlimitedGrind.getInstance().getConfig().getDouble("itemPassif.damageReduction.strength"),
                true,
                0.02);
    }

    @Override
    public void onGetDamage(EntityDamageEvent e) {
        if(e.getEntity() instanceof Player){//TODO: mythic check
            e.setDamage(e.getDamage() * getStrength());
        }
    }
}
