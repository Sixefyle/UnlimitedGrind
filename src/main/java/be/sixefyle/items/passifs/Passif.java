package be.sixefyle.items.passifs;

public enum Passif {
    DOUBLE_DAMAGE(new DoubleDamagePassif(),1);

    private ItemPassif passif;
    private int id;

    Passif(ItemPassif passif, int id) {
        this.passif = passif;
        this.id = id;
    }

    public ItemPassif getPassif() {
        return passif;
    }

    public int getId() {
        return id;
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
