package be.sixefyle.items;

public enum ItemAction {
    PICKUP,
    DROP,
    EQUIP,
    HOLD,
    HOLD_OFF_HAND,
    ;

    public static boolean shouldAffectStats(UGItem ugItem, ItemAction action){
        if(ugItem == null) return false;
        ItemCategory itemCategory = ugItem.getItemCategories();
        if(action.equals(PICKUP)){
            return itemCategory.equals(ItemCategory.MELEE) || itemCategory.equals(ItemCategory.DISTANCE);
        }
        if(action.equals(DROP)){
            return itemCategory.equals(ItemCategory.MELEE) || itemCategory.equals(ItemCategory.DISTANCE);
        }
        if(action.equals(HOLD)){
            return itemCategory.equals(ItemCategory.MELEE) || itemCategory.equals(ItemCategory.DISTANCE);
        }
        if(action.equals(HOLD_OFF_HAND)){
            return itemCategory.equals(ItemCategory.SHIELD);
        }
        if (action.equals(EQUIP)) {
            return itemCategory.equals(ItemCategory.ARMOR);
        }

        return false;
    }
}
