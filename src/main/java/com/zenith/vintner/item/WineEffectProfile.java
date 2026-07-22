package com.zenith.vintner.item;

import com.zenith.vintner.wine.WineQuality;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public enum WineEffectProfile {
    RED(
            effect(MobEffects.STRENGTH, 20, true),
            effect(MobEffects.REGENERATION, 8, false)
    ),
    WHITE(
            effect(MobEffects.SPEED, 30, true),
            effect(MobEffects.LUCK, 45, false)
    ),
    AGED_RED(
            effect(MobEffects.STRENGTH, 40, true),
            effect(MobEffects.REGENERATION, 15, false),
            effect(MobEffects.RESISTANCE, 20, false)
    ),
    AGED_WHITE(
            effect(MobEffects.SPEED, 60, true),
            effect(MobEffects.LUCK, 90, false),
            effect(MobEffects.HASTE, 30, false)
    );

    private final List<EffectDefinition> effects;

    WineEffectProfile(EffectDefinition... effects) {
        this.effects = List.of(effects);
    }

    public void apply(
            LivingEntity consumer,
            WineQuality quality
    ) {
        for (EffectDefinition definition : effects) {
            int duration = Math.round(
                    definition.baseDurationTicks()
                            * quality.durationMultiplier()
            );

            int amplifier = definition.signature()
                    ? quality.signatureEffectAmplifier()
                    : 0;

            consumer.addEffect(
                    new MobEffectInstance(
                            definition.effect(),
                            duration,
                            amplifier
                    )
            );
        }
    }

    private static EffectDefinition effect(
            Holder<MobEffect> effect,
            int durationSeconds,
            boolean signature
    ) {
        return new EffectDefinition(
                effect,
                durationSeconds * 20,
                signature
        );
    }

    private record EffectDefinition(
            Holder<MobEffect> effect,
            int baseDurationTicks,
            boolean signature
    ) {
    }
}
