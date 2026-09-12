package com.zenith.vintner.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Support shape for solid machines that should not attract side connectors. */
final class DecorativeBlockSupport {
    static final VoxelShape VERTICAL_FACES = Shapes.or(
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(0, 15, 0, 16, 16, 16)
    );

    private DecorativeBlockSupport() {
    }
}
