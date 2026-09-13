package com.zenith.vintner.item;

import com.zenith.vintner.wine.WineMetadata;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import com.zenith.vintner.registry.ModItems;
import com.zenith.vintner.vineyard.GrapeVariety;
import com.zenith.vintner.vineyard.GrapeCultivar;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public final class GrapeItem extends Item {
    // Loose grapes remain ingredients. A placed bowl serving represents two
    // grapes, with the food definition owned here for future cultivar balance.
    public static final FoodProperties TWO_GRAPE_SERVING = new FoodProperties.Builder()
            .nutrition(2)
            .saturationModifier(0.1F)
            .build();

    public static FoodProperties servingFood(GrapeCultivar cultivar) {
        return TWO_GRAPE_SERVING;
    }

    public GrapeItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        GrapeVariety fallback = stack.is(ModItems.WHITE_GRAPES)
                ? GrapeVariety.WHITE
                : GrapeVariety.RED;
        return Component.translatable(
                "item.vintner.cultivar_grapes",
                WineMetadata.cultivar(stack, fallback).displayName()
        );
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(
                stack,
                context,
                display,
                tooltip,
                flag
        );

        tooltip.accept(
                WineMetadata.qualityTooltip(stack)
                        .copy()
                        .withStyle(ChatFormatting.GRAY)
        );

        tooltip.accept(
                WineMetadata.vintageTooltip(stack)
                        .copy()
                        .withStyle(ChatFormatting.DARK_GRAY)
        );
    }
}
