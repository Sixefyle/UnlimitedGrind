package be.sixefyle.items.passifs;

public abstract class ItemPassif {

    private String description;
    private String name;

    public ItemPassif(String description, String name) {
        this.description = description;
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }
}
