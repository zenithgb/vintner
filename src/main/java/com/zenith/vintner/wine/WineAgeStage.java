package com.zenith.vintner.wine;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum WineAgeStage {
    YOUNG("young", ChatFormatting.GRAY),
    DEVELOPING("developing", ChatFormatting.GREEN),
    MATURE("mature", ChatFormatting.AQUA),
    PEAK("peak", ChatFormatting.GOLD),
    DECLINING("declining", ChatFormatting.DARK_RED),
    SPOILED("spoiled", ChatFormatting.RED);

    public static final long DEVELOPING_AT = 7L * 24000L;
    public static final long MATURE_AT = 30L * 24000L;
    public static final long PEAK_AT = 60L * 24000L;
    public static final long DECLINING_AT = 120L * 24000L;
    public static final int SPOILED_DAMAGE = 24000;

    private final String translationKey;
    private final ChatFormatting color;

    WineAgeStage(
            String translationKey,
            ChatFormatting color
    ) {
        this.translationKey = translationKey;
        this.color = color;
    }

    public Component displayName() {
        return Component.translatable(
                "wine_age.vintner." + translationKey
        ).withStyle(color);
    }

    public float benefitMultiplier() {
        return switch (this) {
            case YOUNG -> 0.9F;
            case DEVELOPING -> 1.0F;
            case MATURE -> 1.1F;
            case PEAK -> 1.25F;
            case DECLINING -> 0.75F;
            case SPOILED -> 0.25F;
        };
    }

    public static WineAgeStage from(
            long age,
            int damage,
            WineQuality quality
    ) {
        if (damage >= SPOILED_DAMAGE) {
            return SPOILED;
        }

        float potential = quality.ageingPotential();
        long adjusted = Math.round(age / potential);

        if (adjusted >= DECLINING_AT) {
            return DECLINING;
        }
        if (adjusted >= PEAK_AT) {
            return PEAK;
        }
        if (adjusted >= MATURE_AT) {
            return MATURE;
        }
        if (adjusted >= DEVELOPING_AT) {
            return DEVELOPING;
        }
        return YOUNG;
    }
}
