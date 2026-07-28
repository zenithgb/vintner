package com.zenith.vintner.wine;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum CellarRating {
    POOR(0, "poor", 0.5F, 1, ChatFormatting.RED),
    BASIC(1, "basic", 0.8F, 0, ChatFormatting.GRAY),
    GOOD(2, "good", 1.0F, 0, ChatFormatting.GREEN),
    IDEAL(3, "ideal", 1.25F, 0, ChatFormatting.AQUA);

    private final int id;
    private final String translationKey;
    private final float ageRate;
    private final int damagePerSecond;
    private final ChatFormatting color;

    CellarRating(
            int id,
            String translationKey,
            float ageRate,
            int damagePerSecond,
            ChatFormatting color
    ) {
        this.id = id;
        this.translationKey = translationKey;
        this.ageRate = ageRate;
        this.damagePerSecond = damagePerSecond;
        this.color = color;
    }

    public int id() {
        return id;
    }

    public float ageRate() {
        return ageRate;
    }

    public int storageDamage(long elapsedTicks) {
        long damage = damagePerSecond * elapsedTicks / 20L;
        return (int) Math.min(Integer.MAX_VALUE, damage);
    }

    public Component displayName() {
        return Component.translatable(
                "cellar_rating.vintner." + translationKey
        ).withStyle(color);
    }

    public static CellarRating byId(int id) {
        return switch (id) {
            case 0 -> POOR;
            case 2 -> GOOD;
            case 3 -> IDEAL;
            default -> BASIC;
        };
    }
}
