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
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
    public static final BooleanProperty CUP_1 =
            BooleanProperty.create("cup_1");
    public static final BooleanProperty CUP_2 =
            BooleanProperty.create("cup_2");
    public static final BooleanProperty CUP_3 =
            BooleanProperty.create("cup_3");
    public static final BooleanProperty CUP_4 =
            BooleanProperty.create("cup_4");
    public static final EnumProperty<DyeColor> LINEN =
            EnumProperty.create("linen", DyeColor.class);

    private static final double[] CUP_CENTERS_X = {
            3.0 / 16.0,
            6.35 / 16.0,
            9.65 / 16.0,
            13.0 / 16.0
    };
    private static final double CUP_CENTER_Z = 5.0 / 16.0;
    private static final double CUP_TARGET_RADIUS = 1.55 / 16.0;

    private static final VoxelShape PLATTER_SHAPE = Block.box(
            1,
            0,
            1,
            15,
            3.25,
            15
    );
    private static final VoxelShape NORTH_BOTTLE_SHAPE = Block.box(
            10.2,
            2,
            10,
            12.8,
            10,
            12.5
    );
    private static final VoxelShape EAST_BOTTLE_SHAPE = Block.box(
            3.5,
            2,
            10.2,
            6,
            10,
            12.8
    );
    private static final VoxelShape SOUTH_BOTTLE_SHAPE = Block.box(
            3.2,
            2,
            3.5,
            5.8,
            10,
            6
    );
    private static final VoxelShape WEST_BOTTLE_SHAPE = Block.box(
            10,
            2,
            3.2,
            12.5,
            10,
            5.8
    );

    public TastingServiceBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(
                stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(HAS_BOTTLE, false)
                        .setValue(WHITE_WINE, false)
                        .setValue(SERVINGS, 0)
                        .setValue(CUP_1, false)
                        .setValue(CUP_2, false)
                        .setValue(CUP_3, false)
                        .setValue(CUP_4, false)
                        .setValue(LINEN, DyeColor.RED)
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
        if (!state.getValue(HAS_BOTTLE)) {
            return PLATTER_SHAPE;
        }

        VoxelShape bottleShape = switch (state.getValue(FACING)) {
            case EAST -> EAST_BOTTLE_SHAPE;
            case SOUTH -> SOUTH_BOTTLE_SHAPE;
            case WEST -> WEST_BOTTLE_SHAPE;
            default -> NORTH_BOTTLE_SHAPE;
        };
        return Shapes.or(PLATTER_SHAPE, bottleShape);
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

        DyeColor dyeColor = dyeColor(heldStack);
        if (dyeColor != null) {
            if (state.getValue(LINEN) == dyeColor) {
                return InteractionResult.SUCCESS;
            }
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.setBlockAndUpdate(
                        pos,
                        state.setValue(LINEN, dyeColor)
                );
                if (!player.getAbilities().instabuild) {
                    heldStack.shrink(1);
                }
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

        return InteractionResult.TRY_WITH_EMPTY_HAND;
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

        if (!(level.getBlockEntity(pos)
                instanceof TastingServiceBlockEntity service)) {
            return InteractionResult.PASS;
        }

        if (service.hasEmptyBottle()) {
            return returnBottle(serverLevel, pos, player, service);
        }

        if (player.isShiftKeyDown()) {
            return returnBottle(serverLevel, pos, player, service);
        }

        int cupIndex = cupAt(state, pos, hitResult);
        if (cupIndex < 0) {
            player.sendOverlayMessage(
                    Component.translatable(
                            "message.vintner.tasting_service.select_cup"
                    )
            );
            return InteractionResult.SUCCESS;
        }

        ItemStack serving = service.pourServing(cupIndex);

        if (serving.isEmpty()) {
            player.sendOverlayMessage(
                    Component.translatable(
                            service.servings() <= 0
                                    ? "message.vintner.tasting_service.empty"
                                    : "message.vintner.tasting_service.cup_empty"
                    )
            );
            return InteractionResult.SUCCESS;
        }

        WineItem.consumeServing(serverLevel, player, serving);
        serverLevel.playSound(
                null,
                pos,
                SoundEvents.GENERIC_DRINK.value(),
                SoundSource.PLAYERS,
                0.8F,
                1.0F
        );
        if (player instanceof ServerPlayer serverPlayer) {
            ModAdvancements.grantProperPour(serverPlayer);
            boolean joined = service.recordDrinker(
                    serverPlayer.getUUID()
            );
            int guests = service.drinkerCount();
            if (joined && guests == 4) {
                grantSharedVintage(serverLevel, service.drinkers());
            } else if (joined && guests > 1) {
                player.sendOverlayMessage(
                        Component.translatable(
                                "message.vintner.tasting_service.guests",
                                guests,
                                4
                        )
                );
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static void grantSharedVintage(
            ServerLevel level,
            Set<UUID> drinkers
    ) {
        for (UUID uuid : drinkers) {
            ServerPlayer participant = level.getServer()
                    .getPlayerList()
                    .getPlayer(uuid);
            if (participant != null) {
                ModAdvancements.grantGoodCompany(participant);
                participant.sendOverlayMessage(
                        Component.translatable(
                                "message.vintner.tasting_service.good_company"
                        )
                );
            }
        }
    }

    private static int cupAt(
            BlockState state,
            BlockPos pos,
            BlockHitResult hitResult
    ) {
        double worldX = hitResult.getLocation().x - pos.getX();
        double worldZ = hitResult.getLocation().z - pos.getZ();
        double localX;
        double localZ;

        switch (state.getValue(FACING)) {
            case EAST -> {
                localX = worldZ;
                localZ = 1.0 - worldX;
            }
            case SOUTH -> {
                localX = 1.0 - worldX;
                localZ = 1.0 - worldZ;
            }
            case WEST -> {
                localX = 1.0 - worldZ;
                localZ = worldX;
            }
            default -> {
                localX = worldX;
                localZ = worldZ;
            }
        }

        if (Math.abs(localZ - CUP_CENTER_Z) > CUP_TARGET_RADIUS) {
            return -1;
        }

        for (int index = 0; index < CUP_CENTERS_X.length; index++) {
            if (Math.abs(localX - CUP_CENTERS_X[index])
                    <= CUP_TARGET_RADIUS) {
                return index;
            }
        }
        return -1;
    }

    @Nullable
    private static DyeColor dyeColor(ItemStack stack) {
        for (DyeColor color : DyeColor.values()) {
            if (stack.is(Items.DYE.pick(color))) {
                return color;
            }
        }
        return null;
    }

    private static InteractionResult returnBottle(
            ServerLevel level,
            BlockPos pos,
            Player player,
            TastingServiceBlockEntity service
    ) {
        ItemStack bottle = service.removeBottle();

        if (bottle.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!player.addItem(bottle)) {
            popResource(level, pos, bottle);
        }

        level.playSound(
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

        BlockItemStateProperties itemState =
                BlockItemStateProperties.EMPTY.with(LINEN, state);
        for (ItemStack drop : drops) {
            if (drop.is(state.getBlock().asItem())) {
                drop.set(DataComponents.BLOCK_STATE, itemState);
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
        builder.add(
                FACING,
                HAS_BOTTLE,
                WHITE_WINE,
                SERVINGS,
                CUP_1,
                CUP_2,
                CUP_3,
                CUP_4,
                LINEN
        );
    }
}
