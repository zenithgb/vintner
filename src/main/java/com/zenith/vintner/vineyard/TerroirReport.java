package com.zenith.vintner.vineyard;

public record TerroirReport(
        ClimateProfile climate,
        SoilProfile soil,
        TerrainProfile terrain,
        int siteScore
) {
    public TerroirReport {
        siteScore = Math.clamp(siteScore, 0, 100);
    }

    public int vineyardQualityPoints() {
        return Math.clamp(
                Math.round(siteScore * 28.0F / 100.0F),
                0,
                28
        );
    }

    public TerroirRating siteRating() {
        return TerroirRating.fromValue(siteScore);
    }
}
