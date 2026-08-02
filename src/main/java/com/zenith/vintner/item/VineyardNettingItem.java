package com.zenith.vintner.item;

import com.zenith.vintner.block.GrapevineBlock;
import com.zenith.vintner.vineyard.VineManagementSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

/** Reusable bird protection applied to the root of a trained grapevine. */
public final class VineyardNettingItem extends Item {
    public VineyardNettingItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState clicked = context.getLevel().getBlockState(
                context.getClickedPos()
        );
        if (!(clicked.getBlock() instanceof GrapevineBlock)) {
            return InteractionResult.PASS;
        }

        BlockPos rootPos = clicked.getValue(GrapevineBlock.UPPER)
                ? context.getClickedPos().below()
                : context.getClickedPos();
        BlockState root = context.getLevel().getBlockState(rootPos);
        Player player = context.getPlayer();

        if (!(root.getBlock() instanceof GrapevineBlock)
                || root.getValue(GrapevineBlock.UPPER)
                || root.getValue(GrapevineBlock.AGE) < 2) {
            if (!context.getLevel().isClientSide() && player != null) {
                player.sendSystemMessage(Component.translatable(
                        "message.vintner.netting.requires_trained_vine"
                ).withStyle(ChatFormatting.GRAY));
            }
            return InteractionResult.FAIL;
        }

        if (context.getLevel() instanceof ServerLevel level) {
            VineManagementSavedData management =
                    VineManagementSavedData.get(level);
            if (management.netted(rootPos)) {
                if (player != null) {
                    player.sendSystemMessage(Component.translatable(
                            "message.vintner.netting.already_installed"
                    ).withStyle(ChatFormatting.GRAY));
                }
                return InteractionResult.FAIL;
            }

            management.setNetted(rootPos, true);
            if (player != null && !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
            level.playSound(
                    null,
                    rootPos.above(),
                    SoundEvents.WOOL_PLACE,
                    SoundSource.BLOCKS,
                    0.8F,
                    1.25F
            );
            if (player != null) {
                player.sendSystemMessage(Component.translatable(
                        "message.vintner.netting.installed"
                ).withStyle(ChatFormatting.GREEN));
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable(
                "tooltip.vintner.vineyard_netting"
        ).withStyle(ChatFormatting.GRAY));
    }
}
