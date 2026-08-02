package com.zenith.vintner.vineyard;

public record ClimateProfile(
        ClimateBand band,
        float averageTemperature,
        int rainfall,
        int humidity,
        int frostRisk,
        int heatStress,
        int seasonalVariation,
        int growingSeasonDays,
        int suitability
) {
    public static ClimateProfile evaluate(
            float temperature,
            boolean precipitation,
            boolean nearbyWater,
            int elevation,
            boolean frostPocket
    ) {
        int rainfall = Math.clamp(
                (precipitation ? 68 : 18)
                        + (nearbyWater ? 12 : 0),
                0,
                100
        );
        int humidity = Math.clamp(
                (precipitation ? 62 : 20)
                        + (nearbyWater ? 18 : 0),
                0,
                100
        );
        int frostRisk = Math.clamp(
                Math.round((0.55F - temperature) * 85.0F)
                        + Math.max(0, elevation - 96) / 3
                        + (frostPocket ? 18 : 0),
                0,
                100
        );
        int heatStress = Math.clamp(
                Math.round((temperature - 1.1F) * 90.0F),
                0,
                100
        );
        int seasonalVariation = Math.clamp(
                45
                        + Math.round(
                                Math.abs(temperature - 0.8F) * 22.0F
                        )
                        + Math.max(0, elevation - 80) / 5,
                20,
                90
        );
        int growingSeasonDays = Math.clamp(
                Math.round(72.0F + (temperature - 0.5F) * 55.0F)
                        - frostRisk / 4
                        - heatStress / 6,
                20,
                120
        );

        int temperatureFit = Math.clamp(
                100 - Math.round(
                        Math.abs(temperature - 0.82F) * 105.0F
                ),
                0,
                100
        );
        int rainfallFit = Math.clamp(
                100 - Math.abs(rainfall - 65) * 2,
                0,
                100
        );
        int suitability = Math.clamp(
                (temperatureFit * 35
                        + rainfallFit * 25
                        + (100 - frostRisk) * 15
                        + (100 - heatStress) * 15
                        + Math.min(100, growingSeasonDays) * 10) / 100,
                0,
                100
        );

        return new ClimateProfile(
                ClimateBand.fromTemperature(temperature),
                temperature,
                rainfall,
                humidity,
                frostRisk,
                heatStress,
                seasonalVariation,
                growingSeasonDays,
                suitability
        );
    }

    public TerroirRating rainfallRating() {
        return TerroirRating.fromValue(rainfall);
    }

    public TerroirRating humidityRating() {
        return TerroirRating.fromValue(humidity);
    }

    public TerroirRating frostRiskRating() {
        return TerroirRating.fromValue(frostRisk);
    }

    public TerroirRating heatStressRating() {
        return TerroirRating.fromValue(heatStress);
    }
}
