package com.zenith.vintner.item;

import com.zenith.vintner.advancement.ModAdvancements;
import com.zenith.vintner.wine.WineMetadata;
import com.zenith.vintner.wine.WineProvenance;
import com.zenith.vintner.wine.WineQualityProfile;
import com.zenith.vintner.wine.WineReadiness;
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
            WineProvenance provenance =
                    WineMetadata.provenance(bottle);

            if (provenance.known()) {
                player.sendSystemMessage(
                        Component.translatable(
                                "message.vintner.almanac.provenance",
                                provenance.varietyDisplayName(),
                                provenance.batchDay(),
                                provenance.originDisplayName(),
                                provenance.producerDisplayName()
                        ).withStyle(ChatFormatting.DARK_GRAY)
                );
            } else {
                player.sendSystemMessage(
                        Component.translatable(
                                "message.vintner.almanac.provenance_legacy"
                        ).withStyle(ChatFormatting.DARK_GRAY)
                );
            }
            WineQualityProfile quality =
                    WineMetadata.qualityProfile(bottle);
            player.sendSystemMessage(
                    Component.translatable(
                            "message.vintner.almanac.quality_score",
                            quality.score(),
                            quality.foundation(),
                            quality.vineyard(),
                            quality.processing(),
                            quality.fermentation(),
                            quality.ageing(),
                            quality.storage()
                    ).withStyle(ChatFormatting.DARK_GRAY)
            );

            if (WineMetadata.bottleNumber(bottle) > 0) {
                player.sendSystemMessage(
                        Component.translatable(
                                "message.vintner.almanac.bottle_number",
                                WineMetadata.bottleNumber(bottle),
                                WineMetadata.batchBottleCount(bottle)
                        ).withStyle(ChatFormatting.DARK_GRAY)
                );
            }

            player.sendSystemMessage(
                    Component.translatable(
                            "message.vintner.almanac.age",
                            WineMetadata.ageStage(bottle).displayName(),
                            WineMetadata.bottleAgeDays(bottle)
                    ).withStyle(ChatFormatting.DARK_GRAY)
            );
            player.sendSystemMessage(
                    Component.translatable(
                            "message.vintner.almanac.readiness",
                            WineReadiness.from(bottle).displayName()
                    ).withStyle(ChatFormatting.DARK_GRAY)
            );

            if (WineMetadata.bottledAt(bottle) > 0L) {
                player.sendSystemMessage(
                        Component.translatable(
                                "message.vintner.almanac.bottled",
                                WineMetadata.bottledDay(bottle)
                        ).withStyle(ChatFormatting.DARK_GRAY)
                );
            }

            if (WineMetadata.totalStorageTicks(bottle) > 0L) {
                player.sendSystemMessage(
                        Component.translatable(
                                "message.vintner.almanac.storage_history",
                                WineMetadata.totalStorageDays(bottle),
                                WineMetadata.dominantCellarRating(bottle)
                                        .displayName()
                        ).withStyle(ChatFormatting.DARK_GRAY)
                );
            }

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
