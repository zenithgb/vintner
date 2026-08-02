package com.zenith.vintner.wine;

import com.zenith.vintner.advancement.ModAdvancements;
import com.zenith.vintner.block.GrapevineBlock;
import com.zenith.vintner.block.TrellisBlock;
import com.zenith.vintner.block.entity.AgingBarrelBlockEntity;
import com.zenith.vintner.block.entity.FermentationBarrelBlockEntity;
import com.zenith.vintner.registry.ModBlocks;
import com.zenith.vintner.vineyard.GrapeVariety;
import com.zenith.vintner.vineyard.TerroirEvaluator;
import com.zenith.vintner.vineyard.TerroirMessages;
import com.zenith.vintner.vineyard.TerroirReport;
import com.zenith.vintner.vineyard.VineyardSurveyRecord;
import com.zenith.vintner.vineyard.SeasonalContext;
import com.zenith.vintner.vineyard.VineyardWeatherEvent;
import com.zenith.vintner.vineyard.VineyardProtection;
import com.zenith.vintner.vineyard.VineyardIrrigation;
import com.zenith.vintner.vineyard.VineyardManagementAdvice;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Routes the Almanac to a concise report appropriate for the block being
 * inspected. This deliberately consolidates several roadmap instruments into
 * one vanilla-style field book instead of adding single-purpose item clutter.
 */
public final class AlmanacInspection {
    private AlmanacInspection() {
    }

