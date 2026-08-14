package com.zenith.vintner.item;

import com.zenith.vintner.registry.ModItems;
import com.zenith.vintner.wine.WineAgeStage;
import com.zenith.vintner.wine.WineConsumptionManager;
import com.zenith.vintner.wine.WineMetadata;
import com.zenith.vintner.wine.WineQuality;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public final class FilledWineGlassItem extends WineGlassItem {
    public FilledWineGlassItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        WineEffectProfile profile = profile(stack);

        return Component.translatable(
                "item.vintner.filled_wine_glass.named",
                WineMetadata.quality(stack).displayName(),
                profile.displayName(),
                WineMetadata.vintage(stack)
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
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(
                Component.translatable(
                        "tooltip.vintner.wine_glass.serving"
                ).withStyle(ChatFormatting.GRAY)
        );
        tooltip.accept(
                profile(stack).conciseSummary()
                        .copy()
                        .withStyle(ChatFormatting.DARK_GRAY)
        );
    }

    @Override
    public ItemStack finishUsingItem(
            ItemStack stack,
            Level level,
            LivingEntity consumer
    ) {
        WineQuality quality = WineMetadata.quality(stack);
        WineAgeStage ageStage = WineMetadata.ageStage(stack);
        WineEffectProfile profile = profile(stack);
        ItemStack result = super.finishUsingItem(stack, level, consumer);

        if (level instanceof ServerLevel serverLevel) {
            WineConsumptionManager.consume(
                    serverLevel,
                    consumer,
                    profile,
                    quality,
                    ageStage,
                    0.25F
            );
        }

        return result;
    }

    public static ItemStack fromBottle(ItemStack source) {
        ItemStack glass = new ItemStack(ModItems.FILLED_WINE_GLASS);
        WineMetadata.copyBatchMetadata(source, glass);

        String profileId = source.getItem() instanceof WineItem wine
                ? wine.effectProfile().id()
                : WineMetadata.effectProfile(source);
        WineMetadata.setEffectProfile(glass, profileId);
        WineMetadata.setServings(glass, 1);
        return glass;
    }

    private static WineEffectProfile profile(ItemStack stack) {
        return WineEffectProfile.byId(
                WineMetadata.effectProfile(stack)
        );
    }
}
