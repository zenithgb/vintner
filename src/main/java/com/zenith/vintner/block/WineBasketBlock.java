package com.zenith.vintner.block;

import com.mojang.serialization.MapCodec;
import com.zenith.vintner.block.entity.WineBasketBlockEntity;
import com.zenith.vintner.item.VintnerAlmanacItem;
import com.zenith.vintner.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class WineBasketBlock extends BaseEntityBlock {
    public static final MapCodec<WineBasketBlock> CODEC =
            simpleCodec(WineBasketBlock::new);
    public static final EnumProperty<Direction> FACING =
            BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty HAS_BOTTLE =
            BooleanProperty.create("has_bottle");
    private static final VoxelShape NORTH_SOUTH_SHAPE = Shapes.or(
            Block.box(3, 0, 1, 13, 4, 15),
            Block.box(3, 3, 7, 4, 9, 9),
            Block.box(12, 3, 7, 13, 9, 9),
            Block.box(4, 8, 7, 12, 11, 9)
    );
    private static final VoxelShape EAST_WEST_SHAPE = Shapes.or(
            Block.box(1, 0, 3, 15, 4, 13),
            Block.box(7, 3, 3, 9, 9, 4),
            Block.box(7, 3, 12, 9, 9, 13),
            Block.box(7, 8, 4, 9, 11, 12)
    );

    public WineBasketBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HAS_BOTTLE, false));
    }

    @Override
    public MapCodec<WineBasketBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context
    ) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z
                ? NORTH_SOUTH_SHAPE : EAST_WEST_SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(
                FACING, context.getHorizontalDirection().getOpposite()
        );
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WineBasketBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack heldStack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult
    ) {
        if (!(level.getBlockEntity(pos) instanceof WineBasketBlockEntity basket)) {
            return InteractionResult.PASS;
        }
        if (heldStack.is(ModItems.VINTNER_ALMANAC)) {
            if (level instanceof ServerLevel serverLevel) {
                VintnerAlmanacItem.inspectPlacedWine(
                        serverLevel, player, basket.getBottleCopy()
                );
            }
            return InteractionResult.SUCCESS;
        }
        if (!WineBasketBlockEntity.isCompatibleBottle(heldStack)) {
            return heldStack.isEmpty()
                    ? InteractionResult.TRY_WITH_EMPTY_HAND
                    : InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel
                && basket.insertBottle(heldStack)) {
            if (!player.getAbilities().instabuild) {
                heldStack.shrink(1);
            }
            serverLevel.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM,
                    SoundSource.BLOCKS, 0.8F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult
    ) {
        // An unrelated held item must never trigger an implicit retrieval.
        if (!player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!(level.getBlockEntity(pos) instanceof WineBasketBlockEntity basket)
                || basket.getBottleCopy().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel) {
            ItemStack bottle = basket.removeBottle();
            if (!bottle.isEmpty()) {
                if (!player.addItem(bottle)) {
                    popResource(serverLevel, pos, bottle);
                }
                serverLevel.playSound(null, pos,
                        SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                        SoundSource.BLOCKS, 0.8F, 1.0F);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(FACING, HAS_BOTTLE);
    }
}
