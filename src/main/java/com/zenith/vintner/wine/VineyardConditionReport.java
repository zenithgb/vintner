package com.zenith.vintner.wine;

public record VineyardConditionReport(
        boolean openSky,
        boolean suitableTemperature,
        boolean precipitation,
        WineQuality predictedQuality
) {
}
