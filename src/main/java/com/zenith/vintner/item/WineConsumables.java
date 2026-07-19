package com.zenith.vintner.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

import java.util.List;

public final class WineConsumables {
    public static final Consumable RED_WINE =
            Consumable.builder()
                    .consumeSeconds(1.6F)
                    .animation(ItemUseAnimation.DRINK)
                    .sound(SoundEvents.GENERIC_DRINK)
                    .hasConsumeParticles(false)
                    .onConsume(
                            new ApplyStatusEffectsConsumeEffect(
                                    List.of(
                                            new MobEffectInstance(
                                                    MobEffects.STRENGTH,
                                                    20 * 20,
                                                    0
                                            ),
                                            new MobEffectInstance(
                                                    MobEffects.REGENERATION,
                                                    20 * 8,
                                                    0
                                            )
                                    )
                            )
                    )
                    .build();

    public static final Consumable WHITE_WINE =
            Consumable.builder()
                    .consumeSeconds(1.6F)
                    .animation(ItemUseAnimation.DRINK)
                    .sound(SoundEvents.GENERIC_DRINK)
                    .hasConsumeParticles(false)
                    .onConsume(
                            new ApplyStatusEffectsConsumeEffect(
                                    List.of(
                                            new MobEffectInstance(
                                                    MobEffects.SPEED,
                                                    20 * 30,
                                                    0
                                            ),
                                            new MobEffectInstance(
                                                    MobEffects.LUCK,
                                                    20 * 45,
                                                    0
                                            )
                                    )
                            )
                    )
                    .build();

    public static final Consumable AGED_RED_WINE =
            Consumable.builder()
                    .consumeSeconds(1.6F)
                    .animation(ItemUseAnimation.DRINK)
                    .sound(SoundEvents.GENERIC_DRINK)
                    .hasConsumeParticles(false)
                    .onConsume(
                            new ApplyStatusEffectsConsumeEffect(
                                    List.of(
                                            new MobEffectInstance(
                                                    MobEffects.STRENGTH,
                                                    20 * 40,
                                                    0
                                            ),
                                            new MobEffectInstance(
                                                    MobEffects.REGENERATION,
                                                    20 * 15,
                                                    0
                                            ),
                                            new MobEffectInstance(
                                                    MobEffects.RESISTANCE,
                                                    20 * 20,
                                                    0
                                            )
                                    )
                            )
                    )
                    .build();

    public static final Consumable AGED_WHITE_WINE =
            Consumable.builder()
                    .consumeSeconds(1.6F)
                    .animation(ItemUseAnimation.DRINK)
                    .sound(SoundEvents.GENERIC_DRINK)
                    .hasConsumeParticles(false)
                    .onConsume(
                            new ApplyStatusEffectsConsumeEffect(
                                    List.of(
                                            new MobEffectInstance(
                                                    MobEffects.SPEED,
                                                    20 * 60,
                                                    0
                                            ),
                                            new MobEffectInstance(
                                                    MobEffects.LUCK,
                                                    20 * 90,
                                                    0
                                            ),
                                            new MobEffectInstance(
                                                    MobEffects.HASTE,
                                                    20 * 30,
                                                    0
                                            )
                                    )
                            )
                    )
                    .build();

    private WineConsumables() {
    }
}
