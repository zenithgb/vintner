package com.zenith.vintner.effect;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public final class WineProfileMobEffect extends MobEffect {
    public WineProfileMobEffect(
            int color,
            AttributeBonus... bonuses
    ) {
        super(MobEffectCategory.BENEFICIAL, color);

        for (AttributeBonus bonus : bonuses) {
            addAttributeModifier(
                    bonus.attribute(),
                    bonus.id(),
                    bonus.amount(),
                    bonus.operation()
            );
        }
    }

    public static AttributeBonus attribute(
            Holder<Attribute> attribute,
            Identifier id,
            double amount,
            AttributeModifier.Operation operation
    ) {
        return new AttributeBonus(
                attribute,
                id,
                amount,
                operation
        );
    }

    public record AttributeBonus(
            Holder<Attribute> attribute,
            Identifier id,
            double amount,
            AttributeModifier.Operation operation
    ) {
    }
}
