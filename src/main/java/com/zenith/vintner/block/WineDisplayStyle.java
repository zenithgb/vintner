package com.zenith.vintner.block;

import com.zenith.vintner.item.WineItem;
import com.zenith.vintner.wine.WineMetadata;
import com.zenith.vintner.wine.WineStyle;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

/**
 * Compact block-state summary used by storage furniture models. The bottles
 * remain authoritative in their block entity; this only selects a matching
 * red, white, or visibly mixed display.
 */
public enum WineDisplayStyle implements StringRepresentable {
    RED("red"),
    WHITE("white"),
    MIXED("mixed");

    private final String serializedName;

    WineDisplayStyle(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static WineDisplayStyle from(Iterable<ItemStack> bottles) {
        WineStyle first = null;

        for (ItemStack bottle : bottles) {
            if (bottle.isEmpty()
                    || !(bottle.getItem() instanceof WineItem)) {
                continue;
            }

            WineStyle style = WineMetadata.wineStyle(bottle);
            if (first == null) {
                first = style;
            } else if (first != style) {
                return MIXED;
            }
        }

        return first == WineStyle.WHITE ? WHITE : RED;
    }
}
