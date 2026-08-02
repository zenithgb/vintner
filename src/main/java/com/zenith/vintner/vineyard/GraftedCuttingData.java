package com.zenith.vintner.vineyard;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Minimal metadata carried from a nursery graft into the planted vine. */
public final class GraftedCuttingData {
    private static final String ROOTSTOCK_KEY = "vintner_rootstock";

    private GraftedCuttingData() {
    }

    public static void apply(
            ItemStack cutting,
            VineRootstock rootstock
    ) {
        CompoundTag tag = cutting.getOrDefault(
                DataComponents.CUSTOM_DATA,
                CustomData.EMPTY
        ).copyTag();
        tag.putString(ROOTSTOCK_KEY, rootstock.serializedName());
        cutting.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static VineRootstock rootstock(ItemStack cutting) {
        CompoundTag tag = cutting.getOrDefault(
                DataComponents.CUSTOM_DATA,
                CustomData.EMPTY
        ).copyTag();
        return VineRootstock.fromName(
                tag.getString(ROOTSTOCK_KEY).orElse("")
        );
    }
}
