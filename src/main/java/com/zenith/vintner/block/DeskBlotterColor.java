package com.zenith.vintner.block;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public enum DeskBlotterColor implements StringRepresentable {
    WHITE("white", DyeColor.WHITE),
    ORANGE("orange", DyeColor.ORANGE),
    MAGENTA("magenta", DyeColor.MAGENTA),
    LIGHT_BLUE("light_blue", DyeColor.LIGHT_BLUE),
    YELLOW("yellow", DyeColor.YELLOW),
    LIME("lime", DyeColor.LIME),
    PINK("pink", DyeColor.PINK),
    GRAY("gray", DyeColor.GRAY),
    LIGHT_GRAY("light_gray", DyeColor.LIGHT_GRAY),
    CYAN("cyan", DyeColor.CYAN),
    PURPLE("purple", DyeColor.PURPLE),
    BLUE("blue", DyeColor.BLUE),
    BROWN("brown", DyeColor.BROWN),
    GREEN("green", DyeColor.GREEN),
    RED("red", DyeColor.RED),
    BLACK("black", DyeColor.BLACK);

    private final String name;
    private final DyeColor dyeColor;

    DeskBlotterColor(String name, DyeColor dyeColor) {
        this.name = name;
        this.dyeColor = dyeColor;
    }

    @Nullable
    public static DeskBlotterColor fromDye(ItemStack stack) {
        for (DeskBlotterColor color : values()) {
            if (stack.is(Items.DYE.pick(color.dyeColor))) {
                return color;
            }
        }
        return null;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
