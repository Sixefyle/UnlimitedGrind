package be.sixefyle.items.passifs.melee;

import be.sixefyle.UnlimitedGrind;
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
                UnlimitedGrind.getInstance().getConfig().getStringList("itemPassif.lifeConversion.lore"),
                UnlimitedGrind.getInstance().getConfig().getDouble("itemPassif.lifeConversion.strength"),
                true,
                0.015);
        players = new HashMap<>();
    }

    @Override
    public void onEquip(Player player, ItemStack item) {
        if(!players.containsKey(player)){
            players.put(player, new ArrayList<>());
        }
        players.get(player).add(new AttributeModifier(
                attributeName,
                -player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(),
                AttributeModifier.Operation.ADD_NUMBER));
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).addModifier(players.get(player).get(0));

        double bonusDamagePercent = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * getStrength();
        players.get(player).add(new AttributeModifier(
                attributeName,
                bonusDamagePercent,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1));
        player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).addModifier(players.get(player).get(1));
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
    }

    @Override
    public void onUnequip(Player player, ItemStack item) {
        removePassifAttributeModifier(player);
    }

    public void removePassifAttributeModifier(Player player){
        if(players.containsKey(player)){
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).removeModifier(players.get(player).get(0));
            player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).removeModifier(players.get(player).get(1));
            players.remove(player);
        }
    }
}
