package com.zenith.vintner.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public enum GobletMaterial {
    PEWTER("pewter"),
    COPPER("copper"),
    GOLD("gold");

    private final String id;

    GobletMaterial(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public Component displayName() {
        return Component.translatable(
                "goblet_material.vintner." + id
        );
    }

    public static GobletMaterial from(ItemStack stack) {
        return stack.getItem() instanceof GobletItem goblet
                ? goblet.material()
                : PEWTER;
    }
}
