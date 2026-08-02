package com.zenith.vintner.vineyard;

import net.minecraft.network.chat.Component;

public enum SlopeClass {
    FLAT("flat"),
    GENTLE("gentle"),
    MODERATE("moderate"),
    STEEP("steep");

    private final String serializedName;

    SlopeClass(String serializedName) {
        this.serializedName = serializedName;
    }

    public Component displayName() {
        return Component.translatable(
                "slope_class.vintner." + serializedName
        );
    }

    public static SlopeClass fromRise(int rise) {
        if (rise <= 0) {
            return FLAT;
        }
        if (rise <= 2) {
            return GENTLE;
        }
        if (rise <= 4) {
            return MODERATE;
        }
        return STEEP;
    }
}
