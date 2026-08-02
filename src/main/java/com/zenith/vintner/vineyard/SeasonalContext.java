package com.zenith.vintner.vineyard;

import com.zenith.vintner.registry.ModGameRules;
import net.minecraft.server.level.ServerLevel;

/** Immutable calendar reading derived from the world's clock and Vintner gamerule. */
public record SeasonalContext(
        VineyardSeason season,
        int year,
        int dayInSeason,
        int seasonLengthDays
) {
    public static final long TICKS_PER_DAY = 24_000L;

    public SeasonalContext {
        seasonLengthDays = Math.clamp(seasonLengthDays, 1, 96);
        year = Math.max(1, year);
        dayInSeason = Math.clamp(dayInSeason, 1, seasonLengthDays);
    }

    public static SeasonalContext current(ServerLevel level) {
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
}
