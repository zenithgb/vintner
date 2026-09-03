package com.zenith.vintner.wine;

import com.zenith.vintner.advancement.ModAdvancements;
import com.zenith.vintner.block.GrapevineBlock;
import com.zenith.vintner.block.TrellisBlock;
import com.zenith.vintner.block.entity.AgingBarrelBlockEntity;
import com.zenith.vintner.block.entity.FermentationBarrelBlockEntity;
import com.zenith.vintner.estate.EstateInfrastructureReport;
import com.zenith.vintner.item.AlmanacReport;
import com.zenith.vintner.registry.ModBlocks;
import com.zenith.vintner.vineyard.SeasonalContext;
import com.zenith.vintner.vineyard.TerroirEvaluator;
import com.zenith.vintner.vineyard.TerroirMessages;
import com.zenith.vintner.vineyard.TerroirReport;
import com.zenith.vintner.vineyard.VineyardIrrigation;
import com.zenith.vintner.vineyard.VineyardManagementAdvice;
import com.zenith.vintner.vineyard.VineyardProtection;
import com.zenith.vintner.vineyard.VineyardThreat;
import com.zenith.vintner.vineyard.VineyardWeatherEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Builds concise, paged Almanac reports for inspected blocks. */
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

    public static AlmanacReport inspect(
            ServerLevel level,
            BlockPos pos,
            ServerPlayer player
    ) {
        AlmanacReport report = new AlmanacReport();
        switch (classify(level, pos)) {
            case FERMENTATION -> inspectFermentation(
                    level,
                    pos,
                    report
            );
            case AGEING -> inspectAgeing(level, pos, player, report);
            case GRAPEVINE -> inspectGrapevine(
                    level,
                    pos,
                    player,
                    report
            );
            case VINEYARD_SITE -> inspectLand(
                    level,
                    pos,
                    player,
                    report
            );
            case NONE -> report.page(
                    Component.translatable("item.vintner.vintner_almanac"),
                    Component.translatable(
                            "message.vintner.almanac.no_reading"
                    ).withStyle(ChatFormatting.GRAY)
            );
        }
        return report;
    }

    private static void inspectLand(
            ServerLevel level,
            BlockPos pos,
            ServerPlayer player,
            AlmanacReport almanacReport
    ) {
        TerroirReport report = TerroirEvaluator.inspect(level, pos);
        List<Component> entries = TerroirMessages.fullReportEntries(report);
        almanacReport.page(
                Component.translatable("message.vintner.terroir.title"),
                entries.get(0),
                entries.get(1),
                entries.get(2)
        );

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
        almanacReport.page(
                Component.translatable("message.vintner.almanac.season"),
                entries.get(3),
                entries.get(4),
                season(context),
                weather(
                        weather,
                        weather.harvestQualityPoints(
                                level.isRainingAt(pos.above())
                        )
                ),
                cultivation(protectedCultivation, irrigated)
        );

        grantSurvey(player);
    }

    private static void inspectGrapevine(
            ServerLevel level,
            BlockPos pos,
            ServerPlayer player,
            AlmanacReport almanac
    ) {
        BlockState clicked = level.getBlockState(pos);
        BlockPos rootPos = clicked.getValue(GrapevineBlock.UPPER)
                ? pos.below()
                : pos;
        BlockState root = level.getBlockState(rootPos);

        if (!(root.getBlock() instanceof GrapevineBlock)) {
            almanac.page(
                    Component.translatable("item.vintner.vintner_almanac"),
                    Component.translatable(
                            "message.vintner.almanac.no_reading"
                    ).withStyle(ChatFormatting.GRAY)
            );
            return;
        }

        int age = root.getValue(GrapevineBlock.AGE);
        VineyardConditionReport report = GrapeQualityEvaluator.inspect(
                level,
                rootPos
        );
        almanac.page(
                Component.translatable(
                        "message.vintner.almanac.ripeness_title",
                        report.cultivar().displayName()
                ),
                Component.translatable(
                        "message.vintner.almanac.cultivar_profile",
                        report.cultivar().ripeningDisplayName(),
                        report.cultivar().minimumHarvest(),
                        report.cultivar().maximumHarvest(),
                        report.cultivar().wineStyleDisplayName(),
                        report.cultivar().benefitDisplayName()
                ).withStyle(ChatFormatting.DARK_GREEN),
                Component.translatable(
                        "message.vintner.almanac.cultivar_fit",
                        report.cultivar().siteSuitability(report.terroir()),
                        report.cultivar().ageingPotential()
                ).withStyle(ChatFormatting.DARK_GRAY),
                Component.translatable(
                        "message.vintner.almanac.ripeness_stage",
                        Component.translatable("vine_stage.vintner." + age),
                        age,
                        GrapevineBlock.MAX_AGE
                ).withStyle(ChatFormatting.GRAY),
                Component.translatable(
                        "message.vintner.almanac.vine_age",
                        report.vineAgeStage().displayName(),
                        report.vineAgeDays(),
                        report.vineAgeStage().harvestAdjustment(),
                        report.vineAgeStage().qualityPoints()
                ).withStyle(ChatFormatting.DARK_GREEN)
        );
        almanac.page(
                Component.translatable("message.vintner.almanac.vine_health"),
                Component.translatable(
                        "message.vintner.almanac.yield_mode",
                        report.yieldMode().displayName(),
                        report.yieldMode().harvestAdjustment(),
                        report.yieldMode().qualityPoints()
                ).withStyle(ChatFormatting.DARK_GREEN),
                Component.translatable(
                        "message.vintner.almanac.rootstock",
                        report.rootstock().displayName()
                ).withStyle(ChatFormatting.DARK_GREEN),
                Component.translatable(
                        "message.vintner.almanac.vine_health",
                        report.threat().displayName(),
                        report.vineHealthPoints()
                ).withStyle(
                        report.threat() == VineyardThreat.HEALTHY
                                ? ChatFormatting.DARK_GREEN
                                : ChatFormatting.YELLOW
                ),
                Component.translatable(
                        "message.vintner.almanac.ripeness_quality",
                        report.predictedQuality().displayName(),
                        report.qualityScore(),
                        report.terroir().siteScore()
                ).withStyle(ChatFormatting.DARK_GREEN),
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
            AlmanacReport almanac
    ) {
        if (!(level.getBlockEntity(pos)
                instanceof FermentationBarrelBlockEntity barrel)) {
            return;
        }

        Component process = barrel.getBottleCount()
                == FermentationBarrelBlockEntity.CAPACITY
                && !barrel.isReady()
                ? Component.translatable(
                        "message.vintner.almanac.process_time",
                        barrel.getProgressPercent(),
                        barrel.getRemainingSeconds()
                )
                : Component.translatable(
                        "message.vintner.almanac.fill_to_start",
                        FermentationBarrelBlockEntity.CAPACITY
                );
        almanac.page(
                Component.translatable(
                        "message.vintner.almanac.hydrometer_title"
                ),
                WinemakingFeedback.fermentationStatus(barrel),
                process.copy().withStyle(ChatFormatting.DARK_GRAY)
        );
    }

    private static void inspectAgeing(
            ServerLevel level,
            BlockPos pos,
            ServerPlayer player,
            AlmanacReport almanac
    ) {
        if (!(level.getBlockEntity(pos)
                instanceof AgingBarrelBlockEntity barrel)) {
            return;
        }

        CellarConditions conditions = CellarConditions.evaluate(level, pos);
        boolean mounted = EstateInfrastructureReport.hasBarrelStand(
                level,
                pos
        );
        int contribution = EstateInfrastructureReport.ageingContribution(
                conditions.rating(),
                mounted
        );
        Component process = barrel.getBottleCount() == barrel.getCapacity()
                && !barrel.isReady()
                ? Component.translatable(
                        "message.vintner.almanac.process_time",
                        barrel.getProgressPercent(),
                        barrel.getRemainingSeconds()
                )
                : Component.translatable(
                        "message.vintner.almanac.fill_to_start",
                        barrel.getCapacity()
                );
        almanac.page(
                Component.translatable(
                        "message.vintner.almanac.ageing_title",
                        barrel.getVessel().displayName()
                ),
                WinemakingFeedback.agingStatus(barrel),
                process.copy().withStyle(ChatFormatting.DARK_GRAY),
                Component.translatable(
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
                ).withStyle(ChatFormatting.GRAY),
                Component.translatable(
                        "message.vintner.almanac.ageing_facility",
                        Component.translatable(
                                mounted
                                        ? "estate_facility.vintner.mounted"
                                        : "estate_facility.vintner.unmounted"
                        ),
                        Component.literal(
                                contribution > 0
                                        ? "+" + contribution
                                        : Integer.toString(contribution)
                        )
                ).withStyle(contribution > 0
                        ? ChatFormatting.GREEN
                        : ChatFormatting.DARK_GRAY)
        );
        if (player.isShiftKeyDown()) {
            almanac.page(
                    barrel.getVessel().displayName(),
                    barrel.getVessel().guide(),
                    barrel.getVessel().craftingHint()
            );
        }
    }

    private static Component season(SeasonalContext context) {
        return Component.translatable(
                "message.vintner.almanac.season",
                context.season().displayName(),
                context.year(),
                context.dayInSeason(),
                context.seasonLengthDays()
        ).withStyle(ChatFormatting.GRAY);
    }

    private static Component weather(
            VineyardWeatherEvent weather,
            int points
    ) {
        return Component.translatable(
                "message.vintner.almanac.weather_outlook",
                weather.displayName(),
                points
        ).withStyle(points >= 5
                ? ChatFormatting.DARK_GREEN
                : ChatFormatting.GOLD);
    }

    private static Component cultivation(
            boolean protectedCultivation,
            boolean irrigated
    ) {
        if (protectedCultivation && irrigated) {
            return Component.translatable(
                    "message.vintner.almanac.protected_cultivation"
            ).append(Component.literal("\n")).append(
                    Component.translatable(
                            "message.vintner.almanac.irrigated"
                    )
            ).withStyle(ChatFormatting.AQUA);
        }
        if (protectedCultivation) {
            return Component.translatable(
                    "message.vintner.almanac.protected_cultivation"
            ).withStyle(ChatFormatting.AQUA);
        }
        if (irrigated) {
            return Component.translatable(
                    "message.vintner.almanac.irrigated"
            ).withStyle(ChatFormatting.AQUA);
        }
        return Component.translatable(
                "message.vintner.almanac.cultivation_open"
        ).withStyle(ChatFormatting.DARK_GRAY);
    }

    private static void grantSurvey(ServerPlayer player) {
        ModAdvancements.grantSurvey(player);
    }

    public enum Target {
        NONE,
        VINEYARD_SITE,
        GRAPEVINE,
        FERMENTATION,
        AGEING
    }
}
