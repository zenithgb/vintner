package com.zenith.vintner.item;

import com.zenith.vintner.util.VintnerNotifications;
import com.zenith.vintner.advancement.ModAdvancements;
import com.zenith.vintner.vineyard.TerroirEvaluator;
import com.zenith.vintner.vineyard.TerroirMessages;
import com.zenith.vintner.vineyard.TerroirReport;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;

import java.util.function.Consumer;

public final class SoilProbeItem extends Item {
    public static final int MAX_DAMAGE = 128;

    public SoilProbeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!TerroirEvaluator.canProbe(
                context.getLevel().getBlockState(
                        context.getClickedPos()
                )
        )) {
            if (!context.getLevel().isClientSide()
                    && context.getPlayer() != null) {
                VintnerNotifications.send(context.getPlayer(),
                        Component.translatable(
                                "message.vintner.soil_probe.invalid_target"
                        ).withStyle(ChatFormatting.GRAY)
                );
            }
            return InteractionResult.FAIL;
        }

        if (context.getLevel() instanceof ServerLevel serverLevel) {
            TerroirReport report = TerroirEvaluator.inspect(
                    serverLevel,
                    context.getClickedPos()
            );
            if (context.getPlayer() != null) {
                TerroirMessages.sendSoilReport(
                        context.getPlayer(),
                        report
                );
                if (context.getPlayer() instanceof ServerPlayer player) {
                    ModAdvancements.grantSurvey(player);
                }
                if (!context.getPlayer().getAbilities().instabuild) {
                    context.getItemInHand().hurtAndBreak(
                            1,
                            context.getPlayer(),
                            context.getHand()
                    );
                }
            }
            serverLevel.playSound(
                    null,
                    context.getClickedPos(),
                    SoundEvents.BRUSH_GENERIC,
                    SoundSource.PLAYERS,
                    0.8F,
                    0.9F
            );
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
        tooltip.accept(
                Component.translatable(
                        "tooltip.vintner.soil_probe"
                ).withStyle(ChatFormatting.GRAY)
        );
    }
}
