package com.zenith.vintner.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/** Repairs only short air pockets under the authored Desert winery base. */
public final class DesertWineryFoundationSupport {
    private static final int MAX_AIR_GAP = 2;

    private DesertWineryFoundationSupport() {
    }

    public static int fill(
            WorldGenLevel level, BoundingBox piece, BoundingBox chunk
    ) {
        int placed = 0;
        int baseY = piece.minY();
        for (int x = Math.max(piece.minX(), chunk.minX());
                x <= Math.min(piece.maxX(), chunk.maxX()); x++) {
            for (int z = Math.max(piece.minZ(), chunk.minZ());
                    z <= Math.min(piece.maxZ(), chunk.maxZ()); z++) {
                BlockPos base = new BlockPos(x, baseY, z);
                BlockState material = supportMaterial(level.getBlockState(base));
                if (material == null) {
                    continue;
                }
                int gap = 0;
                while (gap < MAX_AIR_GAP && level.getBlockState(
                        base.below(gap + 1)).isAir()) {
                    gap++;
                }
                if (gap == 0 || gap > MAX_AIR_GAP) {
                    continue;
                }
                BlockState below = level.getBlockState(base.below(gap + 1));
                // A deeper void is a cave, not an ordinary uneven sand pocket.
                if (below.isAir() || !below.getFluidState().isEmpty()
                        || !below.isSolid()) {
                    continue;
                }
                for (int depth = gap; depth >= 1; depth--) {
                    if (level.setBlock(base.below(depth), material, 2)) {
                        placed++;
                    }
                }
            }
        }
        return placed;
    }

    private static BlockState supportMaterial(BlockState base) {
        if (base.is(Blocks.CUT_SANDSTONE)) {
            return Blocks.CUT_SANDSTONE.defaultBlockState();
        }
        if (base.is(Blocks.SMOOTH_SANDSTONE)
                || base.is(Blocks.SMOOTH_SANDSTONE_STAIRS)) {
            return Blocks.SMOOTH_SANDSTONE.defaultBlockState();
        }
        if (base.is(Blocks.SANDSTONE) || base.is(Blocks.SANDSTONE_WALL)
                || base.is(Blocks.SANDSTONE_STAIRS)) {
            return Blocks.SANDSTONE.defaultBlockState();
        }
        return null;
    }
}
