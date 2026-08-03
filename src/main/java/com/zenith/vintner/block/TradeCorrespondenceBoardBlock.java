package com.zenith.vintner.block;

import com.mojang.serialization.MapCodec;
import com.zenith.vintner.estate.EstateLedgerSavedData;
import com.zenith.vintner.estate.LedgerEventType;
import com.zenith.vintner.estate.WineContract;
import com.zenith.vintner.estate.WineContractSavedData;
import com.zenith.vintner.item.WineItem;
import com.zenith.vintner.wine.WineMetadata;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Physical dispatch point for fulfilling accepted estate wine orders. */
public final class TradeCorrespondenceBoardBlock extends Block {
    public static final EnumProperty<Direction> FACING =
            BlockStateProperties.HORIZONTAL_FACING;
    public static final MapCodec<TradeCorrespondenceBoardBlock> CODEC =
            simpleCodec(TradeCorrespondenceBoardBlock::new);

    public TradeCorrespondenceBoardBlock(
            BlockBehaviour.Properties properties
    ) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
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
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (!(stack.getItem() instanceof WineItem)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!(level instanceof ServerLevel serverLevel)
                || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        WineContractSavedData.Delivery delivery =
                WineContractSavedData.get(serverLevel).deliver(
                        serverLevel,
                        serverPlayer.getUUID(),
                        stack
                );
        switch (delivery.result()) {
            case NO_ACTIVE_CONTRACT -> message(
                    serverPlayer,
                    "block.vintner.trade_correspondence_board.no_contract",
                    ChatFormatting.GRAY
            );
            case MISMATCH -> message(
                    serverPlayer,
                    "block.vintner.trade_correspondence_board.mismatch",
                    ChatFormatting.RED
            );
            case ACCEPTED -> {
                consumeOne(serverPlayer, stack);
                WineContract contract = delivery.contract();
                serverLevel.playSound(
                        null,
                        pos,
                        SoundEvents.BOOK_PAGE_TURN,
                        SoundSource.BLOCKS,
                        0.8F,
                        1.1F
                );
                serverPlayer.sendSystemMessage(
                        Component.translatable(
                                "block.vintner.trade_correspondence_board.accepted",
                                contract.deliveredBottles(),
                                contract.requiredBottles()
                        ).withStyle(ChatFormatting.GREEN)
                );
            }
            case COMPLETED -> complete(
                    serverLevel,
                    pos,
                    serverPlayer,
                    stack,
                    delivery.contract()
            );
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (level instanceof ServerLevel serverLevel
                && player instanceof ServerPlayer serverPlayer) {
            WineContract active = WineContractSavedData.get(serverLevel)
                    .active(serverPlayer.getUUID());
            if (active == null) {
                message(
                        serverPlayer,
                        "block.vintner.trade_correspondence_board.no_contract",
                        ChatFormatting.GRAY
                );
            } else {
                serverPlayer.sendSystemMessage(
                        Component.translatable(
                                "block.vintner.trade_correspondence_board.status",
                                active.partner().displayName(),
                                active.deliveredBottles(),
                                active.requiredBottles()
                        ).withStyle(ChatFormatting.GOLD)
                );
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static void complete(
            ServerLevel level,
            BlockPos pos,
            ServerPlayer player,
            ItemStack bottle,
            WineContract contract
    ) {
        int quality = WineMetadata.qualityScore(bottle);
        long batch = WineMetadata.batchId(bottle);
        consumeOne(player, bottle);
        ItemStack reward = new ItemStack(
                Items.EMERALD,
                contract.rewardEmeralds()
        );
        if (!player.getInventory().add(reward)) {
            Block.popResource(level, pos.above(), reward);
        }
        EstateLedgerSavedData.get(level).record(
                player,
                LedgerEventType.CONTRACT_COMPLETED,
                contract.partner().fallbackName(),
                1,
                batch,
                quality
        );
        level.playSound(
                null,
                pos,
                SoundEvents.PLAYER_LEVELUP,
                SoundSource.BLOCKS,
                0.8F,
                1.15F
        );
        player.sendSystemMessage(
                Component.translatable(
                        "block.vintner.trade_correspondence_board.completed",
                        contract.partner().displayName(),
                        contract.rewardEmeralds()
                ).withStyle(ChatFormatting.GOLD)
        );
    }

    private static void consumeOne(Player player, ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    private static void message(
            ServerPlayer player,
            String key,
            ChatFormatting color
    ) {
        player.sendSystemMessage(
                Component.translatable(key).withStyle(color)
        );
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(FACING);
    }
}
