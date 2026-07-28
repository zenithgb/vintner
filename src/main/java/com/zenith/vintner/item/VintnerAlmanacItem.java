package com.zenith.vintner.item;

import com.zenith.vintner.advancement.ModAdvancements;
import com.zenith.vintner.wine.WineMetadata;
import com.zenith.vintner.wine.WineTastingProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public final class VintnerAlmanacItem extends Item {
    public VintnerAlmanacItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        ItemStack bottle = player.getItemInHand(otherHand);

        if (!(bottle.getItem() instanceof WineItem)) {
            if (level instanceof ServerLevel) {
                player.sendSystemMessage(
                        Component.translatable(
                                "message.vintner.almanac.no_wine"
                        ).withStyle(ChatFormatting.GRAY)
                );
            }
            return InteractionResult.SUCCESS;
        }

        if (level instanceof ServerLevel) {
            WineMetadata.ensureDefaults(bottle);
            player.sendSystemMessage(
                    Component.translatable(
                            "message.vintner.almanac.identity",
                            WineMetadata.quality(bottle).displayName(),
                            WineMetadata.vintage(bottle),
                            WineMetadata.batchCode(bottle)
                    ).withStyle(ChatFormatting.GOLD)
            );
            player.sendSystemMessage(
                    WineTastingProfile.from(bottle)
                            .description()
                            .copy()
                            .withStyle(ChatFormatting.GRAY)
            );
            player.sendSystemMessage(
                    Component.translatable(
                            "message.vintner.almanac.age",
                            WineMetadata.ageStage(bottle).displayName(),
                            WineMetadata.bottleAgeDays(bottle),
                            WineMetadata.lastCellarRating(bottle)
                                    .displayName()
                    ).withStyle(ChatFormatting.DARK_GRAY)
            );

            if (player instanceof ServerPlayer serverPlayer) {
                ModAdvancements.grantInspection(serverPlayer);
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
        tooltip.accept(
                Component.translatable(
                        "tooltip.vintner.almanac"
                ).withStyle(ChatFormatting.GRAY)
        );
    }
}
