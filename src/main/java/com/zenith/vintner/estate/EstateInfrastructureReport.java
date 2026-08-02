package com.zenith.vintner.estate;

import com.zenith.vintner.block.AgingBarrelBlock;
import com.zenith.vintner.block.BarrelStandBlock;
import com.zenith.vintner.block.CellarCollectionBlock;
import com.zenith.vintner.block.CellarFixtureKind;
import com.zenith.vintner.block.VintageArchiveBlock;
import com.zenith.vintner.block.WineCrateBlock;
import com.zenith.vintner.block.WineRackBlock;
import com.zenith.vintner.wine.CellarConditions;
import com.zenith.vintner.wine.CellarRating;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * On-demand survey of physically built estate facilities near a player.
 * Nothing is bought or toggled in a menu: removing the structure removes the
 * facility from the next survey.
 */
public record EstateInfrastructureReport(
        int agingBarrels,
        int mountedBarrels,
        int idealCellarStations,
        int storageFixtures,
        int tastingCabinets,
        int archives
) {
    public static final int HORIZONTAL_RADIUS = 16;
    public static final int VERTICAL_RADIUS = 8;
    public static final int WORKSHOP_BARRELS = 2;
    public static final int CONTROLLED_CELLAR_STATIONS = 2;
    public static final int WAREHOUSE_FIXTURES = 4;
    public static final int BARREL_WORKSHOP_MASK = 1;
    public static final int CONTROLLED_CELLAR_MASK = 1 << 1;
    public static final int WAREHOUSE_MASK = 1 << 2;
    public static final int TASTING_ROOM_MASK = 1 << 3;

    public static EstateInfrastructureReport survey(
            Level level,
            BlockPos origin
    ) {
        int agingBarrels = 0;
        int mountedBarrels = 0;
        int idealStations = 0;
        int storageFixtures = 0;
        int tastingCabinets = 0;
        int archives = 0;

        BlockPos min = origin.offset(
                -HORIZONTAL_RADIUS,
                -VERTICAL_RADIUS,
                -HORIZONTAL_RADIUS
        );
        BlockPos max = origin.offset(
                HORIZONTAL_RADIUS,
                VERTICAL_RADIUS,
                HORIZONTAL_RADIUS
        );

        for (BlockPos cursor : BlockPos.betweenClosed(min, max)) {
            BlockState state = level.getBlockState(cursor);

            if (state.getBlock() instanceof AgingBarrelBlock) {
                agingBarrels++;
                if (hasBarrelStand(level, cursor)) {
                    mountedBarrels++;
                }
                if (CellarConditions.evaluate(level, cursor).rating()
                        == CellarRating.IDEAL) {
                    idealStations++;
                }
                continue;
            }

            if (state.getBlock() instanceof WineRackBlock
                    || state.getBlock() instanceof WineCrateBlock) {
                storageFixtures++;
                if (CellarConditions.evaluate(level, cursor).rating()
                        == CellarRating.IDEAL) {
                    idealStations++;
                }
                continue;
            }

            if (state.getBlock() instanceof CellarCollectionBlock fixture) {
                storageFixtures++;
                if (fixture.kind() == CellarFixtureKind.TASTING_CABINET) {
                    tastingCabinets++;
                }
                if (CellarConditions.evaluate(level, cursor).rating()
                        == CellarRating.IDEAL) {
                    idealStations++;
                }
                continue;
            }

            if (state.getBlock() instanceof VintageArchiveBlock) {
                archives++;
            }
        }

        return new EstateInfrastructureReport(
                agingBarrels,
                mountedBarrels,
                idealStations,
                storageFixtures,
                tastingCabinets,
                archives
        );
    }

    public static boolean hasBarrelStand(Level level, BlockPos barrelPos) {
        return level.getBlockState(barrelPos.below()).getBlock()
                instanceof BarrelStandBlock;
    }

    public static int ageingContribution(
            CellarRating rating,
            boolean mounted
    ) {
        if (!mounted) {
            return 0;
        }
        int cellar = switch (rating) {
            case POOR -> -1;
            case BASIC -> 0;
            case GOOD -> 1;
            case IDEAL -> 2;
        };
        return cellar + 1;
    }

    public boolean hasBarrelWorkshop() {
        return mountedBarrels >= WORKSHOP_BARRELS;
    }

    public boolean hasControlledCellar() {
        return idealCellarStations >= CONTROLLED_CELLAR_STATIONS;
    }

    public boolean hasWarehouse() {
        return storageFixtures >= WAREHOUSE_FIXTURES;
    }

    public boolean hasTastingRoom() {
        return tastingCabinets > 0 && archives > 0;
    }

    public int facilityMask() {
        int result = 0;
        if (hasBarrelWorkshop()) {
            result |= BARREL_WORKSHOP_MASK;
        }
        if (hasControlledCellar()) {
            result |= CONTROLLED_CELLAR_MASK;
        }
        if (hasWarehouse()) {
            result |= WAREHOUSE_MASK;
        }
        if (hasTastingRoom()) {
            result |= TASTING_ROOM_MASK;
        }
        return result;
    }
}
