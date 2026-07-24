package com.zenith.vintner.wine;

import com.zenith.vintner.item.WineEffectProfile;
import com.zenith.vintner.registry.ModAttachments;
import com.zenith.vintner.registry.ModItemTags;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class WinePairingManager {
    public static final int RECENT_MEAL_TICKS = 60 * 20;
    public static final float DURATION_MULTIPLIER = 1.5F;

    private static final int RED_MEAL = 1;
    private static final int WHITE_MEAL = 2;

    private WinePairingManager() {
    }

    public static void onMealConsumed(
            ServerLevel level,
            LivingEntity consumer,
            ItemStack meal
    ) {
        int mealTypes = mealTypes(meal);

        if (mealTypes == 0) {
            return;
        }

        long gameTime = level.getGameTime();
        WinePairingState current = state(
                consumer,
                gameTime
        );
        WineEffectProfile activeProfile =
                WineEffectProfile.activeProfile(consumer);

        if (activeProfile != null && current.paired()) {
            return;
        }

        if (
                activeProfile != null
                        && matches(mealTypes, activeProfile)
        ) {
            applyPairing(consumer, activeProfile);
            setState(consumer, current.markPaired());
            return;
        }

        setState(
                consumer,
                current.rememberMeal(
                        mealTypes,
                        gameTime + RECENT_MEAL_TICKS
                )
        );
    }

    public static void onWineConsumed(
            ServerLevel level,
            LivingEntity consumer,
            WineEffectProfile profile
    ) {
        WinePairingState current = state(
                consumer,
                level.getGameTime()
        ).beginWineServing();

        if (matches(current.recentMealTypes(), profile)) {
            applyPairing(consumer, profile);
            current = current.markPaired();
        }

        setState(consumer, current);
    }

    public static WinePairingState state(
            LivingEntity consumer,
            long gameTime
    ) {
        return ((AttachmentTarget) consumer)
                .getAttachedOrElse(
                        ModAttachments.WINE_PAIRING,
                        WinePairingState.EMPTY
                )
                .activeAt(gameTime);
    }

    private static int mealTypes(ItemStack meal) {
        int mealTypes = 0;

        if (meal.is(ModItemTags.PAIRS_WITH_RED_WINE)) {
            mealTypes |= RED_MEAL;
        }

        if (meal.is(ModItemTags.PAIRS_WITH_WHITE_WINE)) {
            mealTypes |= WHITE_MEAL;
        }

        return mealTypes;
    }

    private static boolean matches(
            int mealTypes,
            WineEffectProfile profile
    ) {
        int profileType = profile.isRedStyle()
                ? RED_MEAL
                : WHITE_MEAL;
        return (mealTypes & profileType) != 0;
    }

    private static void applyPairing(
            LivingEntity consumer,
            WineEffectProfile profile
    ) {
        profile.extendActiveDuration(
                consumer,
                DURATION_MULTIPLIER
        );

        if (consumer instanceof Player player) {
            player.sendOverlayMessage(
                    Component.translatable(
                            "message.vintner.wine_pairing"
                    )
            );
        }
    }

    private static void setState(
            LivingEntity consumer,
            WinePairingState state
    ) {
        ((AttachmentTarget) consumer).setAttached(
                ModAttachments.WINE_PAIRING,
                state
        );
    }
}
