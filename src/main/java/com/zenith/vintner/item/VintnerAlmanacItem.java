package com.zenith.vintner.item;

import com.zenith.vintner.advancement.ModAdvancements;
import com.zenith.vintner.wine.WineMetadata;
import com.zenith.vintner.wine.WineProvenance;
import com.zenith.vintner.wine.WineQualityProfile;
import com.zenith.vintner.wine.WineReadiness;
import com.zenith.vintner.wine.WineTastingProfile;
import com.zenith.vintner.wine.WineVintageConditions;
import com.zenith.vintner.wine.AlmanacInspection;
import com.zenith.vintner.vineyard.VineyardSurveyRecord;
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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public final class VintnerAlmanacItem extends Item {
    public VintnerAlmanacItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel() instanceof ServerLevel
                && context.getPlayer() != null) {
            AlmanacInspection.inspect(
                    (ServerLevel) context.getLevel(),
                    context.getClickedPos(),
                    context.getPlayer(),
                    context.getItemInHand()
            );
        }
        return InteractionResult.SUCCESS;
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
                var survey = VineyardSurveyRecord.read(
                        player.getItemInHand(hand)
                );
                if (survey.isPresent()) {
                    sendSurveyBookmark(player, survey.get());
                } else {
                    player.sendSystemMessage(
                            Component.translatable(
                                    "message.vintner.almanac.no_wine"
                            ).withStyle(ChatFormatting.GRAY)
                    );
                }
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
                            "message.vintner.almanac.style_estate",
                            WineMetadata.wineStyle(bottle).displayName(),
                            WineMetadata.estateName(bottle)
                    ).withStyle(ChatFormatting.DARK_GRAY)
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
                WineVintageConditions conditions =
                        provenance.vintageConditions();
                if (conditions.known()) {
                    player.sendSystemMessage(
                            Component.translatable(
                                    "message.vintner.almanac.vintage_conditions",
                                    conditions.season().displayName(),
                                    conditions.year(),
                                    conditions.weatherEvent().displayName()
                            ).withStyle(ChatFormatting.DARK_GRAY)
                    );
                    if (conditions.protectedCultivation()) {
                        player.sendSystemMessage(
                                Component.translatable(
                                        "message.vintner.almanac.vintage_protected"
                                ).withStyle(ChatFormatting.DARK_GRAY)
                        );
                    }
                }
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
                            "message.vintner.almanac.value",
                            WineMetadata.estimatedTradeValue(bottle),
                            WineMetadata.settlementPrestige(bottle)
                    ).withStyle(ChatFormatting.DARK_GRAY)
            );
            if (bottle.is(com.zenith.vintner.registry.ModItems.AGED_RED_WINE)
                    || bottle.is(com.zenith.vintner.registry.ModItems.AGED_WHITE_WINE)) {
                var vessel = WineMetadata.agingVessel(bottle);
                player.sendSystemMessage(
                        Component.translatable(
                                "message.vintner.almanac.vessel",
                                vessel.displayName()
                        ).withStyle(ChatFormatting.DARK_GRAY)
                );
                player.sendSystemMessage(
                        Component.translatable(
                                "message.vintner.almanac.vessel_profile",
                                vessel.oxygenExposure(),
                                vessel.tannin(),
                                vessel.spoilageRisk(),
                                vessel.idealStyle()
                        ).withStyle(ChatFormatting.DARK_GRAY)
                );
            }
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
        tooltip.accept(
                Component.translatable(
                        "tooltip.vintner.almanac.inspect"
                ).withStyle(ChatFormatting.DARK_GRAY)
        );
        VineyardSurveyRecord.read(stack).ifPresent(record ->
                tooltip.accept(Component.translatable(
                        "tooltip.vintner.almanac.survey",
                        record.position().getX(),
                        record.position().getZ(),
                        record.siteScore()
                ).withStyle(ChatFormatting.DARK_GREEN))
        );
    }

    private static void sendSurveyBookmark(
            Player player,
            VineyardSurveyRecord record
    ) {
        player.sendSystemMessage(Component.translatable(
                "message.vintner.almanac.bookmark_title"
        ).withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable(
                "message.vintner.almanac.bookmark_location",
                record.position().getX(),
                record.position().getY(),
                record.position().getZ(),
                record.dimension()
        ).withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.translatable(
                "message.vintner.almanac.bookmark_summary",
                Component.translatable(
                        "climate_band.vintner." + record.climate()
                ),
                Component.translatable(
                        "soil_type.vintner." + record.soil()
                ),
                record.siteScore(),
                Component.translatable(
                        "terroir_rating.vintner." + record.rating()
                )
        ).withStyle(ChatFormatting.DARK_GREEN));
    }
}
