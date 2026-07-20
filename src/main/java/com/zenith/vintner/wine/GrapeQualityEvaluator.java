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
        return inspect(level, vinePos).predictedQuality();
    }

    public static VineyardConditionReport inspect(
            Level level,
            BlockPos vinePos
    ) {
        Biome biome = level.getBiome(vinePos).value();

        boolean openSky =
                level.canSeeSky(vinePos.above());

        float temperature = biome.getBaseTemperature();

        boolean suitableTemperature =
                temperature >= 0.5F
                        && temperature <= 1.25F;

        boolean precipitation =
                biome.hasPrecipitation();

        int score = 0;

        if (openSky) {
            score++;
        }

        if (suitableTemperature) {
            score++;
        }

        if (precipitation) {
            score++;
        }

        WineQuality predictedQuality = switch (score) {
            case 3 -> WineQuality.EXCEPTIONAL;
            case 2 -> WineQuality.FINE;
            default -> WineQuality.COMMON;
        };

        return new VineyardConditionReport(
                openSky,
                suitableTemperature,
                precipitation,
                predictedQuality
        );
    }
}
