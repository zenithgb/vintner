package com.zenith.vintner.item;

import com.zenith.vintner.wine.WineMetadata;
import com.zenith.vintner.wine.WineQuality;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public final class WineItem extends Item {
    public WineItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        WineQuality quality = WineMetadata.quality(stack);
        int vintage = WineMetadata.vintage(stack);

        return Component.translatable(
                "item.vintner.wine_named",
                quality.displayName(),
                super.getName(stack),
                vintage
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
