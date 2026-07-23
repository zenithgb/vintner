package com.zenith.vintner.advancement;

import com.zenith.vintner.Vintner;
import com.zenith.vintner.registry.ModBlocks;
import com.zenith.vintner.registry.ModItems;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class ModAdvancements {
    private ModAdvancements() {
    }

    public static void grantPlanting(
            ServerPlayer player,
            Block grapevine
    ) {
        grant(
                player,
                "vintner/plant_grapevine",
                grapevine == ModBlocks.RED_GRAPEVINE
                        ? "red_grapevine"
                        : "white_grapevine"
        );
    }

    public static void grantPressing(
            ServerPlayer player,
            ItemStack must
    ) {
        grant(
                player,
                "vintner/press_must",
                must.is(ModItems.RED_MUST)
                        ? "red_must"
                        : "white_must"
        );
    }

    public static void grantFermentation(
            ServerPlayer player,
            ItemStack wine
    ) {
        grant(
                player,
                "vintner/ferment_wine",
                wine.is(ModItems.RED_WINE)
                        ? "red_wine"
                        : "white_wine"
        );
    }

    public static void grantAging(
            ServerPlayer player,
            ItemStack agedWine
    ) {
        grant(
                player,
                "vintner/age_wine",
                agedWine.is(ModItems.AGED_RED_WINE)
                        ? "aged_red_wine"
                        : "aged_white_wine"
        );
    }

    private static void grant(
            ServerPlayer player,
            String path,
            String criterion
    ) {
        AdvancementHolder advancement = player
                .level()
                .getServer()
                .getAdvancements()
                .get(
                        Identifier.fromNamespaceAndPath(
                                Vintner.MOD_ID,
                                path
                        )
                );

        if (advancement == null) {
            return;
        }

        if (player.getAdvancements()
                .getOrStartProgress(advancement)
                .isDone()) {
            return;
        }

        player.getAdvancements().award(
                advancement,
                criterion
        );
    }
}
