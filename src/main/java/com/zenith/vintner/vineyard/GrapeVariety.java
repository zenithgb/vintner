package com.zenith.vintner.vineyard;

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
}
