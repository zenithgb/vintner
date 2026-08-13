package com.zenith.vintner.item;

import com.zenith.vintner.block.AgingBarrelBlock;
import com.zenith.vintner.block.BarrelStandBlock;
import com.zenith.vintner.block.CellarCollectionBlock;
import com.zenith.vintner.block.FermentationBarrelBlock;
import com.zenith.vintner.block.GrapePressBlock;
import com.zenith.vintner.block.VintageArchiveBlock;
import com.zenith.vintner.block.WineCrateBlock;
import com.zenith.vintner.block.WineRackBlock;
import com.zenith.vintner.registry.ModItems;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class CoopersMalletItem extends Item {
    public static final int MAX_DAMAGE = 64;

    public CoopersMalletItem(Properties properties) {
        super(properties);
    }

    public static void initialize() {
        UseBlockCallback.EVENT.register((
                player,
                level,
                hand,
                hitResult
        ) -> {
            ItemStack mallet = player.getItemInHand(hand);

            if (!mallet.is(ModItems.COOPERS_MALLET)) {
                return InteractionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = level.getBlockState(pos);

            if (state.getBlock() instanceof AgingBarrelBlock barrel) {
                if (player.isSecondaryUseActive()) {
                    return barrel.removeCooperageTreatment(
                            mallet,
                            hand,
                            state,
                            level,
                            pos,
                            player
                    );
                }

                InteractionHand otherHand = hand
                        == InteractionHand.MAIN_HAND
                        ? InteractionHand.OFF_HAND
                        : InteractionHand.MAIN_HAND;

                if (isTreatmentKit(
                        player.getItemInHand(otherHand)
                )) {
                    return InteractionResult.PASS;
                }
            }

            return rotateFixture(
                    level,
                    pos,
                    state,
                    player,
                    mallet,
                    hand
            );
        });
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        BlockState state = context.getLevel().getBlockState(pos);

        return rotateFixture(
                context.getLevel(),
                pos,
                state,
                context.getPlayer(),
                context.getItemInHand(),
                context.getHand()
        );
    }

    private static InteractionResult rotateFixture(
            Level level,
            BlockPos pos,
            BlockState state,
            Player player,
            ItemStack mallet,
            InteractionHand hand
    ) {

        if (!isRotatableFixture(state.getBlock())
                || !state.hasProperty(
                        BlockStateProperties.HORIZONTAL_FACING
                )) {
            return InteractionResult.PASS;
        }

        if (level instanceof ServerLevel serverLevel) {
            Direction facing = state.getValue(
                    BlockStateProperties.HORIZONTAL_FACING
            );
            BlockState rotated = state.setValue(
                    BlockStateProperties.HORIZONTAL_FACING,
                    facing.getClockWise()
            );
            serverLevel.setBlock(
                    pos,
                    rotated,
                    Block.UPDATE_ALL
            );
            serverLevel.playSound(
                    null,
                    pos,
                    SoundEvents.WOOD_PLACE,
                    SoundSource.BLOCKS,
                    0.8F,
                    0.85F
            );

            if (player != null && !player.getAbilities().instabuild) {
                mallet.hurtAndBreak(
                        1,
                        player,
                        hand
                );
            }
        }

        return InteractionResult.SUCCESS;
    }

    private static boolean isTreatmentKit(ItemStack stack) {
        return stack.is(ModItems.TOASTING_KIT)
                || stack.is(ModItems.SEASONING_KIT)
                || stack.is(ModItems.CASK_CONVERSION_KIT);
    }

    private static boolean isRotatableFixture(Block block) {
        return block instanceof AgingBarrelBlock
                || block instanceof FermentationBarrelBlock
                || block instanceof GrapePressBlock
                || block instanceof WineRackBlock
                || block instanceof WineCrateBlock
                || block instanceof VintageArchiveBlock
                || block instanceof BarrelStandBlock
                || block instanceof CellarCollectionBlock;
    }
}
