package com.zenith.vintner.vineyard;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Minimal metadata carried from a nursery graft into the planted vine. */
public final class GraftedCuttingData {
    private static final String ROOTSTOCK_KEY = "vintner_rootstock";
    private static final String CULTIVAR_KEY = "vintner_cultivar";

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

    public static void applyCultivar(
            ItemStack cutting,
            GrapeCultivar cultivar
    ) {
        CompoundTag tag = cutting.getOrDefault(
                DataComponents.CUSTOM_DATA,
                CustomData.EMPTY
        ).copyTag();
        tag.putString(CULTIVAR_KEY, cultivar.serializedName());
        cutting.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static GrapeCultivar cultivar(
            ItemStack cutting,
            GrapeVariety fallback
    ) {
        CompoundTag tag = cutting.getOrDefault(
                DataComponents.CUSTOM_DATA,
                CustomData.EMPTY
        ).copyTag();
        return GrapeCultivar.fromName(
                tag.getString(CULTIVAR_KEY).orElse(""),
                fallback
        );
    }
}
