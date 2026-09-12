package com.zenith.vintner.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/** Shared row-joining rules for the three estate management workstations. */
final class EstateWorkstationConnections {
    static final EnumProperty<Direction> FACING =
            BlockStateProperties.HORIZONTAL_FACING;
    static final BooleanProperty LEFT_CONNECTED =
            BooleanProperty.create("left_connected");
    static final BooleanProperty RIGHT_CONNECTED =
            BooleanProperty.create("right_connected");
    static final BooleanProperty STANDALONE =
            BooleanProperty.create("standalone");

    private EstateWorkstationConnections() {
    }

    static BlockState stateForPlacement(
            BlockState defaultState,
            BlockPlaceContext context
    ) {
        BlockState state = defaultState
                .setValue(
                        FACING,
                        context.getHorizontalDirection().getOpposite()
                )
                .setValue(
                        STANDALONE,
                        context.getPlayer() != null
                                && context.getPlayer().isShiftKeyDown()
                );
        return updateConnections(
                state,
                context.getLevel(),
                context.getClickedPos()
        );
    }

    static BlockState updateConnections(
            BlockState state,
            LevelReader level,
            BlockPos pos
    ) {
        if (state.getValue(STANDALONE)) {
            return state
                    .setValue(LEFT_CONNECTED, false)
                    .setValue(RIGHT_CONNECTED, false);
        }

        Direction facing = state.getValue(FACING);
        return state
                .setValue(
                        LEFT_CONNECTED,
                        canConnectTo(
                                state,
                                level.getBlockState(
                                        pos.relative(
                                                facing.getCounterClockWise()
                                        )
                                )
                        )
                )
                .setValue(
                        RIGHT_CONNECTED,
                        canConnectTo(
                                state,
                                level.getBlockState(
                                        pos.relative(facing.getClockWise())
                                )
                        )
                );
    }

    private static boolean canConnectTo(
            BlockState state,
            BlockState neighbour
    ) {
        return neighbour.getBlock() instanceof EstateWorkstationBlock
                && !neighbour.getValue(STANDALONE)
                && state.getValue(FACING) == neighbour.getValue(FACING);
    }
}

/** Marks a block as one module in a connected estate workstation row. */
interface EstateWorkstationBlock {
}
