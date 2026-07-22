package com.zenith.vintner.block;

import com.zenith.vintner.item.CompostItem;
import com.zenith.vintner.registry.ModBlocks;
import com.zenith.vintner.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

final class VineyardSoilInteraction {
    private VineyardSoilInteraction() {
    }

    static InteractionResult useOnSoilBelow(
            ItemStack stack,
            Level level,
            BlockPos supportPos,
            Player player
    ) {
        BlockPos soilPos = supportPos.below();
        BlockState soilState = level.getBlockState(soilPos);

        if (player.isShiftKeyDown()
                && stack.is(ModItems.COMPOST)) {
            return compostSoil(
                    stack,
                    level,
                    soilPos,
                    soilState,
                    player
            );
        }

        return InteractionResult.PASS;
    }

    private static InteractionResult compostSoil(
            ItemStack stack,
            Level level,
            BlockPos soilPos,
            BlockState soilState,
            Player player
    ) {
        if (!CompostItem.isSuitableGround(soilState)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            BlockState vineyardSoil =
                    ModBlocks.VINEYARD_SOIL.defaultBlockState();

            level.setBlockAndUpdate(soilPos, vineyardSoil);

            level.playSound(
                    null,
                    soilPos,
                    SoundEvents.HOE_TILL,
                    SoundSource.BLOCKS,
                    1.0F,
                    0.9F
            );

            level.gameEvent(
                    GameEvent.BLOCK_CHANGE,
                    soilPos,
                    GameEvent.Context.of(player, vineyardSoil)
            );

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return InteractionResult.SUCCESS;
    }
}
