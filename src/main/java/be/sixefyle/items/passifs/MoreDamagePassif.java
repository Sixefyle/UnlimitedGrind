package be.sixefyle.items.passifs;

import be.sixefyle.UnlimitedGrind;
import be.sixefyle.items.UGItem;
import be.sixefyle.items.passifs.interfaces.OnMeleeHit;
import be.sixefyle.items.passifs.interfaces.OnProjectilHit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

public class MoreDamagePassif extends ItemPassif implements OnMeleeHit, OnProjectilHit {
    public MoreDamagePassif() {
        super(UnlimitedGrind.getInstance().getConfig().getString("itemPassif.moreDamage.name"),
                UnlimitedGrind.getInstance().getConfig().getStringList("itemPassif.moreDamage.lore"),
                UnlimitedGrind.getInstance().getConfig().getDouble("itemPassif.moreDamage.strength"),
                true,
                0.1);
    }

    @Override
    public void doDamage(EntityDamageByEntityEvent e, Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        double mythicBonusDamage = UGItem.isMythic(item) ? 0 : getMythicBonus(UGItem.isMythic(item));

        e.setDamage(e.getFinalDamage() * (1 + getStrength() + mythicBonusDamage));
    }

    @Override
    public void onHit(ProjectileHitEvent e, Player player) {

    }
}
