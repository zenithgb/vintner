package com.zenith.vintner.registry;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ItemLike;
import com.zenith.vintner.vineyard.GrapeCultivar;

public final class ModTrades {
    private static final int FALLBACK_MAX_USES = 4;
    private static final int STANDARD_MAX_USES = 12;
    private static final float REPUTATION_DISCOUNT = 0.05F;

    private ModTrades() {
    }

    public static void initialize() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof WanderingTrader trader) {
                addFallbackCuttingOffer(
                        trader.getOffers(),
                        ModItems.RED_GRAPE_CUTTING
                );
                addFallbackCuttingOffer(
                        trader.getOffers(),
                        ModItems.WHITE_GRAPE_CUTTING
                );
            } else if (entity instanceof Villager villager) {
                refreshVillagerOffers(villager);
            }
        });

        UseEntityCallback.EVENT.register(
                (player, level, hand, entity, hitResult) -> {
                    if (!level.isClientSide()
                            && entity instanceof Villager villager) {
                        refreshVillagerOffers(villager);
                    }

                    return net.minecraft.world.InteractionResult.PASS;
                }
        );
    }

    public static void refreshVillagerOffers(Villager villager) {
        if (villager.getVillagerData()
                .profession()
                .is(VillagerProfession.FARMER)) {
            addFallbackCuttingOffer(
                    villager.getOffers(),
                    ModItems.RED_GRAPE_CUTTING
            );
            addFallbackCuttingOffer(
                    villager.getOffers(),
                    ModItems.WHITE_GRAPE_CUTTING
            );
            return;
        }

        int level = villager.getVillagerData().level();

        if (villager.getVillagerData()
                .profession()
                .is(ModVillagers.WINEMAKER)) {
            addWinemakerOffers(villager.getOffers(), level);
        } else if (villager.getVillagerData()
                .profession()
                .is(ModVillagers.COOPER)) {
            addCooperOffers(villager.getOffers(), level);
        }
    }

    private static void addWinemakerOffers(
            MerchantOffers offers,
            int level
    ) {
        if (level >= 1) {
            addBuyOffer(offers, ModItems.RED_GRAPES, 16, 1, 2);
            addBuyOffer(offers, ModItems.WHITE_GRAPES, 16, 1, 2);
            addCultivarOffer(offers, 1, GrapeCultivar.EMBER_NOIR, 2);
            addCultivarOffer(offers, 1, GrapeCultivar.GOLDEN_VALE, 2);
        }

        if (level >= 2) {
            addCultivarOffer(offers, 2, GrapeCultivar.VALE_PINOT, 10);
            addCultivarOffer(offers, 2, GrapeCultivar.FROSTLING, 10);
            addBuyOffer(offers, Items.GLASS_BOTTLE, 12, 1, 10);
            addSellOffer(offers, 2, ModItems.RED_MUST, 1, 10);
            addSellOffer(offers, 2, ModItems.WHITE_MUST, 1, 10);
        }

        if (level >= 3) {
            addCultivarOffer(offers, 3, GrapeCultivar.SUNCREST, 15);
            addCultivarOffer(offers, 3, GrapeCultivar.GREENWAKE, 15);
            addCultivarOffer(offers, 3, GrapeCultivar.IRONWOOD_RED, 15);
            addCultivarOffer(offers, 3, GrapeCultivar.SILVERLEAF, 15);
            addSellOffer(
                    offers,
                    5,
                    ModItems.VINTNER_ALMANAC,
                    1,
                    15
            );
            addSellOffer(
                    offers,
                    8,
                    ModBlocks.GRAPE_PRESS,
                    1,
                    15
            );
        }

        if (level >= 4) {
            addCultivarOffer(offers, 4, GrapeCultivar.NIGHTBERRY, 20);
            addCultivarOffer(offers, 4, GrapeCultivar.HONEYCREST, 20);
            addBuyOffer(offers, ModItems.RED_WINE, 1, 3, 20);
            addBuyOffer(offers, ModItems.WHITE_WINE, 1, 3, 20);
        }

        if (level >= 5) {
            addCultivarOffer(offers, 6, GrapeCultivar.RIVER_GARNET, 30);
            addCultivarOffer(offers, 6, GrapeCultivar.STONEFLOWER, 30);
            addSellOffer(
                    offers,
                    12,
                    ModBlocks.TASTING_CABINET,
                    1,
                    30
            );
            addSellOffer(
                    offers,
                    18,
                    ModBlocks.VINTAGE_ARCHIVE,
                    1,
                    30
            );
        }
    }

    private static void addCooperOffers(
            MerchantOffers offers,
            int level
    ) {
        if (level >= 1) {
            addBuyOffer(offers, Items.STICK, 24, 1, 2);
            addBuyOffer(offers, Items.OAK_PLANKS, 16, 1, 2);
            addSellOffer(
                    offers,
                    4,
                    ModItems.COOPERS_MALLET,
                    1,
                    2
            );
        }

        if (level >= 2) {
            addSellOffer(offers, 3, ModBlocks.BARREL_STAND, 1, 10);
            addSellOffer(
                    offers,
                    8,
                    ModBlocks.FERMENTATION_BARREL,
                    1,
                    10
            );
        }

        if (level >= 3) {
            addSellOffer(offers, 12, ModBlocks.AGING_BARREL, 1, 15);
            addSellOffer(offers, 5, ModBlocks.WINE_RACK, 1, 15);
            addSellOffer(offers, 6, ModBlocks.WINE_CRATE, 1, 15);
        }

        if (level >= 4) {
            addSellOffer(
                    offers,
                    8,
                    ModItems.TOASTING_KIT,
                    1,
                    20
            );
            addSellOffer(
                    offers,
                    10,
                    ModItems.SEASONING_KIT,
                    1,
                    20
            );
            addSellOffer(
                    offers,
                    14,
                    ModItems.CASK_CONVERSION_KIT,
                    1,
                    20
            );
        }

        if (level >= 5) {
            addSellOffer(
                    offers,
                    10,
                    ModBlocks.LABELLED_CELLAR_SHELF,
                    1,
                    30
            );
            addSellOffer(
                    offers,
                    18,
                    ModBlocks.VINTAGE_ARCHIVE,
                    1,
                    30
            );
        }
    }

    private static void addFallbackCuttingOffer(
            MerchantOffers offers,
            Item cutting
    ) {
        if (hasOffer(offers, Items.EMERALD, cutting)) {
            return;
        }

        offers.add(
                new MerchantOffer(
                        new ItemCost(Items.EMERALD),
                        new ItemStack(cutting, 2),
                        FALLBACK_MAX_USES,
                        1,
                        REPUTATION_DISCOUNT
                )
        );
    }

    private static void addBuyOffer(
            MerchantOffers offers,
            ItemLike payment,
            int paymentCount,
            int emeraldCount,
            int xp
    ) {
        if (hasOffer(offers, payment, Items.EMERALD)) {
            return;
        }

        offers.add(
                new MerchantOffer(
                        new ItemCost(payment, paymentCount),
                        new ItemStack(Items.EMERALD, emeraldCount),
                        STANDARD_MAX_USES,
                        xp,
                        REPUTATION_DISCOUNT
                )
        );
    }

    private static void addSellOffer(
            MerchantOffers offers,
            int emeraldCount,
            ItemLike result,
            int resultCount,
            int xp
    ) {
        if (hasOffer(offers, Items.EMERALD, result)) {
            return;
        }

        offers.add(
                new MerchantOffer(
                        new ItemCost(Items.EMERALD, emeraldCount),
                        new ItemStack(result, resultCount),
                        STANDARD_MAX_USES,
                        xp,
                        REPUTATION_DISCOUNT
                )
        );
    }

    private static void addCultivarOffer(
            MerchantOffers offers,
            int emeraldCount,
            GrapeCultivar cultivar,
            int xp
    ) {
        ItemStack result = ModItems.cultivarCutting(cultivar);
        result.setCount(2);
        boolean exists = offers.stream().anyMatch(offer ->
                offer.getBaseCostA().is(Items.EMERALD)
                        && ItemStack.isSameItemSameComponents(
                                offer.getResult(),
                                result
                        )
        );
        if (exists) {
            return;
        }
        offers.add(new MerchantOffer(
                new ItemCost(Items.EMERALD, emeraldCount),
                result,
                STANDARD_MAX_USES,
                xp,
                REPUTATION_DISCOUNT
        ));
    }

    private static boolean hasOffer(
            MerchantOffers offers,
            ItemLike payment,
            ItemLike result
    ) {
        return offers.stream().anyMatch(offer ->
                offer.getBaseCostA().is(payment.asItem())
                        && offer.getResult().is(result.asItem())
        );
    }
}
