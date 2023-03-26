package be.sixefyle.items;

public enum ItemAction {
    PICKUP,
    DROP,
    EQUIP,
    HAND,
    OFF_HAND,
    ;

    public static boolean shouldAffectStats(UGItem ugItem, ItemAction action){
        if(ugItem == null) return false;
        ItemCategory itemCategory = ugItem.getItemCategory();
        if(action.equals(PICKUP)){
            return itemCategory.equals(ItemCategory.MELEE_DISTANCE);
        }
        if(action.equals(DROP)){
            return itemCategory.equals(ItemCategory.MELEE_DISTANCE);
        }
        if(action.equals(HAND)){
            return itemCategory.equals(ItemCategory.MELEE_DISTANCE);
        }
        if(action.equals(OFF_HAND)){
            return itemCategory.equals(ItemCategory.SHIELD);
        }
        if (action.equals(EQUIP)) {
            return itemCategory.equals(ItemCategory.ARMOR);
        }

        return false;
    }
}
