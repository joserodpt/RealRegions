package joserodpt.realregions.regions;

public enum RegionOrigin {
    REALREGIONS("&fReal&eRegions"),
    REALMINES("&fReal&9Mines");

    private final String s;

    RegionOrigin(String s) {
        this.s = s;
    }

    public String getDisplayName() {
        return s;
    }
}
