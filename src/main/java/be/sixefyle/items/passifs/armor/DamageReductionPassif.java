package be.sixefyle.items.passifs.armor;

import be.sixefyle.UnlimitedGrind;
import be.sixefyle.items.passifs.ItemPassif;
import be.sixefyle.items.passifs.interfaces.OnReceiveDamage;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class DamageReductionPassif extends ItemPassif implements OnReceiveDamage {
    public DamageReductionPassif() {
        super(UnlimitedGrind.getInstance().getConfig().getString("itemPassif.damageReduction.name"),
                UnlimitedGrind.getInstance().getConfig().getStringList("itemPassif.damageReduction.description"),
                UnlimitedGrind.getInstance().getConfig().getDouble("itemPassif.damageReduction.strength"),
                true,
                0.02);
    }

    @Override
    public void onGetDamage(EntityDamageEvent e, Player player, ItemStack armor) {
        e.setDamage(e.getDamage() / (getStrength()+1));
    }
}
