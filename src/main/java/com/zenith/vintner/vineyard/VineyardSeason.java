package com.zenith.vintner.vineyard;

import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;

/** Four readable vineyard seasons used when no compatible season mod supplies one. */
public enum VineyardSeason {
    SPRING("season.vintner.spring"),
    SUMMER("season.vintner.summer"),
    AUTUMN("season.vintner.autumn"),
    WINTER("season.vintner.winter");

    private final String translationKey;

    VineyardSeason(String translationKey) {
        this.translationKey = translationKey;
    }

    public Component displayName() {
        return Component.translatable(translationKey);
    }

    public boolean shouldGrow(RandomSource random, int baseDenominator) {
        if (this == WINTER) {
            return false;
        }
        int denominator = switch (this) {
            case SPRING -> Math.max(1, baseDenominator * 3 / 4);
            case SUMMER -> baseDenominator;
            case AUTUMN -> Math.max(1, baseDenominator * 3 / 2);
            case WINTER -> Integer.MAX_VALUE;
        };
        return random.nextInt(denominator) == 0;
    }
}
