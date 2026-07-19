package com.zenith.vintner.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TrellisBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<TrellisBlock> CODEC = simpleCodec(TrellisBlock::new);

    private static final VoxelShape NORTH_SOUTH_SHAPE = Shapes.or(
            Block.box(1.0, 0.0, 7.0, 3.0, 16.0, 9.0),
            Block.box(13.0, 0.0, 7.0, 15.0, 16.0, 9.0),
            Block.box(3.0, 3.0, 7.25, 13.0, 4.0, 8.75),
            Block.box(3.0, 8.0, 7.25, 13.0, 9.0, 8.75),
            Block.box(3.0, 13.0, 7.25, 13.0, 14.0, 8.75)
    );

    private static final VoxelShape EAST_WEST_SHAPE = Shapes.or(
            Block.box(7.0, 0.0, 1.0, 9.0, 16.0, 3.0),
            Block.box(7.0, 0.0, 13.0, 9.0, 16.0, 15.0),
            Block.box(7.25, 3.0, 3.0, 8.75, 4.0, 13.0),
            Block.box(7.25, 8.0, 3.0, 8.75, 9.0, 13.0),
            Block.box(7.25, 13.0, 3.0, 8.75, 14.0, 13.0)
    );

    public TrellisBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos placementPos = context.getClickedPos();

        BlockState belowState = context.getLevel().getBlockState(placementPos.below());
        if (belowState.is(this)) {
            return defaultBlockState().setValue(FACING, belowState.getValue(FACING));
        }

        BlockState aboveState = context.getLevel().getBlockState(placementPos.above());
        if (aboveState.is(this)) {
            return defaultBlockState().setValue(FACING, aboveState.getValue(FACING));
        }

        return defaultBlockState().setValue(
                FACING,
                context.getHorizontalDirection().getOpposite()
        );
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X
                ? EAST_WEST_SHAPE
                : NORTH_SOUTH_SHAPE;
    }
}
