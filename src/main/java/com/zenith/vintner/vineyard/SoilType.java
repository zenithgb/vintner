package com.zenith.vintner.vineyard;

import net.minecraft.network.chat.Component;

public enum SoilType {
    CLAY("clay", 35, 75, 90, 50, 40, 55),
    LIMESTONE("limestone", 65, 50, 45, 60, 65, 90),
    CHALK("chalk", 80, 35, 35, 55, 50, 85),
    GRAVEL("gravel", 95, 30, 20, 80, 75, 55),
    SAND("sand", 90, 25, 15, 85, 65, 30),
    LOAM("loam", 70, 85, 70, 60, 85, 50),
    VOLCANIC("volcanic", 75, 80, 55, 85, 70, 95),
    ALLUVIAL("alluvial", 55, 95, 90, 55, 90, 75);

    private final String serializedName;
    private final int drainage;
    private final int fertility;
    private final int waterRetention;
    private final int heatRetention;
    private final int rootDepth;
    private final int mineralCharacter;

    SoilType(
            String serializedName,
            int drainage,
            int fertility,
            int waterRetention,
            int heatRetention,
            int rootDepth,
            int mineralCharacter
    ) {
        this.serializedName = serializedName;
        this.drainage = drainage;
        this.fertility = fertility;
        this.waterRetention = waterRetention;
        this.heatRetention = heatRetention;
        this.rootDepth = rootDepth;
        this.mineralCharacter = mineralCharacter;
    }

    public Component displayName() {
        return Component.translatable(
                "soil_type.vintner." + serializedName
        );
    }

    public int drainage() {
        return drainage;
    }

    public int fertility() {
        return fertility;
    }

    public int waterRetention() {
        return waterRetention;
    }

    public int heatRetention() {
        return heatRetention;
    }

    public int rootDepth() {
        return rootDepth;
    }

    public int mineralCharacter() {
        return mineralCharacter;
    }
}
