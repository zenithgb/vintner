package com.zenith.vintner.block;

import com.mojang.serialization.MapCodec;
import com.zenith.vintner.estate.EstateLedgerSavedData;
import com.zenith.vintner.estate.EstateReputationSavedData;
import com.zenith.vintner.estate.EstateSavedData;
import com.zenith.vintner.estate.WineContractSavedData;
import com.zenith.vintner.vineyard.TerroirEvaluator;
import com.zenith.vintner.wine.WineMarketRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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

/** A villager-independent source of public wine orders. */
public final class VillageContractBoardBlock extends Block {
    public static final EnumProperty<Direction> FACING =
            BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<WoodVariant> WOOD =
            EnumProperty.create("wood", WoodVariant.class);
    public static final MapCodec<VillageContractBoardBlock> CODEC =
            simpleCodec(VillageContractBoardBlock::new);

    public VillageContractBoardBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WOOD, WoodVariant.OAK));
    }

    @Override
    public MapCodec<VillageContractBoardBlock> codec() {
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
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (!(level instanceof ServerLevel serverLevel)
                || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        if (EstateSavedData.get(serverLevel)
                .find(serverPlayer.getUUID()).isEmpty()) {
            serverPlayer.connection.send(
                    new ClientboundSetActionBarTextPacket(
                            Component.translatable(
                                    "block.vintner.village_contract_board.no_estate"
                            )
                    )
            );
            return InteractionResult.SUCCESS;
        }
        var reputation = EstateReputationSavedData.get(serverLevel)
                .syncFromLedger(
                        serverPlayer.getUUID(),
                        EstateLedgerSavedData.get(serverLevel)
                                .entries(serverPlayer.getUUID())
                );
        var market = WineMarketRegion.from(
                serverLevel,
                pos,
                TerroirEvaluator.inspect(serverLevel, pos)
        );
        var offers = WineContractSavedData.get(serverLevel)
                .currentContracts(
                        serverLevel,
                        serverPlayer.getUUID(),
                        market,
                        reputation.score(),
                        true
                );
        serverPlayer.connection.send(
                new ClientboundSetActionBarTextPacket(
                        Component.translatable(
                                "block.vintner.village_contract_board.posted",
                                offers.size()
                        )
                )
        );
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(FACING, WOOD);
    }
}
