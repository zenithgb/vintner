package com.zenith.vintner.item;

import com.zenith.vintner.advancement.ModAdvancements;
import com.zenith.vintner.block.GrapevineBlock;
import com.zenith.vintner.block.TrellisBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.function.Supplier;

public final class GrapeCuttingItem extends Item {
    private final Supplier<Block> grapevineSupplier;

    public GrapeCuttingItem(
            Supplier<Block> grapevineSupplier,
            Properties properties
    ) {
        super(properties);
        this.grapevineSupplier = grapevineSupplier;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        if (!(clickedState.getBlock() instanceof TrellisBlock)
                || clickedState.getBlock() instanceof GrapevineBlock) {
            return super.useOn(context);
        }

        if (level.getBlockState(clickedPos.below()).getBlock()
                instanceof GrapevineBlock) {
            return InteractionResult.FAIL;
        }

        BlockPos plantingPos = findPlantingPos(level, clickedPos);
        BlockState trellisState = level.getBlockState(plantingPos);

        if (level instanceof ServerLevel serverLevel) {
            Player player = context.getPlayer();
            BlockState plantedState = grapevineSupplier.get()
                    .defaultBlockState()
                    .setValue(
                            TrellisBlock.FACING,
                            trellisState.getValue(TrellisBlock.FACING)
                    )
                    .setValue(
                            TrellisBlock.NORTH,
                            trellisState.getValue(TrellisBlock.NORTH)
                    )
                    .setValue(
                            TrellisBlock.EAST,
                            trellisState.getValue(TrellisBlock.EAST)
                    )
                    .setValue(
                            TrellisBlock.SOUTH,
                            trellisState.getValue(TrellisBlock.SOUTH)
                    )
                    .setValue(
                            TrellisBlock.WEST,
                            trellisState.getValue(TrellisBlock.WEST)
                    )
                    .setValue(
                            TrellisBlock.ISOLATED,
                            trellisState.getValue(TrellisBlock.ISOLATED)
                    );

            serverLevel.setBlock(
                    plantingPos,
                    plantedState,
                    Block.UPDATE_ALL
            );
            serverLevel.gameEvent(
                    GameEvent.BLOCK_CHANGE,
                    plantingPos,
                    GameEvent.Context.of(player, plantedState)
            );

            if (player instanceof ServerPlayer serverPlayer) {
                ModAdvancements.grantPlanting(
                        serverPlayer,
                        grapevineSupplier.get()
                );
            }

            if (player == null
                    || !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }

        return InteractionResult.SUCCESS;
    }

    private static BlockPos findPlantingPos(
            Level level,
            BlockPos clickedPos
    ) {
        BlockState aboveState = level.getBlockState(clickedPos.above());
        BlockState belowState = level.getBlockState(clickedPos.below());

        if (!isBareTrellis(aboveState)
                && isBareTrellis(belowState)) {
            return clickedPos.below();
        }

        return clickedPos;
    }

    private static boolean isBareTrellis(BlockState state) {
        return state.getBlock() instanceof TrellisBlock
                && !(state.getBlock() instanceof GrapevineBlock);
    }
}