    public static Target classify(ServerLevel level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof FermentationBarrelBlockEntity) {
            return Target.FERMENTATION;
        }
        if (blockEntity instanceof AgingBarrelBlockEntity) {
            return Target.AGEING;
        }

        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof GrapevineBlock) {
            return Target.GRAPEVINE;
        }
        if (state.getBlock() instanceof TrellisBlock
                || state.is(ModBlocks.VINEYARD_SOIL)
                || TerroirEvaluator.canProbe(state)) {
            return Target.VINEYARD_SITE;
        }
        return Target.NONE;
    }

    public static void inspect(
            ServerLevel level,
            BlockPos pos,
            Player player,
            ItemStack almanac
    ) {
        switch (classify(level, pos)) {
            case FERMENTATION -> inspectFermentation(level, pos, player);
            case AGEING -> inspectAgeing(level, pos, player);
            case GRAPEVINE -> inspectGrapevine(level, pos, player);
            case VINEYARD_SITE -> inspectLand(
                    level,
                    pos,
                    player,
                    almanac
            );
            case NONE -> player.sendSystemMessage(
                    Component.translatable(
                            "message.vintner.almanac.no_reading"
                    ).withStyle(ChatFormatting.GRAY)
            );
        }
    }

    private static void inspectLand(
            ServerLevel level,
            BlockPos pos,
            Player player,
            ItemStack almanac
    ) {
        TerroirReport report = TerroirEvaluator.inspect(level, pos);
        TerroirMessages.sendFullReport(
                player,
                report
        );
        sendSeasonalOutlook(level, pos, player, report);
        if (player.isShiftKeyDown()) {
            VineyardSurveyRecord record = VineyardSurveyRecord.capture(
                    level,
                    pos,
                    report
            );
            record.save(almanac);
            player.sendSystemMessage(Component.translatable(
                    "message.vintner.almanac.survey_recorded",
                    record.position().getX(),
                    record.position().getY(),
                    record.position().getZ()
            ).withStyle(ChatFormatting.GREEN));
        } else {
            player.sendSystemMessage(Component.translatable(
                    "message.vintner.almanac.sneak_to_record"
            ).withStyle(ChatFormatting.DARK_GRAY));
        }
        grantSurvey(player);
    }

    private static void inspectGrapevine(
            ServerLevel level,
            BlockPos pos,
            Player player
    ) {
        BlockState clicked = level.getBlockState(pos);
        BlockPos rootPos = clicked.getValue(GrapevineBlock.UPPER)
                ? pos.below()
                : pos;
        BlockState root = level.getBlockState(rootPos);

        if (!(root.getBlock() instanceof GrapevineBlock grapevine)) {
            player.sendSystemMessage(Component.translatable(
                    "message.vintner.almanac.no_reading"
            ).withStyle(ChatFormatting.GRAY));
            return;
        }

        int age = root.getValue(GrapevineBlock.AGE);
        VineyardConditionReport report = GrapeQualityEvaluator.inspect(
                level,
                rootPos
        );
        GrapeVariety variety = grapevine.getVariety();

        player.sendSystemMessage(Component.translatable(
                "message.vintner.almanac.ripeness_title",
                Component.translatable(
                        variety == GrapeVariety.RED
                                ? "grape_variety.vintner.red"
                                : "grape_variety.vintner.white"
                )
        ).withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable(
                "message.vintner.almanac.ripeness_stage",
                Component.translatable(
                        "vine_stage.vintner." + age
                ),
                age,
                GrapevineBlock.MAX_AGE
        ).withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.translatable(
                "message.vintner.almanac.vine_age",
                report.vineAgeStage().displayName(),
                report.vineAgeDays(),
                report.vineAgeStage().harvestAdjustment(),
                report.vineAgeStage().qualityPoints()
        ).withStyle(ChatFormatting.DARK_GREEN));
        player.sendSystemMessage(Component.translatable(
                "message.vintner.almanac.yield_mode",
                report.yieldMode().displayName(),
                report.yieldMode().harvestAdjustment(),
                report.yieldMode().qualityPoints()
        ).withStyle(ChatFormatting.DARK_GREEN));
        player.sendSystemMessage(Component.translatable(
                "message.vintner.almanac.ripeness_quality",
                report.predictedQuality().displayName(),
                report.qualityScore(),
                report.terroir().siteScore()
        ).withStyle(ChatFormatting.DARK_GREEN));
        player.sendSystemMessage(Component.translatable(
                report.ripeHarvest()
                        ? "message.vintner.almanac.ripeness_ready"
                        : "message.vintner.almanac.ripeness_wait"
        ).withStyle(
                report.ripeHarvest()
                        ? ChatFormatting.GREEN
                        : ChatFormatting.DARK_GRAY
        ));
        sendSeasonalOutlook(
                player,
                report.seasonalContext(),
                report.weatherEvent(),
                report.harvestWeatherPoints(),
                report.protectedCultivation(),
                report.irrigated()
        );
        player.sendSystemMessage(
                VineyardManagementAdvice.recommend(
                        report.preparedSoil(),
                        report.weatherEvent(),
                        report.protectedCultivation(),
                        report.irrigated(),
                        report.ripeHarvest()
                ).message().copy().withStyle(ChatFormatting.AQUA)
        );
        grantSurvey(player);
    }

    private static void inspectFermentation(
            ServerLevel level,
            BlockPos pos,
            Player player
    ) {
        if (!(level.getBlockEntity(pos)
                instanceof FermentationBarrelBlockEntity barrel)) {
            return;
        }

        player.sendSystemMessage(Component.translatable(
                "message.vintner.almanac.hydrometer_title"
        ).withStyle(ChatFormatting.GOLD));
        WinemakingFeedback.showFermentationStatus(player, barrel);

        if (barrel.getBottleCount()
                == FermentationBarrelBlockEntity.CAPACITY
                && !barrel.isReady()) {
            player.sendSystemMessage(Component.translatable(
                    "message.vintner.almanac.process_time",
                    barrel.getProgressPercent(),
                    barrel.getRemainingSeconds()
            ).withStyle(ChatFormatting.DARK_GRAY));
        } else if (barrel.getBottleCount()
                < FermentationBarrelBlockEntity.CAPACITY) {
            player.sendSystemMessage(Component.translatable(
                    "message.vintner.almanac.fill_to_start",
                    FermentationBarrelBlockEntity.CAPACITY
            ).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static void inspectAgeing(
            ServerLevel level,
            BlockPos pos,
            Player player
    ) {
        if (!(level.getBlockEntity(pos)
                instanceof AgingBarrelBlockEntity barrel)) {
            return;
        }

        player.sendSystemMessage(Component.translatable(
                "message.vintner.almanac.ageing_title",
                barrel.getVessel().displayName()
        ).withStyle(ChatFormatting.GOLD));
        WinemakingFeedback.showAgingStatus(player, barrel);

        if (barrel.getBottleCount() == barrel.getCapacity()
                && !barrel.isReady()) {
            player.sendSystemMessage(Component.translatable(
                    "message.vintner.almanac.process_time",
                    barrel.getProgressPercent(),
                    barrel.getRemainingSeconds()
            ).withStyle(ChatFormatting.DARK_GRAY));
        } else if (barrel.getBottleCount() < barrel.getCapacity()) {
            player.sendSystemMessage(Component.translatable(
                    "message.vintner.almanac.fill_to_start",
                    barrel.getCapacity()
            ).withStyle(ChatFormatting.DARK_GRAY));
        }

        CellarConditions conditions = CellarConditions.evaluate(level, pos);
        player.sendSystemMessage(Component.translatable(
                "message.vintner.almanac.cellar_conditions",
                conditions.rating().displayName(),
                Component.translatable(
                        conditions.stableTemperature()
                                ? "cellar_temperature.vintner.stable"
                                : conditions.heatSource()
                                ? "cellar_temperature.vintner.warm"
                                : "cellar_temperature.vintner.variable"
                ),
                Component.translatable(
                        conditions.humid()
                                ? "cellar_humidity.vintner.humid"
                                : "cellar_humidity.vintner.dry"
                )
        ).withStyle(ChatFormatting.GRAY));

        if (player.isShiftKeyDown()) {
            player.sendSystemMessage(
                    barrel.getVessel().guide()
                            .copy()
                            .withStyle(ChatFormatting.GRAY)
            );
            player.sendSystemMessage(
                    barrel.getVessel().craftingHint()
                            .copy()
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        } else {
            player.sendSystemMessage(Component.translatable(
                    "message.vintner.almanac.sneak_for_vessel_guide"
            ).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static void grantSurvey(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ModAdvancements.grantSurvey(serverPlayer);
        }
    }

    private static void sendSeasonalOutlook(
            ServerLevel level,
            BlockPos pos,
            Player player,
            TerroirReport report
    ) {
        SeasonalContext context = SeasonalContext.current(level);
        boolean protectedCultivation = VineyardProtection.isProtected(
                level,
                pos
        );
        boolean irrigated = VineyardIrrigation.isIrrigated(level, pos);
        VineyardWeatherEvent weather = VineyardWeatherEvent.at(
                level,
                pos,
                report.climate(),
                context
        ).mitigatedBy(protectedCultivation, irrigated);
        sendSeasonalOutlook(
                player,
                context,
                weather,
                weather.harvestQualityPoints(level.isRainingAt(pos.above())),
                protectedCultivation,
                irrigated
        );
    }

    private static void sendSeasonalOutlook(
            Player player,
            SeasonalContext context,
            VineyardWeatherEvent weather,
            int weatherPoints,
            boolean protectedCultivation,
            boolean irrigated
    ) {
        player.sendSystemMessage(Component.translatable(
                "message.vintner.almanac.season",
                context.season().displayName(),
                context.year(),
                context.dayInSeason(),
                context.seasonLengthDays()
        ).withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.translatable(
                "message.vintner.almanac.weather_outlook",
                weather.displayName(),
                weatherPoints
        ).withStyle(
                weatherPoints >= 5
                        ? ChatFormatting.DARK_GREEN
                        : ChatFormatting.GOLD
        ));
        if (protectedCultivation) {
            player.sendSystemMessage(Component.translatable(
                    "message.vintner.almanac.protected_cultivation"
            ).withStyle(ChatFormatting.AQUA));
        }
        if (irrigated) {
            player.sendSystemMessage(Component.translatable(
                    "message.vintner.almanac.irrigated"
            ).withStyle(ChatFormatting.AQUA));
        }
    }

    public enum Target {
        NONE,
        VINEYARD_SITE,
        GRAPEVINE,
        FERMENTATION,
        AGEING
    }
}
