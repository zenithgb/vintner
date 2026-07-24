package com.zenith.vintner.block;

import net.minecraft.util.StringRepresentable;

public enum WoodVariant implements StringRepresentable {
    OAK("oak"),
    SPRUCE("spruce"),
    BIRCH("birch"),
    JUNGLE("jungle"),
    ACACIA("acacia"),
    DARK_OAK("dark_oak"),
    MANGROVE("mangrove"),
    CHERRY("cherry"),
    PALE_OAK("pale_oak"),
    BAMBOO("bamboo"),
    CRIMSON("crimson"),
    WARPED("warped");

    private final String id;

    WoodVariant(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String trellisId() {
        return id + "_trellis";
    }

    public String grapePressId() {
        return this == OAK
                ? "grape_press"
                : id + "_grape_press";
    }

    public String fermentationBarrelId() {
        return this == OAK
                ? "fermentation_barrel"
                : id + "_fermentation_barrel";
    }

    public String agingBarrelId() {
        return this == DARK_OAK
                ? "aging_barrel"
                : id + "_aging_barrel";
    }

    public String grapevineId(boolean red) {
        if (this == OAK) {
            return red ? "red_grapevine" : "white_grapevine";
        }

        return id + (red
                ? "_red_grapevine"
                : "_white_grapevine");
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}
