package com.zenith.vintner.vineyard;

import net.minecraft.network.chat.Component;

public enum GrapeVariety {
    RED(
            6,
            3,
            4
    ),
    WHITE(
            4,
            2,
            3
    );

    private final int growthChanceDenominator;
    private final int minimumHarvest;
    private final int maximumHarvest;

    GrapeVariety(
            int growthChanceDenominator,
            int minimumHarvest,
            int maximumHarvest
    ) {
        this.growthChanceDenominator = growthChanceDenominator;
        this.minimumHarvest = minimumHarvest;
        this.maximumHarvest = maximumHarvest;
    }

    public int growthChanceDenominator() {
        return growthChanceDenominator;
    }

    public int minimumHarvest() {
        return minimumHarvest;
    }

    public int maximumHarvest() {
        return maximumHarvest;
    }

    public Component displayName() {
        return Component.translatable(
                "grape_variety.vintner."
                        + name().toLowerCase(java.util.Locale.ROOT)
        );
    }

    /**
     * A readable variety fit based on the same climate, soil, and terrain
     * measurements already exposed by the Almanac.
     */
    public int siteSuitability(TerroirReport report) {
        float preferredTemperature = this == RED ? 0.95F : 0.68F;
        int temperatureFit = Math.clamp(
                100 - Math.round(
                        Math.abs(
                                report.climate().averageTemperature()
                                        - preferredTemperature
                        ) * 120.0F
                ),
                0,
                100
        );
        int soilFit = this == RED
                ? average(
                        report.soil().drainage(),
                        report.soil().heatRetention(),
                        report.soil().mineralCharacter()
                )
                : average(
                        report.soil().drainage(),
                        report.soil().waterRetention(),
                        report.soil().rootDepth()
                );
        int terrainFit = this == RED
                ? average(
                        report.terrain().sunExposure(),
                        report.terrain().sunExposure(),
                        100 - report.climate().frostRisk()
                )
                : average(
                        report.terrain().sunExposure(),
                        100 - report.climate().heatStress(),
                        100 - report.terrain().windExposure()
                );

        return Math.clamp(
                (temperatureFit * 45 + soilFit * 30 + terrainFit * 25)
                        / 100,
                0,
                100
        );
    }

    private static int average(int first, int second, int third) {
        return (first + second + third) / 3;
    }
}
