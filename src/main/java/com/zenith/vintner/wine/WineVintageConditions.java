package com.zenith.vintner.wine;

import com.zenith.vintner.vineyard.SeasonalContext;
import com.zenith.vintner.vineyard.VineyardSeason;
import com.zenith.vintner.vineyard.VineyardWeatherEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Locale;

/**
 * The native seasonal conditions attached to a harvested batch. Unknown
 * conditions keep bottles from older worlds fully compatible.
 */
public record WineVintageConditions(
        VineyardSeason season,
        int year,
        VineyardWeatherEvent weatherEvent,
        boolean protectedCultivation,
        boolean irrigated,
        boolean known
) {
    private static final String VERSION_KEY = "VintnerVintageConditionsVersion";
    private static final String SEASON_KEY = "VintnerHarvestSeason";
    private static final String YEAR_KEY = "VintnerHarvestYear";
    private static final String WEATHER_KEY = "VintnerHarvestWeather";
    private static final String PROTECTED_KEY = "VintnerProtectedHarvest";
    private static final String IRRIGATED_KEY = "VintnerIrrigatedHarvest";

    public WineVintageConditions {
        season = season == null ? VineyardSeason.SPRING : season;
        year = Math.max(1, year);
        weatherEvent = weatherEvent == null
                ? VineyardWeatherEvent.CALM
                : weatherEvent;
    }

    public static WineVintageConditions unknown() {
        return new WineVintageConditions(
                VineyardSeason.SPRING,
                1,
                VineyardWeatherEvent.CALM,
                false,
                false,
                false
        );
    }

    public static WineVintageConditions harvested(
            SeasonalContext context,
            VineyardWeatherEvent weatherEvent,
            boolean protectedCultivation
    ) {
        return harvested(
                context,
                weatherEvent,
                protectedCultivation,
                false
        );
    }

    public static WineVintageConditions harvested(
            SeasonalContext context,
            VineyardWeatherEvent weatherEvent,
            boolean protectedCultivation,
            boolean irrigated
    ) {
        return new WineVintageConditions(
                context.season(),
                context.year(),
                weatherEvent,
                protectedCultivation,
                irrigated,
                true
        );
    }

    void write(CompoundTag tag) {
        if (!known) {
            return;
        }
        tag.putInt(VERSION_KEY, 1);
        tag.putString(SEASON_KEY, season.name().toLowerCase(Locale.ROOT));
        tag.putInt(YEAR_KEY, year);
        tag.putString(
                WEATHER_KEY,
                weatherEvent.name().toLowerCase(Locale.ROOT)
        );
        tag.putBoolean(PROTECTED_KEY, protectedCultivation);
        tag.putBoolean(IRRIGATED_KEY, irrigated);
    }

    static WineVintageConditions read(CompoundTag tag) {
        if (tag.getIntOr(VERSION_KEY, 0) <= 0) {
            return unknown();
        }
        return new WineVintageConditions(
                parseSeason(tag.getStringOr(SEASON_KEY, "spring")),
                tag.getIntOr(YEAR_KEY, 1),
                parseWeather(tag.getStringOr(WEATHER_KEY, "calm")),
                tag.getBooleanOr(PROTECTED_KEY, false),
                tag.getBooleanOr(IRRIGATED_KEY, false),
                true
        );
    }

    void save(ValueOutput output, String prefix) {
        output.putBoolean(prefix + "VintageConditionsKnown", known);
        if (!known) {
            return;
        }
        output.putString(
                prefix + "HarvestSeason",
                season.name().toLowerCase(Locale.ROOT)
        );
        output.putInt(prefix + "HarvestYear", year);
        output.putString(
                prefix + "HarvestWeather",
                weatherEvent.name().toLowerCase(Locale.ROOT)
        );
        output.putBoolean(
                prefix + "ProtectedHarvest",
                protectedCultivation
        );
        output.putBoolean(prefix + "IrrigatedHarvest", irrigated);
    }

    static WineVintageConditions load(ValueInput input, String prefix) {
        if (!input.getBooleanOr(
                prefix + "VintageConditionsKnown",
                false
        )) {
            return unknown();
        }
        return new WineVintageConditions(
                parseSeason(input.getStringOr(
                        prefix + "HarvestSeason",
                        "spring"
                )),
                input.getIntOr(prefix + "HarvestYear", 1),
                parseWeather(input.getStringOr(
                        prefix + "HarvestWeather",
                        "calm"
                )),
                input.getBooleanOr(
                        prefix + "ProtectedHarvest",
                        false
                ),
                input.getBooleanOr(
                        prefix + "IrrigatedHarvest",
                        false
                ),
                true
        );
    }

    private static VineyardSeason parseSeason(String value) {
        try {
            return VineyardSeason.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return VineyardSeason.SPRING;
        }
    }

    private static VineyardWeatherEvent parseWeather(String value) {
        try {
            return VineyardWeatherEvent.valueOf(
                    value.toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            return VineyardWeatherEvent.CALM;
        }
    }
}
