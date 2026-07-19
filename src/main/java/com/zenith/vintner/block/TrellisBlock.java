package com.zenith.vintner.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TrellisBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<TrellisBlock> CODEC =
            simpleCodec(TrellisBlock::new);

    public static final BooleanProperty LEFT =
            BooleanProperty.create("left");

    public static final BooleanProperty RIGHT =
            BooleanProperty.create("right");

    public static final BooleanProperty ISOLATED =
            BooleanProperty.create("isolated");

    private static final VoxelShape NORTH_SOUTH_SHAPE =
            Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);

    private static final VoxelShape EAST_WEST_SHAPE =
            Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);

    public TrellisBlock(BlockBehaviour.Properties properties) {
        super(properties);

        registerDefaultState(
                stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(LEFT, false)
                        .setValue(RIGHT, false)
                        .setValue(ISOLATED, false)
        );
    }

    @Override
    public MapCodec<? extends TrellisBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos placementPos = context.getClickedPos();

        BlockState verticalNeighbor = findVerticalTrellis(
                context,
                placementPos
        );

        Direction facing;

        if (verticalNeighbor != null) {
            facing = verticalNeighbor.getValue(FACING);
        } else {
            BlockState rowNeighbor = null;

            if (!isManualPlacementOverride(context)) {
                rowNeighbor = findCompatibleRowNeighbor(
                        context,
                        placementPos
                );
            }

            facing = rowNeighbor != null
                    ? rowNeighbor.getValue(FACING)
                    : context.getHorizontalDirection().getOpposite();
        }

        boolean isolated;

        if (verticalNeighbor != null) {
            isolated = verticalNeighbor.getValue(ISOLATED);
        } else {
            isolated = isManualPlacementOverride(context);
        }

        BlockState state = defaultBlockState()
                .setValue(FACING, facing)
                .setValue(ISOLATED, isolated);

        return updateConnections(
                state,
                context.getLevel(),
                placementPos
        );
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction directionToNeighbour,
            BlockPos neighbourPos,
            BlockState neighbourState,
            RandomSource random
    ) {
        if (directionToNeighbour.getAxis().isHorizontal()) {
            return updateConnections(state, level, pos);
        }

        return super.updateShape(
                state,
                level,
                ticks,
                pos,
                directionToNeighbour,
                neighbourPos,
                neighbourState,
                random
        );
    }

    protected static BlockState updateConnections(
            BlockState state,
            BlockGetter level,
            BlockPos pos
    ) {
        Direction facing = state.getValue(FACING);
        Direction leftDirection = getLeftDirection(facing);
        Direction rightDirection = leftDirection.getOpposite();

        return state
                .setValue(
                        LEFT,
                        connectsTo(
                                state,
                                level.getBlockState(
                                        pos.relative(leftDirection)
                                ),
                                leftDirection
                        )
                )
                .setValue(
                        RIGHT,
                        connectsTo(
                                state,
                                level.getBlockState(
                                        pos.relative(rightDirection)
                                ),
                                rightDirection
                        )
                );
    }

    private static boolean connectsTo(
            BlockState state,
            BlockState neighbourState,
            Direction direction
    ) {
        if (!isTrellisState(neighbourState)) {
            return false;
        }

        if (state.getValue(ISOLATED)
                || neighbourState.getValue(ISOLATED)) {
            return false;
        }

        Direction facing = state.getValue(FACING);
        Direction neighbourFacing = neighbourState.getValue(FACING);

        return direction.getAxis() != facing.getAxis()
                && neighbourFacing.getAxis() == facing.getAxis();
    }

    private static Direction getLeftDirection(Direction facing) {
        return switch (facing) {
            case NORTH -> Direction.WEST;
            case SOUTH -> Direction.EAST;
            case EAST -> Direction.NORTH;
            case WEST -> Direction.SOUTH;
            default -> throw new IllegalStateException(
                    "Trellis facing must be horizontal: " + facing
            );
        };
    }

    private static BlockState findVerticalTrellis(
            BlockPlaceContext context,
            BlockPos placementPos
    ) {
        BlockState belowState = context.getLevel()
                .getBlockState(placementPos.below());

        if (isTrellisState(belowState)) {
            return belowState;
        }

        BlockState aboveState = context.getLevel()
                .getBlockState(placementPos.above());

        if (isTrellisState(aboveState)) {
            return aboveState;
        }

        return null;
    }

    private static BlockState findCompatibleRowNeighbor(
            BlockPlaceContext context,
            BlockPos placementPos
    ) {
        Direction[] horizontalDirections = {
                Direction.NORTH,
                Direction.SOUTH,
                Direction.EAST,
                Direction.WEST
        };

        BlockState selectedState = null;
        Direction.Axis selectedFacingAxis = null;

        for (Direction direction : horizontalDirections) {
            BlockState neighbourState = context.getLevel().getBlockState(
                    placementPos.relative(direction)
            );

            if (!isTrellisState(neighbourState)) {
                continue;
            }

            Direction neighbourFacing =
                    neighbourState.getValue(FACING);

            if (direction.getAxis() == neighbourFacing.getAxis()) {
                continue;
            }

            if (selectedFacingAxis == null) {
                selectedState = neighbourState;
                selectedFacingAxis = neighbourFacing.getAxis();
                continue;
            }

            if (selectedFacingAxis != neighbourFacing.getAxis()) {
                return null;
            }
        }

        return selectedState;
    }

    protected static boolean isTrellisState(BlockState state) {
        return state.getBlock() instanceof TrellisBlock;
    }

    private static boolean isManualPlacementOverride(
            BlockPlaceContext context
    ) {
        return context.getPlayer() != null
                && context.getPlayer().isShiftKeyDown();
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return state.getValue(FACING).getAxis()
                == Direction.Axis.X
                ? EAST_WEST_SHAPE
                : NORTH_SOUTH_SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(FACING, LEFT, RIGHT, ISOLATED);
    }
}
