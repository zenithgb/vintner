package com.zenith.vintner.wine;

import com.zenith.vintner.vineyard.TerroirReport;
import com.zenith.vintner.vineyard.SeasonalContext;
import com.zenith.vintner.vineyard.VineyardWeatherEvent;
import com.zenith.vintner.vineyard.VineAgeStage;
import com.zenith.vintner.vineyard.VineYieldMode;
import com.zenith.vintner.vineyard.VineRootstock;
import com.zenith.vintner.vineyard.VineyardThreat;
import com.zenith.vintner.vineyard.GrapeCultivar;

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
        VineYieldMode yieldMode,
        VineRootstock rootstock,
        GrapeCultivar cultivar,
        VineyardThreat threat,
        int vineHealthPoints,
        int qualityScore,
        WineQualityProfile qualityProfile,
        WineQuality predictedQuality,
        TerroirReport terroir,
        SeasonalContext seasonalContext,
        VineyardWeatherEvent weatherEvent,
        int harvestWeatherPoints,
        boolean protectedCultivation,
        boolean irrigated,
        boolean netted
) {
}
