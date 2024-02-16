package joserodpt.realregions.api.config;

public enum ReplacableVar {

    NAME("%name%"),
    WORLD("%world%"),
    INPUT("%input%");

    private String key;
    private String val;
    ReplacableVar(String key) {
        this.key = key;
    }

    public ReplacableVar eq(String val) {
        this.val = val;
        return this;
    }

    public String getKey() {
        return key;
    }

    public String getVal() {
        return val;
    }
}
