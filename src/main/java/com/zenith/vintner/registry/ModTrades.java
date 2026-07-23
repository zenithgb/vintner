package com.zenith.vintner.registry;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public final class ModTrades {
    private static final int GRAPES_PER_TRADE = 2;
    private static final int MAX_USES = 4;
    private static final int TRADE_XP = 1;
    private static final float REPUTATION_DISCOUNT = 0.05F;

    private ModTrades() {
    }

    public static void initialize() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof WanderingTrader trader) {
                addGrapeOfferIfMissing(
                        trader.getOffers(),
                        ModItems.RED_GRAPE_CUTTING
                );
                addGrapeOfferIfMissing(
                        trader.getOffers(),
                        ModItems.WHITE_GRAPE_CUTTING
                );
            } else if (entity instanceof Villager villager) {
                addFarmerOffersIfApplicable(villager);
            }
        });

        net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT
                .register((player, level, hand, entity, hitResult) -> {
                    if (!level.isClientSide()
                            && entity instanceof Villager villager) {
                        addFarmerOffersIfApplicable(villager);
                    }

                    return net.minecraft.world.InteractionResult.PASS;
                });
    }

    private static void addFarmerOffersIfApplicable(
            Villager villager
    ) {
        if (!villager.getVillagerData()
                .profession()
                .is(VillagerProfession.FARMER)) {
            return;
        }

        addGrapeOfferIfMissing(
                villager.getOffers(),
                ModItems.RED_GRAPE_CUTTING
        );
        addGrapeOfferIfMissing(
                villager.getOffers(),
                ModItems.WHITE_GRAPE_CUTTING
        );
    }

    private static void addGrapeOfferIfMissing(
            MerchantOffers offers,
            Item grapes
    ) {
        boolean alreadyPresent = offers.stream()
                .anyMatch(offer -> offer.getResult().is(grapes));

        if (alreadyPresent) {
            return;
        }

        offers.add(
                new MerchantOffer(
                        new ItemCost(Items.EMERALD),
                        new ItemStack(grapes, GRAPES_PER_TRADE),
                        MAX_USES,
                        TRADE_XP,
                        REPUTATION_DISCOUNT
                )
        );
    }
}
