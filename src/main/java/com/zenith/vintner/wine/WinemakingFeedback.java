package com.zenith.vintner.wine;

import com.zenith.vintner.block.entity.AgingBarrelBlockEntity;
import com.zenith.vintner.block.entity.FermentationBarrelBlockEntity;
import com.zenith.vintner.block.entity.GrapePressBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class WinemakingFeedback {
    private WinemakingFeedback() {
    }

    public static void showPressStatus(
            Player player,
            GrapePressBlockEntity press
    ) {
        String key = press.canPress()
                ? "message.vintner.press.ready"
                : "message.vintner.press.status";

        show(
                player,
                key,
                press.getInput().getCount(),
                GrapePressBlockEntity.CAPACITY,
                press.getOutput().getCount(),
                GrapePressBlockEntity.CAPACITY
        );
    }

    public static void showPressInsertRejected(
            Player player,
            GrapePressBlockEntity press
    ) {
        String key = press.getInput().getCount()
                >= GrapePressBlockEntity.CAPACITY
                ? "message.vintner.press.input_full"
                : "message.vintner.press.incompatible";

        show(player, key);
    }

    public static void showPressRejected(
            Player player,
            GrapePressBlockEntity press
    ) {
        if (press.getInput().getCount()
                < GrapePressBlockEntity.GRAPES_PER_PRESS) {
            int needed = GrapePressBlockEntity.GRAPES_PER_PRESS
                    - press.getInput().getCount();

            show(
                    player,
                    "message.vintner.press.needs_grapes",
                    needed
            );
            return;
        }

        if (press.getOutput().getCount()
                >= GrapePressBlockEntity.CAPACITY) {
            show(player, "message.vintner.press.output_full");
            return;
        }

        show(player, "message.vintner.press.incompatible");
    }

    public static void showNoMust(Player player) {
        show(player, "message.vintner.press.no_must");
    }

    public static void showFermentationStatus(
            Player player,
            FermentationBarrelBlockEntity barrel
    ) {
        show(player, fermentationStatus(barrel));
    }

    public static Component fermentationStatus(
            FermentationBarrelBlockEntity barrel
    ) {
        if (barrel.getBottleCount() <= 0) {
            return Component.translatable(
                    "message.vintner.fermentation.empty"
            );
        }

        if (barrel.isReady()) {
            return Component.translatable(
                    "message.vintner.fermentation.ready",
                    wineType(barrel.getBatchType()),
                    barrel.getBottleCount(),
                    FermentationBarrelBlockEntity.CAPACITY
            );
        }

        if (barrel.getBottleCount()
                < FermentationBarrelBlockEntity.CAPACITY) {
            return Component.translatable(
                    "message.vintner.fermentation.waiting",
                    wineType(barrel.getBatchType()),
                    barrel.getBottleCount(),
                    FermentationBarrelBlockEntity.CAPACITY
            );
        }

        return Component.translatable(
                "message.vintner.fermentation.progress",
                wineType(barrel.getBatchType()),
                barrel.getBottleCount(),
                FermentationBarrelBlockEntity.CAPACITY,
                barrel.getProgressPercent()
        );
    }

    public static void showFermentationInsertRejected(
            Player player,
            FermentationBarrelBlockEntity barrel
    ) {
        if (barrel.isReady()) {
            show(
                    player,
                    "message.vintner.fermentation.collect_ready"
            );
        } else if (barrel.getBottleCount()
                >= FermentationBarrelBlockEntity.CAPACITY) {
            show(player, "message.vintner.fermentation.full");
        } else {
            show(
                    player,
                    "message.vintner.fermentation.incompatible"
            );
        }
    }

    public static void showAgingStatus(
            Player player,
            AgingBarrelBlockEntity barrel
    ) {
        show(player, agingStatus(barrel));
    }

    public static Component agingStatus(
            AgingBarrelBlockEntity barrel
    ) {
        if (barrel.getBottleCount() <= 0) {
            return Component.translatable("message.vintner.aging.empty");
        }

        if (barrel.isReady()) {
            return Component.translatable(
                    "message.vintner.aging.ready",
                    wineType(barrel.getWineType()),
                    barrel.getBottleCount(),
                    barrel.getCapacity()
            );
        }

        if (barrel.getBottleCount()
                < barrel.getCapacity()) {
            return Component.translatable(
                    "message.vintner.aging.waiting",
                    wineType(barrel.getWineType()),
                    barrel.getBottleCount(),
                    barrel.getCapacity()
            );
        }

        return Component.translatable(
                "message.vintner.aging.progress",
                wineType(barrel.getWineType()),
                barrel.getBottleCount(),
                barrel.getCapacity(),
                barrel.getProgressPercent()
        );
    }

    public static void showAgingInsertRejected(
            Player player,
            AgingBarrelBlockEntity barrel
    ) {
        if (barrel.isReady()) {
            show(player, "message.vintner.aging.collect_ready");
        } else if (barrel.getBottleCount()
                >= barrel.getCapacity()) {
            show(player, "message.vintner.aging.full");
        } else {
            show(player, "message.vintner.aging.incompatible");
        }
    }

    private static Component wineType(int type) {
        return Component.translatable(
                type == 1
                        ? "wine_type.vintner.red"
                        : "wine_type.vintner.white"
        );
    }

    private static void show(
            Player player,
            String key,
            Object... arguments
    ) {
        show(player, Component.translatable(key, arguments));
    }

    private static void show(Player player, Component message) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(
                    message,
                    true
            );
        }
    }
}
