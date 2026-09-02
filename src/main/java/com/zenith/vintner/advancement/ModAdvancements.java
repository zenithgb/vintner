package com.zenith.vintner.advancement;

import com.zenith.vintner.Vintner;
import com.zenith.vintner.block.GrapevineBlock;
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
                grapevine instanceof GrapevineBlock vine
                        && vine.getVariety()
                        == com.zenith.vintner.vineyard.GrapeVariety.RED
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

    public static void grantInspection(ServerPlayer player) {
        grant(
                player,
                "vintner/inspect_wine",
                "inspected"
        );
    }

    public static void grantSurvey(ServerPlayer player) {
        grant(
                player,
                "vintner/survey_vineyard",
                "surveyed"
        );
    }

    public static void grantProperPour(ServerPlayer player) {
        grant(
                player,
                "vintner/proper_pour",
                "proper_pour"
        );
    }

    public static void grantGoodCompany(ServerPlayer player) {
        grant(
                player,
                "vintner/to_good_company",
                "good_company"
        );
    }

    public static void grantIdealCellar(ServerPlayer player) {
        grant(
                player,
                "vintner/ideal_cellar",
                "ideal_cellar"
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
