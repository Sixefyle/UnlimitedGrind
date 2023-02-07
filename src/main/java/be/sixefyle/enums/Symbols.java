package be.sixefyle.enums;

public enum Symbols {
    ISLAND("☁"),
    PLAYER("🔥"),
    POWER("⚔"),
    HEALTH("❤"),
    COIN("\uD83D\uDCB5"),
    CRYSTALS("⟡"),
    CRITICS("⚡"),
    ;

    private String symbol;

    Symbols(String symbol) {
        this.symbol = symbol;
    }

    public String get() {
        return symbol;
    }
}
