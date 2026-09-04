package com.zenith.vintner.vineyard;

import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * A named grape selection. Cultivars share the existing red/white vine
 * blocks, but make site choice, yield, resilience, and wine identity differ.
 */
public enum GrapeCultivar {
    EMBER_NOIR(GrapeVariety.RED, 1.05F, 42, 42, 82, 48, 52, 78,
            0, -1, 2, 2, "late", "structured", "long_cellaring"),
    VALE_PINOT(GrapeVariety.RED, 0.62F, 66, 62, 72, 58, 70, 62,
            -1, -1, 0, 2, "early", "silky", "precision"),
    SUNCREST(GrapeVariety.RED, 1.18F, 28, 30, 84, 44, 45, 72,
            -1, 1, 1, 0, "early", "bright", "heat_resilience"),
    IRONWOOD_RED(GrapeVariety.RED, 0.84F, 55, 48, 74, 50, 76, 88,
            0, 0, 1, 3, "mid", "firm", "ageing"),
    NIGHTBERRY(GrapeVariety.RED, 0.70F, 58, 54, 78, 42, 68, 72,
            1, 0, 2, 2, "late", "aromatic", "disease_resistance"),
    RIVER_GARNET(GrapeVariety.RED, 0.78F, 74, 68, 66, 64, 80, 55,
            -1, 1, 2, 1, "early", "juicy", "reliable_yield"),

    GOLDEN_VALE(GrapeVariety.WHITE, 0.72F, 58, 52, 76, 60, 72, 68,
            0, 0, 1, 2, "mid", "rounded", "balance"),
    FROSTLING(GrapeVariety.WHITE, 0.48F, 52, 48, 72, 55, 78, 70,
            1, -1, 2, 1, "early", "crisp", "frost_resilience"),
    GREENWAKE(GrapeVariety.WHITE, 0.66F, 72, 70, 80, 66, 76, 58,
            -1, 1, 2, 0, "early", "herbal", "reliable_yield"),
    SILVERLEAF(GrapeVariety.WHITE, 0.58F, 48, 44, 88, 38, 84, 82,
            1, -1, 1, 3, "late", "mineral", "ageing"),
    HONEYCREST(GrapeVariety.WHITE, 0.98F, 45, 46, 70, 56, 58, 62,
            0, 1, 1, 1, "mid", "lush", "warm_climates"),
    STONEFLOWER(GrapeVariety.WHITE, 0.76F, 38, 36, 90, 34, 66, 92,
            1, 0, 2, 2, "late", "floral", "poor_soils");

    private static final List<GrapeCultivar> ACTIVE_VALUES = List.of(
            EMBER_NOIR,
            VALE_PINOT,
            SUNCREST,
            RIVER_GARNET,
            GOLDEN_VALE,
            FROSTLING,
            HONEYCREST,
            STONEFLOWER
    );

    private final GrapeVariety variety;
    private final float preferredTemperature;
    private final int preferredRainfall;
    private final int preferredHumidity;
    private final int preferredDrainage;
    private final int preferredWaterRetention;
    private final int preferredRootDepth;
    private final int preferredMinerality;
    private final int growthAdjustment;
    private final int harvestAdjustment;
    private final int resilience;
    private final int ageingPotential;
    private final String ripening;
    private final String wineStyle;
    private final String benefit;

    GrapeCultivar(
            GrapeVariety variety,
            float preferredTemperature,
            int preferredRainfall,
            int preferredHumidity,
            int preferredDrainage,
            int preferredWaterRetention,
            int preferredRootDepth,
            int preferredMinerality,
            int growthAdjustment,
            int harvestAdjustment,
            int resilience,
            int ageingPotential,
            String ripening,
            String wineStyle,
            String benefit
    ) {
        this.variety = variety;
        this.preferredTemperature = preferredTemperature;
        this.preferredRainfall = preferredRainfall;
        this.preferredHumidity = preferredHumidity;
        this.preferredDrainage = preferredDrainage;
        this.preferredWaterRetention = preferredWaterRetention;
        this.preferredRootDepth = preferredRootDepth;
        this.preferredMinerality = preferredMinerality;
        this.growthAdjustment = growthAdjustment;
        this.harvestAdjustment = harvestAdjustment;
        this.resilience = resilience;
        this.ageingPotential = ageingPotential;
        this.ripening = ripening;
        this.wineStyle = wineStyle;
        this.benefit = benefit;
    }

    public GrapeVariety variety() {
        return variety;
    }

    public int growthChanceDenominator() {
        return Math.max(2, variety.growthChanceDenominator() + growthAdjustment);
    }

    public int minimumHarvest() {
        return Math.max(1, variety.minimumHarvest() + harvestAdjustment);
    }

    public int maximumHarvest() {
        return Math.max(minimumHarvest(), variety.maximumHarvest() + harvestAdjustment);
    }

