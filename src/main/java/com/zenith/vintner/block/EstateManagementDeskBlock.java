package com.zenith.vintner.block;

import com.mojang.serialization.MapCodec;
import com.zenith.vintner.block.entity.EstateManagementDeskBlockEntity;
import com.zenith.vintner.estate.EstateDeskReport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class EstateManagementDeskBlock
        extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING =
            BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DeskBlotterColor> BLOTTER_COLOR =
            EnumProperty.create("blotter_color", DeskBlotterColor.class);
    public static final BooleanProperty HAS_LEDGER =
            BooleanProperty.create("has_ledger");
    public static final BooleanProperty HAS_MAP =
            BooleanProperty.create("has_map");
    public static final MapCodec<EstateManagementDeskBlock> CODEC =
            simpleCodec(EstateManagementDeskBlock::new);
    private static final VoxelShape SHAPE = Block.box(
            0.25,
            0,
            0.5,
            15.75,
            14.75,
            15
    );

    public EstateManagementDeskBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(
                stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(
                                BLOTTER_COLOR,
                                DeskBlotterColor.GREEN
                        )
                        .setValue(HAS_LEDGER, false)
                        .setValue(HAS_MAP, false)
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
        DeskBlotterColor blotterColor =
                DeskBlotterColor.fromDye(heldStack);
        if (blotterColor != null) {
            if (state.getValue(BLOTTER_COLOR) == blotterColor) {
                return InteractionResult.SUCCESS;
            }
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.setBlockAndUpdate(
                        pos,
                        state.setValue(BLOTTER_COLOR, blotterColor)
                );
                consumeOne(player, heldStack);
                serverLevel.playSound(
                        null,
                        pos,
                        SoundEvents.DYE_USE,
                        SoundSource.BLOCKS,
                        0.8F,
                        1.0F
                );
            }
            return InteractionResult.SUCCESS;
        }

        if (heldStack.is(Items.WRITABLE_BOOK)) {
            return installAccessory(
                    heldStack,
                    state,
                    level,
                    pos,
                    player,
                    HAS_LEDGER
            );
        }
        if (heldStack.is(Items.FILLED_MAP)) {
            if (state.getValue(HAS_MAP)) {
                return InteractionResult.SUCCESS;
            }
            if (level instanceof ServerLevel serverLevel
                    && serverLevel.getBlockEntity(pos)
                    instanceof EstateManagementDeskBlockEntity desk) {
                desk.setMap(heldStack);
                serverLevel.setBlockAndUpdate(
                        pos,
                        state.setValue(HAS_MAP, true)
                );
                consumeOne(player, heldStack);
                serverLevel.playSound(
                        null,
                        pos,
                        SoundEvents.ITEM_FRAME_ADD_ITEM,
                        SoundSource.BLOCKS,
                        0.8F,
                        1.0F
                );
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    public MapCodec<EstateManagementDeskBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        return new EstateManagementDeskBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(
                FACING,
                context.getHorizontalDirection().getOpposite()
        );
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

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (player.isShiftKeyDown()) {
            ItemStack recovered = ItemStack.EMPTY;
            BlockState updated = state;
            if (state.getValue(HAS_MAP)) {
                if (level.getBlockEntity(pos)
                        instanceof EstateManagementDeskBlockEntity desk) {
                    recovered = desk.takeMap();
                }
                if (recovered.isEmpty()) {
                    // Compatibility for desks using the earlier decorative
                    // empty-map state.
                    recovered = new ItemStack(Items.MAP);
                }
                updated = state.setValue(HAS_MAP, false);
            } else if (state.getValue(HAS_LEDGER)) {
                recovered = new ItemStack(Items.WRITABLE_BOOK);
                updated = state.setValue(HAS_LEDGER, false);
            }
            if (recovered.isEmpty()) {
                return InteractionResult.PASS;
            }
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.setBlockAndUpdate(pos, updated);
                Block.popResource(serverLevel, pos, recovered);
                serverLevel.playSound(
                        null,
                        pos,
                        SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                        SoundSource.BLOCKS,
                        0.8F,
                        1.0F
                );
            }
            return InteractionResult.SUCCESS;
        }
        if (level instanceof ServerLevel serverLevel
                && player instanceof ServerPlayer serverPlayer) {
            EstateDeskReport.open(serverLevel, pos, serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult installAccessory(
            ItemStack heldStack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BooleanProperty property
    ) {
        if (state.getValue(property)) {
            return InteractionResult.SUCCESS;
        }
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.setBlockAndUpdate(
                    pos,
                    state.setValue(property, true)
            );
            consumeOne(player, heldStack);
            serverLevel.playSound(
                    null,
                    pos,
                    SoundEvents.ITEM_FRAME_ADD_ITEM,
                    SoundSource.BLOCKS,
                    0.8F,
                    1.0F
            );
        }
        return InteractionResult.SUCCESS;
    }

    private static void consumeOne(Player player, ItemStack heldStack) {
        if (!player.getAbilities().instabuild) {
            heldStack.shrink(1);
        }
    }

    @Override
    protected List<ItemStack> getDrops(
            BlockState state,
            LootParams.Builder params
    ) {
        List<ItemStack> drops =
                new ArrayList<>(super.getDrops(state, params));
        if (state.getValue(HAS_LEDGER)) {
            drops.add(new ItemStack(Items.WRITABLE_BOOK));
        }
        if (state.getValue(HAS_MAP)) {
            BlockEntity blockEntity = params.getOptionalParameter(
                    LootContextParams.BLOCK_ENTITY
            );
            if (blockEntity
                    instanceof EstateManagementDeskBlockEntity desk) {
                ItemStack storedMap = desk.getMapCopy();
                drops.add(storedMap.isEmpty()
                        ? new ItemStack(Items.MAP)
                        : storedMap);
            } else {
                drops.add(new ItemStack(Items.MAP));
            }
        }
        return drops;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(FACING, BLOTTER_COLOR, HAS_LEDGER, HAS_MAP);
    }
}
