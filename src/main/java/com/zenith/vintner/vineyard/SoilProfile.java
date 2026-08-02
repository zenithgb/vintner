package com.zenith.vintner.vineyard;

public record SoilProfile(
        SoilType type,
        int drainage,
        int fertility,
        int waterRetention,
        int heatRetention,
        int rootDepth,
        int mineralCharacter,
        int suitability
) {
    public static SoilProfile of(SoilType type) {
        int suitability = (
                balanced(type.drainage(), 70, 55)
                        + balanced(type.fertility(), 70, 50)
                        + balanced(type.waterRetention(), 60, 55)
                        + balanced(type.heatRetention(), 65, 60)
                        + type.rootDepth()
                        + type.mineralCharacter()
        ) / 6;

        return new SoilProfile(
                type,
                type.drainage(),
                type.fertility(),
                type.waterRetention(),
                type.heatRetention(),
                type.rootDepth(),
                type.mineralCharacter(),
                Math.clamp(suitability, 0, 100)
        );
    }

    public TerroirRating drainageRating() {
        return TerroirRating.fromValue(drainage);
    }

    public TerroirRating fertilityRating() {
        return TerroirRating.fromValue(fertility);
    }

    public TerroirRating waterRetentionRating() {
        return TerroirRating.fromValue(waterRetention);
    }

    public TerroirRating heatRetentionRating() {
        return TerroirRating.fromValue(heatRetention);
    }

    public TerroirRating rootDepthRating() {
        return TerroirRating.fromValue(rootDepth);
    }

    public TerroirRating mineralRating() {
        return TerroirRating.fromValue(mineralCharacter);
    }

    private static int balanced(
            int value,
            int ideal,
            int tolerance
    ) {
        return Math.clamp(
                100 - Math.abs(value - ideal) * 100 / tolerance,
                0,
                100
        );
    }
}
