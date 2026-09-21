package com.zenith.vintner.wine;

import com.zenith.vintner.registry.ModItems;
import net.minecraft.world.item.ItemStack;

/**
 * The four visible sealed-bottle finishes used by wine displays.
 *
 * <p>The stored stack remains authoritative: colour comes from its persisted
 * wine-style metadata, while the aged finish follows the actual aged bottle
 * item. Renderers use this value only to select an equivalent 3D bottle model;
 * they never replace or rewrite the stored bottle.</p>
 */
public enum WineBottleAppearance {
    RED("wine_basket_bottle_red"),
    WHITE("wine_basket_bottle_white"),
    AGED_RED("wine_basket_bottle_aged_red"),
    AGED_WHITE("wine_basket_bottle_aged_white");

    private final String itemModelId;

    WineBottleAppearance(String itemModelId) {
        this.itemModelId = itemModelId;
    }

    public String itemModelId() {
        return itemModelId;
    }

    public static WineBottleAppearance from(ItemStack bottle) {
        boolean white = WineMetadata.wineStyle(bottle) == WineStyle.WHITE;
        boolean aged = bottle.is(ModItems.AGED_RED_WINE)
                || bottle.is(ModItems.AGED_WHITE_WINE);

        if (aged) {
            return white ? AGED_WHITE : AGED_RED;
        }
        return white ? WHITE : RED;
    }
}
