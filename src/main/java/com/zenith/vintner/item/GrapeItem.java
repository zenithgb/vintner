package com.zenith.vintner.item;

import com.zenith.vintner.block.TrellisBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.function.Supplier;

public final class GrapeItem extends Item {
    private final Supplier<Block> grapevineSupplier;

    public GrapeItem(
            Supplier<Block> grapevineSupplier,
            Properties properties
    ) {
        super(properties);
        this.grapevineSupplier = grapevineSupplier;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(pos);

        if (!(clickedState.getBlock() instanceof TrellisBlock)
                || clickedState.getBlock()
                instanceof com.zenith.vintner.block.GrapevineBlock) {
            return super.useOn(context);
        }

        if (level instanceof ServerLevel serverLevel) {
            BlockState plantedState = grapevineSupplier.get()
                    .defaultBlockState()
                    .setValue(
                            TrellisBlock.FACING,
                            clickedState.getValue(TrellisBlock.FACING)
                    )
                    .setValue(
                            TrellisBlock.LEFT,
                            clickedState.getValue(TrellisBlock.LEFT)
                    )
                    .setValue(
                            TrellisBlock.RIGHT,
                            clickedState.getValue(TrellisBlock.RIGHT)
                    )
                    .setValue(
                            TrellisBlock.ISOLATED,
                            clickedState.getValue(TrellisBlock.ISOLATED)
                    );

            serverLevel.setBlock(
                    pos,
                    plantedState,
                    Block.UPDATE_ALL
            );

            serverLevel.gameEvent(
                    GameEvent.BLOCK_CHANGE,
                    pos,
                    GameEvent.Context.of(
                            context.getPlayer(),
                            plantedState
                    )
            );

            Player player = context.getPlayer();

            if (player == null
                    || !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }

        return InteractionResult.SUCCESS;
    }
}
