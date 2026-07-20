package com.zenith.vintner.item;

import com.zenith.vintner.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class CompostItem extends Item {
    public CompostItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        BlockState state = context.getLevel().getBlockState(pos);

        if (!isSuitableGround(state)) {
            return InteractionResult.PASS;
        }

        if (context.getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.setBlockAndUpdate(
                    pos,
                    ModBlocks.VINEYARD_SOIL.defaultBlockState()
            );

            serverLevel.playSound(
                    null,
                    pos,
                    SoundEvents.HOE_TILL,
                    SoundSource.BLOCKS,
                    1.0F,
                    0.9F
            );

            if (context.getPlayer() == null
                    || !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }

        return InteractionResult.SUCCESS;
    }

    public static boolean isSuitableGround(BlockState state) {
        return state.is(Blocks.DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.FARMLAND);
    }
}
