package com.zenith.vintner.wine;

import com.zenith.vintner.vineyard.ClimateBand;
import com.zenith.vintner.vineyard.SlopeClass;
import com.zenith.vintner.vineyard.TerroirReport;
import net.minecraft.network.chat.Component;

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
        if (elevation >= 96 || slope == SlopeClass.STEEP) {
            return MINING;
        }
        return AGRICULTURAL;
    }
}
