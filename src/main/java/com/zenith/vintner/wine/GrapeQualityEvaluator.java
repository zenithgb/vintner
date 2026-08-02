package com.zenith.vintner.wine;

import com.zenith.vintner.block.GrapevineBlock;
import com.zenith.vintner.registry.ModBlocks;
import com.zenith.vintner.vineyard.TerroirEvaluator;
import com.zenith.vintner.vineyard.TerroirReport;
import com.zenith.vintner.vineyard.SeasonalContext;
import com.zenith.vintner.vineyard.VineyardWeatherEvent;
import com.zenith.vintner.vineyard.VineyardProtection;
import com.zenith.vintner.vineyard.VineyardIrrigation;
import com.zenith.vintner.vineyard.GrapeVariety;
import com.zenith.vintner.vineyard.VineAgeSavedData;
import com.zenith.vintner.vineyard.VineAgeStage;
import com.zenith.vintner.vineyard.VineManagementSavedData;
import com.zenith.vintner.vineyard.VineYieldMode;
import com.zenith.vintner.vineyard.VineyardThreat;

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
        boolean ripeHarvest = matureVine;
        boolean dryHarvestWeather = !level.isRainingAt(vinePos.above());
        long currentDay = level instanceof ServerLevel serverLevel
                ? Math.floorDiv(
                        serverLevel.getOverworldClockTime(),
                        SeasonalContext.TICKS_PER_DAY
                )
                : 0L;
        long vineAgeDays = level instanceof ServerLevel serverLevel
                ? VineAgeSavedData.get(serverLevel).ageDays(
                        vinePos,
                        currentDay
                )
                : 0L;
        VineAgeStage vineAgeStage = VineAgeStage.atDays(vineAgeDays);
        VineYieldMode yieldMode = level instanceof ServerLevel serverLevel
                ? VineManagementSavedData.get(serverLevel).mode(vinePos)
                : VineYieldMode.BALANCED;
        boolean managedYield = yieldMode != VineYieldMode.HIGH_YIELD;
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
        VineyardThreat threat = VineyardThreat.assess(
                preparedSoil,
                matureVine,
                terroir,
                weatherEvent
        );
        int vineHealthPoints = terroir.siteScore() >= 45
                ? threat.healthPoints()
                : Math.min(2, threat.healthPoints());
        boolean healthyVine = vineHealthPoints == 6;
        int harvestWeatherPoints = weatherEvent.harvestQualityPoints(
                !dryHarvestWeather
        );

        int score = scoreWithTerroirAgeWeatherYieldAndHealth(
                terroir.vineyardQualityPoints(variety),
                vineAgeStage.qualityPoints(),
                vineHealthPoints,
                yieldMode.qualityPoints(),
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
                vineAgeStage,
                vineAgeDays,
                yieldMode,
                threat,
                vineHealthPoints,
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
        return scoreWithTerroirAgeAndWeather(
                sitePoints,
                matureVine ? 6 : 0,
                healthyVine,
                managedYield,
                ripeHarvest,
                harvestWeatherPoints
        );
    }

    public static int scoreWithTerroirAgeAndWeather(
            int sitePoints,
            int vineAgePoints,
            boolean healthyVine,
            boolean managedYield,
            boolean ripeHarvest,
            int harvestWeatherPoints
    ) {
        return scoreWithTerroirAgeWeatherAndYield(
                sitePoints,
                vineAgePoints,
                healthyVine,
                managedYield ? 5 : 0,
                ripeHarvest,
                harvestWeatherPoints
        );
    }

    public static int scoreWithTerroirAgeWeatherAndYield(
            int sitePoints,
            int vineAgePoints,
            boolean healthyVine,
            int yieldQualityPoints,
            boolean ripeHarvest,
            int harvestWeatherPoints
    ) {
        return scoreWithTerroirAgeWeatherYieldAndHealth(
                sitePoints,
                vineAgePoints,
                healthyVine ? 6 : 0,
                yieldQualityPoints,
                ripeHarvest,
                harvestWeatherPoints
        );
    }

    public static int scoreWithTerroirAgeWeatherYieldAndHealth(
            int sitePoints,
            int vineAgePoints,
            int vineHealthPoints,
            int yieldQualityPoints,
            boolean ripeHarvest,
            int harvestWeatherPoints
    ) {
        int score = Math.clamp(sitePoints, 0, 28);
        score += Math.clamp(vineAgePoints, 0, 6);
        score += Math.clamp(vineHealthPoints, 0, 6);
        score += Math.clamp(yieldQualityPoints, 0, 7);
        score += ripeHarvest ? 8 : 0;
        score += Math.clamp(harvestWeatherPoints, 0, 7);
        return Math.min(60, score);
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

}
