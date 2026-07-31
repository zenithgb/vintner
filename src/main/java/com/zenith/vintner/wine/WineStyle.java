package com.zenith.vintner.wine;

import com.zenith.vintner.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public enum WineStyle {
    RED("red"),
    WHITE("white");

    private final String id;

    WineStyle(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public Component displayName() {
        return Component.translatable("wine_style.vintner." + id);
    }

    public static WineStyle from(ItemStack stack) {
        return stack.is(ModItems.WHITE_GRAPES)
                || stack.is(ModItems.WHITE_GRAPE_CUTTING)
                || stack.is(ModItems.WHITE_MUST)
                || stack.is(ModItems.WHITE_WINE)
                || stack.is(ModItems.AGED_WHITE_WINE)
                ? WHITE
                : RED;
    }

    public static WineStyle byId(String id) {
        return WHITE.id.equals(id) ? WHITE : RED;
    }
}
