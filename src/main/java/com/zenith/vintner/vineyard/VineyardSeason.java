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
        return shouldGrow(random, baseDenominator, false);
    }

    public boolean shouldGrow(
            RandomSource random,
            int baseDenominator,
            boolean protectedCultivation
    ) {
        if (this == WINTER && !protectedCultivation) {
            return false;
        }
        int denominator = growthChanceDenominator(
                baseDenominator,
                protectedCultivation
        );
        return denominator > 0 && random.nextInt(denominator) == 0;
    }

    public int growthChanceDenominator(
            int baseDenominator,
            boolean protectedCultivation
    ) {
        if (this == WINTER && !protectedCultivation) {
            return 0;
        }
        return switch (this) {
            case SPRING -> Math.max(1, baseDenominator * 3 / 4);
            case SUMMER -> baseDenominator;
            case AUTUMN -> Math.max(1, baseDenominator * 3 / 2);
            case WINTER -> Math.max(1, baseDenominator * 2);
        };
    }
}
