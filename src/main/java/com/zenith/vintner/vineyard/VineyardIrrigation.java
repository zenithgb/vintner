package com.zenith.vintner.vineyard;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;

/** Recognizes vanilla-style water channels serving a vineyard row. */
public final class VineyardIrrigation {
    private static final int WATER_RADIUS = 4;

    private VineyardIrrigation() {
    }

    /** Exact irrigation influence for one already-loaded, synchronous plot analysis. */
    public static final class PlotInfluence {
        private final Level level;
        private final int minX;
        private final int minZ;
        private final int width;
        private final int depth;
        private final int minY;
        private final boolean[][] heights;

        public PlotInfluence(Level level, int minX, int minZ, int width, int depth,
                             int minY, int height) {
            this.level = level;
            this.minX = minX;
            this.minZ = minZ;
            this.width = width;
            this.depth = depth;
            this.minY = minY;
            this.heights = new boolean[height][];
        }

        public boolean isIrrigated(BlockPos rootPos) {
            int height = rootPos.getY() - minY;
            if (heights[height] == null) {
                heights[height] = inspectHeight(rootPos.getY());
            }
            return heights[height][(rootPos.getX() - minX) * depth + rootPos.getZ() - minZ];
        }

        private boolean[] inspectHeight(int rootY) {
            boolean[] irrigated = new boolean[width * depth];
            for (int x = -WATER_RADIUS; x < width + WATER_RADIUS; x++) {
                for (int z = -WATER_RADIUS; z < depth + WATER_RADIUS; z++) {
                    BlockPos waterPos = new BlockPos(minX + x, rootY - 1, minZ + z);
                    if (!level.getFluidState(waterPos).is(FluidTags.WATER)
                            && !level.getFluidState(waterPos.above()).is(FluidTags.WATER)) {
                        continue;
                    }
                    // Invert the original nonzero Manhattan-radius neighborhood.
                    // A water position serves roots at this height or one above it.
                    for (int dx = -WATER_RADIUS; dx <= WATER_RADIUS; dx++) {
                        for (int dz = -WATER_RADIUS; dz <= WATER_RADIUS; dz++) {
                            int distance = Math.abs(dx) + Math.abs(dz);
                            int rootX = x + dx;
                            int rootZ = z + dz;
                            if (distance == 0 || distance > WATER_RADIUS
                                    || rootX < 0 || rootX >= width || rootZ < 0 || rootZ >= depth) {
                                continue;
                            }
                            irrigated[rootX * depth + rootZ] = true;
                        }
                    }
                }
            }
            return irrigated;
        }
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
