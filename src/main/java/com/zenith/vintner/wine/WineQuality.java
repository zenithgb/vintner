package com.zenith.vintner.wine;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum WineQuality {
    COMMON(0, "common", ChatFormatting.WHITE),
    FINE(1, "fine", ChatFormatting.AQUA),
    EXCEPTIONAL(2, "exceptional", ChatFormatting.GOLD);

    private final int id;
    private final String translationKey;
    private final ChatFormatting color;

    WineQuality(
            int id,
            String translationKey,
            ChatFormatting color
    ) {
        this.id = id;
        this.translationKey = translationKey;
        this.color = color;
    }

    public int id() {
        return id;
    }

    public Component displayName() {
        return Component.translatable(
                "wine_quality.vintner." + translationKey
        ).withStyle(color);
    }

    public WineQuality improved() {
        return switch (this) {
            case COMMON -> FINE;
            case FINE, EXCEPTIONAL -> EXCEPTIONAL;
        };
    }

    public static WineQuality byId(int id) {
        return switch (id) {
            case 1 -> FINE;
            case 2 -> EXCEPTIONAL;
            default -> COMMON;
        };
    }
}
