package com.zenith.vintner.vineyard;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

/**
 * A stable seasonal outlook for a vineyard region. Events affect outcomes and
 * growth pressure but never remove blocks or erase a vineyard.
 */
public enum VineyardWeatherEvent {
    CALM("weather_event.vintner.calm", 5, 100),
    IDEAL_SEASON("weather_event.vintner.ideal_season", 7, 75),
    COOL_RIPENING("weather_event.vintner.cool_ripening", 7, 110),
    LATE_FROST("weather_event.vintner.late_frost", 2, 180),
    HEATWAVE("weather_event.vintner.heatwave", 3, 145),
    HEAVY_RAIN("weather_event.vintner.heavy_rain", 1, 140),
    DROUGHT("weather_event.vintner.drought", 1, 155),
    HAIL("weather_event.vintner.hail", 0, 175);

    private final String translationKey;
    private final int harvestQualityPoints;
    private final int growthDenominatorPercent;

    VineyardWeatherEvent(
            String translationKey,
            int harvestQualityPoints,
            int growthDenominatorPercent
    ) {
        this.translationKey = translationKey;
        this.harvestQualityPoints = harvestQualityPoints;
        this.growthDenominatorPercent = growthDenominatorPercent;
    }

    public Component displayName() {
        return Component.translatable(translationKey);
    }

    public int harvestQualityPoints(boolean currentlyRaining) {
        if (this == CALM && currentlyRaining) {
            return 2;
        }
        return harvestQualityPoints;
    }

    public int adjustGrowthDenominator(int denominator) {
        return Math.max(1, denominator * growthDenominatorPercent / 100);
    }

    public VineyardWeatherEvent mitigatedBy(boolean protectedCultivation) {
        return mitigatedBy(protectedCultivation, false);
    }

    public VineyardWeatherEvent mitigatedBy(
            boolean protectedCultivation,
            boolean irrigated
    ) {
        if (irrigated && this == DROUGHT) {
            return CALM;
        }
        if (!protectedCultivation) {
            return this;
        }
        return switch (this) {
            case LATE_FROST, HEAVY_RAIN, HAIL -> CALM;
            default -> this;
        };
    }

    public static VineyardWeatherEvent at(
            ServerLevel level,
            BlockPos pos,
            ClimateProfile climate,
            SeasonalContext context
    ) {
        return forSite(
                level.getSeed(),
                pos.getX() >> 7,
                pos.getZ() >> 7,
                climate,
                context
        );
    }

    public static VineyardWeatherEvent forSite(
            long seed,
            int regionX,
            int regionZ,
            ClimateProfile climate,
            SeasonalContext context
    ) {
        long mixed = seed;
        mixed ^= regionX * 0x9E3779B97F4A7C15L;
        mixed ^= regionZ * 0xC2B2AE3D27D4EB4FL;
        mixed ^= context.year() * 0x165667B19E3779F9L;
        mixed ^= context.season().ordinal() * 0x85EBCA77C2B2AE63L;
        mixed ^= mixed >>> 33;
        mixed *= 0xff51afd7ed558ccdL;
        mixed ^= mixed >>> 33;
        int roll = (int) Math.floorMod(mixed, 100L);

        return switch (context.season()) {
            case SPRING -> springEvent(roll, climate);
            case SUMMER -> summerEvent(roll, climate);
            case AUTUMN -> autumnEvent(roll, climate);
            case WINTER -> CALM;
        };
    }

    private static VineyardWeatherEvent springEvent(
            int roll,
            ClimateProfile climate
    ) {
        int frostChance = Math.clamp(climate.frostRisk() / 4, 2, 22);
        if (roll < frostChance) {
            return LATE_FROST;
        }
        if (roll < frostChance + Math.clamp(climate.rainfall() / 8, 3, 12)) {
            return HEAVY_RAIN;
        }
        return roll >= 84 ? IDEAL_SEASON : CALM;
    }

    private static VineyardWeatherEvent summerEvent(
            int roll,
            ClimateProfile climate
    ) {
        int heatChance = Math.clamp(climate.heatStress() / 4, 2, 20);
        if (roll < heatChance) {
            return HEATWAVE;
        }
        int droughtChance = Math.clamp((55 - climate.rainfall()) / 4, 0, 12);
        if (roll < heatChance + droughtChance) {
            return DROUGHT;
        }
        if (roll >= 96) {
            return HAIL;
        }
        return roll >= 82 ? IDEAL_SEASON : CALM;
    }

    private static VineyardWeatherEvent autumnEvent(
            int roll,
            ClimateProfile climate
    ) {
        int rainChance = Math.clamp(climate.rainfall() / 7, 3, 14);
        if (roll < rainChance) {
            return HEAVY_RAIN;
        }
        if (roll >= 82 && climate.averageTemperature() < 0.95F) {
            return COOL_RIPENING;
        }
        return roll >= 88 ? IDEAL_SEASON : CALM;
    }
}
