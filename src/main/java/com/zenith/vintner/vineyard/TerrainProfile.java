package com.zenith.vintner.vineyard;

import net.minecraft.core.Direction;

public record TerrainProfile(
        int elevation,
        SlopeClass slope,
        Direction aspect,
        int sunExposure,
        int waterDistance,
        int windExposure,
        boolean frostPocket,
        boolean terraced,
        int suitability
) {
    public static TerrainProfile evaluate(
            int elevation,
            int heightRange,
            Direction aspect,
            boolean openSky,
            int waterDistance,
            int windExposure,
            boolean frostPocket,
            boolean terraced
    ) {
        SlopeClass slope = SlopeClass.fromRise(heightRange);
        int aspectBonus = slope == SlopeClass.FLAT
                ? 0
                : switch (aspect) {
                    case SOUTH -> 20;
                    case EAST, WEST -> 10;
                    case NORTH -> -10;
                    default -> 0;
                };
        int sunExposure = Math.clamp(
                (openSky ? 70 : 25) + aspectBonus,
                0,
                100
        );
        int slopeFit = switch (slope) {
            case FLAT -> 75;
            case GENTLE -> 100;
            case MODERATE -> 82;
            case STEEP -> 45;
        };
        int waterFit = waterDistance <= 6
                ? 85
                : waterDistance <= 12 ? 70 : 50;
        int windFit = Math.clamp(
                100 - Math.abs(windExposure - 55),
                0,
                100
        );
        int suitability = Math.clamp(
                (sunExposure * 35
                        + slopeFit * 25
                        + waterFit * 15
                        + windFit * 15
                        + (frostPocket ? 20 : 100) * 10) / 100
                        + (terraced ? 5 : 0),
                0,
                100
        );

        return new TerrainProfile(
                elevation,
                slope,
                aspect,
                sunExposure,
                waterDistance,
                windExposure,
                frostPocket,
                terraced,
                suitability
        );
    }

    public TerroirRating sunRating() {
        return TerroirRating.fromValue(sunExposure);
    }

    public TerroirRating windRating() {
        return TerroirRating.fromValue(windExposure);
    }
}
