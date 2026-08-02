package com.zenith.vintner.item;

import com.zenith.vintner.advancement.ModAdvancements;
import com.zenith.vintner.block.VintageArchiveBlock;
import com.zenith.vintner.estate.EstateProfile;
import com.zenith.vintner.estate.EstateInfrastructureReport;
import com.zenith.vintner.estate.EstateLedgerSavedData;
import com.zenith.vintner.estate.EstateReputationProfile;
import com.zenith.vintner.estate.EstateReputationSavedData;
import com.zenith.vintner.estate.EstateReputationTier;
import com.zenith.vintner.estate.EstateSavedData;
import com.zenith.vintner.estate.LedgerEventType;
import com.zenith.vintner.estate.VineyardPlot;
import com.zenith.vintner.estate.VineyardPlotReport;
import com.zenith.vintner.estate.VineyardPlotSavedData;
import com.zenith.vintner.wine.WineMetadata;
import com.zenith.vintner.wine.WineAppraisal;
import com.zenith.vintner.wine.WineMarketOutlook;
import com.zenith.vintner.wine.WineMarketRegion;
import com.zenith.vintner.wine.WineProvenance;
import com.zenith.vintner.wine.WineQualityProfile;
import com.zenith.vintner.wine.WineReadiness;
import com.zenith.vintner.wine.WineTastingProfile;
import com.zenith.vintner.wine.WineVintageConditions;
import com.zenith.vintner.wine.AlmanacInspection;
import com.zenith.vintner.vineyard.TerroirEvaluator;
import com.zenith.vintner.vineyard.VineyardSurveyRecord;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
        if (context.getLevel() instanceof ServerLevel serverLevel
                && context.getPlayer() != null
                && context.getPlayer().isShiftKeyDown()
                && context.getLevel().getBlockState(
                        context.getClickedPos()
                ).getBlock() instanceof VintageArchiveBlock) {
            VintageArchiveBlock.registerEstate(
                    serverLevel,
                    context.getClickedPos(),
                    context.getPlayer(),
                    context.getHand(),
                    context.getItemInHand()
            );
            return InteractionResult.SUCCESS;
        }

        if (context.getLevel() instanceof ServerLevel serverLevel
                && context.getPlayer() instanceof ServerPlayer player) {
            if (tryRegisterPlot(serverLevel, player, context)) {
                return InteractionResult.SUCCESS;
            }
            AlmanacInspection.inspect(
                    serverLevel,
                    context.getClickedPos(),
                    player,
                    context.getItemInHand()
            );
            if (!player.isShiftKeyDown()) {
                sendContainingPlot(
                        serverLevel,
                        player,
                        context.getClickedPos()
                );
            }
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
                if (player.isShiftKeyDown()) {
                    var estate = EstateSavedData.get((ServerLevel) level)
                            .find(player.getUUID());
                    if (estate.isPresent()) {
                        sendEstateProfile(player, estate.get());
                        return InteractionResult.SUCCESS;
                    }
                }
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
                    if (conditions.irrigated()) {
                        player.sendSystemMessage(
                                Component.translatable(
                                        "message.vintner.almanac.vintage_irrigated"
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
            WineAppraisal appraisal = WineAppraisal.evaluate(
                    bottle,
                    producerReputationTier(
                            (ServerLevel) level,
                            provenance
                    )
            );
            player.sendSystemMessage(
                    Component.translatable(
                            "message.vintner.almanac.value",
                            appraisal.totalValue(),
                            appraisal.prestige()
                    ).withStyle(ChatFormatting.DARK_GRAY)
            );
            if (appraisal.producerAdjustment() > 0
                    || appraisal.conditionAdjustment() < 0) {
                player.sendSystemMessage(
                        Component.translatable(
                                "message.vintner.almanac.market_factors",
                                signed(appraisal.producerAdjustment()),
                                signed(appraisal.conditionAdjustment())
                        ).withStyle(ChatFormatting.DARK_GRAY)
                );
            }
            WineMarketOutlook market = WineMarketOutlook.bestFor(
                    bottle,
                    appraisal
            );
            if (market.estimatedValue() > 0) {
                player.sendSystemMessage(
                        Component.translatable(
                                "message.vintner.almanac.market_outlook",
                                market.bestBuyer().displayName(),
                                signed(market.buyerAdjustment()),
                                market.estimatedValue()
                        ).withStyle(ChatFormatting.DARK_GRAY)
                );
                WineMarketRegion localRegion = WineMarketRegion.from(
                        TerroirEvaluator.inspect(
                                level,
                                player.blockPosition()
                        )
                );
                WineMarketOutlook localMarket =
                        WineMarketOutlook.forBuyer(
                                bottle,
                                appraisal,
                                localRegion.buyerType()
                        );
                player.sendSystemMessage(
                        Component.translatable(
                                "message.vintner.almanac.local_market",
                                localRegion.displayName(),
                                signed(localMarket.buyerAdjustment()),
                                localMarket.estimatedValue()
                        ).withStyle(ChatFormatting.DARK_GRAY)
                );
            }
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

    private static EstateReputationTier producerReputationTier(
            ServerLevel level,
            WineProvenance provenance
    ) {
        if (provenance.producerId().isBlank()) {
            return EstateReputationTier.NEW_ESTATE;
        }
        try {
            return EstateReputationSavedData.get(level)
                    .profile(java.util.UUID.fromString(
                            provenance.producerId()
                    ))
                    .tier();
        } catch (IllegalArgumentException exception) {
            return EstateReputationTier.NEW_ESTATE;
        }
    }

    private static String signed(int value) {
        return value > 0 ? "+" + value : Integer.toString(value);
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
        if (stack.getCustomName() != null) {
            tooltip.accept(
                    Component.translatable(
                            "tooltip.vintner.almanac.estate"
                    ).withStyle(ChatFormatting.DARK_GRAY)
            );
        }
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

    private static void sendEstateProfile(
            Player player,
            EstateProfile profile
    ) {
        player.sendSystemMessage(Component.translatable(
                "message.vintner.estate.summary",
                profile.estateName(),
                profile.foundingYear()
        ).withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable(
                "message.vintner.estate.vineyard",
                profile.vineyardName(),
                profile.homeRegionDisplayName()
        ).withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.translatable(
                "message.vintner.estate.identity",
                profile.bottleLabel(),
                Component.translatable(
                        "color.minecraft." + profile.crestColor()
                )
        ).withStyle(ChatFormatting.DARK_GRAY));
        if (player instanceof ServerPlayer serverPlayer) {
            var plots = VineyardPlotSavedData.get(
                    (ServerLevel) serverPlayer.level()
            ).plots(serverPlayer.getUUID());
            int totalArea = plots.stream()
                    .mapToInt(VineyardPlot::area)
                    .sum();
            player.sendSystemMessage(Component.translatable(
                    "message.vintner.estate.plots",
                    plots.size(),
                    totalArea
            ).withStyle(ChatFormatting.DARK_GREEN));
            EstateInfrastructureReport infrastructure =
                    EstateInfrastructureReport.survey(
                            serverPlayer.level(),
                            serverPlayer.blockPosition()
                    );
            player.sendSystemMessage(Component.translatable(
                    "message.vintner.estate.infrastructure",
                    infrastructure.hasBarrelWorkshop()
                            ? Component.translatable(
                                    "estate_facility.vintner.ready"
                            )
                            : Component.literal(
                                    infrastructure.mountedBarrels()
                                            + "/"
                                            + EstateInfrastructureReport
                                            .WORKSHOP_BARRELS
                            ),
                    infrastructure.hasControlledCellar()
                            ? Component.translatable(
                                    "estate_facility.vintner.ready"
                            )
                            : Component.literal(
                                    infrastructure.idealCellarStations()
                                            + "/"
                                            + EstateInfrastructureReport
                                            .CONTROLLED_CELLAR_STATIONS
                            ),
                    infrastructure.hasWarehouse()
                            ? Component.translatable(
                                    "estate_facility.vintner.ready"
                            )
                            : Component.literal(
                                    infrastructure.storageFixtures()
                                            + "/"
                                            + EstateInfrastructureReport
                                            .WAREHOUSE_FIXTURES
                            ),
                    infrastructure.hasTastingRoom()
                            ? Component.translatable(
                                    "estate_facility.vintner.ready"
                            )
                            : Component.translatable(
                                    "estate_facility.vintner.incomplete"
                            )
            ).withStyle(ChatFormatting.DARK_AQUA));
            EstateReputationSavedData reputationData =
                    EstateReputationSavedData.get(
                            (ServerLevel) serverPlayer.level()
                    );
            EstateReputationProfile reputation =
                    reputationData.syncFromLedger(
                            serverPlayer.getUUID(),
                            EstateLedgerSavedData.get(
                                    (ServerLevel) serverPlayer.level()
                            ).entries(serverPlayer.getUUID())
                    );
            reputation = reputationData.recordInfrastructure(
                    serverPlayer.getUUID(),
                    infrastructure
            );
            EstateReputationTier next = reputation.tier().next();
            Component reputationSummary = next == null
                    ? Component.translatable(
                            "message.vintner.estate.reputation.max",
                            Component.translatable(
                                    reputation.tier().translationKey()
                            ),
                            reputation.score()
                    )
                    : Component.translatable(
                            "message.vintner.estate.reputation.progress",
                            Component.translatable(
                                    reputation.tier().translationKey()
                            ),
                            reputation.score(),
                            next.minimumScore()
                    );
            player.sendSystemMessage(reputationSummary.copy().withStyle(
                    ChatFormatting.LIGHT_PURPLE
            ));
        }
    }

    private static boolean tryRegisterPlot(
            ServerLevel level,
            ServerPlayer player,
            UseOnContext context
    ) {
        if (!player.isShiftKeyDown()
                || context.getItemInHand().getCustomName() == null
                || AlmanacInspection.classify(
                        level,
                        context.getClickedPos()
                ) != AlmanacInspection.Target.VINEYARD_SITE
                || EstateSavedData.get(level)
                        .find(player.getUUID())
                        .isEmpty()) {
            return false;
        }

        var firstCorner = VineyardSurveyRecord.read(
                context.getItemInHand()
        );
        if (firstCorner.isEmpty()) {
            return false;
        }

        String dimension = level.dimension().identifier().toString();
        if (!firstCorner.get().dimension().equals(dimension)) {
            player.sendSystemMessage(Component.translatable(
                    "message.vintner.plot.dimension_mismatch"
            ).withStyle(ChatFormatting.RED));
            return true;
        }

        BlockPos secondCorner = TerroirEvaluator.resolveSitePosition(
                level,
                context.getClickedPos()
        );
        VineyardPlotSavedData.Registration registration =
                VineyardPlotSavedData.get(level).register(
                        player,
                        level,
                        firstCorner.get().position(),
                        secondCorner,
                        context.getItemInHand()
                                .getCustomName()
                                .getString()
                );

        if (!registration.successful()) {
            String key = registration.status()
                    == VineyardPlotSavedData.Status.TOO_LARGE
                    ? "message.vintner.plot.too_large"
                    : "message.vintner.plot.full";
            player.sendSystemMessage(Component.translatable(
                    key,
                    VineyardPlot.MAX_SIDE,
                    VineyardPlotSavedData.MAX_PLOTS_PER_ESTATE
            ).withStyle(ChatFormatting.RED));
            return true;
        }

        VineyardSurveyRecord.clear(context.getItemInHand());
        VineyardPlot plot = registration.plot();
        EstateLedgerSavedData.get(level).record(
                player,
                registration.status()
                        == VineyardPlotSavedData.Status.UPDATED
                        ? LedgerEventType.PLOT_UPDATED
                        : LedgerEventType.PLOT_REGISTERED,
                plot.name(),
                plot.area(),
                0L,
                0
        );
        player.sendSystemMessage(Component.translatable(
                registration.status()
                        == VineyardPlotSavedData.Status.UPDATED
                        ? "message.vintner.plot.updated"
                        : "message.vintner.plot.created",
                plot.name(),
                plot.area(),
                plot.width(),
                plot.depth()
        ).withStyle(ChatFormatting.GREEN));
        sendPlotReport(level, player, plot);
        return true;
    }

    private static void sendContainingPlot(
            ServerLevel level,
            ServerPlayer player,
            BlockPos pos
    ) {
        VineyardPlotSavedData.get(level)
                .findContaining(player.getUUID(), level, pos)
                .ifPresent(plot -> sendPlotReport(level, player, plot));
    }

    private static void sendPlotReport(
            ServerLevel level,
            Player player,
            VineyardPlot plot
    ) {
        VineyardPlotReport report = VineyardPlotReport.analyze(level, plot);
        player.sendSystemMessage(Component.translatable(
                "message.vintner.plot.summary",
                plot.name(),
                report.area()
        ).withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable(
                "message.vintner.plot.vines",
                report.vineCount(),
                report.varietySummary(),
                report.averageAgeDays()
        ).withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.translatable(
                "message.vintner.plot.conditions",
                Component.translatable(
                        "soil_type.vintner." + report.soil()
                ),
                Component.translatable(
                        "climate_band.vintner." + report.climate()
                ),
                report.healthPercent()
        ).withStyle(ChatFormatting.DARK_GREEN));
        player.sendSystemMessage(Component.translatable(
                "message.vintner.plot.projection",
                report.projectedYield(),
                report.projectedQuality()
        ).withStyle(ChatFormatting.DARK_GRAY));
        player.sendSystemMessage(Component.translatable(
                "message.vintner.plot.irrigation",
                report.irrigatedVines(),
                report.vineCount(),
                report.irrigationPercent(),
                report.hasImprovedIrrigation()
                        ? Component.translatable(
                                "estate_upgrade.vintner.irrigation.ready"
                        )
                        : Component.translatable(
                                "estate_upgrade.vintner.irrigation.incomplete",
                                VineyardPlotReport
                                        .IMPROVED_IRRIGATION_MINIMUM_VINES,
                                VineyardPlotReport
                                        .IMPROVED_IRRIGATION_PERCENT
                        )
        ).withStyle(report.hasImprovedIrrigation()
                ? ChatFormatting.AQUA
                : ChatFormatting.DARK_GRAY));
    }
}
