package com.zenith.vintner.item;

import com.zenith.vintner.block.TrellisBlock;
import com.zenith.vintner.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public final class GrapeItem extends Item {
    public GrapeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(pos);

        if (!clickedState.is(ModBlocks.OAK_TRELLIS)) {
            return super.useOn(context);
        }

        if (level instanceof ServerLevel serverLevel) {
            BlockState plantedState = ModBlocks.GRAPEVINE
                    .defaultBlockState()
                    .setValue(
                            TrellisBlock.FACING,
                            clickedState.getValue(TrellisBlock.FACING)
                    );

            serverLevel.setBlock(
                    pos,
                    plantedState,
                    net.minecraft.world.level.block.Block.UPDATE_ALL
            );

            serverLevel.gameEvent(
                    GameEvent.BLOCK_CHANGE,
                    pos,
                    GameEvent.Context.of(context.getPlayer(), plantedState)
            );

            Player player = context.getPlayer();

            if (player == null || !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }

        return InteractionResult.SUCCESS;
    }
}
