package com.zenith.vintner.block;

import com.mojang.serialization.MapCodec;
import com.zenith.vintner.advancement.ModAdvancements;
import com.zenith.vintner.block.entity.AgingBarrelBlockEntity;
import com.zenith.vintner.registry.ModBlockEntities;
import com.zenith.vintner.registry.ModItems;
import com.zenith.vintner.wine.WinemakingFeedback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class AgingBarrelBlock extends BaseEntityBlock {
    public static final MapCodec<AgingBarrelBlock> CODEC =
            simpleCodec(AgingBarrelBlock::new);

    public static final EnumProperty<Direction> FACING =
            BlockStateProperties.HORIZONTAL_FACING;

    public static final IntegerProperty STATUS =
            IntegerProperty.create("status", 0, 2);

    public static final IntegerProperty WINE_TYPE =
            IntegerProperty.create("wine_type", 0, 2);

    public AgingBarrelBlock(
            BlockBehaviour.Properties properties
    ) {
        super(properties);

        registerDefaultState(
                stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(STATUS, 0)
                        .setValue(WINE_TYPE, 0)
        );
    }

    @Override
    public MapCodec<AgingBarrelBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(
            BlockPlaceContext context
    ) {
        return defaultBlockState().setValue(
                FACING,
                context.getHorizontalDirection().getOpposite()
        );
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        return new AgingBarrelBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity>
    @Nullable BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return level.isClientSide()
                ? null
                : createTickerHelper(
                        type,
                        ModBlockEntities.AGING_BARREL,
                        AgingBarrelBlockEntity::serverTick
                );
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack heldStack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (!heldStack.is(ModItems.RED_WINE)
                && !heldStack.is(ModItems.WHITE_WINE)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof AgingBarrelBlockEntity barrel)) {
            return InteractionResult.PASS;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        if (!barrel.insertOne(heldStack)) {
            WinemakingFeedback.showAgingInsertRejected(
                    player,
                    barrel
            );
            return InteractionResult.SUCCESS;
        }

        if (!player.getAbilities().instabuild) {
            heldStack.shrink(1);
        }

        serverLevel.playSound(
                null,
                pos,
                SoundEvents.BOTTLE_EMPTY,
                SoundSource.BLOCKS,
                0.9F,
                0.8F
        );

        WinemakingFeedback.showAgingStatus(player, barrel);

        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof AgingBarrelBlockEntity barrel)) {
            return InteractionResult.PASS;
        }

        ItemStack agedWine = barrel.takeOneAgedWine();

        if (agedWine.isEmpty()) {
            WinemakingFeedback.showAgingStatus(player, barrel);
            return InteractionResult.SUCCESS;
        }

        if (!player.addItem(agedWine)) {
            Block.popResource(serverLevel, pos, agedWine);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            ModAdvancements.grantAging(
                    serverPlayer,
                    agedWine
            );
        }

        serverLevel.playSound(
                null,
                pos,
                SoundEvents.BOTTLE_FILL,
                SoundSource.BLOCKS,
                0.9F,
                0.9F
        );

        WinemakingFeedback.showAgingStatus(player, barrel);

        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(
            BlockState state,
            Level level,
            BlockPos pos,
            Direction direction
    ) {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        return blockEntity instanceof AgingBarrelBlockEntity barrel
                ? barrel.getComparatorSignal()
                : 0;
    }

    @Override
    protected List<ItemStack> getDrops(
            BlockState state,
            LootParams.Builder params
    ) {
        List<ItemStack> drops =
                new ArrayList<>(super.getDrops(state, params));

        BlockEntity blockEntity = params.getOptionalParameter(
                LootContextParams.BLOCK_ENTITY
        );

        if (blockEntity instanceof AgingBarrelBlockEntity barrel) {
            ItemStack contents = barrel.getStoredContentsCopy();

            if (!contents.isEmpty()) {
                drops.add(contents);
            }
        }

        return drops;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(FACING, STATUS, WINE_TYPE);
    }
}
