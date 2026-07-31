package com.zenith.vintner.wine;

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
        int qualityScore,
        WineQualityProfile qualityProfile,
        WineQuality predictedQuality
) {
}
