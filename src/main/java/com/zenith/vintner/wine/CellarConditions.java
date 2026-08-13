package com.zenith.vintner.wine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public record CellarConditions(
        CellarRating rating,
        boolean sheltered,
        boolean underground,
        boolean dark,
        boolean humid,
        boolean heatSource,
        boolean stableTemperature,
        boolean disturbed
) {
    public static CellarConditions evaluate(
            Level level,
            BlockPos position
    ) {
        boolean sheltered = !level.canSeeSky(position.above());
        boolean underground =
                position.getY() <= level.getSeaLevel() - 4;
        boolean dark =
                level.getMaxLocalRawBrightness(position) <= 7;
        boolean humid = hasNearbyWater(level, position);
        boolean heatSource = hasNearbyHeat(level, position);
        boolean stableTemperature = sheltered && underground;
        boolean disturbed = level.hasNeighborSignal(position)
                || hasNearbyPiston(level, position);

        return new CellarConditions(
                ratingFor(
                        sheltered,
                        underground,
                        dark,
                        humid,
                        heatSource,
                        stableTemperature,
                        disturbed
                ),
                sheltered,
                underground,
                dark,
                humid,
                heatSource,
                stableTemperature,
                disturbed
        );
    }

    public static CellarRating ratingFor(
            boolean sheltered,
            boolean underground,
            boolean dark,
            boolean humid,
            boolean heatSource
    ) {
        return ratingFor(
                sheltered,
                underground,
                dark,
                humid,
                heatSource,
                sheltered && underground,
                false
        );
    }

    public static CellarRating ratingFor(
            boolean sheltered,
            boolean underground,
            boolean dark,
            boolean humid,
            boolean heatSource,
            boolean stableTemperature,
            boolean disturbed
    ) {
        int score = 0;
        score += sheltered ? 1 : 0;
        score += underground ? 1 : 0;
        score += dark ? 1 : 0;
        score += humid ? 1 : 0;
        score += stableTemperature ? 1 : 0;
        score -= heatSource ? 3 : 0;
        score -= disturbed ? 2 : 0;

        if (score >= 5) {
            return CellarRating.IDEAL;
        }
        if (score >= 3) {
            return CellarRating.GOOD;
        }
        if (score >= 1) {
            return CellarRating.BASIC;
        }
        return CellarRating.POOR;
    }

    private static boolean hasNearbyPiston(
            Level level,
            BlockPos position
    ) {
        for (Direction direction : Direction.values()) {
            var state = level.getBlockState(position.relative(direction));
            if (state.is(Blocks.PISTON)
                    || state.is(Blocks.STICKY_PISTON)
                    || state.is(Blocks.MOVING_PISTON)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasNearbyWater(
            Level level,
            BlockPos position
    ) {
        for (Direction direction : Direction.values()) {
            if (level.getFluidState(
                    position.relative(direction)
            ).is(FluidTags.WATER)) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasNearbyHeat(
            Level level,
            BlockPos position
    ) {
        for (Direction direction : Direction.values()) {
            var state = level.getBlockState(
                    position.relative(direction)
            );

            if (state.is(Blocks.LAVA)
                    || state.is(Blocks.FIRE)
                    || state.is(Blocks.SOUL_FIRE)
                    || state.is(Blocks.CAMPFIRE)
                    || state.is(Blocks.SOUL_CAMPFIRE)) {
                return true;
            }
        }

        return false;
    }
}
