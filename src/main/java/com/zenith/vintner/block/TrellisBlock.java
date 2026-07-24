package com.zenith.vintner.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TrellisBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<TrellisBlock> CODEC =
            simpleCodec(TrellisBlock::new);



    public static final EnumProperty<RowConnection> NORTH =
            EnumProperty.create("north", RowConnection.class);

    public static final EnumProperty<RowConnection> EAST =
            EnumProperty.create("east", RowConnection.class);

    public static final EnumProperty<RowConnection> SOUTH =
            EnumProperty.create("south", RowConnection.class);

    public static final EnumProperty<RowConnection> WEST =
            EnumProperty.create("west", RowConnection.class);

    public static final BooleanProperty ISOLATED =
            BooleanProperty.create("isolated");

    public static final BooleanProperty HAS_ABOVE =
            BooleanProperty.create("has_above");

    public static final BooleanProperty HAS_BELOW =
            BooleanProperty.create("has_below");

    private static final VoxelShape POST_SHAPE =
            Block.box(7.0, 0.0, 7.0, 9.0, 16.0, 9.0);

    private static final double[] WIRE_HEIGHTS = {
            4.0,
            7.0,
            10.0,
            13.0
    };

    private final WoodVariant woodVariant;

    public TrellisBlock(BlockBehaviour.Properties properties) {
        this(WoodVariant.OAK, properties);
    }

    public TrellisBlock(
            WoodVariant woodVariant,
            BlockBehaviour.Properties properties
    ) {
        super(properties);
        this.woodVariant = woodVariant;

        registerDefaultState(
                stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(NORTH, RowConnection.NONE)
                        .setValue(EAST, RowConnection.NONE)
                        .setValue(SOUTH, RowConnection.NONE)
                        .setValue(WEST, RowConnection.NONE)
                        .setValue(ISOLATED, false)
                        .setValue(HAS_ABOVE, false)
                        .setValue(HAS_BELOW, false)
        );
    }

    public final WoodVariant woodVariant() {
        return woodVariant;
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

        Direction facing = verticalNeighbor != null
                ? verticalNeighbor.getValue(FACING)
                : context.getHorizontalDirection().getOpposite();

        boolean isolated = verticalNeighbor != null
                ? verticalNeighbor.getValue(ISOLATED)
                : isManualPlacementOverride(context);

        BlockState state = defaultBlockState()
                .setValue(FACING, facing)
                .setValue(ISOLATED, isolated)
                .setValue(
                        HAS_ABOVE,
                        isTrellisState(
                                context.getLevel().getBlockState(
                                        placementPos.above()
                                )
                        )
                )
                .setValue(
                        HAS_BELOW,
                        isTrellisState(
                                context.getLevel().getBlockState(
                                        placementPos.below()
                                )
                        )
                );

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

        if (directionToNeighbour == Direction.UP) {
            return updateConnections(
                    state.setValue(
                            HAS_ABOVE,
                            isTrellisState(neighbourState)
                    ),
                    level,
                    pos
            );
        }

        if (directionToNeighbour == Direction.DOWN) {
            return updateConnections(
                    state.setValue(
                            HAS_BELOW,
                            isTrellisState(neighbourState)
                    ),
                    level,
                    pos
            );
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

    private static BlockState updateConnections(
            BlockState state,
            LevelReader level,
            BlockPos pos
    ) {
        if (state.getValue(ISOLATED)) {
            return state
                    .setValue(NORTH, RowConnection.NONE)
                    .setValue(EAST, RowConnection.NONE)
                    .setValue(SOUTH, RowConnection.NONE)
                    .setValue(WEST, RowConnection.NONE);
        }

        return state
                .setValue(
                        NORTH,
                        connectionToNeighbour(
                                state,
                                level,
                                pos,
                                Direction.NORTH
                        )
                )
                .setValue(
                        EAST,
                        connectionToNeighbour(
                                state,
                                level,
                                pos,
                                Direction.EAST
                        )
                )
                .setValue(
                        SOUTH,
                        connectionToNeighbour(
                                state,
                                level,
                                pos,
                                Direction.SOUTH
                        )
                )
                .setValue(
                        WEST,
                        connectionToNeighbour(
                                state,
                                level,
                                pos,
                                Direction.WEST
                        )
                );
    }

    private static RowConnection connectionToNeighbour(
            BlockState state,
            LevelReader level,
            BlockPos pos,
            Direction direction
    ) {
        BlockPos neighbourPos = pos.relative(direction);

        if (canConnectTo(
                state,
                level.getBlockState(neighbourPos)
        )) {
            return RowConnection.LEVEL;
        }



        return RowConnection.NONE;
    }

    private static boolean canConnectTo(
            BlockState state,
            BlockState neighbourState
    ) {
        return !state.getValue(ISOLATED)
                && isTrellisState(neighbourState)
                && !neighbourState.getValue(ISOLATED)
                && state.getValue(HAS_ABOVE)
                == neighbourState.getValue(HAS_ABOVE);
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
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        InteractionResult result =
                VineyardSoilInteraction.useOnSoilBelow(
                        stack,
                        level,
                        pos,
                        player
                );

        if (result.consumesAction()) {
            return result;
        }

        return super.useItemOn(
                stack,
                state,
                level,
                pos,
                player,
                hand,
                hitResult
        );
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return createTrellisShape(state);
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return createTrellisShape(state);
    }

    private static VoxelShape createTrellisShape(BlockState state) {
        VoxelShape shape = POST_SHAPE;

        if (state.getValue(HAS_ABOVE)
                || (state.getBlock() instanceof GrapevineBlock
                && !state.getValue(GrapevineBlock.UPPER))) {
            return shape;
        }

        if (state.getValue(NORTH) != RowConnection.NONE) {
            shape = addWireShapes(shape, Direction.NORTH);
        }

        if (state.getValue(EAST) != RowConnection.NONE) {
            shape = addWireShapes(shape, Direction.EAST);
        }

        if (state.getValue(SOUTH) != RowConnection.NONE) {
            shape = addWireShapes(shape, Direction.SOUTH);
        }

        if (state.getValue(WEST) != RowConnection.NONE) {
            shape = addWireShapes(shape, Direction.WEST);
        }

        return shape;
    }

    private static VoxelShape addWireShapes(
            VoxelShape shape,
            Direction direction
    ) {
        for (double y : WIRE_HEIGHTS) {
            shape = Shapes.or(
                    shape,
                    createWireShape(direction, y)
            );
        }

        return shape;
    }

    private static VoxelShape createWireShape(
            Direction direction,
            double y
    ) {
        return switch (direction) {
            case WEST -> Block.box(
                    0.0, y, 7.5,
                    8.0, y + 1.0, 8.5
            );
            case EAST -> Block.box(
                    8.0, y, 7.5,
                    16.0, y + 1.0, 8.5
            );
            case NORTH -> Block.box(
                    7.5, y, 0.0,
                    8.5, y + 1.0, 8.0
            );
            case SOUTH -> Block.box(
                    7.5, y, 8.0,
                    8.5, y + 1.0, 16.0
            );
            default -> throw new IllegalStateException(
                    "Trellis connection must be horizontal: "
                            + direction
            );
        };
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(
                FACING,
                NORTH,
                EAST,
                SOUTH,
                WEST,
                ISOLATED,
                HAS_ABOVE,
                HAS_BELOW
        );
    }

    public enum RowConnection implements StringRepresentable {
        NONE("none"),
        LEVEL("level");

        private final String serializedName;

        RowConnection(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }

}
