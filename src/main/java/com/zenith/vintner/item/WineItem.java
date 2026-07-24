package com.zenith.vintner.item;

import com.zenith.vintner.wine.WineMetadata;
import com.zenith.vintner.wine.WineQuality;
import com.zenith.vintner.wine.WineConsumptionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
                effectProfile.conciseSummary()
                        .copy()
                        .withStyle(ChatFormatting.GRAY)
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

        if (level instanceof ServerLevel serverLevel) {
            WineConsumptionManager.consume(
                    serverLevel,
                    consumer,
                    effectProfile,
                    quality
            );
        }

        return result;
    }
}
