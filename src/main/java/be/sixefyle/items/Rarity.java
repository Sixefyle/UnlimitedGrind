package be.sixefyle.items;

import net.md_5.bungee.api.ChatColor;

public enum Rarity {
    COMMON(ChatColor.WHITE, "common"),
    UNCOMMON(ChatColor.GREEN, "uncommon"),
    MAGIC(ChatColor.BLUE, "magic"),
    RARE(ChatColor.YELLOW, "rare"),
    LEGENDARY(ChatColor.GOLD, "legendary"),
    MYTHIC(ChatColor.AQUA, "mythic"),
    ;

    ChatColor color;
    String id;

    Rarity(ChatColor color, String id) {
        this.color = color;
        this.id = id;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getId() {
        return id;
    }
}
