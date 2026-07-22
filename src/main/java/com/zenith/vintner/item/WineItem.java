package com.zenith.vintner.item;

import com.zenith.vintner.wine.WineMetadata;
import com.zenith.vintner.wine.WineQuality;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public final class WineItem extends Item {
    private final WineEffectProfile effectProfile;

    public WineItem(
            WineEffectProfile effectProfile,
            Properties properties
    ) {
        super(properties);
        this.effectProfile = effectProfile;
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

        tooltip.accept(
                Component.translatable(
                        "tooltip.vintner.effect_bonus",
                        WineMetadata.quality(stack).effectBonus()
                ).withStyle(ChatFormatting.GRAY)
        );
    }

    @Override
    public ItemStack finishUsingItem(
            ItemStack stack,
            Level level,
            LivingEntity consumer
    ) {
        WineQuality quality = WineMetadata.quality(stack);
        ItemStack result = super.finishUsingItem(
                stack,
                level,
                consumer
        );

        if (!level.isClientSide()) {
            effectProfile.apply(consumer, quality);
        }

        return result;
    }
}
