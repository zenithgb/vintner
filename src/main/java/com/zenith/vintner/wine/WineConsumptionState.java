package com.zenith.vintner.wine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record WineConsumptionState(
        int drinks,
        long recoveryTime,
        int servingUnits
) {
    public static final WineConsumptionState SOBER =
            new WineConsumptionState(0, 0L, 0);

    public WineConsumptionState(int drinks, long recoveryTime) {
        this(drinks, recoveryTime, Math.max(0, drinks) * 4);
    }

    public static final Codec<WineConsumptionState> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("drinks")
                            .forGetter(WineConsumptionState::drinks),
                    Codec.LONG.fieldOf("recovery_time")
                            .forGetter(WineConsumptionState::recoveryTime),
                    Codec.INT.optionalFieldOf("serving_units", -1)
                            .forGetter(WineConsumptionState::servingUnits)
            ).apply(instance, WineConsumptionState::new));

    public int effectiveServingUnits() {
        return servingUnits >= 0
                ? servingUnits
                : Math.max(0, drinks) * 4;
    }

    public WineConsumptionState activeAt(long gameTime) {
        if (gameTime >= recoveryTime) {
            return SOBER;
        }

        return this;
    }
}
