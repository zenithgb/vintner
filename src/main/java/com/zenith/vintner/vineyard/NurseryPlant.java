package com.zenith.vintner.vineyard;

import com.zenith.vintner.registry.ModItems;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/** Plant material that can be multiplied in a nursery bed. */
public enum NurseryPlant implements StringRepresentable {
    RED_GRAPE,
    WHITE_GRAPE,
    ADAPTED_ROOTSTOCK,
    RESISTANT_ROOTSTOCK;

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public Item item() {
        return switch (this) {
            case RED_GRAPE -> ModItems.RED_GRAPE_CUTTING;
            case WHITE_GRAPE -> ModItems.WHITE_GRAPE_CUTTING;
            case ADAPTED_ROOTSTOCK -> ModItems.ROOTSTOCK_CUTTING;
            case RESISTANT_ROOTSTOCK ->
                    ModItems.RESISTANT_ROOTSTOCK_CUTTING;
        };
    }

    public VineRootstock rootstock() {
        return switch (this) {
            case ADAPTED_ROOTSTOCK -> VineRootstock.ADAPTED;
            case RESISTANT_ROOTSTOCK -> VineRootstock.RESISTANT;
            default -> VineRootstock.OWN_ROOTS;
        };
    }

    public boolean isRootstock() {
        return rootstock() != VineRootstock.OWN_ROOTS;
    }

    public static NurseryPlant fromItem(ItemStack stack) {
        for (NurseryPlant plant : values()) {
            if (stack.is(plant.item())) {
                return plant;
            }
        }
        return null;
    }
}
