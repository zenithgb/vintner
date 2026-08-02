package com.zenith.vintner.vineyard;

import com.zenith.vintner.Vintner;
import com.zenith.vintner.compat.sereneseasons.SereneSeasonsIntegration;
import com.zenith.vintner.registry.ModGameRules;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerLevel;

import java.util.concurrent.atomic.AtomicBoolean;

/** Immutable calendar reading derived from the world's clock and Vintner gamerule. */
public record SeasonalContext(
        VineyardSeason season,
        int year,
        int dayInSeason,
        int seasonLengthDays
) {
    public static final long TICKS_PER_DAY = 24_000L;
    private static final AtomicBoolean SERENE_FALLBACK_LOGGED =
            new AtomicBoolean();

    public SeasonalContext {
        seasonLengthDays = Math.clamp(seasonLengthDays, 1, 96);
        year = Math.max(1, year);
        dayInSeason = Math.clamp(dayInSeason, 1, seasonLengthDays);
    }

    public static SeasonalContext current(ServerLevel level) {
        if (FabricLoader.getInstance().isModLoaded("sereneseasons")) {
            try {
                return SereneSeasonsIntegration.current(level);
            } catch (LinkageError | RuntimeException exception) {
                if (SERENE_FALLBACK_LOGGED.compareAndSet(false, true)) {
                    Vintner.LOGGER.warn(
                            "Serene Seasons is present but its season state could not be read; using Vintner's native calendar.",
                            exception
                    );
                }
            }
        }
        return atTick(
                level.getOverworldClockTime(),
                level.getGameRules().get(ModGameRules.SEASON_LENGTH_DAYS)
        );
    }

    public static SeasonalContext atTick(long tick, int seasonLengthDays) {
        return atDay(Math.floorDiv(tick, TICKS_PER_DAY), seasonLengthDays);
    }

    public static SeasonalContext atDay(long day, int seasonLengthDays) {
        int length = Math.clamp(seasonLengthDays, 1, 96);
        long daysPerYear = length * (long) VineyardSeason.values().length;
        long dayInYear = Math.floorMod(day, daysPerYear);
        int seasonIndex = (int) (dayInYear / length);
        int dayInSeason = (int) (dayInYear % length) + 1;
        int year = (int) Math.max(1L, Math.floorDiv(day, daysPerYear) + 1L);
        return new SeasonalContext(
                VineyardSeason.values()[seasonIndex],
                year,
                dayInSeason,
                length
        );
    }

    /**
     * Converts an external season cycle to Vintner's calendar vocabulary.
     * The world's monotonic clock supplies the vintage year because optional
     * season APIs commonly expose only the position within their current cycle.
     */
    public static SeasonalContext fromExternalCycle(
            VineyardSeason season,
            long worldTick,
            int seasonCycleTick,
            int dayDuration,
            int seasonDuration,
            int cycleDuration
    ) {
        int safeDayDuration = Math.max(1, dayDuration);
        int safeSeasonDuration = Math.max(
                safeDayDuration,
                seasonDuration
        );
        long safeCycleDuration = Math.max(
                safeSeasonDuration * (long) VineyardSeason.values().length,
                (long) cycleDuration
        );
        int seasonLengthDays = Math.clamp(
                Math.max(1, safeSeasonDuration / safeDayDuration),
                1,
                96
        );
        int tickWithinSeason = Math.floorMod(
                seasonCycleTick,
                safeSeasonDuration
        );
        int dayInSeason = Math.clamp(
                tickWithinSeason / safeDayDuration + 1,
                1,
                seasonLengthDays
        );
        long elapsedTicks = Math.max(0L, worldTick);
        long elapsedYears = Math.floorDiv(elapsedTicks, safeCycleDuration);
        int year = (int) Math.clamp(
                elapsedYears + 1L,
                1L,
                Integer.MAX_VALUE
        );
        return new SeasonalContext(
                season,
                year,
                dayInSeason,
                seasonLengthDays
        );
    }
}
