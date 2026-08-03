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
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Physical dispatch point for fulfilling accepted estate wine orders. */
public final class TradeCorrespondenceBoardBlock extends Block {
    public static final EnumProperty<Direction> FACING =
            BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DeskModuleConnection> CONNECTION =
            EnumProperty.create(
                    "connection",
                    DeskModuleConnection.class
            );
    public static final MapCodec<TradeCorrespondenceBoardBlock> CODEC =
            simpleCodec(TradeCorrespondenceBoardBlock::new);
    private static final VoxelShape NORTH_SOUTH_SHAPE = Shapes.or(
            Block.box(0.75, 9, 1.25, 15.25, 16, 14.25),
            Block.box(1.25, 0, 1.75, 2.75, 10, 3.5),
            Block.box(13.25, 0, 1.75, 14.75, 10, 3.5),
            Block.box(1.25, 0, 12.5, 2.75, 10, 14.25),
            Block.box(13.25, 0, 12.5, 14.75, 10, 14.25)
    );
    private static final VoxelShape EAST_WEST_SHAPE = Shapes.or(
            Block.box(1.25, 9, 0.75, 14.75, 16, 15.25),
            Block.box(1.75, 0, 1.25, 3.5, 10, 2.75),
            Block.box(12.5, 0, 1.25, 14.25, 10, 2.75),
            Block.box(1.75, 0, 13.25, 3.5, 10, 14.75),
            Block.box(12.5, 0, 13.25, 14.25, 10, 14.75)
    );

    public TradeCorrespondenceBoardBlock(
            BlockBehaviour.Properties properties
    ) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(CONNECTION, DeskModuleConnection.NONE));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        return updateAttachment(
                defaultBlockState().setValue(FACING, facing),
                context.getLevel(),
                context.getClickedPos()
        );
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction directionToNeighbour,
            BlockPos neighbourPos,
            BlockState neighbourState,
            RandomSource random
    ) {
        if (directionToNeighbour.getAxis().isHorizontal()) {
            return updateAttachment(state, level, pos);
        }
        return super.updateShape(
                state,
                level,
                ticks,
                pos,
                directionToNeighbour,
                neighbourPos,
                neighbourState,
                random
        );
    }

    private static BlockState updateAttachment(
            BlockState state,
            LevelReader level,
            BlockPos pos
    ) {
        DeskModuleConnection.Attachment attachment =
                DeskModuleConnection.find(
                        level,
                        pos,
                        state.getValue(FACING)
                );
        return state
                .setValue(FACING, attachment.facing())
                .setValue(CONNECTION, attachment.connection());
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return shapeFor(state);
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return shapeFor(state);
    }

    private static VoxelShape shapeFor(BlockState state) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z
                ? NORTH_SOUTH_SHAPE
                : EAST_WEST_SHAPE;
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
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable(
                    "message.vintner.desk_module.use_desk"
            ));
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
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable(
                    "message.vintner.desk_module.use_desk"
            ));
        }
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult dispatchWine(
            ServerLevel level,
            BlockPos deskPos,
            ServerPlayer player,
            ItemStack stack
    ) {
        WineContractSavedData.Delivery delivery =
                WineContractSavedData.get(level).deliver(
                        level,
                        player.getUUID(),
                        stack
                );
        switch (delivery.result()) {
            case NO_ACTIVE_CONTRACT -> message(
                    player,
                    "block.vintner.trade_correspondence_board.no_contract",
                    ChatFormatting.GRAY
            );
            case MISMATCH -> message(
                    player,
                    "block.vintner.trade_correspondence_board.mismatch",
                    ChatFormatting.RED
            );
            case ACCEPTED -> {
                consumeOne(player, stack);
                WineContract contract = delivery.contract();
                level.playSound(
                        null,
                        deskPos,
                        SoundEvents.BOOK_PAGE_TURN,
                        SoundSource.BLOCKS,
                        0.8F,
                        1.1F
                );
                player.sendSystemMessage(
                        Component.translatable(
                                "block.vintner.trade_correspondence_board.accepted",
                                contract.deliveredBottles(),
                                contract.requiredBottles()
                        ).withStyle(ChatFormatting.GREEN)
                );
            }
            case COMPLETED -> complete(
                    level,
                    deskPos,
                    player,
                    stack,
                    delivery.contract()
            );
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
        builder.add(FACING, CONNECTION);
    }
}
