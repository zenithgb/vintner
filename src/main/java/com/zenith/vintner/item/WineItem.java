package com.zenith.vintner.item;

import com.zenith.vintner.wine.WineMetadata;
import com.zenith.vintner.wine.WineQuality;
import com.zenith.vintner.wine.WineConsumptionManager;
import com.zenith.vintner.wine.WineAgeStage;
import com.zenith.vintner.block.entity.WineBottleBlockEntity;
import com.zenith.vintner.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

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

        int bottleNumber = WineMetadata.bottleNumber(stack);

        if (bottleNumber > 0) {
            tooltip.accept(
                    Component.translatable(
                            "tooltip.vintner.wine.bottle_number",
                            bottleNumber,
                            WineMetadata.batchBottleCount(stack)
                    ).withStyle(ChatFormatting.DARK_GRAY)
            );
        }

        int servings = WineMetadata.servings(stack);
        tooltip.accept(
                Component.translatable(
                        servings == 1
                                ? "tooltip.vintner.wine.serving"
                                : "tooltip.vintner.wine.servings",
                        servings,
                        WineMetadata.SERVINGS_PER_BOTTLE
                ).withStyle(ChatFormatting.GRAY)
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
        WineAgeStage ageStage = WineMetadata.ageStage(stack);
        int servings = WineMetadata.servings(stack);
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
                    quality,
                    ageStage,
                    servings
                            / (float) WineMetadata.SERVINGS_PER_BOTTLE
            );
        }

        return result;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPlaceContext placement = new BlockPlaceContext(context);

        if (!placement.canPlace()) {
            return InteractionResult.FAIL;
        }

        Level level = context.getLevel();
        BlockPos pos = placement.getClickedPos();
        BlockState state = ModBlocks.WINE_BOTTLE
                .getStateForPlacement(placement);

        if (state == null) {
            return InteractionResult.FAIL;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        serverLevel.setBlock(pos, state, Block.UPDATE_ALL);

        if (!(serverLevel.getBlockEntity(pos)
                instanceof WineBottleBlockEntity bottleEntity)) {
            serverLevel.removeBlock(pos, false);
            return InteractionResult.FAIL;
        }

        ItemStack bottle = context.getItemInHand().copyWithCount(1);
        WineMetadata.ensureDefaults(bottle);
        WineMetadata.setEffectProfile(bottle, effectProfile.id());
        WineMetadata.ensureBatchIdentity(
                bottle,
                WineMetadata.createBatchId(
                        serverLevel.getGameTime(),
                        pos
                )
        );
        bottleEntity.setBottle(bottle);
        serverLevel.gameEvent(
                GameEvent.BLOCK_PLACE,
                pos,
                GameEvent.Context.of(context.getPlayer(), state)
        );
        serverLevel.playSound(
                null,
                pos,
                SoundEvents.GLASS_PLACE,
                SoundSource.BLOCKS,
                0.8F,
                1.0F
        );

        if (context.getPlayer() == null
                || !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }

        return InteractionResult.SUCCESS;
    }

    public WineEffectProfile effectProfile() {
        return effectProfile;
    }

    public static void consumeServing(
            ServerLevel level,
            LivingEntity consumer,
            ItemStack serving
    ) {
        WineEffectProfile profile = serving.getItem() instanceof WineItem wine
                ? wine.effectProfile()
                : WineEffectProfile.byId(
                        WineMetadata.effectProfile(serving)
                );

        WineConsumptionManager.consume(
                level,
                consumer,
                profile,
                WineMetadata.quality(serving),
                WineMetadata.ageStage(serving),
                1.0F / WineMetadata.SERVINGS_PER_BOTTLE
        );
    }
}
