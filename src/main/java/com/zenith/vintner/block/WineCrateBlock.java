package com.zenith.vintner.block;

import com.mojang.serialization.MapCodec;
import com.zenith.vintner.advancement.ModAdvancements;
import com.zenith.vintner.block.entity.WineCrateBlockEntity;
import com.zenith.vintner.item.WineItem;
import com.zenith.vintner.registry.ModBlockEntities;
import com.zenith.vintner.registry.ModItems;
import com.zenith.vintner.wine.CellarConditions;
import com.zenith.vintner.wine.CellarRating;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class WineCrateBlock extends BaseEntityBlock {
    public static final MapCodec<WineCrateBlock> CODEC =
            simpleCodec(WineCrateBlock::new);
    public static final EnumProperty<Direction> FACING =
            BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty BOTTLE_ROWS =
            IntegerProperty.create("bottle_rows", 0, 4);
    private static final VoxelShape SHAPE =
            Block.box(0.5, 0, 0.5, 15.5, 12, 15.5);

    public WineCrateBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(
                stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(BOTTLE_ROWS, 0)
        );
    }

    @Override
    public MapCodec<WineCrateBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return SHAPE;
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
        return new WineCrateBlockEntity(pos, state);
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
                        ModBlockEntities.WINE_CRATE,
                        WineCrateBlockEntity::serverTick
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
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof WineCrateBlockEntity crate)) {
            return InteractionResult.PASS;
        }

        if (heldStack.is(ModItems.VINTNER_ALMANAC)) {
            if (level instanceof ServerLevel) {
                CellarConditions conditions =
                        CellarConditions.evaluate(level, pos);
                player.sendSystemMessage(
                        Component.translatable(
                                "message.vintner.wine_crate.summary",
                                crate.getBottleCount(),
                                WineCrateBlockEntity.CAPACITY,
                                conditions.rating().displayName()
                        )
                );

                if (conditions.rating() == CellarRating.IDEAL
                        && player instanceof ServerPlayer serverPlayer) {
                    ModAdvancements.grantIdealCellar(serverPlayer);
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (!(heldStack.getItem() instanceof WineItem)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (!crate.canInsert(heldStack)) {
            if (level instanceof ServerLevel) {
                player.sendSystemMessage(
                        Component.translatable(
                                "message.vintner.wine_crate.full"
                        )
                );
            }
            return InteractionResult.SUCCESS;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        if (!crate.insertOne(heldStack)) {
            return InteractionResult.SUCCESS;
        }

        if (!player.getAbilities().instabuild) {
            heldStack.shrink(1);
        }

        serverLevel.playSound(
                null,
                pos,
                SoundEvents.ITEM_FRAME_ADD_ITEM,
                SoundSource.BLOCKS,
                0.8F,
                0.85F
        );
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

        if (!(blockEntity instanceof WineCrateBlockEntity crate)) {
            return InteractionResult.PASS;
        }

        ItemStack bottle = crate.takeLastBottle();

        if (bottle.isEmpty()) {
            player.sendSystemMessage(
                    Component.translatable(
                            "message.vintner.wine_crate.empty"
                    )
            );
            return InteractionResult.SUCCESS;
        }

        if (!player.addItem(bottle)) {
            Block.popResource(serverLevel, pos, bottle);
        }

        serverLevel.playSound(
                null,
                pos,
                SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                SoundSource.BLOCKS,
                0.8F,
                0.85F
        );
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(
            Level level,
            BlockPos pos,
            BlockState state,
            Player player
    ) {
        if (!level.isClientSide()
                && player.getAbilities().instabuild
                && level.getBlockEntity(pos)
                instanceof WineCrateBlockEntity crate) {
            for (ItemStack bottle : crate.removeAllBottles()) {
                Block.popResource(level, pos, bottle);
            }
        }

        return super.playerWillDestroy(level, pos, state, player);
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
        return blockEntity instanceof WineCrateBlockEntity crate
                ? crate.getComparatorSignal()
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

        if (blockEntity instanceof WineCrateBlockEntity crate) {
            drops.addAll(crate.getStoredBottlesCopy());
        }

        return drops;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(FACING, BOTTLE_ROWS);
    }
}
