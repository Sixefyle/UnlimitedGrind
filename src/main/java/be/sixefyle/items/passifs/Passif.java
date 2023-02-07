package be.sixefyle.items.passifs;

import be.sixefyle.items.ItemCategory;
import be.sixefyle.items.passifs.armor.DamageReduction;
import be.sixefyle.items.passifs.melee.ExplodePassif;

public enum Passif { //TODO: minPower for getting passif?
    MORE_DAMAGE(1, new MoreDamagePassif(), ItemCategory.MELEE_DISTANCE),
    EXPLOSION(2, new ExplodePassif(), ItemCategory.MELEE),
    DAMAGE_REDUCTION(3, new DamageReduction(), ItemCategory.ARMOR)
    ;

    private ItemPassif passif;
    private int id;
    private ItemCategory itemCategory;

    Passif(int id, ItemPassif passif, ItemCategory itemCategory) {
        this.passif = passif;
        this.id = id;
        this.itemCategory = itemCategory;
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
