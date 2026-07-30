package com.zenith.vintner.wine;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum WineQuality {
    /*
     * IDs 0, 1, and 2 deliberately retain their pre-1.2 meanings so
     * existing bottles and barrel saves remain readable. New tiers use
     * new IDs even though their gameplay order differs from ID order.
     */
    ROUGH(3, "rough", ChatFormatting.DARK_RED, 0, 29, 20),
    TABLE(0, "table", ChatFormatting.GRAY, 30, 44, 38),
    GOOD(4, "good", ChatFormatting.GREEN, 45, 59, 52),
    FINE(1, "fine", ChatFormatting.AQUA, 60, 74, 67),
    EXCEPTIONAL(2, "exceptional", ChatFormatting.GOLD, 75, 89, 82),
    LEGENDARY(5, "legendary", ChatFormatting.LIGHT_PURPLE, 90, 100, 95);

    private final int id;
    private final String translationKey;
    private final ChatFormatting color;
    private final int minimumScore;
    private final int maximumScore;
    private final int baselineScore;

    WineQuality(
            int id,
            String translationKey,
            ChatFormatting color,
            int minimumScore,
            int maximumScore,
            int baselineScore
    ) {
        this.id = id;
        this.translationKey = translationKey;
        this.color = color;
        this.minimumScore = minimumScore;
        this.maximumScore = maximumScore;
        this.baselineScore = baselineScore;
    }

    public int id() {
        return id;
    }

    public Component displayName() {
        return Component.translatable(
                "wine_quality.vintner." + translationKey
        ).withStyle(color);
    }

    public int minimumScore() {
        return minimumScore;
    }

    public int maximumScore() {
        return maximumScore;
    }

    public int baselineScore() {
        return baselineScore;
    }

    public float durationMultiplier() {
        return switch (this) {
            case ROUGH -> 0.75F;
            case TABLE -> 1.0F;
            case GOOD -> 1.1F;
            case FINE -> 1.25F;
            case EXCEPTIONAL -> 1.5F;
            case LEGENDARY -> 1.75F;
        };
    }

    public int signatureEffectAmplifier() {
        return switch (this) {
            case LEGENDARY -> 2;
            case EXCEPTIONAL -> 1;
            default -> 0;
        };
    }

    public float ageingPotential() {
        return switch (this) {
            case ROUGH -> 0.6F;
            case TABLE -> 0.75F;
            case GOOD -> 0.9F;
            case FINE -> 1.0F;
            case EXCEPTIONAL -> 1.5F;
            case LEGENDARY -> 2.0F;
        };
    }

    public Component effectBonus() {
        return Component.translatable(
                "wine_effect_bonus.vintner." + translationKey
        ).withStyle(color);
    }

    public static WineQuality fromScore(int score) {
        int safeScore = Math.clamp(score, 0, 100);

        for (WineQuality quality : values()) {
            if (safeScore >= quality.minimumScore
                    && safeScore <= quality.maximumScore) {
                return quality;
            }
        }

        return TABLE;
    }

    public static WineQuality byId(int id) {
        return switch (id) {
            case 1 -> FINE;
            case 2 -> EXCEPTIONAL;
            case 3 -> ROUGH;
            case 4 -> GOOD;
            case 5 -> LEGENDARY;
            default -> TABLE;
        };
    }
}
