package com.zenith.vintner.registry;

import com.zenith.vintner.Vintner;
import com.zenith.vintner.effect.WineProfileMobEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import static com.zenith.vintner.effect.WineProfileMobEffect.attribute;

public final class ModMobEffects {
    public static final Holder<MobEffect> RED_WINE_PROFILE = register(
            "red_wine_profile",
            new WineProfileMobEffect(
                    0x7A1F2B,
                    attribute(
                            Attributes.KNOCKBACK_RESISTANCE,
                            id("wine_knockback_resistance"),
                            0.25,
                            AttributeModifier.Operation.ADD_VALUE
                    )
            )
    );

    public static final Holder<MobEffect> WHITE_WINE_PROFILE = register(
            "white_wine_profile",
            new WineProfileMobEffect(
                    0xD8C76A,
                    attribute(
                            Attributes.BLOCK_BREAK_SPEED,
                            id("wine_block_break_speed"),
                            0.15,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
            )
    );

    public static final Holder<MobEffect> AGED_RED_WINE_PROFILE = register(
            "aged_red_wine_profile",
            new WineProfileMobEffect(
                    0x4A111D,
                    attribute(
                            Attributes.KNOCKBACK_RESISTANCE,
                            id("wine_knockback_resistance"),
                            0.4,
                            AttributeModifier.Operation.ADD_VALUE
                    )
            )
    );

    public static final Holder<MobEffect> AGED_WHITE_WINE_PROFILE = register(
            "aged_white_wine_profile",
            new WineProfileMobEffect(
                    0xA89245,
                    attribute(
                            Attributes.BLOCK_BREAK_SPEED,
                            id("wine_block_break_speed"),
                            0.25,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
            )
    );

    private ModMobEffects() {
    }

    private static Holder<MobEffect> register(
            String name,
            MobEffect effect
    ) {
        Identifier id = Identifier.fromNamespaceAndPath(
                Vintner.MOD_ID,
                name
        );

        return Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                id,
                effect
        );
    }

    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(
                Vintner.MOD_ID,
                name
        );
    }

    public static void initialize() {
    }
}
