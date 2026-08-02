package com.zenith.vintner.vineyard;

import net.minecraft.network.chat.Component;

public enum TerroirRating {
    VERY_LOW("very_low"),
    LOW("low"),
    MODERATE("moderate"),
    HIGH("high"),
    VERY_HIGH("very_high");

    private final String serializedName;

    TerroirRating(String serializedName) {
        this.serializedName = serializedName;
    }

    public Component displayName() {
        return Component.translatable(
                "terroir_rating.vintner." + serializedName
        );
    }

    public static TerroirRating fromValue(int value) {
        int clamped = Math.clamp(value, 0, 100);
        if (clamped < 20) {
            return VERY_LOW;
        }
        if (clamped < 40) {
            return LOW;
        }
        if (clamped < 65) {
            return MODERATE;
        }
        if (clamped < 85) {
            return HIGH;
        }
        return VERY_HIGH;
    }
}
