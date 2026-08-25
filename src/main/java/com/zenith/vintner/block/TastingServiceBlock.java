package com.zenith.vintner.block;

import com.mojang.serialization.MapCodec;
import com.zenith.vintner.advancement.ModAdvancements;
import com.zenith.vintner.block.entity.TastingServiceBlockEntity;
import com.zenith.vintner.item.VintnerAlmanacItem;
import com.zenith.vintner.item.WineItem;
import com.zenith.vintner.registry.ModBlockEntities;
import com.zenith.vintner.registry.ModItems;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
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

/**
 * One complete tabletop wine service. The block owns the bottle and its
 * metadata; the four integrated tasting cups are a visual serving counter.
 */
public final class TastingServiceBlock extends BaseEntityBlock {
    public static final MapCodec<TastingServiceBlock> CODEC =
            simpleCodec(TastingServiceBlock::new);
    public static final EnumProperty<Direction> FACING =
            BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty HAS_BOTTLE =
            BooleanProperty.create("has_bottle");
    public static final BooleanProperty WHITE_WINE =
            BooleanProperty.create("white_wine");
    public static final IntegerProperty SERVINGS =
            IntegerProperty.create("servings", 0, 4);

    private static final VoxelShape SHAPE = Block.box(
            1,
            0,
            1,
            15,
            10,
            15
    );

    public TastingServiceBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(
                stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(HAS_BOTTLE, false)
                        .setValue(WHITE_WINE, false)
                        .setValue(SERVINGS, 0)
        );
    }

    @Override
    public MapCodec<TastingServiceBlock> codec() {
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
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(
                FACING,
                context.getHorizontalDirection().getOpposite()
        );
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TastingServiceBlockEntity(pos, state);
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
    protected InteractionResult useItemOn(
            ItemStack heldStack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (!(level.getBlockEntity(pos)
                instanceof TastingServiceBlockEntity service)) {
            return InteractionResult.PASS;
        }

        if (heldStack.is(ModItems.VINTNER_ALMANAC)) {
            if (level instanceof ServerLevel serverLevel) {
                VintnerAlmanacItem.inspectPlacedWine(
                        serverLevel,
                        player,
                        service.getBottleCopy()
                );
            }
            return InteractionResult.SUCCESS;
        }

        if (heldStack.getItem() instanceof WineItem) {
            if (!(level instanceof ServerLevel serverLevel)) {
                return InteractionResult.SUCCESS;
            }

            if (!service.insertBottle(heldStack)) {
                player.sendOverlayMessage(
                        Component.translatable(
                                "message.vintner.tasting_service.occupied"
                        )
                );
                return InteractionResult.SUCCESS;
            }

            if (!player.getAbilities().instabuild) {
                heldStack.shrink(1);
            }

            serverLevel.playSound(
                    null,
                    pos,
                    SoundEvents.GLASS_PLACE,
                    SoundSource.BLOCKS,
                    0.75F,
                    1.0F
            );
            return InteractionResult.SUCCESS;
        }

        if (!heldStack.is(ModItems.WINE_GLASS)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        ItemStack serving = service.pourServing();

        if (serving.isEmpty()) {
            player.sendOverlayMessage(
                    Component.translatable(
                            "message.vintner.tasting_service.empty"
                    )
            );
            return InteractionResult.SUCCESS;
        }

        if (!player.getAbilities().instabuild) {
            heldStack.shrink(1);
        }

        if (!player.addItem(serving)) {
            popResource(serverLevel, pos, serving);
        }

        serverLevel.playSound(
                null,
                pos,
                SoundEvents.BOTTLE_FILL,
                SoundSource.BLOCKS,
                0.85F,
                1.1F
        );

        if (player instanceof ServerPlayer serverPlayer) {
            ModAdvancements.grantProperPour(serverPlayer);
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
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        if (!(level.getBlockEntity(pos)
                instanceof TastingServiceBlockEntity service)) {
            return InteractionResult.PASS;
        }

        ItemStack bottle = service.removeBottle();

        if (bottle.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!player.addItem(bottle)) {
            popResource(serverLevel, pos, bottle);
        }

        serverLevel.playSound(
                null,
                pos,
                SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                SoundSource.BLOCKS,
                0.65F,
                1.1F
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
                instanceof TastingServiceBlockEntity service) {
            ItemStack bottle = service.removeBottle();
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
        List<ItemStack> drops =
                new ArrayList<>(super.getDrops(state, params));
        BlockEntity blockEntity = params.getOptionalParameter(
                LootContextParams.BLOCK_ENTITY
        );

        if (blockEntity instanceof TastingServiceBlockEntity service) {
            ItemStack bottle = service.getBottleCopy();
            if (!bottle.isEmpty()) {
                drops.add(bottle);
            }
        }

        return drops;
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
        return state.getValue(SERVINGS) * 15 / 4;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(FACING, HAS_BOTTLE, WHITE_WINE, SERVINGS);
    }
}
