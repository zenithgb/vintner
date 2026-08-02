package com.zenith.vintner.wine;

import com.zenith.vintner.vineyard.ClimateBand;
import com.zenith.vintner.vineyard.SlopeClass;
import com.zenith.vintner.vineyard.TerroirReport;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;

/** A coarse, world-derived market region used for local demand. */
public enum WineMarketRegion {
    AGRICULTURAL("agricultural", WineBuyerType.VILLAGE_MERCHANT),
    COASTAL("coastal", WineBuyerType.COASTAL_SETTLEMENT),
    COLD("cold", WineBuyerType.COLD_REGION_SETTLEMENT),
    MINING("mining", WineBuyerType.MINING_SETTLEMENT);

    private final String id;
    private final WineBuyerType buyerType;

    WineMarketRegion(String id, WineBuyerType buyerType) {
        this.id = id;
        this.buyerType = buyerType;
    }

    public Component displayName() {
        return Component.translatable("wine_market_region.vintner." + id);
    }

    public WineBuyerType buyerType() {
        return buyerType;
    }

    public static WineMarketRegion from(TerroirReport report) {
        return classify(
                report.climate().band(),
                report.terrain().waterDistance(),
                report.terrain().elevation(),
                report.terrain().slope()
        );
    }

    /**
     * Uses the biome as the primary regional identity. Terrain is a
     * fallback, not a reason for a lush jungle settlement to become a
     * mining market merely because it sits on a tall or steep hill.
     */
    public static WineMarketRegion from(
            ServerLevel level,
            BlockPos pos,
            TerroirReport report
    ) {
        if (report.climate().band() == ClimateBand.COLD
                || report.climate().band() == ClimateBand.COOL) {
            return COLD;
        }

        var biome = level.getBiome(pos);
        if (biome.is(BiomeTags.IS_OCEAN)
                || biome.is(BiomeTags.IS_BEACH)
                || biome.is(BiomeTags.IS_RIVER)) {
            return COASTAL;
        }
        if (biome.is(BiomeTags.IS_MOUNTAIN)
                || biome.is(BiomeTags.IS_BADLANDS)) {
            return MINING;
        }
        if (biome.is(BiomeTags.IS_JUNGLE)
                || biome.is(BiomeTags.IS_SAVANNA)
                || biome.is(BiomeTags.IS_FOREST)) {
            return AGRICULTURAL;
        }

        return classify(
                report.climate().band(),
                report.terrain().waterDistance(),
                report.terrain().elevation(),
                report.terrain().slope()
        );
    }

    public static WineMarketRegion classify(
            ClimateBand climate,
            int waterDistance,
            int elevation,
            SlopeClass slope
    ) {
        if (climate == ClimateBand.COLD || climate == ClimateBand.COOL) {
            return COLD;
        }
        if (waterDistance <= 6) {
            return COASTAL;
        }
        if (elevation >= 128
                || elevation >= 112 && slope == SlopeClass.STEEP) {
            return MINING;
        }
        return AGRICULTURAL;
    }
}
