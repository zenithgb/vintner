package com.zenith.vintner.item;

import com.zenith.vintner.wine.WineQuality;
import com.zenith.vintner.registry.ModMobEffects;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public enum WineEffectProfile {
    RED(ModMobEffects.RED_WINE_PROFILE, 20, "red"),
    WHITE(ModMobEffects.WHITE_WINE_PROFILE, 45, "white"),
    AGED_RED(ModMobEffects.AGED_RED_WINE_PROFILE, 40, "aged_red"),
    AGED_WHITE(ModMobEffects.AGED_WHITE_WINE_PROFILE, 90, "aged_white");

    private final Holder<MobEffect> effect;
    private final int baseDurationTicks;
    private final String translationKey;

    WineEffectProfile(
            Holder<MobEffect> effect,
            int baseDurationSeconds,
            String translationKey
    ) {
        this.effect = effect;
        this.baseDurationTicks = baseDurationSeconds * 20;
        this.translationKey = translationKey;
    }

    public void apply(
            LivingEntity consumer,
            WineQuality quality,
            float consumptionMultiplier
    ) {
        clearActiveProfile(consumer);

        int duration = Math.max(
                1,
                Math.round(
                        baseDurationTicks
                                * quality.durationMultiplier()
                                * consumptionMultiplier
                )
        );

        consumer.addEffect(
                new MobEffectInstance(
                        effect,
                        duration,
                        quality.signatureEffectAmplifier(),
                        false,
                        false,
                        false
                )
        );
    }

    public boolean isActive(LivingEntity consumer) {
        return consumer.hasEffect(effect);
    }

    public int remainingDuration(LivingEntity consumer) {
        MobEffectInstance instance = consumer.getEffect(effect);
        return instance == null ? 0 : instance.getDuration();
    }

    public int amplifier(LivingEntity consumer) {
        MobEffectInstance instance = consumer.getEffect(effect);
        return instance == null ? -1 : instance.getAmplifier();
    }

    public boolean isRedStyle() {
        return this == RED || this == AGED_RED;
    }

    public void extendActiveDuration(
            LivingEntity consumer,
            float multiplier
    ) {
        MobEffectInstance active = consumer.getEffect(effect);

        if (active == null) {
            return;
        }

        MobEffectInstance extended =
                active.withScaledDuration(multiplier);
        consumer.addEffect(extended);
    }

    public Component effectSummary() {
        return Component.translatable(
                "wine_profile.vintner." + translationKey
        );
    }

    public Component conciseSummary() {
        return Component.translatable(
                "tooltip.vintner.wine_benefit." + translationKey
        );
    }

    public static void clearActiveProfile(
            LivingEntity consumer
    ) {
        for (WineEffectProfile profile : values()) {
            consumer.removeEffect(profile.effect);
        }
    }

    public static WineEffectProfile activeProfile(
            LivingEntity consumer
    ) {
        for (WineEffectProfile profile : values()) {
            if (profile.isActive(consumer)) {
                return profile;
            }
        }

        return null;
    }
}
