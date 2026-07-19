package com.zenith.vintner.wine;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class WineMetadata {
    private static final String VINTAGE_KEY = "VintnerVintage";
    private static final String QUALITY_KEY = "VintnerQuality";

    /*
     * One Minecraft year is currently treated as 96 in-game days.
     * This is intentionally isolated here so later seasons or calendar
     * compatibility can replace the calculation cleanly.
     */
    private static final long DAYS_PER_YEAR = 96L;

    private WineMetadata() {
    }

    public static int vintageFromGameTime(long gameTime) {
        long day = Math.max(0L, gameTime / 24000L);
        return Math.toIntExact((day / DAYS_PER_YEAR) + 1L);
    }

    public static void apply(
            ItemStack stack,
            int vintage,
            WineQuality quality
    ) {
        CompoundTag tag = getTagCopy(stack);
        tag.putInt(VINTAGE_KEY, Math.max(1, vintage));
        tag.putInt(QUALITY_KEY, quality.id());

        stack.set(
                DataComponents.CUSTOM_DATA,
                CustomData.of(tag)
        );
    }

    public static void ensureDefaults(ItemStack stack) {
        if (!hasMetadata(stack)) {
            apply(stack, 1, WineQuality.COMMON);
        }
    }

    public static boolean hasMetadata(ItemStack stack) {
        CompoundTag tag = getTagCopy(stack);

        return tag.contains(VINTAGE_KEY)
                && tag.contains(QUALITY_KEY);
    }

    public static int vintage(ItemStack stack) {
        return getTagCopy(stack).getIntOr(VINTAGE_KEY, 1);
    }

    public static WineQuality quality(ItemStack stack) {
        int qualityId = getTagCopy(stack).getIntOr(
                QUALITY_KEY,
                WineQuality.COMMON.id()
        );

        return WineQuality.byId(qualityId);
    }

    public static void improveQuality(ItemStack stack) {
        apply(
                stack,
                vintage(stack),
                quality(stack).improved()
        );
    }

    public static boolean matchesBatch(
            ItemStack first,
            ItemStack second
    ) {
        return vintage(first) == vintage(second)
                && quality(first) == quality(second);
    }

    public static Component vintageTooltip(ItemStack stack) {
        return Component.translatable(
                "tooltip.vintner.vintage",
                vintage(stack)
        );
    }

    public static Component qualityTooltip(ItemStack stack) {
        return Component.translatable(
                "tooltip.vintner.quality",
                quality(stack).displayName()
        );
    }

    private static CompoundTag getTagCopy(ItemStack stack) {
        CustomData data = stack.getOrDefault(
                DataComponents.CUSTOM_DATA,
                CustomData.EMPTY
        );

        return data.copyTag();
    }
}