    public int ageingPotential() {
        return ageingPotential;
    }

    public float ageingMultiplier() {
        return 0.85F + ageingPotential * 0.10F;
    }

    public int healthBonus(VineyardThreat threat) {
        if (threat == VineyardThreat.HEALTHY) {
            return 0;
        }
        return switch (this) {
            case FROSTLING -> threat == VineyardThreat.FROST_DAMAGE ? 2 : 0;
            case SUNCREST, HONEYCREST ->
                    threat == VineyardThreat.HEAT_STRESS
                            || threat == VineyardThreat.DROUGHT_STRESS ? 2 : 0;
            case NIGHTBERRY, RIVER_GARNET, GREENWAKE ->
                    threat == VineyardThreat.MILDEW_RISK
                            || threat == VineyardThreat.ROT_RISK
                            || threat == VineyardThreat.PEST_PRESSURE
                            ? resilience : 0;
            case STONEFLOWER -> threat == VineyardThreat.NUTRIENT_IMBALANCE ? 2 : 0;
            default -> resilience >= 2 ? 1 : 0;
        };
    }

    public int siteSuitability(TerroirReport report) {
        int climate = average(
                fit(report.climate().averageTemperature(), preferredTemperature, 0.62F),
                fit(report.climate().rainfall(), preferredRainfall, 70),
                fit(report.climate().humidity(), preferredHumidity, 70)
        );
        int soil = average(
                fit(report.soil().drainage(), preferredDrainage, 75),
                fit(report.soil().waterRetention(), preferredWaterRetention, 75),
                average(
                        fit(report.soil().rootDepth(), preferredRootDepth, 75),
                        fit(report.soil().mineralCharacter(), preferredMinerality, 75),
                        report.soil().suitability()
                )
        );
        int terrain = average(
                report.terrain().sunExposure(),
                100 - Math.max(0, report.terrain().windExposure() - 75),
                report.terrain().suitability()
        );
        return Math.clamp((climate * 45 + soil * 35 + terrain * 20) / 100, 0, 100);
    }

    public Component displayName() {
        return Component.translatable("grape_cultivar.vintner." + serializedName());
    }

    public Component ripeningDisplayName() {
        return Component.translatable("grape_ripening.vintner." + ripening);
    }

    public Component wineStyleDisplayName() {
        return Component.translatable("grape_style.vintner." + wineStyle);
    }

    public Component benefitDisplayName() {
        return Component.translatable("grape_benefit.vintner." + benefit);
    }

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * Cultivars available in normal 1.4.0 play. The remaining enum values stay
     * readable so development worlds and stacks made before the roster was
     * narrowed do not lose their identity.
     */
    public boolean isActive() {
        return ACTIVE_VALUES.contains(this);
    }

    public static List<GrapeCultivar> activeValues() {
        return ACTIVE_VALUES;
    }

    /** Index stored in the block state to select the cultivar vine palette. */
    public int visualIndex() {
        return switch (this) {
            case EMBER_NOIR, IRONWOOD_RED -> 0;
            case VALE_PINOT, NIGHTBERRY -> 1;
            case SUNCREST -> 2;
            case RIVER_GARNET -> 3;
            case GOLDEN_VALE, GREENWAKE -> 0;
            case FROSTLING, SILVERLEAF -> 1;
            case HONEYCREST -> 2;
            case STONEFLOWER -> 3;
        };
    }

    /** Stable asset name, independent of the legacy serialized identity. */
    public String visualName() {
        return switch (this) {
            case EMBER_NOIR, IRONWOOD_RED -> "crimson";
            case VALE_PINOT, NIGHTBERRY -> "shaded";
            case SUNCREST -> "sunlit";
            case RIVER_GARNET -> "riverside";
            case GOLDEN_VALE, GREENWAKE -> "golden";
            case FROSTLING, SILVERLEAF -> "frosted";
            case HONEYCREST -> "honeyed";
            case STONEFLOWER -> "stony";
        };
    }

    public static GrapeCultivar defaultFor(GrapeVariety variety) {
        return variety == GrapeVariety.RED ? EMBER_NOIR : GOLDEN_VALE;
    }

    public static GrapeCultivar fromName(String name, GrapeVariety fallback) {
        return Arrays.stream(values())
                .filter(value -> value.serializedName().equals(name))
                .findFirst()
                .orElse(defaultFor(fallback));
    }

    public static boolean isCultivarName(String name) {
        return Arrays.stream(values()).anyMatch(value -> value.serializedName().equals(name));
    }

    private static int fit(float value, float target, float tolerance) {
        return Math.clamp(100 - Math.round(Math.abs(value - target) * 100.0F / tolerance), 0, 100);
    }

    private static int average(int first, int second, int third) {
        return (first + second + third) / 3;
    }
}
