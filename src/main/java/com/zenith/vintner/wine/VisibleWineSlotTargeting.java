package com.zenith.vintner.wine;

import com.zenith.vintner.block.CellarCollectionBlock;
import com.zenith.vintner.block.CellarFixtureKind;
import com.zenith.vintner.block.TastingServiceBlock;
import com.zenith.vintner.block.WineBasketBlock;
import com.zenith.vintner.block.WineBottleBlock;
import com.zenith.vintner.block.WineCrateBlock;
import com.zenith.vintner.block.WineRackBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/** Resolves the visible bottle slot crossed by the current crosshair ray. */
public final class VisibleWineSlotTargeting {
    public static final int NO_SLOT = -1;

    private static final double PIXEL = 1.0 / 16.0;
    private static final double SURFACE_EPSILON = 1.0E-4;
    private static final double[] CRATE_CENTRES = {
            3.0, 6.33, 9.67, 13.0
    };
    private static final double[] FIXTURE_CENTRES = {
            3.75, 6.25, 9.75, 12.25
    };
    private static final AABB[] RACK_SLOTS = createRackSlots();
    private static final AABB[] CRATE_SLOTS = createCrateSlots();
    private static final AABB[] LABELLED_SHELF_SLOTS =
            createFixtureSlots(3.18);
    private static final AABB[] TASTING_CABINET_SLOTS =
            createFixtureSlots(2.68);

    private VisibleWineSlotTargeting() {
    }

    /**
     * Returns a zero-based visual/storage slot, or {@link #NO_SLOT} when the
     * ray crosses only furniture. Empty slots are returned deliberately so a
     * caller never falls through to a different occupied bottle behind them.
     */
    public static int targetedSlot(
            BlockState state,
            BlockPos pos,
            Vec3 rayStart,
            Vec3 rayEnd
    ) {
        Vec3 localStart = toNorthLocal(
                state, pos, rayStart
        );
        Vec3 localEnd = toNorthLocal(
                state, pos, rayEnd
        );

        if (state.getBlock() instanceof WineBottleBlock) {
            return intersects(
                    box(6.25, 0, 6.25, 9.75, 10, 9.75),
                    localStart,
                    localEnd
            ) ? 0 : NO_SLOT;
        }

        if (state.getBlock() instanceof WineBasketBlock) {
            if (!state.getValue(WineBasketBlock.HAS_BOTTLE)) {
                return NO_SLOT;
            }
            // Bounds of the approved angled bottle pose, excluding the base.
            return intersects(
                    box(6.1, 3.0, 6.0, 9.9, 6.4, 14.6),
                    localStart,
                    localEnd
            ) ? 0 : NO_SLOT;
        }

        if (state.getBlock() instanceof WineCrateBlock) {
            return nearestSlot(
                    CRATE_SLOTS, localStart, localEnd
            );
        }

        if (state.getBlock() instanceof WineRackBlock) {
            if (!startsInFront(localStart)) {
                return NO_SLOT;
            }
            return nearestSlot(
                    RACK_SLOTS, localStart, localEnd
            );
        }

        if (state.getBlock() instanceof CellarCollectionBlock collection) {
            if (!startsInFront(localStart)) {
                return NO_SLOT;
            }
            AABB[] slots = collection.kind()
                    == CellarFixtureKind.TASTING_CABINET
                    ? TASTING_CABINET_SLOTS
                    : LABELLED_SHELF_SLOTS;
            return nearestSlot(
                    slots, localStart, localEnd
            );
        }

        return NO_SLOT;
    }

    /** Uses the actual block-outline hit so a tray hit cannot reach through. */
    public static boolean targetsTastingServiceBottle(
            BlockState state,
            BlockPos pos,
            BlockHitResult hit
    ) {
        if (!(state.getBlock() instanceof TastingServiceBlock)
                || !state.getValue(TastingServiceBlock.HAS_BOTTLE)
                || !hit.getBlockPos().equals(pos)) {
            return false;
        }

        Vec3 localHit = toNorthLocal(state, pos, hit.getLocation());
        return box(10.2, 2.0, 10.0, 12.8, 10.0, 12.5)
                .inflate(SURFACE_EPSILON)
                .contains(localHit);
    }

    private static AABB[] createRackSlots() {
        return new AABB[]{
                horizontalBottle(5.0, 2.68, 11.6),
                horizontalBottle(11.0, 2.68, 11.6),
                horizontalBottle(5.0, 10.18, 11.6),
                horizontalBottle(11.0, 10.18, 11.6),
        };
    }

    private static AABB[] createCrateSlots() {
        AABB[] slots = new AABB[16];
        int slot = 0;
        for (double z : CRATE_CENTRES) {
            for (double x : CRATE_CENTRES) {
                slots[slot++] = box(
                        x - 1.5, 1.25, z - 1.5,
                        x + 1.5, 9.25, z + 1.5
                );
            }
        }
        return slots;
    }

    private static AABB[] createFixtureSlots(double lowerY) {
        AABB[] slots = new AABB[8];
        for (int slot = 0; slot < slots.length; slot++) {
            double y = slot < 4 ? lowerY : 10.18;
            slots[slot] = horizontalBottle(
                    FIXTURE_CENTRES[slot % 4], y, 11.25
            );
        }
        return slots;
    }

    private static AABB horizontalBottle(
            double x,
            double y,
            double backZ
    ) {
        return box(
                x - 1.55, y - 1.55, 2.5,
                x + 1.55, y + 1.55, backZ + 0.35
        );
    }

    private static int nearestSlot(
            AABB[] slots,
            Vec3 start,
            Vec3 end
    ) {
        int nearestSlot = NO_SLOT;
        double nearestDistance = Double.POSITIVE_INFINITY;

        for (int slot = 0; slot < slots.length; slot++) {
            Optional<Vec3> intersection = slots[slot].clip(start, end);
            if (intersection.isEmpty()) {
                continue;
            }
            double distance = start.distanceToSqr(intersection.get());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestSlot = slot;
            }
        }
        return nearestSlot;
    }

    private static boolean intersects(AABB box, Vec3 start, Vec3 end) {
        return box.contains(start) || box.clip(start, end).isPresent();
    }

    private static boolean startsInFront(Vec3 localStart) {
        return localStart.z < 0.5;
    }

    private static AABB box(
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ
    ) {
        return new AABB(
                minX * PIXEL,
                minY * PIXEL,
                minZ * PIXEL,
                maxX * PIXEL,
                maxY * PIXEL,
                maxZ * PIXEL
        );
    }

    private static Vec3 toNorthLocal(
            BlockState state,
            BlockPos pos,
            Vec3 worldPoint
    ) {
        double x = worldPoint.x - pos.getX();
        double y = worldPoint.y - pos.getY();
        double z = worldPoint.z - pos.getZ();
        Direction facing = state.hasProperty(
                BlockStateProperties.HORIZONTAL_FACING
        ) ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH;

        return switch (facing) {
            case EAST -> new Vec3(z, y, 1.0 - x);
            case SOUTH -> new Vec3(1.0 - x, y, 1.0 - z);
            case WEST -> new Vec3(1.0 - z, y, x);
            default -> new Vec3(x, y, z);
        };
    }
}
