package com.zenith.vintner.wine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public final class GrapeQualityEvaluator {
    private GrapeQualityEvaluator() {
    }

    public static WineQuality evaluate(
            Level level,
            BlockPos vinePos
    ) {
        Biome biome = level.getBiome(vinePos).value();

        int score = 0;

        if (level.canSeeSky(vinePos.above())) {
            score++;
        }

        float temperature = biome.getBaseTemperature();

        if (temperature >= 0.5F && temperature <= 1.25F) {
            score++;
        }

        if (biome.hasPrecipitation()) {
            score++;
        }

        return switch (score) {
            case 3 -> WineQuality.EXCEPTIONAL;
            case 2 -> WineQuality.FINE;
            default -> WineQuality.COMMON;
        };
    }
}
