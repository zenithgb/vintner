package com.zenith.vintner.item;

import com.zenith.vintner.wine.WineMetadata;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public final class GrapeItem extends Item {
    public GrapeItem(Properties properties) {
        super(properties);
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
