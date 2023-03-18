package be.sixefyle.items.passifs.melee;

import be.sixefyle.UGPlayer;
import be.sixefyle.UnlimitedGrind;
import be.sixefyle.items.UGItem;
import be.sixefyle.items.passifs.ItemPassif;
import be.sixefyle.items.passifs.interfaces.OnEquip;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LifeConversion extends ItemPassif implements OnEquip {
    private HashMap<Player, List<AttributeModifier>> players;
    private String attributeName = "life_conversion";
    public LifeConversion() {
        super(UnlimitedGrind.getInstance().getConfig().getString("itemPassif.lifeConversion.name"),
                UnlimitedGrind.getInstance().getConfig().getString("itemPassif.lifeConversion.itemPrefixName"),
                UnlimitedGrind.getInstance().getConfig().getStringList("itemPassif.lifeConversion.description"),
                UnlimitedGrind.getInstance().getConfig().getStringList("itemPassif.lifeConversion.lore"),
                UnlimitedGrind.getInstance().getConfig().getDouble("itemPassif.lifeConversion.strength"),
                true,
                1);
        players = new HashMap<>();
    }

    @Override
    public void onEquip(Player player, ItemStack item) {
        if(!players.containsKey(player)){
            players.put(player, new ArrayList<>());
        }

        UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
        ugPlayer.setMaxHealth(1);
        ugPlayer.setHealthLocked(true);

        double bonusDamagePercent = getStrength() + (UGItem.isMythic(item) ? getMythicBonus() : 0);
        players.get(player).add(new AttributeModifier(
                attributeName,
                bonusDamagePercent,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1));
        player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).addModifier(players.get(player).get(0));
    }

    @Override
    public void onUnequip(Player player, ItemStack item) {
        removePassifAttributeModifier(player);
        UGPlayer ugPlayer = UGPlayer.GetUGPlayer(player);
        ugPlayer.setHealthLocked(false);
        ugPlayer.setHealthFromStat();
    }

    public void removePassifAttributeModifier(Player player){
        if(players.containsKey(player)){
            player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).removeModifier(players.get(player).get(0));
            players.remove(player);
        }
    }
}
