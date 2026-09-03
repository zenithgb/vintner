package com.zenith.vintner.block;

import com.zenith.vintner.util.VintnerNotifications;
import com.mojang.serialization.MapCodec;
import com.zenith.vintner.advancement.ModAdvancements;
import com.zenith.vintner.block.entity.AgingBarrelBlockEntity;
import com.zenith.vintner.estate.EstateLedgerSavedData;
import com.zenith.vintner.estate.LedgerEventType;
import com.zenith.vintner.registry.ModBlockEntities;
import com.zenith.vintner.registry.ModItems;
import com.zenith.vintner.wine.AgingVessel;
import com.zenith.vintner.wine.WinemakingFeedback;
import com.zenith.vintner.wine.AlmanacInspection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.ChatFormatting;
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

public class AgingBarrelBlock extends BaseEntityBlock {
    public static final MapCodec<AgingBarrelBlock> CODEC =
            simpleCodec(AgingBarrelBlock::new);

    public static final EnumProperty<Direction> FACING =
            BlockStateProperties.HORIZONTAL_FACING;

    public static final IntegerProperty STATUS =
            IntegerProperty.create("status", 0, 2);

    public static final IntegerProperty WINE_TYPE =
            IntegerProperty.create("wine_type", 0, 2);

    public static final EnumProperty<AgingVessel> VESSEL =
            EnumProperty.create("vessel", AgingVessel.class);

    private final AgingVessel vessel;

    public AgingBarrelBlock(
            BlockBehaviour.Properties properties
    ) {
        this(AgingVessel.OAK, properties);
    }

    public AgingBarrelBlock(
            AgingVessel vessel,
            BlockBehaviour.Properties properties
    ) {
        super(properties);
        this.vessel = vessel;

        registerDefaultState(
                stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(STATUS, 0)
                        .setValue(WINE_TYPE, 0)
                        .setValue(VESSEL, vessel)
        );
    }

