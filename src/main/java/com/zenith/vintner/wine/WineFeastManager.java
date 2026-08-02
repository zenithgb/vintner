package com.zenith.vintner.wine;

import com.zenith.vintner.block.CellarCollectionBlock;
import com.zenith.vintner.block.CellarFixtureKind;
import com.zenith.vintner.item.WineEffectProfile;
import com.zenith.vintner.registry.ModAttachments;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public final class WineFeastManager {
    public static final int DURATION_TICKS = 4 * 60 * 20;
    public static final int CABINET_RADIUS = 4;
    public static final double PLAYER_RADIUS = 8.0;
    public static final float EXHAUSTION_MULTIPLIER = 0.85F;
    public static final float ACTIVE_WINE_MULTIPLIER = 1.15F;

    private WineFeastManager() {
    }

    public static boolean tryStartSharedFeast(
            ServerLevel level,
            LivingEntity consumer
    ) {
        if (!(consumer instanceof ServerPlayer host)) {
            return false;
        }

        long gameTime = level.getGameTime();
        if (state(host, gameTime).isActiveAt(gameTime)
                || !hasTastingCabinet(level, host.blockPosition())) {
            return false;
        }

        double radiusSquared = PLAYER_RADIUS * PLAYER_RADIUS;
        List<ServerPlayer> diners = level.getPlayers(
                player -> player.distanceToSqr(host) <= radiusSquared
        );
        if (diners.size() < 2) {
            return false;
        }

        WineFeastState feast = new WineFeastState(
                gameTime + DURATION_TICKS
        );
        for (ServerPlayer diner : diners) {
            ((AttachmentTarget) diner).setAttached(
                    ModAttachments.WINE_FEAST,
                    feast
            );

            WineEffectProfile profile =
                    WineEffectProfile.activeProfile(diner);
            if (profile != null) {
                profile.extendActiveDuration(
                        diner,
                        ACTIVE_WINE_MULTIPLIER
                );
            }

            diner.sendOverlayMessage(
                    Component.translatable(
                            "message.vintner.shared_feast"
                    )
            );
        }
        return true;
    }

    public static WineFeastState state(
            LivingEntity diner,
            long gameTime
    ) {
        return ((AttachmentTarget) diner)
                .getAttachedOrElse(
                        ModAttachments.WINE_FEAST,
                        WineFeastState.EMPTY
                )
                .activeAt(gameTime);
    }

    public static float adjustExhaustion(
            LivingEntity diner,
            float amount
    ) {
        long gameTime = diner.level().getGameTime();
        return state(diner, gameTime)
                .isActiveAt(gameTime)
                ? amount * EXHAUSTION_MULTIPLIER
                : amount;
    }

    private static boolean hasTastingCabinet(
            ServerLevel level,
            BlockPos center
    ) {
        BlockPos min = center.offset(
                -CABINET_RADIUS,
                -2,
                -CABINET_RADIUS
        );
        BlockPos max = center.offset(
                CABINET_RADIUS,
                2,
                CABINET_RADIUS
        );

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (level.getBlockState(pos).getBlock()
                    instanceof CellarCollectionBlock collection
                    && collection.kind()
                    == CellarFixtureKind.TASTING_CABINET) {
                return true;
            }
        }
        return false;
    }
}
