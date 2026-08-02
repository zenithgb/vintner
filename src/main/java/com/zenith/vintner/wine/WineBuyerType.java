package com.zenith.vintner.wine;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Stable market profiles used to explain who is most likely to value a wine.
 * These are preference profiles, not proof that a matching buyer is present.
 */
public enum WineBuyerType {
    TAVERN_KEEPER("tavern_keeper"),
    VILLAGE_MERCHANT("village_merchant"),
    NOBLE_HOUSEHOLD("noble_household"),
    MONASTERY("monastery"),
    MINING_SETTLEMENT("mining_settlement"),
    COASTAL_SETTLEMENT("coastal_settlement"),
    COLD_REGION_SETTLEMENT("cold_region_settlement"),
    FESTIVAL_ORGANISER("festival_organiser"),
    TRAVELLING_MERCHANT("travelling_merchant"),
    COLLECTOR("collector");

    private final String id;

    WineBuyerType(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public Component displayName() {
        return Component.translatable("wine_buyer.vintner." + id);
    }

    public int preferenceAdjustment(ItemStack bottle) {
        WineStyle style = WineMetadata.wineStyle(bottle);
        WineQuality quality = WineMetadata.quality(bottle);
        WineAgeStage age = WineMetadata.ageStage(bottle);

        if (age == WineAgeStage.SPOILED) {
            return 0;
        }

        return switch (this) {
            case TAVERN_KEEPER -> tavernPreference(quality, age);
            case VILLAGE_MERCHANT -> reliablePreference(quality);
            case NOBLE_HOUSEHOLD -> luxuryPreference(quality)
                    + maturePreference(age);
            case MONASTERY -> maturePreference(age);
            case MINING_SETTLEMENT -> (style == WineStyle.RED ? 2 : 0)
                    + reliablePreference(quality);
            case COASTAL_SETTLEMENT -> (style == WineStyle.WHITE ? 2 : 0)
                    + freshPreference(age);
            case COLD_REGION_SETTLEMENT -> (style == WineStyle.RED ? 1 : 0)
                    + robustPreference(quality, age);
            case FESTIVAL_ORGANISER -> reliablePreference(quality)
                    + freshPreference(age);
            case TRAVELLING_MERCHANT -> merchantPreference(quality, age);
            case COLLECTOR -> collectorPreference(quality, age);
        };
    }

    private static int tavernPreference(
            WineQuality quality,
            WineAgeStage age
    ) {
        int qualityPreference = switch (quality) {
            case ROUGH -> 1;
            case TABLE -> 2;
            case GOOD -> 1;
            default -> 0;
        };
        return qualityPreference + freshPreference(age);
    }

    private static int reliablePreference(WineQuality quality) {
        return switch (quality) {
            case TABLE, GOOD -> 1;
            default -> 0;
        };
    }

    private static int luxuryPreference(WineQuality quality) {
        return switch (quality) {
            case FINE -> 1;
            case EXCEPTIONAL -> 2;
            case LEGENDARY -> 3;
            default -> 0;
        };
    }

    private static int maturePreference(WineAgeStage age) {
        return switch (age) {
            case MATURE -> 1;
            case PEAK -> 2;
            default -> 0;
        };
    }

    private static int freshPreference(WineAgeStage age) {
        return switch (age) {
            case YOUNG, DEVELOPING -> 1;
            default -> 0;
        };
    }

    private static int robustPreference(
            WineQuality quality,
            WineAgeStage age
    ) {
        int qualityPreference = switch (quality) {
            case EXCEPTIONAL, LEGENDARY -> 2;
            case FINE -> 1;
            default -> 0;
        };
        return qualityPreference + maturePreference(age);
    }

    private static int merchantPreference(
            WineQuality quality,
            WineAgeStage age
    ) {
        int qualityPreference = switch (quality) {
            case GOOD, FINE -> 1;
            default -> 0;
        };
        return qualityPreference + (age == WineAgeStage.MATURE ? 1 : 0);
    }

    private static int collectorPreference(
            WineQuality quality,
            WineAgeStage age
    ) {
        int qualityPreference = switch (quality) {
            case EXCEPTIONAL -> 2;
            case LEGENDARY -> 4;
            default -> 0;
        };
        int agePreference = switch (age) {
            case MATURE -> 1;
            case PEAK -> 3;
            default -> 0;
        };
        return qualityPreference + agePreference;
    }
}
