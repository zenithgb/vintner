package com.zenith.vintner.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Delivers Vintner feedback once, from the authoritative server side. */
public final class VintnerNotifications {
    private VintnerNotifications() {
    }

    public static void send(Player player, Component message) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(message);
        }
    }

    public static void send(
            Player player,
            Component message,
            boolean overlay
    ) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(message, overlay);
        }
    }
}
