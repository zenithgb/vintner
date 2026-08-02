package com.zenith.vintner.vineyard;

import net.minecraft.network.chat.Component;

public enum ClimateBand {
    COLD("cold"),
    COOL("cool"),
    TEMPERATE("temperate"),
    WARM("warm"),
    HOT("hot");

    private final String serializedName;

    ClimateBand(String serializedName) {
        this.serializedName = serializedName;
    }

    public Component displayName() {
        return Component.translatable(
                "climate_band.vintner." + serializedName
        );
    }

    public static ClimateBand fromTemperature(float temperature) {
        if (temperature < 0.25F) {
            return COLD;
        }
        if (temperature < 0.55F) {
            return COOL;
        }
        if (temperature <= 1.1F) {
            return TEMPERATE;
        }
        if (temperature <= 1.5F) {
            return WARM;
        }
        return HOT;
    }
}
