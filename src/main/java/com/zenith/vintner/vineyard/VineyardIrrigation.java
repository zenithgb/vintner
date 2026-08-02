package com.zenith.vintner.vineyard;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;

/** Recognizes vanilla-style water channels serving a vineyard row. */
public final class VineyardIrrigation {
    private static final int WATER_RADIUS = 4;

    private VineyardIrrigation() {
    }

    public static boolean isIrrigated(Level level, BlockPos rootPos) {
        for (int x = -WATER_RADIUS; x <= WATER_RADIUS; x++) {
            for (int z = -WATER_RADIUS; z <= WATER_RADIUS; z++) {
                int distance = Math.abs(x) + Math.abs(z);
                if (distance == 0 || distance > WATER_RADIUS) {
                    continue;
                }
                for (int y = -1; y <= 0; y++) {
                    if (level.getFluidState(rootPos.offset(x, y, z))
                            .is(FluidTags.WATER)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
