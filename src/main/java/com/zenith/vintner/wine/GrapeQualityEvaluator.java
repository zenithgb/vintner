package com.zenith.vintner.wine;

import com.zenith.vintner.block.GrapevineBlock;
import com.zenith.vintner.block.TrellisBlock;
import com.zenith.vintner.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

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

        BlockState vineState = level.getBlockState(vinePos);
        int vineAge = vineState.hasProperty(GrapevineBlock.AGE)
                ? vineState.getValue(GrapevineBlock.AGE)
                : 0;
        boolean matureVine = vineAge >= GrapevineBlock.MAX_AGE;
        boolean healthyVine = preparedSoil
                && suitableTemperature
                && precipitation;
        boolean managedYield = connectionCount(vineState) <= 2;
        boolean ripeHarvest = matureVine;
        boolean dryHarvestWeather = !level.isRainingAt(vinePos.above());

        int score = score(
                openSky,
                suitableTemperature,
                precipitation,
                preparedSoil,
                matureVine,
                healthyVine,
                managedYield,
                ripeHarvest,
                dryHarvestWeather
        );

        WineQualityProfile profile =
                WineQualityProfile.vineyard(score);
        WineQuality predictedQuality = profile.quality();

        return new VineyardConditionReport(
                openSky,
                suitableTemperature,
                precipitation,
                preparedSoil,
                matureVine,
                healthyVine,
                managedYield,
                ripeHarvest,
                dryHarvestWeather,
                score,
                profile,
                predictedQuality
        );
    }

    public static int score(
            boolean openSky,
            boolean suitableTemperature,
            boolean precipitation,
            boolean preparedSoil,
            boolean matureVine,
            boolean healthyVine,
            boolean managedYield,
            boolean ripeHarvest,
            boolean dryHarvestWeather
    ) {
        int score = 0;
        score += openSky ? 8 : 0;
        score += suitableTemperature ? 8 : 0;
        score += precipitation ? 4 : 0;
        score += preparedSoil ? 8 : 0;
        score += matureVine ? 6 : 0;
        score += healthyVine ? 6 : 0;
        score += managedYield ? 5 : 0;
        score += ripeHarvest ? 8 : 0;
        score += dryHarvestWeather ? 7 : 0;
        return score;
    }

    private static int connectionCount(BlockState state) {
        if (!(state.getBlock() instanceof TrellisBlock)) {
            return 0;
        }
        int count = 0;
        if (state.getValue(TrellisBlock.NORTH)
                == TrellisBlock.RowConnection.LEVEL) {
            count++;
        }
        if (state.getValue(TrellisBlock.EAST)
                == TrellisBlock.RowConnection.LEVEL) {
            count++;
        }
        if (state.getValue(TrellisBlock.SOUTH)
                == TrellisBlock.RowConnection.LEVEL) {
            count++;
        }
        if (state.getValue(TrellisBlock.WEST)
                == TrellisBlock.RowConnection.LEVEL) {
            count++;
        }
        return count;
    }
}
