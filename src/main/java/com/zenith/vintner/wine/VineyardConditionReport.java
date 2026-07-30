package com.zenith.vintner.wine;

public record VineyardConditionReport(
        boolean openSky,
        boolean suitableTemperature,
        boolean precipitation,
        boolean preparedSoil,
        int qualityScore,
        WineQualityProfile qualityProfile,
        WineQuality predictedQuality
) {
}
