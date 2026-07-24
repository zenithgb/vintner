package com.zenith.vintner.wine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record WinePairingState(
        int recentMealTypes,
        long mealExpiresAt,
        boolean paired
) {
    public static final WinePairingState EMPTY =
            new WinePairingState(0, 0L, false);

    public static final Codec<WinePairingState> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("recent_meal_types")
                            .forGetter(
                                    WinePairingState::recentMealTypes
                            ),
                    Codec.LONG.fieldOf("meal_expires_at")
                            .forGetter(
                                    WinePairingState::mealExpiresAt
                            ),
                    Codec.BOOL.fieldOf("paired")
                            .forGetter(WinePairingState::paired)
            ).apply(instance, WinePairingState::new));

    public WinePairingState activeAt(long gameTime) {
        if (recentMealTypes == 0 || gameTime < mealExpiresAt) {
            return this;
        }

        return new WinePairingState(0, 0L, paired);
    }

    public WinePairingState rememberMeal(
            int mealTypes,
            long expiresAt
    ) {
        return new WinePairingState(
                mealTypes,
                expiresAt,
                false
        );
    }

    public WinePairingState beginWineServing() {
        return new WinePairingState(
                recentMealTypes,
                mealExpiresAt,
                false
        );
    }

    public WinePairingState markPaired() {
        return new WinePairingState(0, 0L, true);
    }
}
