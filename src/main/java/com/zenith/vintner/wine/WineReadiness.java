package com.zenith.vintner.wine;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public enum WineReadiness {
    HOLD("hold", ChatFormatting.GREEN),
    DRINK_NOW("drink_now", ChatFormatting.GOLD),
    PAST_PEAK("past_peak", ChatFormatting.DARK_RED),
    SPOILED("spoiled", ChatFormatting.RED);

    private final String translationKey;
    private final ChatFormatting color;

    WineReadiness(
            String translationKey,
            ChatFormatting color
    ) {
        this.translationKey = translationKey;
        this.color = color;
    }

    public Component displayName() {
        return Component.translatable(
                "wine_readiness.vintner." + translationKey
        ).withStyle(color);
    }

    public static WineReadiness from(ItemStack bottle) {
        return switch (WineMetadata.ageStage(bottle)) {
            case YOUNG, DEVELOPING -> HOLD;
            case MATURE, PEAK -> DRINK_NOW;
            case DECLINING -> PAST_PEAK;
            case SPOILED -> SPOILED;
        };
    }
}
