package be.sixefyle.items;

import be.sixefyle.UnlimitedGrind;
import be.sixefyle.items.passifs.ItemPassif;
import be.sixefyle.items.passifs.Passif;
import be.sixefyle.utils.PlaceholderUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class UGItem {
    private ItemStack item;
    private double power;
    private List<Passif> passifList = new ArrayList<>();

    public UGItem(ItemStack itemStack, double power, int[] passifIDs) {
        this.power = power;
        this.item = itemStack;

        Passif passif;
        for (int id : passifIDs) {
            passif = Passif.getByID(id);
            if(passif != null){
                passifList.add(passif);
            }
        }
    }

    public UGItem(Material itemType, String name, List<String> lore, double power, List<Passif> passifList) {
        this.power = power;
        this.passifList = passifList;

        initItem(itemType, name, lore);
    }

    public void initItem(Material material, String name, List<String> lore){
        if(item == null){
            item = new ItemStack(material);
        }
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.displayName(Component.text(PlaceholderUtils.replace(this, name)));

        List<Component> loreComp = new ArrayList<>();
        if(lore != null){
            for (String s : lore) {
                loreComp.add(Component.text(PlaceholderUtils.replace(this, s)));
            }
            loreComp.add(Component.text(""));
        }

        ItemPassif itemPassif;
        for (Passif passif : passifList) {
            itemPassif = passif.getPassif();
            loreComp.add(Component.text(PlaceholderUtils.replace(itemPassif, passif.getPassif().getDescription())));
        }

        itemMeta.lore(loreComp);

        itemMeta.getPersistentDataContainer().set(
                new NamespacedKey(UnlimitedGrind.getInstance(), "power"), PersistentDataType.DOUBLE, power);

        int[] passifIds = new int[passifList.size()];
        for (int i = 0; i < passifList.size(); i++) {
            passifIds[i] = passifList.get(i).getId();
        }
        itemMeta.getPersistentDataContainer().set(
                new NamespacedKey(UnlimitedGrind.getInstance(), "passifIdArray"), PersistentDataType.INTEGER_ARRAY, passifIds);

        item.setItemMeta(itemMeta);
    }

    public ItemStack getItem() {
        return item;
    }

    public double getPower() {
        return power;
    }

    public List<Passif> getPassifList() {
        return passifList;
    }

    public static UGItem getFromItemStack(ItemStack item){
        ItemMeta itemMeta = item.getItemMeta();
        NamespacedKey powerKey = new NamespacedKey(UnlimitedGrind.getInstance(), "power");
        if(itemMeta.getPersistentDataContainer().has(powerKey)) {
            NamespacedKey passifKey = new NamespacedKey(UnlimitedGrind.getInstance(), "passifIdArray");
            double power = itemMeta.getPersistentDataContainer().get(powerKey, PersistentDataType.DOUBLE);
            int[] passifs = itemMeta.getPersistentDataContainer().get(passifKey, PersistentDataType.INTEGER_ARRAY);

            return new UGItem(item, power, passifs);
        }
        return null;
    }
}
