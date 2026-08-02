package com.zenith.vintner.vineyard;

import net.minecraft.network.chat.Component;

/**
 * One concise, prioritized action for improving or tending an inspected vine.
 */
public enum VineyardManagementAdvice {
    PREPARE_SOIL("message.vintner.almanac.advice.prepare_soil"),
    IRRIGATE("message.vintner.almanac.advice.irrigate"),
    PROTECT("message.vintner.almanac.advice.protect"),
    WAIT("message.vintner.almanac.advice.wait"),
    HARVEST("message.vintner.almanac.advice.harvest");

    private final String translationKey;

    VineyardManagementAdvice(String translationKey) {
        this.translationKey = translationKey;
    }

    public Component message() {
        return Component.translatable(translationKey);
    }

    public static VineyardManagementAdvice recommend(
            boolean preparedSoil,
            VineyardWeatherEvent weather,
            boolean protectedCultivation,
            boolean irrigated,
            boolean ripeHarvest
    ) {
        if (!preparedSoil) {
            return PREPARE_SOIL;
        }
        if (weather == VineyardWeatherEvent.DROUGHT && !irrigated) {
            return IRRIGATE;
        }
        if (isShelterWeather(weather) && !protectedCultivation) {
            return PROTECT;
        }
        return ripeHarvest ? HARVEST : WAIT;
    }

    private static boolean isShelterWeather(
            VineyardWeatherEvent weather
    ) {
        return weather == VineyardWeatherEvent.LATE_FROST
                || weather == VineyardWeatherEvent.HEAVY_RAIN
                || weather == VineyardWeatherEvent.HAIL;
    }
}
