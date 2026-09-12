package com.zenith.vintner.block;

import com.zenith.vintner.item.WineItem;
import com.zenith.vintner.registry.ModItems;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

/**
 * Compact block-state summary for the visible finish on stored bottles.
 * Bottle items remain authoritative; this only selects table, aged, or mixed
 * furniture models.
 */
public enum WineDisplayAge implements StringRepresentable {
    TABLE("table"),
    AGED("aged"),
    MIXED("mixed");

    private final String serializedName;

    WineDisplayAge(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static boolean isAged(ItemStack bottle) {
        return bottle.is(ModItems.AGED_RED_WINE)
                || bottle.is(ModItems.AGED_WHITE_WINE);
    }

    public static WineDisplayAge from(Iterable<ItemStack> bottles) {
        Boolean firstAged = null;

        for (ItemStack bottle : bottles) {
            if (bottle.isEmpty()
                    || !(bottle.getItem() instanceof WineItem)) {
                continue;
            }

            boolean aged = isAged(bottle);
            if (firstAged == null) {
                firstAged = aged;
            } else if (firstAged != aged) {
                return MIXED;
            }
        }

        return Boolean.TRUE.equals(firstAged) ? AGED : TABLE;
    }
}
