package com.zenith.vintner.item;

import com.zenith.vintner.util.VintnerNotifications;
import com.zenith.vintner.block.GrapevineBlock;
import com.zenith.vintner.block.NurseryBedBlock;
import com.zenith.vintner.block.TrellisBlock;
import com.zenith.vintner.block.WoodVariant;
import com.zenith.vintner.registry.ModBlocks;
import com.zenith.vintner.registry.ModItems;
import com.zenith.vintner.vineyard.GrapeVariety;
import com.zenith.vintner.vineyard.GrapeCultivar;
import com.zenith.vintner.vineyard.GraftedCuttingData;
import com.zenith.vintner.vineyard.NurseryPlant;
import com.zenith.vintner.vineyard.VineRootstock;
import com.zenith.vintner.vineyard.VineManagementSavedData;
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

        InteractionResult nurseryResult = graftNurseryRootstock(
                context.getLevel(),
                context.getClickedPos(),
                player,
                context.getItemInHand(),
                context.getHand(),
                player.getItemInHand(cuttingHand)
        );

        if (nurseryResult.consumesAction()) {
            return nurseryResult;
        }

        return graft(
                context.getLevel(),
                context.getClickedPos(),
                player,
                context.getItemInHand(),
                context.getHand(),
                player.getItemInHand(cuttingHand)
        );
    }

    public static InteractionResult graftNurseryRootstock(
            Level level,
            BlockPos clickedPos,
            Player player,
            ItemStack knife,
            InteractionHand knifeHand,
            ItemStack cutting
    ) {
        BlockState state = level.getBlockState(clickedPos);

        if (!(state.getBlock() instanceof NurseryBedBlock)
                || !NurseryBedBlock.readyToHarvest(state)
                || cuttingVariety(cutting) == null) {
            return InteractionResult.PASS;
        }

        NurseryPlant nurseryPlant = state.getValue(NurseryBedBlock.PLANT);
        if (!nurseryPlant.isRootstock()) {
            return InteractionResult.PASS;
        }

        VineRootstock rootstock = nurseryPlant.rootstock();

        if (level instanceof ServerLevel serverLevel) {
            ItemStack graftedCutting = cutting.copy();
            graftedCutting.setCount(1);
            GraftedCuttingData.apply(graftedCutting, rootstock);

            serverLevel.setBlock(
                    clickedPos,
                    NurseryBedBlock.emptiedState(state),
                    Block.UPDATE_ALL
            );
            VineManagementSavedData.get(serverLevel).remove(clickedPos);
            if (!player.getAbilities().instabuild) {
                cutting.shrink(1);
                knife.hurtAndBreak(1, player, knifeHand);
            }
            if (!player.addItem(graftedCutting)) {
                Block.popResource(
                        serverLevel,
                        clickedPos.above(),
                        graftedCutting
                );
            }
            VintnerNotifications.send(player,
                    net.minecraft.network.chat.Component.translatable(
                            "message.vintner.grafted_cutting",
                            rootstock.displayName()
                    )
            );
            serverLevel.playSound(
                    null,
                    clickedPos,
                    SoundEvents.SHEEP_SHEAR,
                    SoundSource.BLOCKS,
                    0.9F,
                    1.35F
            );
        }

        return InteractionResult.SUCCESS;
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
                || !isMatchingUpper(rootVine, upperState)) {
            return InteractionResult.FAIL;
        }

        GrapeCultivar targetCultivar = GraftedCuttingData.cultivar(
                cutting,
                targetVariety
        );
        GrapeCultivar currentCultivar = level instanceof ServerLevel serverLevel
                ? VineManagementSavedData.get(serverLevel)
                        .cultivar(rootPos, rootVine.getVariety())
                : GrapeCultivar.defaultFor(rootVine.getVariety());
        if (rootVine.getVariety() == targetVariety
                && currentCultivar == targetCultivar) {
            return InteractionResult.FAIL;
        }

        if (level instanceof ServerLevel serverLevel) {
            boolean changesColour = rootVine.getVariety() != targetVariety;
            BlockState graftedRoot = changesColour
                    ? graftedState(rootState, targetVariety, false)
                    : rootState.setValue(GrapevineBlock.AGE, 2);
            BlockState graftedUpper = changesColour
                    ? graftedState(upperState, targetVariety, true)
                    : upperState.setValue(GrapevineBlock.AGE, 2);

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
            VineManagementSavedData.get(serverLevel).setCultivar(
                    rootPos,
                    targetCultivar
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
