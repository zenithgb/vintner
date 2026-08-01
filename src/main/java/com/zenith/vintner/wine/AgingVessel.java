package com.zenith.vintner.wine;

import net.minecraft.network.chat.Component;

/**
 * Functional ageing-vessel choices for Phase 3. Cosmetic wood-family ageing
 * barrels remain oak-style vessels; the specialist blocks opt into the other
 * profiles explicitly.
 */
public enum AgingVessel {
    OAK(
            "oak", 4, 20 * 90, 11, 1, 0,
            "moderate", "firm", "low", "red"
    ),
    CHESTNUT(
            "chestnut", 4, 20 * 75, 10, 2, 1,
            "high", "bold", "medium", "red"
    ),
    NEUTRAL(
            "neutral", 4, 20 * 110, 6, 0, 0,
            "low", "soft", "very_low", "versatile"
    ),
    LARGE_CASK(
            "large_cask", 8, 20 * 140, 8, 1, 0,
            "very_low", "soft", "low", "versatile"
    );

    private final String id;
    private final int capacity;
    private final int agingTime;
    private final int baseQualityContribution;
    private final int spoilageRiskPenalty;
    private final int mismatchPenalty;
    private final String oxygenExposure;
    private final String tannin;
    private final String spoilageRisk;
    private final String idealStyle;

    AgingVessel(
            String id,
            int capacity,
            int agingTime,
            int baseQualityContribution,
            int spoilageRiskPenalty,
            int mismatchPenalty,
            String oxygenExposure,
            String tannin,
            String spoilageRisk,
            String idealStyle
    ) {
        this.id = id;
        this.capacity = capacity;
        this.agingTime = agingTime;
        this.baseQualityContribution = baseQualityContribution;
        this.spoilageRiskPenalty = spoilageRiskPenalty;
        this.mismatchPenalty = mismatchPenalty;
        this.oxygenExposure = oxygenExposure;
        this.tannin = tannin;
        this.spoilageRisk = spoilageRisk;
        this.idealStyle = idealStyle;
    }

    public String id() {
        return id;
    }

    public int capacity() {
        return capacity;
    }

    public int agingTime() {
        return agingTime;
    }

    public int agingTimeSeconds() {
        return agingTime / 20;
    }

    public int qualityContribution(int wineType) {
        int stylePenalty = idealStyle.equals("red") && wineType == 2
                ? mismatchPenalty
                : 0;
        return baseQualityContribution
                - spoilageRiskPenalty
                - stylePenalty;
    }

    public int spoilageRiskPenalty() {
        return spoilageRiskPenalty;
    }

    public Component oxygenExposure() {
        return detail("oxygen", oxygenExposure);
    }

    public Component tannin() {
        return detail("tannin", tannin);
    }

    public Component spoilageRisk() {
        return detail("risk", spoilageRisk);
    }

    public Component idealStyle() {
        return detail("style", idealStyle);
    }

    public String tastingNote(boolean red) {
        return switch (this) {
            case OAK -> red ? "soft_oak" : "rounded_mineral";
            case CHESTNUT -> "warm_spice";
            case NEUTRAL -> red ? "red_fruit" : "crisp_acidity";
            case LARGE_CASK -> red
                    ? "earth_and_cedar"
                    : "floral_mineral";
        };
    }

    public Component displayName() {
        return Component.translatable("aging_vessel.vintner." + id);
    }

    public Component guide() {
        return Component.translatable(
                "aging_vessel.vintner.guide." + id
        );
    }

    public Component craftingHint() {
        return Component.translatable(
                "aging_vessel.vintner.crafting." + id
        );
    }

    private static Component detail(String category, String value) {
        return Component.translatable(
                "aging_vessel.vintner." + category + "." + value
        );
    }

    public static AgingVessel byId(String id) {
        for (AgingVessel vessel : values()) {
            if (vessel.id.equals(id)) {
                return vessel;
            }
        }
        return OAK;
    }
}
