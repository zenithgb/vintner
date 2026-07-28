package com.zenith.vintner.wine;

import com.zenith.vintner.item.WineEffectProfile;
import com.zenith.vintner.registry.ModAttachments;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class WineConsumptionManager {
    public static final int RECOVERY_TICKS = 5 * 60 * 20;
    public static final int MAX_TRACKED_DRINKS = 4;

    private WineConsumptionManager() {
    }

    public static ConsumptionResult consume(
            ServerLevel level,
            LivingEntity consumer,
            WineEffectProfile profile,
            WineQuality quality
    ) {
        return consume(
                level,
                consumer,
                profile,
                quality,
                WineAgeStage.DEVELOPING
        );
    }

    public static ConsumptionResult consume(
            ServerLevel level,
            LivingEntity consumer,
            WineEffectProfile profile,
            WineQuality quality,
            WineAgeStage ageStage
    ) {
        AttachmentTarget target = (AttachmentTarget) consumer;
        long gameTime = level.getGameTime();

        WineConsumptionState current = target.getAttachedOrElse(
                ModAttachments.WINE_CONSUMPTION,
                WineConsumptionState.SOBER
        ).activeAt(gameTime);

        int nextDrinkCount = Math.min(
                MAX_TRACKED_DRINKS,
                current.drinks() + 1
        );
        float benefitMultiplier =
                benefitMultiplier(nextDrinkCount)
                        * ageStage.benefitMultiplier();

        target.setAttached(
                ModAttachments.WINE_CONSUMPTION,
                new WineConsumptionState(
                        nextDrinkCount,
                        gameTime + RECOVERY_TICKS
                )
        );

        profile.apply(
                consumer,
                quality,
                benefitMultiplier
        );

        boolean impaired = applyImpairment(
                consumer,
                nextDrinkCount
        );

        if (ageStage == WineAgeStage.SPOILED) {
            consumer.addEffect(
                    new MobEffectInstance(
                            MobEffects.NAUSEA,
                            20 * 20,
                            0
                    )
            );
            impaired = true;
        }
        sendFeedback(consumer, nextDrinkCount);
        WinePairingManager.onWineConsumed(
                level,
                consumer,
                profile
        );

        return new ConsumptionResult(
                nextDrinkCount,
                benefitMultiplier,
                impaired
        );
    }

    public static WineConsumptionState state(
            LivingEntity consumer,
            long gameTime
    ) {
        return ((AttachmentTarget) consumer)
                .getAttachedOrElse(
                        ModAttachments.WINE_CONSUMPTION,
                        WineConsumptionState.SOBER
                )
                .activeAt(gameTime);
    }

    public static float adjustGeneralExhaustion(
            LivingEntity consumer,
            float amount
    ) {
        if (WineEffectProfile.AGED_WHITE.isActive(consumer)) {
            return amount * qualityAdjustedMultiplier(
                    0.6F,
                    WineEffectProfile.AGED_WHITE.amplifier(consumer)
            );
        }

        if (WineEffectProfile.WHITE.isActive(consumer)) {
            return amount * qualityAdjustedMultiplier(
                    0.75F,
                    WineEffectProfile.WHITE.amplifier(consumer)
            );
        }

        return amount;
    }

    public static float adjustMeleeExhaustion(
            LivingEntity consumer,
            float amount
    ) {
        if (WineEffectProfile.AGED_RED.isActive(consumer)) {
            return amount * qualityAdjustedMultiplier(
                    0.25F,
                    WineEffectProfile.AGED_RED.amplifier(consumer)
            );
        }

        if (WineEffectProfile.RED.isActive(consumer)) {
            return amount * qualityAdjustedMultiplier(
                    0.5F,
                    WineEffectProfile.RED.amplifier(consumer)
            );
        }

        return amount;
    }

    private static float benefitMultiplier(int drinkCount) {
        return switch (drinkCount) {
            case 1 -> 1.0F;
            case 2 -> 0.75F;
            case 3 -> 0.5F;
            default -> 0.25F;
        };
    }

    private static float qualityAdjustedMultiplier(
            float baseMultiplier,
            int amplifier
    ) {
        return Math.max(
                0.1F,
                baseMultiplier - Math.max(0, amplifier) * 0.1F
        );
    }

    private static boolean applyImpairment(
            LivingEntity consumer,
            int drinkCount
    ) {
        if (drinkCount == 3) {
            consumer.addEffect(
                    new MobEffectInstance(
                            MobEffects.NAUSEA,
                            15 * 20,
                            0
                    )
            );
            return true;
        }

        if (drinkCount >= 4) {
            consumer.addEffect(
                    new MobEffectInstance(
                            MobEffects.NAUSEA,
                            30 * 20,
                            0
                    )
            );
            consumer.addEffect(
                    new MobEffectInstance(
                            MobEffects.SLOWNESS,
                            20 * 20,
                            0
                    )
            );
            consumer.addEffect(
                    new MobEffectInstance(
                            MobEffects.WEAKNESS,
                            20 * 20,
                            0
                    )
            );
            return true;
        }

        return false;
    }

    private static void sendFeedback(
            LivingEntity consumer,
            int drinkCount
    ) {
        if (!(consumer instanceof Player player)) {
            return;
        }

        String messageKey = switch (drinkCount) {
            case 2 -> "message.vintner.wine_diminishing";
            case 3 -> "message.vintner.wine_impaired";
            case 4 -> "message.vintner.wine_overindulged";
            default -> null;
        };

        if (messageKey != null) {
            player.sendOverlayMessage(
                    Component.translatable(messageKey)
            );
        }
    }

    public record ConsumptionResult(
            int drinkCount,
            float benefitMultiplier,
            boolean impaired
    ) {
    }
}
