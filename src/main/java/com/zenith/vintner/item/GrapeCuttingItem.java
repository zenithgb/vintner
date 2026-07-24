package com.zenith.vintner.item;

import com.zenith.vintner.advancement.ModAdvancements;
import com.zenith.vintner.block.GrapevineBlock;
import com.zenith.vintner.block.TrellisBlock;
import com.zenith.vintner.block.WoodVariant;
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

import java.util.function.Function;

public final class GrapeCuttingItem extends Item {
    private final Function<WoodVariant, Block> grapevineFactory;

    public GrapeCuttingItem(
            Function<WoodVariant, Block> grapevineFactory,
            Properties properties
    ) {
        super(properties);
        this.grapevineFactory = grapevineFactory;
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
        WoodVariant woodVariant =
                ((TrellisBlock) trellisState.getBlock()).woodVariant();
        Block grapevine = grapevineFactory.apply(woodVariant);

        if (level instanceof ServerLevel serverLevel) {
            Player player = context.getPlayer();
            BlockState plantedState = grapevine
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
                    )
                    .setValue(
                            TrellisBlock.HAS_ABOVE,
                            trellisState.getValue(TrellisBlock.HAS_ABOVE)
                    )
                    .setValue(
                            TrellisBlock.HAS_BELOW,
                            trellisState.getValue(TrellisBlock.HAS_BELOW)
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
                        grapevine
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
