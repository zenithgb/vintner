package com.zenith.vintner.block;

import com.mojang.serialization.MapCodec;
import com.zenith.vintner.block.entity.WineBottleBlockEntity;
import com.zenith.vintner.advancement.ModAdvancements;
import com.zenith.vintner.item.VintnerAlmanacItem;
import com.zenith.vintner.registry.ModBlockEntities;
import com.zenith.vintner.registry.ModItems;
import com.zenith.vintner.wine.WineMetadata;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

public final class WineBottleBlock extends BaseEntityBlock {
    public static final MapCodec<WineBottleBlock> CODEC =
            simpleCodec(WineBottleBlock::new);
    public static final EnumProperty<Direction> FACING =
            BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty SERVINGS =
            IntegerProperty.create(
                    "servings",
                    0,
                    WineMetadata.SERVINGS_PER_BOTTLE
            );
    private static final VoxelShape SHAPE = Block.box(
            6.25,
            0,
            6.25,
            9.75,
            10,
            9.75
    );

    public WineBottleBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(
                stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(
                                SERVINGS,
                                WineMetadata.SERVINGS_PER_BOTTLE
                        )
        );
    }

    @Override
    public MapCodec<WineBottleBlock> codec() {
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
            net.minecraft.world.item.context.BlockPlaceContext context
    ) {
        return defaultBlockState().setValue(
                FACING,
                context.getHorizontalDirection().getOpposite()
        );
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WineBottleBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return null;
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

        if (!(blockEntity instanceof WineBottleBlockEntity bottleEntity)) {
            return InteractionResult.PASS;
        }

        ItemStack bottle = bottleEntity.takeBottle();

        if (bottle.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!player.addItem(bottle)) {
            popResource(serverLevel, pos, bottle);
        }

        serverLevel.removeBlock(pos, false);

        serverLevel.playSound(
                null,
                pos,
                SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                SoundSource.BLOCKS,
                0.8F,
                1.0F
        );
        return InteractionResult.SUCCESS;
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
        if (heldStack.is(ModItems.VINTNER_ALMANAC)) {
            if (level instanceof ServerLevel serverLevel
                    && level.getBlockEntity(pos)
                    instanceof WineBottleBlockEntity bottleEntity) {
                VintnerAlmanacItem.inspectPlacedWine(
                        serverLevel,
                        player,
                        bottleEntity.getBottleCopy()
                );
            }
            return InteractionResult.SUCCESS;
        }

        if (!heldStack.is(ModItems.WINE_GLASS)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (!(level.getBlockEntity(pos)
                instanceof WineBottleBlockEntity bottleEntity)) {
            return InteractionResult.PASS;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        ItemStack filledGlass = bottleEntity.pourServing();
        if (filledGlass.isEmpty()) {
            player.sendSystemMessage(
                    Component.translatable(
                            "message.vintner.wine_bottle.empty"
                    )
            );
            return InteractionResult.SUCCESS;
        }

        if (!player.getAbilities().instabuild) {
            heldStack.shrink(1);
        }

        if (!player.addItem(filledGlass)) {
            popResource(serverLevel, pos, filledGlass);
        }

        serverLevel.playSound(
                null,
                pos,
                SoundEvents.BOTTLE_FILL,
                SoundSource.BLOCKS,
                0.9F,
                1.1F
        );

        if (player instanceof net.minecraft.server.level.ServerPlayer
                serverPlayer) {
            ModAdvancements.grantProperPour(serverPlayer);
        }

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
                instanceof WineBottleBlockEntity bottleEntity) {
            ItemStack bottle = bottleEntity.takeBottle();

            if (!bottle.isEmpty()) {
                popResource(level, pos, bottle);
            }
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected List<ItemStack> getDrops(
            BlockState state,
            LootParams.Builder params
    ) {
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, params));
        BlockEntity blockEntity = params.getOptionalParameter(
                LootContextParams.BLOCK_ENTITY
        );

        if (blockEntity instanceof WineBottleBlockEntity bottleEntity) {
            ItemStack bottle = bottleEntity.getBottleCopy();

            if (!bottle.isEmpty()) {
                drops.add(bottle);
            }
        }

        return drops;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(FACING);
        builder.add(SERVINGS);
    }
}
