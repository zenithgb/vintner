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

    public int vineyardQualityPoints(GrapeVariety variety) {
        int adjustedSiteScore = (
                siteScore * 3 + variety.siteSuitability(this)
        ) / 4;
        return Math.clamp(
                Math.round(adjustedSiteScore * 28.0F / 100.0F),
                0,
                28
        );
    }

    public int vineyardQualityPoints(GrapeCultivar cultivar) {
        int adjustedSiteScore = (
                siteScore * 2 + cultivar.siteSuitability(this)
        ) / 3;
        return Math.clamp(
                Math.round(adjustedSiteScore * 28.0F / 100.0F),
                0,
                28
        );
    }

    public GrapeVariety recommendedVariety() {
        int red = GrapeVariety.RED.siteSuitability(this);
        int white = GrapeVariety.WHITE.siteSuitability(this);
        return red >= white ? GrapeVariety.RED : GrapeVariety.WHITE;
    }

    public TerroirRating siteRating() {
        return TerroirRating.fromValue(siteScore);
    }
}
