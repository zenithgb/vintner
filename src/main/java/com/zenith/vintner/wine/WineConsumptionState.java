package com.zenith.vintner.wine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record WineConsumptionState(
        int drinks,
        long recoveryTime
) {
    public static final WineConsumptionState SOBER =
            new WineConsumptionState(0, 0L);

    public static final Codec<WineConsumptionState> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("drinks")
                            .forGetter(WineConsumptionState::drinks),
                    Codec.LONG.fieldOf("recovery_time")
                            .forGetter(WineConsumptionState::recoveryTime)
            ).apply(instance, WineConsumptionState::new));

    public WineConsumptionState activeAt(long gameTime) {
        if (gameTime >= recoveryTime) {
            return SOBER;
        }

        return this;
    }
}
