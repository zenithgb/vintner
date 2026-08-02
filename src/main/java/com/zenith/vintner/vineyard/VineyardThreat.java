package com.zenith.vintner.vineyard;

import net.minecraft.network.chat.Component;

/** One prioritized, seasonal vineyard pressure shown through the Almanac. */
public enum VineyardThreat {
    HEALTHY(6),
    NUTRIENT_IMBALANCE(2),
    DROUGHT_STRESS(3),
    FROST_DAMAGE(1),
    HEAT_STRESS(2),
    MILDEW_RISK(3),
    ROT_RISK(1);

    private final int healthPoints;

    VineyardThreat(int healthPoints) {
        this.healthPoints = healthPoints;
    }

    public int healthPoints() {
        return healthPoints;
    }

    public Component displayName() {
        return Component.translatable(
                "vineyard_threat.vintner."
                        + name().toLowerCase(java.util.Locale.ROOT)
        );
    }

    public Component advice() {
        return Component.translatable(
                "message.vintner.almanac.threat_advice."
                        + name().toLowerCase(java.util.Locale.ROOT)
        );
    }

    public static VineyardThreat assess(
            boolean preparedSoil,
            boolean matureVine,
            TerroirReport terroir,
            VineyardWeatherEvent weather
    ) {
        if (!preparedSoil) {
            return NUTRIENT_IMBALANCE;
        }

        return switch (weather) {
            case LATE_FROST -> FROST_DAMAGE;
            case DROUGHT -> DROUGHT_STRESS;
            case HEATWAVE -> HEAT_STRESS;
            case HEAVY_RAIN -> {
                if (matureVine) {
                    yield ROT_RISK;
                }
                if (terroir.climate().humidity() >= 60
                        && terroir.terrain().windExposure() < 45) {
                    yield MILDEW_RISK;
                }
                yield HEALTHY;
            }
            default -> HEALTHY;
        };
    }
}
