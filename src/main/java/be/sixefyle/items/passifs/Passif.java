package be.sixefyle.items.passifs;

import be.sixefyle.items.ItemCategory;
import be.sixefyle.items.Rarity;
import be.sixefyle.items.passifs.armor.DamageReductionPassif;
import be.sixefyle.items.passifs.armor.ThunderStormPassif;
import be.sixefyle.items.passifs.melee.ExplodePassif;
import be.sixefyle.items.passifs.melee.LethalBlock;

public enum Passif {
    MORE_DAMAGE(1, new MoreDamagePassif(), ItemCategory.MELEE_DISTANCE),
    EXPLOSION(2, new ExplodePassif(), ItemCategory.MELEE),
    DAMAGE_REDUCTION(3, new DamageReductionPassif(), ItemCategory.ARMOR),
    THUNDER_STORM(4, new ThunderStormPassif(), ItemCategory.ARMOR, Rarity.MYTHIC),
    LETHAL_BLOCK(5, new LethalBlock(), ItemCategory.SHIELD),
    ;

    private ItemPassif passif;
    private int id;
    private ItemCategory itemCategory;
    private Rarity requiredRarity;

    Passif(int id, ItemPassif passif, ItemCategory itemCategory) {
        this.passif = passif;
        this.id = id;
        this.itemCategory = itemCategory;
    }

    Passif(int id, ItemPassif passif, ItemCategory itemCategory, Rarity requiredRarity) {
        this.passif = passif;
        this.id = id;
        this.itemCategory = itemCategory;
        this.requiredRarity = requiredRarity;
    }

    public Rarity getRequiredRarity() {
        return requiredRarity;
    }

    public ItemPassif getItemPassif() {
        return passif;
    }

    public int getId() {
        return id;
    }

    public ItemPassif getPassif() {
        return passif;
    }

    public ItemCategory getItemCategory() {
        return itemCategory;
    }

    public static Passif getByID(int id){
        for (Passif value : Passif.values()) {
            if(value.getId() == id){
                return value;
            }
        }
        return null;
    }
}
