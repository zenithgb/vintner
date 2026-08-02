package com.zenith.vintner.wine;

import com.zenith.vintner.block.GrapevineBlock;
import com.zenith.vintner.block.TrellisBlock;
import com.zenith.vintner.registry.ModBlocks;
import com.zenith.vintner.vineyard.TerroirEvaluator;
import com.zenith.vintner.vineyard.TerroirReport;
import com.zenith.vintner.vineyard.SeasonalContext;
import com.zenith.vintner.vineyard.VineyardWeatherEvent;
import com.zenith.vintner.vineyard.VineyardProtection;
import com.zenith.vintner.vineyard.VineyardIrrigation;
import com.zenith.vintner.vineyard.GrapeVariety;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
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
        TerroirReport terroir = TerroirEvaluator.inspect(
                level,
                vinePos
        );
        boolean openSky = terroir.terrain().sunExposure() >= 50;
        boolean suitableTemperature =
                terroir.climate().suitability() >= 45;
        boolean precipitation =
                terroir.climate().rainfall() >= 40;

        boolean preparedSoil =
                level.getBlockState(vinePos.below())
                        .is(ModBlocks.VINEYARD_SOIL);

        BlockState vineState = level.getBlockState(vinePos);
        GrapeVariety variety = vineState.getBlock()
                instanceof GrapevineBlock grapevine
                ? grapevine.getVariety()
                : GrapeVariety.RED;
        int vineAge = vineState.hasProperty(GrapevineBlock.AGE)
                ? vineState.getValue(GrapevineBlock.AGE)
                : 0;
        boolean matureVine = vineAge >= GrapevineBlock.MAX_AGE;
        boolean healthyVine = preparedSoil
                && terroir.siteScore() >= 45;
        boolean managedYield = connectionCount(vineState) <= 2;
        boolean ripeHarvest = matureVine;
        boolean dryHarvestWeather = !level.isRainingAt(vinePos.above());
        SeasonalContext seasonalContext = level instanceof ServerLevel serverLevel
                ? SeasonalContext.current(serverLevel)
                : SeasonalContext.atDay(0, 8);
        boolean protectedCultivation = VineyardProtection.isProtected(
                level,
                vinePos
        );
        boolean irrigated = VineyardIrrigation.isIrrigated(
                level,
                vinePos
        );
        VineyardWeatherEvent weatherEvent = level instanceof ServerLevel serverLevel
                ? VineyardWeatherEvent.at(
                        serverLevel,
                        vinePos,
                        terroir.climate(),
                        seasonalContext
                )
                : VineyardWeatherEvent.CALM;
        weatherEvent = weatherEvent.mitigatedBy(
                protectedCultivation,
                irrigated
        );
        int harvestWeatherPoints = weatherEvent.harvestQualityPoints(
                !dryHarvestWeather
        );

        int score = scoreWithTerroirAndWeather(
                terroir.vineyardQualityPoints(variety),
                matureVine,
                healthyVine,
                managedYield,
                ripeHarvest,
                harvestWeatherPoints
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
                predictedQuality,
                terroir,
                seasonalContext,
                weatherEvent,
                harvestWeatherPoints,
                protectedCultivation,
                irrigated
        );
    }

    public static int scoreWithTerroirAndWeather(
            int sitePoints,
            boolean matureVine,
            boolean healthyVine,
            boolean managedYield,
            boolean ripeHarvest,
            int harvestWeatherPoints
    ) {
        int score = Math.clamp(sitePoints, 0, 28);
        score += matureVine ? 6 : 0;
        score += healthyVine ? 6 : 0;
        score += managedYield ? 5 : 0;
        score += ripeHarvest ? 8 : 0;
        score += Math.clamp(harvestWeatherPoints, 0, 7);
        return score;
    }

    public static int scoreWithTerroir(
            int sitePoints,
            boolean matureVine,
            boolean healthyVine,
            boolean managedYield,
            boolean ripeHarvest,
            boolean dryHarvestWeather
    ) {
        int score = Math.clamp(sitePoints, 0, 28);
        score += matureVine ? 6 : 0;
        score += healthyVine ? 6 : 0;
        score += managedYield ? 5 : 0;
        score += ripeHarvest ? 8 : 0;
        score += dryHarvestWeather ? 7 : 0;
        return score;
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
