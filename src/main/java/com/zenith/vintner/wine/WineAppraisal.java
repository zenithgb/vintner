package com.zenith.vintner.wine;

import com.zenith.vintner.estate.EstateReputationTier;
import net.minecraft.world.item.ItemStack;

/**
 * Deterministic bottle appraisal shared by the Almanac and future buyers.
 * Demand-specific factors remain zero until their Phase 8 systems exist.
 */
public record WineAppraisal(
        int styleValue,
        int qualityAdjustment,
        int ageAdjustment,
        int producerAdjustment,
        int conditionAdjustment,
        int totalValue,
        int prestige
) {
    private static final int BASE_STYLE_VALUE = 2;
    private static final int DAMAGE_STEP = WineAgeStage.SPOILED_DAMAGE / 4;

    public static WineAppraisal evaluate(
            ItemStack bottle,
            EstateReputationTier producerTier
    ) {
        WineQuality quality = WineMetadata.quality(bottle);
        WineAgeStage ageStage = WineMetadata.ageStage(bottle);
        int qualityAdjustment = quality.tradeValue() - BASE_STYLE_VALUE;
        int ageAdjustment = switch (ageStage) {
            case YOUNG -> 0;
            case DEVELOPING -> 1;
            case MATURE -> 2;
            case PEAK -> 4;
            case DECLINING -> -1;
            case SPOILED -> 0;
        };
        int producerAdjustment = producerTier == null
                ? 0
                : producerTier.ordinal();
        int damage = WineMetadata.storageDamage(bottle);
        int conditionAdjustment = damage <= 0
                ? 0
                : -Math.min(
                        3,
                        Math.max(1, Math.ceilDiv(damage, DAMAGE_STEP))
                );
        int total = ageStage == WineAgeStage.SPOILED
                ? 0
                : Math.max(
                        0,
                        BASE_STYLE_VALUE
                                + qualityAdjustment
                                + ageAdjustment
                                + producerAdjustment
                                + conditionAdjustment
                );
        int prestige = ageStage == WineAgeStage.SPOILED
                ? 0
                : quality.prestigeValue() + producerAdjustment;

        return new WineAppraisal(
                BASE_STYLE_VALUE,
                qualityAdjustment,
                ageAdjustment,
                producerAdjustment,
                conditionAdjustment,
                total,
                prestige
        );
    }

    public static WineAppraisal independent(ItemStack bottle) {
        return evaluate(bottle, EstateReputationTier.NEW_ESTATE);
    }
}
