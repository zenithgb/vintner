package com.zenith.vintner.item;

import com.zenith.vintner.advancement.ModAdvancements;
import com.zenith.vintner.block.VintageArchiveBlock;
import com.zenith.vintner.estate.EstateProfile;
import com.zenith.vintner.estate.EstateLedgerSavedData;
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
                && context.getLevel().getBlockState(
                        context.getClickedPos()
                ).getBlock() instanceof VintageArchiveBlock) {
            VintageArchiveBlock.useAlmanac(
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
            AlmanacReport report = AlmanacInspection.inspect(
                    serverLevel,
                    context.getClickedPos(),
                    player
            );
            if (!player.isShiftKeyDown()) {
                appendContainingPlot(
                        serverLevel,
                        player,
                        context.getClickedPos(),
                        report
                );
            }
            report.open(player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        if (!(level instanceof ServerLevel serverLevel)
                || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        ItemStack bottle = player.getItemInHand(otherHand);

        if (!canInspect(bottle)) {
            openGuide(
                    serverLevel,
                    serverPlayer,
                    player.getItemInHand(hand)
            );
            return InteractionResult.SUCCESS;
        }

        openWineReport(serverLevel, serverPlayer, bottle);

        return InteractionResult.SUCCESS;
    }

    private static void openWineReport(
            ServerLevel level,
            ServerPlayer player,
            ItemStack bottle
    ) {
        WineMetadata.ensureDefaults(bottle);
        WineProvenance provenance = WineMetadata.provenance(bottle);
        WineQualityProfile quality = WineMetadata.qualityProfile(bottle);
        WineAppraisal appraisal = WineAppraisal.evaluate(
                bottle,
                producerReputationTier(level, provenance)
        );
        AlmanacReport report = new AlmanacReport();

        report.page(
                Component.translatable(
                        "message.vintner.almanac.identity",
                        WineMetadata.quality(bottle).displayName(),
                        WineMetadata.vintage(bottle),
                        WineMetadata.batchCode(bottle)
                ),
                WineTastingProfile.from(bottle)
                        .description()
                        .copy()
                        .withStyle(ChatFormatting.GRAY),
                Component.translatable(
                        "message.vintner.almanac.style_estate",
                        WineMetadata.wineStyle(bottle).displayName(),
                        WineMetadata.estateName(bottle)
                ).withStyle(ChatFormatting.DARK_GRAY),
                provenance.known()
                        ? Component.translatable(
                                "message.vintner.almanac.provenance",
                                provenance.varietyDisplayName(),
                                provenance.batchDay(),
                                provenance.originDisplayName(),
                                provenance.producerDisplayName()
                        ).withStyle(ChatFormatting.DARK_GRAY)
                        : Component.translatable(
                                "message.vintner.almanac.provenance_legacy"
                        ).withStyle(ChatFormatting.DARK_GRAY)
        );

        WineVintageConditions conditions = provenance.vintageConditions();
        Component vintageConditions = conditions.known()
                ? Component.translatable(
                        "message.vintner.almanac.vintage_conditions",
                        conditions.season().displayName(),
                        conditions.year(),
                        conditions.weatherEvent().displayName()
                ).withStyle(ChatFormatting.DARK_GRAY)
                : Component.translatable(
                        "message.vintner.almanac.provenance_legacy"
                ).withStyle(ChatFormatting.DARK_GRAY);
        report.page(
                Component.translatable("message.vintner.almanac.quality_title"),
                Component.translatable(
                        "message.vintner.almanac.quality_score",
                        quality.score(),
                        quality.foundation(),
                        quality.vineyard(),
                        quality.processing(),
                        quality.fermentation(),
                        quality.ageing(),
                        quality.storage()
                ).withStyle(ChatFormatting.DARK_GRAY),
                vintageConditions,
                Component.translatable(
                        "message.vintner.almanac.age",
                        WineMetadata.ageStage(bottle).displayName(),
                        WineMetadata.bottleAgeDays(bottle)
                ).withStyle(ChatFormatting.DARK_GRAY),
                WineMetadata.bottleNumber(bottle) > 0
                        ? Component.translatable(
                                "message.vintner.almanac.bottle_number",
                                WineMetadata.bottleNumber(bottle),
                                WineMetadata.batchBottleCount(bottle)
                        ).withStyle(ChatFormatting.DARK_GRAY)
                        : Component.empty()
        );

        WineMarketOutlook bestMarket = WineMarketOutlook.bestFor(
                bottle,
                appraisal
        );
        WineMarketRegion localRegion = WineMarketRegion.from(
                level,
                player.blockPosition(),
                TerroirEvaluator.inspect(level, player.blockPosition())
        );
        WineMarketOutlook localMarket = WineMarketOutlook.forBuyer(
                bottle,
                appraisal,
                localRegion.buyerType()
        );
        report.page(
                Component.translatable("message.vintner.almanac.market_title"),
                Component.translatable(
                        "message.vintner.almanac.value",
                        appraisal.totalValue(),
                        appraisal.prestige()
                ).withStyle(ChatFormatting.DARK_GRAY),
                Component.translatable(
                        "message.vintner.almanac.market_outlook",
                        bestMarket.bestBuyer().displayName(),
                        signed(bestMarket.buyerAdjustment()),
                        bestMarket.estimatedValue()
                ).withStyle(ChatFormatting.DARK_GRAY),
                Component.translatable(
                        "message.vintner.almanac.local_market",
                        localRegion.displayName(),
                        signed(localMarket.buyerAdjustment()),
                        localMarket.estimatedValue()
                ).withStyle(ChatFormatting.DARK_GRAY),
                Component.translatable(
                        "message.vintner.almanac.readiness",
                        WineReadiness.from(bottle).displayName()
                ).withStyle(ChatFormatting.DARK_GREEN)
        );

        if (bottle.is(com.zenith.vintner.registry.ModItems.AGED_RED_WINE)
                || bottle.is(com.zenith.vintner.registry.ModItems.AGED_WHITE_WINE)) {
            var vessel = WineMetadata.agingVessel(bottle);
            report.page(
                    Component.translatable("message.vintner.almanac.cellar_title"),
                    Component.translatable(
                            "message.vintner.almanac.vessel",
                            vessel.displayName()
                    ).withStyle(ChatFormatting.DARK_GRAY),
                    Component.translatable(
                            "message.vintner.almanac.vessel_profile",
                            vessel.oxygenExposure(),
                            vessel.tannin(),
                            vessel.spoilageRisk(),
                            vessel.idealStyle()
                    ).withStyle(ChatFormatting.DARK_GRAY),
                    Component.translatable(
                            "message.vintner.almanac.storage_history",
                            WineMetadata.totalStorageDays(bottle),
                            WineMetadata.dominantCellarRating(bottle)
                                    .displayName()
                    ).withStyle(ChatFormatting.DARK_GRAY),
                    Component.translatable(
                            "message.vintner.almanac.servings",
                            WineMetadata.servings(bottle),
                            WineMetadata.SERVINGS_PER_BOTTLE
                    ).withStyle(ChatFormatting.DARK_GRAY)
            );
        }

        report.open(player);
        ModAdvancements.grantInspection(player);
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

    public static boolean canInspect(ItemStack stack) {
        return stack.getItem() instanceof WineItem;
    }

    public static void inspectPlacedWine(
            ServerLevel level,
            Player player,
            ItemStack bottle
    ) {
        if (!canInspect(bottle)) {
            return;
        }

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
                        "message.vintner.almanac.servings",
                        WineMetadata.servings(bottle),
                        WineMetadata.SERVINGS_PER_BOTTLE
                ).withStyle(ChatFormatting.DARK_GRAY)
        );
        player.sendSystemMessage(
                Component.translatable(
                        "message.vintner.almanac.readiness",
                        WineReadiness.from(bottle).displayName()
                ).withStyle(ChatFormatting.DARK_GRAY)
        );

        if (player instanceof ServerPlayer serverPlayer) {
            ModAdvancements.grantInspection(serverPlayer);
        }
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
        tooltip.accept(
                Component.translatable(
                        "tooltip.vintner.almanac.estate"
                ).withStyle(ChatFormatting.DARK_GRAY)
        );
        tooltip.accept(
                Component.translatable(
                        "tooltip.vintner.almanac.plot"
                ).withStyle(ChatFormatting.DARK_GRAY)
        );
        if (stack.getCustomName() != null) {
            tooltip.accept(Component.translatable(
                    "tooltip.vintner.almanac.custom_name",
                    stack.getCustomName()
            ).withStyle(ChatFormatting.GOLD));
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

    private static void appendSurveyBookmark(
            AlmanacReport report,
            VineyardSurveyRecord record,
            String plotName
    ) {
        report.page(
                Component.translatable(
                        "message.vintner.almanac.bookmark_title"
                ),
                Component.translatable(
                        "message.vintner.almanac.bookmark_location",
                        record.position().getX(),
                        record.position().getY(),
                        record.position().getZ(),
                        record.dimension()
                ).withStyle(ChatFormatting.GRAY),
                Component.translatable(
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
                ).withStyle(ChatFormatting.DARK_GREEN),
                Component.translatable(
                        "message.vintner.almanac.guide.corner_next",
                        plotName
                ).withStyle(ChatFormatting.AQUA)
        );
    }

    private static void openGuide(
            ServerLevel level,
            ServerPlayer player,
            ItemStack almanac
    ) {
        AlmanacReport report = new AlmanacReport();
        report.page(
                Component.translatable(
                        "message.vintner.almanac.guide_title"
                ),
                Component.translatable(
                        "message.vintner.almanac.guide.inspect"
                ).withStyle(ChatFormatting.GRAY),
                Component.translatable(
                        "message.vintner.almanac.guide.wine"
                ).withStyle(ChatFormatting.GRAY),
                Component.translatable(
                        "message.vintner.almanac.guide.estate"
                ).withStyle(ChatFormatting.GRAY),
                Component.translatable(
                        "message.vintner.almanac.guide.plot"
                ).withStyle(ChatFormatting.GRAY)
        );

        var estate = EstateSavedData.get(level).find(player.getUUID());
        var plots = VineyardPlotSavedData.get(level).plots(player.getUUID());
        int totalArea = plots.stream().mapToInt(VineyardPlot::area).sum();
        if (estate.isPresent()) {
            EstateProfile profile = estate.get();
            report.page(
                    Component.translatable(
                            "message.vintner.almanac.guide.estate_title"
                    ),
                    Component.translatable(
                            "message.vintner.almanac.guide.estate_registered",
                            profile.estateName(),
                            plots.size(),
                            totalArea
                    ).withStyle(ChatFormatting.DARK_GREEN),
                    Component.translatable(
                            "message.vintner.almanac.guide.desk"
                    ).withStyle(ChatFormatting.GRAY)
            );
        } else {
            report.page(
                    Component.translatable(
                            "message.vintner.almanac.guide.estate_title"
                    ),
                    Component.translatable(
                            "message.vintner.almanac.guide.estate_unregistered"
                    ).withStyle(ChatFormatting.GOLD)
            );
        }

        VineyardSurveyRecord.read(almanac).ifPresentOrElse(
                record -> appendSurveyBookmark(
                        report,
                        record,
                        desiredPlotName(level, player, almanac)
                ),
                () -> report.page(
                        Component.translatable(
                                "message.vintner.almanac.bookmark_title"
                        ),
                        Component.translatable(
                                "message.vintner.almanac.guide.no_corner"
                        ).withStyle(ChatFormatting.GRAY)
                )
        );
        report.open(player);
    }

    private static boolean tryRegisterPlot(
            ServerLevel level,
            ServerPlayer player,
            UseOnContext context
    ) {
        if (!player.isShiftKeyDown()
                || AlmanacInspection.classify(
                        level,
                        context.getClickedPos()
                ) != AlmanacInspection.Target.VINEYARD_SITE) {
            return false;
        }

        if (EstateSavedData.get(level)
                .find(player.getUUID())
                .isEmpty()) {
            player.sendSystemMessage(Component.translatable(
                    "message.vintner.plot.requires_estate"
            ).withStyle(ChatFormatting.RED), true);
            return true;
        }

        var firstCorner = VineyardSurveyRecord.read(
                context.getItemInHand()
        );
        if (firstCorner.isEmpty()) {
            startPlotSurvey(level, player, context);
            return true;
        }

        String dimension = level.dimension().identifier().toString();
        if (!firstCorner.get().dimension().equals(dimension)) {
            startPlotSurvey(level, player, context);
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
                        desiredPlotName(
                                level,
                                player,
                                context.getItemInHand()
                        )
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
        ).withStyle(ChatFormatting.GREEN), true);
        AlmanacReport report = new AlmanacReport();
        appendPlotReport(level, plot, report);
        report.open(player);
        return true;
    }

    private static void startPlotSurvey(
            ServerLevel level,
            ServerPlayer player,
            UseOnContext context
    ) {
        BlockPos firstCorner = TerroirEvaluator.resolveSitePosition(
                level,
                context.getClickedPos()
        );
        VineyardSurveyRecord.capture(
                level,
                firstCorner,
                TerroirEvaluator.inspect(level, firstCorner)
        ).save(context.getItemInHand());
        player.sendSystemMessage(Component.translatable(
                "message.vintner.plot.first_corner",
                firstCorner.getX(),
                firstCorner.getZ(),
                desiredPlotName(level, player, context.getItemInHand())
        ).withStyle(ChatFormatting.GREEN), true);
    }

    private static String desiredPlotName(
            ServerLevel level,
            ServerPlayer player,
            ItemStack almanac
    ) {
        Component customName = almanac.getCustomName();
        var estate = EstateSavedData.get(level).find(player.getUUID());
        if (customName != null
                && estate.map(profile -> !profile.estateName()
                        .equalsIgnoreCase(customName.getString()))
                        .orElse(true)) {
            return EstateProfile.sanitizeName(customName.getString());
        }

        var plots = VineyardPlotSavedData.get(level).plots(player.getUUID());
        for (int index = 1;
                index <= VineyardPlotSavedData.MAX_PLOTS_PER_ESTATE + 1;
                index++) {
            String candidate = "Vineyard Plot " + index;
            if (plots.stream().noneMatch(plot ->
                    plot.name().equalsIgnoreCase(candidate))) {
                return candidate;
            }
        }
        return "Vineyard Plot " + (plots.size() + 1);
    }

    private static void appendContainingPlot(
            ServerLevel level,
            ServerPlayer player,
            BlockPos pos,
            AlmanacReport report
    ) {
        VineyardPlotSavedData.get(level)
                .findContaining(player.getUUID(), level, pos)
                .ifPresent(plot -> appendPlotReport(level, plot, report));
    }

    private static void appendPlotReport(
            ServerLevel level,
            VineyardPlot plot,
            AlmanacReport almanac
    ) {
        VineyardPlotReport report = VineyardPlotReport.analyze(level, plot);
        almanac.page(
                Component.translatable(
                        "message.vintner.plot.summary",
                        plot.name(),
                        report.area()
                ),
                Component.translatable(
                        "message.vintner.plot.vines",
                        report.vineCount(),
                        report.varietySummary(),
                        report.averageAgeDays()
                ).withStyle(ChatFormatting.GRAY),
                Component.translatable(
                        "message.vintner.plot.conditions",
                        Component.translatable("soil_type.vintner." + report.soil()),
                        Component.translatable("climate_band.vintner." + report.climate()),
                        report.healthPercent()
                ).withStyle(ChatFormatting.DARK_GREEN),
                Component.translatable(
                        "message.vintner.plot.projection",
                        report.projectedYield(),
                        report.projectedQuality()
                ).withStyle(ChatFormatting.DARK_GRAY),
                Component.translatable(
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
                                        VineyardPlotReport.IMPROVED_IRRIGATION_MINIMUM_VINES,
                                        VineyardPlotReport.IMPROVED_IRRIGATION_PERCENT
                                )
                ).withStyle(report.hasImprovedIrrigation()
                        ? ChatFormatting.AQUA
                        : ChatFormatting.DARK_GRAY)
        );
    }
}
