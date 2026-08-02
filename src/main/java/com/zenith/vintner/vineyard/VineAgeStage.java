package com.zenith.vintner.vineyard;

import net.minecraft.network.chat.Component;

/**
 * Long-term age of an established grapevine. This is deliberately separate
 * from the block's short growth cycle: growth controls ripeness, while vine
 * age describes the roots and wood that persist across harvests.
 */
public enum VineAgeStage {
    NEW_PLANTING(0, 1, 0),
    YOUNG(8, 1, 1),
    MATURE(32, 0, 3),
    OLD(96, -1, 5),
    ANCIENT(192, -2, 6);

    private final int minimumDays;
    private final int harvestAdjustment;
    private final int qualityPoints;

    VineAgeStage(
            int minimumDays,
            int harvestAdjustment,
            int qualityPoints
    ) {
        this.minimumDays = minimumDays;
        this.harvestAdjustment = harvestAdjustment;
        this.qualityPoints = qualityPoints;
    }

    public int minimumDays() {
        return minimumDays;
    }

    public int harvestAdjustment() {
        return harvestAdjustment;
    }

    public int qualityPoints() {
        return qualityPoints;
    }

    public Component displayName() {
        return Component.translatable(
                "vine_age.vintner." + name().toLowerCase(java.util.Locale.ROOT)
        );
    }

    public static VineAgeStage atDays(long ageDays) {
        long safeDays = Math.max(0L, ageDays);
        VineAgeStage result = NEW_PLANTING;

        for (VineAgeStage candidate : values()) {
            if (safeDays < candidate.minimumDays) {
                break;
            }
            result = candidate;
        }

        return result;
    }
}
