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

    public String wineRackId() {
        return this == OAK
                ? "wine_rack"
                : id + "_wine_rack";
    }

    public String wineCrateId() {
        return this == OAK
                ? "wine_crate"
                : id + "_wine_crate";
    }

    public String vintageArchiveId() {
        return this == OAK
                ? "vintage_archive"
                : id + "_vintage_archive";
    }

    public String barrelStandId() {
        return this == OAK
                ? "barrel_stand"
                : id + "_barrel_stand";
    }

    public String labelledCellarShelfId() {
        return this == OAK
                ? "labelled_cellar_shelf"
                : id + "_labelled_cellar_shelf";
    }

    public String tastingCabinetId() {
        return this == OAK
                ? "tasting_cabinet"
                : id + "_tasting_cabinet";
    }

    public String estateManagementDeskId() {
        return this == OAK
                ? "estate_management_desk"
                : id + "_estate_management_desk";
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
