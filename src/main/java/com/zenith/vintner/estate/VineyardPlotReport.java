package com.zenith.vintner.estate;

import com.zenith.vintner.block.GrapevineBlock;
import com.zenith.vintner.vineyard.GrapeVariety;
import com.zenith.vintner.vineyard.SeasonalContext;
import com.zenith.vintner.vineyard.TerroirEvaluator;
import com.zenith.vintner.vineyard.TerroirReport;
import com.zenith.vintner.vineyard.VineAgeSavedData;
import com.zenith.vintner.vineyard.VineyardIrrigation;
import com.zenith.vintner.wine.GrapeQualityEvaluator;
import com.zenith.vintner.wine.VineyardConditionReport;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Locale;
import java.util.Optional;

/** Live operational summary for one named plot. */
public record VineyardPlotReport(
        int area,
        int vineCount,
        int redVines,
        int whiteVines,
        int irrigatedVines,
        long averageAgeDays,
        String soil,
        String climate,
        int projectedYield,
        int projectedQuality,
        int healthPercent
) {
    private static final int MAX_CONDITION_SAMPLES = 32;
    private static final int ANALYSIS_MARGIN = 12;
    public static final int IMPROVED_IRRIGATION_MINIMUM_VINES = 4;
    public static final int IMPROVED_IRRIGATION_PERCENT = 75;

    public static VineyardPlotReport analyze(
            ServerLevel level,
            VineyardPlot plot
    ) {
        TerroirReport terroir = TerroirEvaluator.inspect(
                level,
                plot.center()
        );
        int vines = 0;
        int red = 0;
        int white = 0;
        int irrigatedVines = 0;
        long totalAge = 0L;
        int projectedYield = 0;
        int qualityTotal = 0;
        int healthTotal = 0;
        int conditionSamples = 0;
        long currentDay = Math.floorDiv(
                level.getOverworldClockTime(),
                SeasonalContext.TICKS_PER_DAY
        );
        VineAgeSavedData ages = VineAgeSavedData.get(level);

        for (int x = plot.minX(); x <= plot.maxX(); x++) {
            for (int z = plot.minZ(); z <= plot.maxZ(); z++) {
                for (int y = plot.anchorY() - 4;
                        y <= plot.anchorY() + 5;
                        y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!(state.getBlock() instanceof GrapevineBlock vine)
                            || state.getValue(GrapevineBlock.UPPER)) {
                        continue;
                    }

                    vines++;
                    totalAge += ages.ageDays(pos, currentDay);
                    GrapeVariety variety = vine.getVariety();
                    if (variety == GrapeVariety.RED) {
                        red++;
                    } else {
                        white++;
                    }
                    if (VineyardIrrigation.isIrrigated(level, pos)) {
                        irrigatedVines++;
                    }
                    if (state.getValue(GrapevineBlock.AGE)
                            >= GrapevineBlock.MAX_AGE) {
                        projectedYield += variety.maximumHarvest();
                    }

                    if (conditionSamples < MAX_CONDITION_SAMPLES) {
                        VineyardConditionReport condition =
                                GrapeQualityEvaluator.inspectWithTerroir(
                                        level,
                                        pos,
                                        terroir
                                );
                        qualityTotal += condition.qualityScore();
                        healthTotal += condition.vineHealthPoints();
                        conditionSamples++;
                    }
                    break;
                }
            }
        }

        return new VineyardPlotReport(
                plot.area(),
                vines,
                red,
                white,
                irrigatedVines,
                vines == 0 ? 0L : totalAge / vines,
                terroir.soil().type().name().toLowerCase(Locale.ROOT),
                terroir.climate().band().name().toLowerCase(Locale.ROOT),
                projectedYield,
                conditionSamples == 0
                        ? 0
                        : qualityTotal / conditionSamples,
                conditionSamples == 0
                        ? 100
                        : Math.clamp(
                                healthTotal * 100
                                        / (conditionSamples * 6),
                                0,
                                100
                        )
        );
    }

    public static Optional<VineyardPlotReport> analyzeIfLoaded(
            ServerLevel level,
            VineyardPlot plot
    ) {
        if (!level.dimension().identifier().toString().equals(plot.dimension())
                || !hasLoadedAnalysisArea(level, plot)) {
            return Optional.empty();
        }
        return Optional.of(analyze(level, plot));
    }

    private static boolean hasLoadedAnalysisArea(
            ServerLevel level,
            VineyardPlot plot
    ) {
        int minimumChunkX = (plot.minX() - ANALYSIS_MARGIN) >> 4;
        int maximumChunkX = (plot.maxX() + ANALYSIS_MARGIN) >> 4;
        int minimumChunkZ = (plot.minZ() - ANALYSIS_MARGIN) >> 4;
        int maximumChunkZ = (plot.maxZ() + ANALYSIS_MARGIN) >> 4;
        for (int chunkX = minimumChunkX;
                chunkX <= maximumChunkX;
                chunkX++) {
            for (int chunkZ = minimumChunkZ;
                    chunkZ <= maximumChunkZ;
                    chunkZ++) {
                if (!level.getChunkSource().hasChunk(chunkX, chunkZ)) {
                    return false;
                }
            }
        }
        return true;
    }

    public String varietySummary() {
        if (redVines > 0 && whiteVines > 0) {
            return "Mixed";
        }
        if (redVines > 0) {
            return "Red";
        }
        if (whiteVines > 0) {
            return "White";
        }
        return "Unplanted";
    }

    public int irrigationPercent() {
        return vineCount == 0
                ? 0
                : Math.clamp(irrigatedVines * 100 / vineCount, 0, 100);
    }

    public boolean hasImprovedIrrigation() {
        return vineCount >= IMPROVED_IRRIGATION_MINIMUM_VINES
                && irrigationPercent() >= IMPROVED_IRRIGATION_PERCENT;
    }
}
