package com.zenith.vintner.wine;

import com.zenith.vintner.vineyard.TerroirReport;
import com.zenith.vintner.vineyard.SeasonalContext;
import com.zenith.vintner.vineyard.VineyardWeatherEvent;
import com.zenith.vintner.vineyard.VineAgeStage;

public record VineyardConditionReport(
        boolean openSky,
        boolean suitableTemperature,
        boolean precipitation,
        boolean preparedSoil,
        boolean matureVine,
        boolean healthyVine,
        boolean managedYield,
        boolean ripeHarvest,
        boolean dryHarvestWeather,
        VineAgeStage vineAgeStage,
        long vineAgeDays,
        int qualityScore,
        WineQualityProfile qualityProfile,
        WineQuality predictedQuality,
        TerroirReport terroir,
        SeasonalContext seasonalContext,
        VineyardWeatherEvent weatherEvent,
        int harvestWeatherPoints,
        boolean protectedCultivation,
        boolean irrigated
) {
}
