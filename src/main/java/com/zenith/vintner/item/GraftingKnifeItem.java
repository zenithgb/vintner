package com.zenith.vintner.item;

import com.zenith.vintner.block.GrapevineBlock;
import com.zenith.vintner.block.TrellisBlock;
import com.zenith.vintner.block.WoodVariant;
import com.zenith.vintner.registry.ModBlocks;
import com.zenith.vintner.registry.ModItems;
import com.zenith.vintner.vineyard.GrapeVariety;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/** Changes a trained vine's variety while leaving its persistent roots intact. */
public final class GraftingKnifeItem extends Item {
    public static final int MAX_DAMAGE = 96;

    public GraftingKnifeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();

        if (player == null) {
            return InteractionResult.PASS;
        }

        InteractionHand cuttingHand = context.getHand()
                == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;

        return graft(
                context.getLevel(),
                context.getClickedPos(),
                player,
                context.getItemInHand(),
                context.getHand(),
                player.getItemInHand(cuttingHand)
        );
    }

    public static InteractionResult graft(
            Level level,
            BlockPos clickedPos,
            Player player,
            ItemStack knife,
            InteractionHand knifeHand,
            ItemStack cutting
    ) {
        BlockState clickedState = level.getBlockState(clickedPos);

        if (!(clickedState.getBlock() instanceof GrapevineBlock clickedVine)) {
            return InteractionResult.PASS;
        }

        GrapeVariety targetVariety = cuttingVariety(cutting);

        if (targetVariety == null) {
            return InteractionResult.PASS;
        }

        BlockPos rootPos = clickedState.getValue(GrapevineBlock.UPPER)
                ? clickedPos.below()
                : clickedPos;
        BlockState rootState = level.getBlockState(rootPos);
        BlockState upperState = level.getBlockState(rootPos.above());

        if (!(rootState.getBlock() instanceof GrapevineBlock rootVine)
                || rootState.getValue(GrapevineBlock.UPPER)
                || !isMatchingUpper(rootVine, upperState)
                || rootVine.getVariety() == targetVariety) {
            return InteractionResult.FAIL;
        }

        if (level instanceof ServerLevel serverLevel) {
            BlockState graftedRoot = graftedState(
                    rootState,
                    targetVariety,
                    false
            );
            BlockState graftedUpper = graftedState(
                    upperState,
                    targetVariety,
                    true
            );

            int atomicFlags = Block.UPDATE_CLIENTS
                    | Block.UPDATE_KNOWN_SHAPE;
            serverLevel.setBlock(
                    rootPos,
                    graftedRoot,
                    atomicFlags
            );
            serverLevel.setBlock(
                    rootPos.above(),
                    graftedUpper,
                    atomicFlags
            );
            serverLevel.updateNeighborsAt(rootPos, graftedRoot.getBlock());
            serverLevel.updateNeighborsAt(
                    rootPos.above(),
                    graftedUpper.getBlock()
            );
            serverLevel.playSound(
                    null,
                    rootPos.above(),
                    SoundEvents.SHEEP_SHEAR,
                    SoundSource.BLOCKS,
                    0.9F,
                    1.25F
            );
            serverLevel.gameEvent(
                    GameEvent.BLOCK_CHANGE,
                    rootPos,
                    GameEvent.Context.of(player, graftedRoot)
            );

            if (!player.getAbilities().instabuild) {
                cutting.shrink(1);
                knife.hurtAndBreak(1, player, knifeHand);
            }
        }

        return InteractionResult.SUCCESS;
    }

    private static boolean isMatchingUpper(
            GrapevineBlock rootVine,
            BlockState upperState
    ) {
        return upperState.getBlock() instanceof GrapevineBlock upperVine
                && upperVine.getVariety() == rootVine.getVariety()
                && upperState.getValue(GrapevineBlock.UPPER);
    }

    private static BlockState graftedState(
            BlockState source,
            GrapeVariety targetVariety,
            boolean upper
    ) {
        WoodVariant woodVariant =
                ((TrellisBlock) source.getBlock()).woodVariant();
        BlockState target = ModBlocks.grapevine(
                targetVariety,
                woodVariant
        ).defaultBlockState();

        return target
                .setValue(TrellisBlock.FACING, source.getValue(TrellisBlock.FACING))
                .setValue(TrellisBlock.NORTH, source.getValue(TrellisBlock.NORTH))
                .setValue(TrellisBlock.EAST, source.getValue(TrellisBlock.EAST))
                .setValue(TrellisBlock.SOUTH, source.getValue(TrellisBlock.SOUTH))
                .setValue(TrellisBlock.WEST, source.getValue(TrellisBlock.WEST))
                .setValue(TrellisBlock.ISOLATED, source.getValue(TrellisBlock.ISOLATED))
                .setValue(TrellisBlock.HAS_ABOVE, source.getValue(TrellisBlock.HAS_ABOVE))
                .setValue(TrellisBlock.HAS_BELOW, source.getValue(TrellisBlock.HAS_BELOW))
                .setValue(GrapevineBlock.UPPER, upper)
                .setValue(GrapevineBlock.AGE, 2);
    }

    private static GrapeVariety cuttingVariety(ItemStack cutting) {
        if (cutting.is(ModItems.RED_GRAPE_CUTTING)) {
            return GrapeVariety.RED;
        }
        if (cutting.is(ModItems.WHITE_GRAPE_CUTTING)) {
            return GrapeVariety.WHITE;
        }
        return null;
    }
}
