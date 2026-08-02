package com.zenith.vintner.compat.sereneseasons;

import com.zenith.vintner.vineyard.SeasonalContext;
import com.zenith.vintner.vineyard.VineyardSeason;
import net.minecraft.server.level.ServerLevel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** Isolated optional integration; this class is only loaded when Serene Seasons is present. */
public final class SereneSeasonsIntegration {
    private SereneSeasonsIntegration() {
    }

    public static SeasonalContext current(ServerLevel level) {
        try {
            Class<?> helperType = Class.forName(
                    "sereneseasons.api.season.SeasonHelper"
            );
            Class<?> stateType = Class.forName(
                    "sereneseasons.api.season.ISeasonState"
            );
            Method getSeasonState = helperType.getMethod(
                    "getSeasonState",
                    net.minecraft.world.level.Level.class
            );
            Object state = getSeasonState.invoke(null, level);
            if (state == null) {
                throw new IllegalStateException(
                        "Serene Seasons returned no season state"
                );
            }
            Object externalSeason = stateType
                    .getMethod("getSeason")
                    .invoke(state);
            String seasonName = externalSeason instanceof Enum<?> season
                    ? season.name()
                    : externalSeason.toString();
            return SeasonalContext.fromExternalCycle(
                    VineyardSeason.valueOf(seasonName),
                    level.getOverworldClockTime(),
                    invokeInt(stateType, state, "getSeasonCycleTicks"),
                    invokeInt(stateType, state, "getDayDuration"),
                    invokeInt(stateType, state, "getSeasonDuration"),
                    invokeInt(stateType, state, "getCycleDuration")
            );
        } catch (ClassNotFoundException
                 | NoSuchMethodException
                 | IllegalAccessException exception) {
            throw new IllegalStateException(
                    "The installed Serene Seasons API is incompatible",
                    exception
            );
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException(
                    "Serene Seasons failed to provide its season state",
                    exception.getCause()
            );
        }
    }

    private static int invokeInt(
            Class<?> stateType,
            Object state,
            String methodName
    ) throws NoSuchMethodException,
            InvocationTargetException,
            IllegalAccessException {
        return (int) stateType.getMethod(methodName).invoke(state);
    }
}
