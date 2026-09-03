package com.zenith.vintner.block;

import com.zenith.vintner.util.VintnerNotifications;
import com.mojang.serialization.MapCodec;
import com.zenith.vintner.block.entity.VintageArchiveBlockEntity;
import com.zenith.vintner.estate.EstateProfile;
import com.zenith.vintner.estate.EstateLedgerEvent;
import com.zenith.vintner.estate.EstateLedgerSavedData;
import com.zenith.vintner.estate.EstateSavedData;
import com.zenith.vintner.estate.LedgerEventType;
import com.zenith.vintner.item.WineItem;
import com.zenith.vintner.registry.ModBlockEntities;
import com.zenith.vintner.registry.ModItems;
import com.zenith.vintner.wine.WineMetadata;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class VintageArchiveBlock extends BaseEntityBlock {
    public static final MapCodec<VintageArchiveBlock> CODEC =
            simpleCodec(VintageArchiveBlock::new);
    public static final EnumProperty<Direction> FACING =
            BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape NORTH_SOUTH_SHAPE =
            Block.box(1, 0, 2, 15, 16, 14);
    private static final VoxelShape EAST_WEST_SHAPE =
            Block.box(2, 0, 1, 14, 16, 15);

    public VintageArchiveBlock(
            BlockBehaviour.Properties properties
    ) {
        super(properties);
        registerDefaultState(
                stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    public MapCodec<VintageArchiveBlock> codec() {
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
        return state.getValue(FACING).getAxis()
                == Direction.Axis.Z
                ? NORTH_SOUTH_SHAPE
                : EAST_WEST_SHAPE;
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
        return new VintageArchiveBlockEntity(pos, state);
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

        if (!(blockEntity
                instanceof VintageArchiveBlockEntity archive)) {
            return InteractionResult.PASS;
        }

        if (heldStack.is(ModItems.VINTNER_ALMANAC)) {
            if (level instanceof ServerLevel serverLevel) {
                useAlmanac(
                        serverLevel,
                        pos,
                        player,
                        hand,
                        heldStack
                );
            }
            return InteractionResult.SUCCESS;
        }

        if (!(heldStack.getItem() instanceof WineItem)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        WineMetadata.ensureDefaults(heldStack);
        WineMetadata.ensureBatchIdentity(
                heldStack,
                WineMetadata.createBatchId(
                        level.getGameTime(),
                        pos
                )
        );

        VintageArchiveBlockEntity.RecordResult result =
                archive.record(heldStack);

        if (result == VintageArchiveBlockEntity.RecordResult.ADDED
                && player instanceof net.minecraft.server.level.ServerPlayer
                        owner) {
            EstateLedgerSavedData.get(serverLevel).recordWine(
                    owner,
                    LedgerEventType.ARCHIVED,
                    heldStack,
                    1
            );
        }

        VintnerNotifications.send(player,
                Component.translatable(
                        result.translationKey(),
                        WineMetadata.batchCode(heldStack),
                        archive.getRecordCount(),
                        VintageArchiveBlockEntity.CAPACITY
                )
        );

        if (result
                != VintageArchiveBlockEntity.RecordResult.FULL) {
            serverLevel.playSound(
                    null,
                    pos,
                    SoundEvents.BOOK_PAGE_TURN,
                    SoundSource.BLOCKS,
                    0.8F,
                    result
                            == VintageArchiveBlockEntity
                                    .RecordResult.ADDED
                            ? 1.0F
                            : 1.2F
            );
        }

        return InteractionResult.SUCCESS;
    }

    public static void useAlmanac(
            ServerLevel level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            ItemStack almanac
    ) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer owner)) {
            return;
        }

        var existing = EstateSavedData.get(level).find(owner.getUUID());
        Component customName = almanac.getCustomName();
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        boolean hasCrest = player.getItemInHand(otherHand)
                .getItem() instanceof BannerItem;
        boolean changesName = existing.isPresent()
                && customName != null
                && !existing.get().estateName()
                        .equalsIgnoreCase(customName.getString());

        if (existing.isPresent() && !changesName && !hasCrest
                && level.getBlockEntity(pos)
                        instanceof VintageArchiveBlockEntity archive) {
            archive.reportNext(player);
            return;
        }

        registerEstate(level, pos, player, hand, almanac);
    }

    public static void registerEstate(
            ServerLevel level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            ItemStack almanac
    ) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer owner)) {
            return;
        }

        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        ItemStack crestStack = player.getItemInHand(otherHand);
        DyeColor crest = crestStack.getItem() instanceof BannerItem banner
                ? banner.getColor()
                : null;
        EstateSavedData estates = EstateSavedData.get(level);
        var existing = estates.find(owner.getUUID());
        Component customName = almanac.getCustomName();
        if (existing.isPresent() && customName == null) {
            VintnerNotifications.send(owner, Component.translatable(
                    "message.vintner.estate.already_registered",
                    existing.get().estateName()
            ).withStyle(net.minecraft.ChatFormatting.GRAY), true);
            return;
        }

        boolean updating = existing.isPresent();
        String requestedName = customName == null
                ? owner.getGameProfile().name() + "'s Estate"
                : customName.getString();
        EstateProfile profile = estates.register(
                owner,
                level,
                pos,
                requestedName,
                crest
        );
        EstateLedgerSavedData.get(level).record(
                owner,
                updating
                        ? LedgerEventType.ESTATE_RENAMED
                        : LedgerEventType.FOUNDING,
                profile.estateName(),
                1,
                0L,
                0
        );

        VintnerNotifications.send(player, Component.translatable(
                updating
                        ? "message.vintner.estate.updated"
                        : "message.vintner.estate.registered",
                profile.estateName(),
                profile.foundingYear(),
                profile.homeRegionDisplayName()
        ));
        if (!updating && customName == null) {
            VintnerNotifications.send(owner, Component.translatable(
                    "message.vintner.estate.custom_name_hint"
            ).withStyle(net.minecraft.ChatFormatting.GRAY), true);
        }
        level.playSound(
                null,
                pos,
                SoundEvents.BOOK_PAGE_TURN,
                SoundSource.BLOCKS,
                1.0F,
                updating ? 1.2F : 0.9F
        );
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (level instanceof ServerLevel serverLevel
                && player instanceof net.minecraft.server.level.ServerPlayer
                        owner
                && player.isShiftKeyDown()) {
            sendLedgerSummary(serverLevel, owner);
        } else if (level instanceof ServerLevel
                && level.getBlockEntity(pos)
                instanceof VintageArchiveBlockEntity archive) {
            VintnerNotifications.send(player,
                    Component.translatable(
                            "message.vintner.vintage_archive.summary",
                            archive.getRecordCount(),
                            VintageArchiveBlockEntity.CAPACITY
                    )
            );
        }

        return InteractionResult.SUCCESS;
    }

    private static void sendLedgerSummary(
            ServerLevel level,
            net.minecraft.server.level.ServerPlayer owner
    ) {
        if (EstateSavedData.get(level).find(owner.getUUID()).isEmpty()) {
            VintnerNotifications.send(owner, Component.translatable(
                    "message.vintner.ledger.unregistered"
            ));
            return;
        }

        EstateLedgerSavedData ledger = EstateLedgerSavedData.get(level);
        List<EstateLedgerEvent> entries = ledger.entries(owner.getUUID());
        VintnerNotifications.send(owner, Component.translatable(
                "message.vintner.ledger.summary",
                entries.size(),
                EstateLedgerSavedData.MAX_EVENTS_PER_ESTATE
        ));
        if (entries.isEmpty()) {
            VintnerNotifications.send(owner, Component.translatable(
                    "message.vintner.ledger.empty"
            ));
            return;
        }

        EstateLedgerEvent best = ledger.bestVintage(owner.getUUID());
        if (best != null) {
            VintnerNotifications.send(owner, Component.translatable(
                    "message.vintner.ledger.best",
                    best.detail(),
                    best.quality()
            ));
        }

        entries.stream().limit(5).forEach(event -> {
            Component type = Component.translatable(
                    event.eventType().translationKey()
            );
            if (event.quality() > 0 && event.amount() > 1) {
                VintnerNotifications.send(owner, Component.translatable(
                        "message.vintner.ledger.entry.amount_quality",
                        event.day(),
                        type,
                        event.detail(),
                        event.amount(),
                        event.quality()
                ));
            } else if (event.quality() > 0) {
                VintnerNotifications.send(owner, Component.translatable(
                        "message.vintner.ledger.entry.quality",
                        event.day(),
                        type,
                        event.detail(),
                        event.quality()
                ));
            } else if (event.amount() > 1) {
                VintnerNotifications.send(owner, Component.translatable(
                        "message.vintner.ledger.entry.amount",
                        event.day(),
                        type,
                        event.detail(),
                        event.amount()
                ));
            } else {
                VintnerNotifications.send(owner, Component.translatable(
                        "message.vintner.ledger.entry",
                        event.day(),
                        type,
                        event.detail()
                ));
            }
        });
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
        return level.getBlockEntity(pos)
                instanceof VintageArchiveBlockEntity archive
                ? archive.getComparatorSignal()
                : 0;
    }

    @Override
    protected List<ItemStack> getDrops(
            BlockState state,
            LootParams.Builder params
    ) {
        List<ItemStack> drops = super.getDrops(state, params);
        BlockEntity blockEntity = params.getOptionalParameter(
                LootContextParams.BLOCK_ENTITY
        );

        if (!(blockEntity
                instanceof VintageArchiveBlockEntity archive)
                || archive.getRecordCount() == 0) {
            return drops;
        }

        TagValueOutput output = TagValueOutput.createWithContext(
                ProblemReporter.DISCARDING,
                params.getLevel().registryAccess()
        );
        archive.saveCustomOnly(output);

        for (ItemStack drop : drops) {
            if (drop.is(state.getBlock().asItem())) {
                BlockItem.setBlockEntityData(
                        drop,
                        ModBlockEntities.VINTAGE_ARCHIVE,
                        output
                );
                break;
            }
        }

        return drops;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(FACING);
    }
}
