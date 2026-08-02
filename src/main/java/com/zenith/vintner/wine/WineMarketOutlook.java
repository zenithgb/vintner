package com.zenith.vintner.wine;

import net.minecraft.world.item.ItemStack;

/** A deterministic comparison of one bottle against known buyer profiles. */
public record WineMarketOutlook(
        WineBuyerType bestBuyer,
        int buyerAdjustment,
        int estimatedValue
) {
    public static WineMarketOutlook bestFor(
            ItemStack bottle,
            WineAppraisal appraisal
    ) {
        WineBuyerType bestBuyer = WineBuyerType.VILLAGE_MERCHANT;
        int bestAdjustment = 0;

        if (appraisal.totalValue() > 0) {
            for (WineBuyerType buyer : WineBuyerType.values()) {
                int adjustment = buyer.preferenceAdjustment(bottle);

                if (adjustment > bestAdjustment) {
                    bestBuyer = buyer;
                    bestAdjustment = adjustment;
                }
            }
        }

        return new WineMarketOutlook(
                bestBuyer,
                bestAdjustment,
                appraisal.totalValue() == 0
                        ? 0
                        : appraisal.totalValue() + bestAdjustment
        );
    }
}
