package com.zenith.vintner.block;

import com.mojang.serialization.MapCodec;
import com.zenith.vintner.advancement.ModAdvancements;
import com.zenith.vintner.block.entity.GrapePressBlockEntity;
import com.zenith.vintner.registry.ModItems;
import com.zenith.vintner.wine.WinemakingEffects;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class GrapePressBlock extends BaseEntityBlock {
    public static final MapCodec<GrapePressBlock> CODEC =
            simpleCodec(GrapePressBlock::new);

    public static final EnumProperty<Direction> FACING =
            BlockStateProperties.HORIZONTAL_FACING;

    public static final IntegerProperty INPUT_LEVEL =
            IntegerProperty.create("input_level", 0, 2);

    public static final IntegerProperty INPUT_TYPE =
            IntegerProperty.create("input_type", 0, 2);

    public static final IntegerProperty OUTPUT_TYPE =
            IntegerProperty.create("output_type", 0, 2);

    public GrapePressBlock(BlockBehaviour.Properties properties) {
        super(properties);

        registerDefaultState(
                stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(INPUT_LEVEL, 0)
                        .setValue(INPUT_TYPE, 0)
                        .setValue(OUTPUT_TYPE, 0)
        );
    }

    @Override
    public MapCodec<GrapePressBlock> codec() {
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
        return new GrapePressBlockEntity(pos, state);
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

        if (!(blockEntity instanceof GrapePressBlockEntity press)) {
            return InteractionResult.PASS;
        }

        if (heldStack.is(Items.GLASS_BOTTLE)) {
            /*
             * The block entity inventory is authoritative on the
             * server. Always accept the client interaction so the
             * server can either bottle must or explain why it cannot.
             */
            if (!(level instanceof ServerLevel serverLevel)) {
                return InteractionResult.SUCCESS;
            }

            if (!press.hasMust()) {
                WinemakingFeedback.showNoMust(player);
                return InteractionResult.SUCCESS;
            }

            ItemStack bottledMust = press.bottleOneMust();

            if (bottledMust.isEmpty()) {
                return InteractionResult.FAIL;
            }

            if (!player.getAbilities().instabuild) {
                heldStack.shrink(1);
            }

            if (!player.addItem(bottledMust)) {
                Block.popResource(
                        serverLevel,
                        pos,
                        bottledMust
                );
            }

            serverLevel.playSound(
                    null,
                    pos,
                    SoundEvents.BOTTLE_FILL,
                    SoundSource.BLOCKS,
                    0.9F,
                    1.0F
            );

            WinemakingFeedback.showPressStatus(player, press);

            return InteractionResult.SUCCESS;
        }

        if (!heldStack.is(ModItems.RED_GRAPES)
                && !heldStack.is(ModItems.WHITE_GRAPES)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (!press.canInsert(heldStack)) {
            if (level instanceof ServerLevel) {
                WinemakingFeedback.showPressInsertRejected(
                        player,
                        press
                );
            }

            return InteractionResult.SUCCESS;
        }

        if (level instanceof ServerLevel serverLevel) {
            int inserted = press.insert(heldStack, 1);

            if (inserted > 0
                    && !player.getAbilities().instabuild) {
                heldStack.shrink(inserted);
            }

            serverLevel.playSound(
                    null,
                    pos,
                    SoundEvents.ITEM_FRAME_ADD_ITEM,
                    SoundSource.BLOCKS,
                    0.8F,
                    0.9F
            );

            WinemakingFeedback.showPressStatus(player, press);
        }

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
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof GrapePressBlockEntity press)) {
            return InteractionResult.PASS;
        }

        if (!press.canPress()) {
            WinemakingFeedback.showPressRejected(player, press);
            return InteractionResult.SUCCESS;
        }

        if (level instanceof ServerLevel serverLevel
                && press.press()) {
            if (player instanceof ServerPlayer serverPlayer) {
                ModAdvancements.grantPressing(
                        serverPlayer,
                        press.getOutput()
                );
            }

            serverLevel.playSound(
                    null,
                    pos,
                    SoundEvents.WOODEN_TRAPDOOR_CLOSE,
                    SoundSource.BLOCKS,
                    1.0F,
                    0.7F
            );

            WinemakingEffects.press(
                    serverLevel,
                    pos,
                    press.getOutput().is(ModItems.RED_MUST)
                            ? ModItems.RED_GRAPES
                            : ModItems.WHITE_GRAPES
            );

            WinemakingFeedback.showPressStatus(player, press);
        }

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

        return blockEntity instanceof GrapePressBlockEntity press
                ? press.getComparatorSignal()
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

        if (blockEntity instanceof GrapePressBlockEntity press) {
            ItemStack storedGrapes =
                    press.getStoredGrapesCopy();

            if (!storedGrapes.isEmpty()) {
                drops.add(storedGrapes);
            }
        }

        /*
         * Must is stored as unbottled liquid, so it is intentionally
         * lost when the press is broken.
         */
        return drops;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(
                FACING,
                INPUT_LEVEL,
                INPUT_TYPE,
                OUTPUT_TYPE
        );
    }
}
