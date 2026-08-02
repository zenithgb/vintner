package com.zenith.vintner.vineyard;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StainedGlassBlock;

/** Small, readable greenhouse-cover check shared by growth and inspection. */
public final class VineyardProtection {
    private static final int MIN_ROOF_OFFSET = 2;
    private static final int MAX_ROOF_OFFSET = 6;

    private VineyardProtection() {
    }

    public static boolean isProtected(Level level, BlockPos rootPos) {
        for (int offset = MIN_ROOF_OFFSET;
             offset <= MAX_ROOF_OFFSET;
             offset++) {
            Block block = level.getBlockState(rootPos.above(offset)).getBlock();
            if (block == Blocks.GLASS
                    || block == Blocks.TINTED_GLASS
                    || block instanceof StainedGlassBlock) {
                return true;
            }
        }
        return false;
    }
}