    public AgingVessel vessel() {
        return vessel;
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
        InteractionHand malletHand = findMalletHand(player);

        if (malletHand != null && player.isSecondaryUseActive()) {
            return removeCooperageTreatment(
                    player.getItemInHand(malletHand),
                    malletHand,
                    state,
                    level,
                    pos,
                    player
            );
        }

        CooperageUse cooperageUse = findCooperageUse(
                player,
                hand,
                heldStack
        );

        if (cooperageUse != null) {
            return applyCooperageKit(
                    cooperageUse,
                    state,
                    level,
                    pos,
                    player
            );
        }

        if (vesselForKit(heldStack) != null) {
            if (!level.isClientSide()) {
                VintnerNotifications.send(player, Component.translatable(
                        "message.vintner.aging.mallet_required"
                ).withStyle(ChatFormatting.YELLOW));
            }
            return InteractionResult.SUCCESS;
        }

        if (heldStack.is(ModItems.VINTNER_ALMANAC)) {
            if (level instanceof ServerLevel serverLevel
                    && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                AlmanacInspection.inspect(
                        serverLevel,
                        pos,
                        serverPlayer
                ).open(serverPlayer);
            }
            return InteractionResult.SUCCESS;
        }

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

    private InteractionResult applyCooperageKit(
            CooperageUse cooperageUse,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player
    ) {
        if (vessel != AgingVessel.OAK) {
            return InteractionResult.PASS;
        }

        if (!(level.getBlockEntity(pos)
                instanceof AgingBarrelBlockEntity barrel)) {
            return InteractionResult.PASS;
        }

        AgingVessel requestedVessel = cooperageUse.requestedVessel();
        AgingVessel activeVessel = state.getValue(VESSEL);

        if (activeVessel != AgingVessel.OAK) {
            if (!level.isClientSide()) {
                String message = activeVessel == requestedVessel
                        ? "message.vintner.aging.upgrade_already_applied"
                        : "message.vintner.aging.upgrade_recover_first";
                VintnerNotifications.send(player, Component.translatable(
                        message,
                        activeVessel.displayName()
                ).withStyle(ChatFormatting.YELLOW));
            }
            return InteractionResult.SUCCESS;
        }

        if (!barrel.isEmpty()) {
            if (!level.isClientSide()) {
                VintnerNotifications.send(player, Component.translatable(
                        "message.vintner.aging.upgrade_empty_required"
                ).withStyle(ChatFormatting.RED));
            }
            return InteractionResult.SUCCESS;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        serverLevel.setBlock(
                pos,
                state.setValue(VESSEL, requestedVessel),
                Block.UPDATE_ALL
        );

        if (!player.getAbilities().instabuild) {
            cooperageUse.kit().shrink(1);
            cooperageUse.mallet().hurtAndBreak(
                    1,
                    player,
                    cooperageUse.malletHand()
            );
        }

        serverLevel.playSound(
                null,
                pos,
                SoundEvents.ANVIL_USE,
                SoundSource.BLOCKS,
                0.65F,
                1.15F
        );
        VintnerNotifications.send(player, Component.translatable(
                "message.vintner.aging.upgrade_applied",
                requestedVessel.displayName()
        ).withStyle(ChatFormatting.GREEN));

        return InteractionResult.SUCCESS;
    }

    public InteractionResult removeCooperageTreatment(
            ItemStack mallet,
            InteractionHand malletHand,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player
    ) {
        if (vessel != AgingVessel.OAK) {
            return InteractionResult.PASS;
        }

        if (!(level.getBlockEntity(pos)
                instanceof AgingBarrelBlockEntity barrel)) {
            return InteractionResult.PASS;
        }

        AgingVessel activeVessel = state.getValue(VESSEL);

        if (activeVessel == AgingVessel.OAK) {
            if (!level.isClientSide()) {
                VintnerNotifications.send(player, Component.translatable(
                        "message.vintner.aging.treatment_none"
                ).withStyle(ChatFormatting.YELLOW));
            }
            return InteractionResult.SUCCESS;
        }

        if (!barrel.isEmpty()) {
            if (!level.isClientSide()) {
                VintnerNotifications.send(player, Component.translatable(
                        "message.vintner.aging.upgrade_empty_required"
                ).withStyle(ChatFormatting.RED));
            }
            return InteractionResult.SUCCESS;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        ItemStack recoveredKit = kitForVessel(activeVessel);

        serverLevel.setBlock(
                pos,
                state.setValue(VESSEL, AgingVessel.OAK),
                Block.UPDATE_ALL
        );

        if (!recoveredKit.isEmpty()
                && !player.addItem(recoveredKit)) {
            Block.popResource(serverLevel, pos, recoveredKit);
        }

        if (!player.getAbilities().instabuild) {
            mallet.hurtAndBreak(1, player, malletHand);
        }

        serverLevel.playSound(
                null,
                pos,
                SoundEvents.ANVIL_USE,
                SoundSource.BLOCKS,
                0.55F,
                0.8F
        );
        VintnerNotifications.send(player, Component.translatable(
                "message.vintner.aging.treatment_removed",
                activeVessel.displayName()
        ).withStyle(ChatFormatting.GREEN));

        return InteractionResult.SUCCESS;
    }

    @Nullable
    private static InteractionHand findMalletHand(Player player) {
        if (player.getMainHandItem().is(ModItems.COOPERS_MALLET)) {
            return InteractionHand.MAIN_HAND;
        }
        if (player.getOffhandItem().is(ModItems.COOPERS_MALLET)) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    @Nullable
    private static CooperageUse findCooperageUse(
            Player player,
            InteractionHand usedHand,
            ItemStack heldStack
    ) {
        InteractionHand otherHand = usedHand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);

        if (heldStack.is(ModItems.COOPERS_MALLET)) {
            AgingVessel requestedVessel = vesselForKit(otherStack);

            return requestedVessel == null
                    ? null
                    : new CooperageUse(
                            heldStack,
                            usedHand,
                            otherStack,
                            requestedVessel
                    );
        }

        AgingVessel requestedVessel = vesselForKit(heldStack);

        if (requestedVessel != null
                && otherStack.is(ModItems.COOPERS_MALLET)) {
            return new CooperageUse(
                    otherStack,
                    otherHand,
                    heldStack,
                    requestedVessel
            );
        }

        return null;
    }

    private static AgingVessel vesselForKit(ItemStack stack) {
        if (stack.is(ModItems.TOASTING_KIT)) {
            return AgingVessel.CHESTNUT;
        }
        if (stack.is(ModItems.SEASONING_KIT)) {
            return AgingVessel.NEUTRAL;
        }
        if (stack.is(ModItems.CASK_CONVERSION_KIT)) {
            return AgingVessel.LARGE_CASK;
        }
        return null;
    }

    private static ItemStack kitForVessel(AgingVessel vessel) {
        return switch (vessel) {
            case CHESTNUT -> new ItemStack(ModItems.TOASTING_KIT);
            case NEUTRAL -> new ItemStack(ModItems.SEASONING_KIT);
            case LARGE_CASK -> new ItemStack(
                    ModItems.CASK_CONVERSION_KIT
            );
            case OAK -> ItemStack.EMPTY;
        };
    }

    private record CooperageUse(
            ItemStack mallet,
            InteractionHand malletHand,
            ItemStack kit,
            AgingVessel requestedVessel
    ) {
    }

    private void showVesselGuide(
            Player player,
            AgingVessel activeVessel
    ) {
        VintnerNotifications.send(player,
                Component.translatable(
                        "message.vintner.almanac.vessel_guide",
                        activeVessel.displayName()
                ).withStyle(ChatFormatting.GOLD)
        );
        VintnerNotifications.send(player,
                Component.translatable(
                        "message.vintner.almanac.vessel_capacity",
                        activeVessel.capacity(),
                        activeVessel.agingTimeSeconds()
                ).withStyle(ChatFormatting.GRAY)
        );
        VintnerNotifications.send(player,
                activeVessel.guide()
                        .copy()
                        .withStyle(ChatFormatting.GRAY)
        );
        VintnerNotifications.send(player,
                activeVessel.craftingHint()
                        .copy()
                        .withStyle(ChatFormatting.DARK_GRAY)
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
            EstateLedgerSavedData.get(serverLevel).recordWine(
                    serverPlayer,
                    LedgerEventType.BOTTLING,
                    agedWine,
                    1
            );
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

        if (vessel == AgingVessel.OAK) {
            ItemStack kit = kitForVessel(state.getValue(VESSEL));

            if (!kit.isEmpty()) {
                drops.add(kit);
            }
        }

        return drops;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(FACING, STATUS, WINE_TYPE, VESSEL);
    }
}
