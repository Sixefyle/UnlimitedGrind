package be.sixefyle.enums;

public enum Symbols {
    ISLAND("☁"),
    PLAYER("🔥"),
    POWER("⚔"),
    HEALTH("❤"),
    COIN("ↂ"),
    CRYSTALS("⟡"),
    CRITICS("⚡"),
    ENCHANTS("☄"),
    SECONDARY_STATS("◇"),
    PRIMARY_STATS("◆"),
    ARMOR("🛡"),
    DANGER("⚠"),
    ;

    private String symbol;

    Symbols(String symbol) {
        this.symbol = symbol;
    }

    public String get() {
        return symbol;
    }
}
