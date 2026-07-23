package com.zenith.vintner.wine;

import com.zenith.vintner.registry.ModBlocks;

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
                level.canSeeSky(vinePos.above(2));

        float temperature = biome.getBaseTemperature();

        boolean suitableTemperature =
                temperature >= 0.5F
                        && temperature <= 1.25F;

        boolean precipitation =
                biome.hasPrecipitation();

        boolean preparedSoil =
                level.getBlockState(vinePos.below())
                        .is(ModBlocks.VINEYARD_SOIL);

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

        if (preparedSoil) {
            score++;
        }

        WineQuality predictedQuality = switch (score) {
            case 4 -> WineQuality.EXCEPTIONAL;
            case 2, 3 -> WineQuality.FINE;
            default -> WineQuality.COMMON;
        };

        return new VineyardConditionReport(
                openSky,
                suitableTemperature,
                precipitation,
                preparedSoil,
                predictedQuality
        );
    }
}
