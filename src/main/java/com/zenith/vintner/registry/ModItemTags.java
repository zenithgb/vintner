package com.zenith.vintner.registry;

import com.zenith.vintner.Vintner;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModItemTags {
    public static final TagKey<Item> WINES = create("wines");
    public static final TagKey<Item> RED_WINES = create("red_wines");
    public static final TagKey<Item> WHITE_WINES = create("white_wines");
    public static final TagKey<Item> PAIRS_WITH_RED_WINE = create(
            "pairs_with_red_wine"
    );
    public static final TagKey<Item> PAIRS_WITH_WHITE_WINE = create(
            "pairs_with_white_wine"
    );
    public static final TagKey<Item> COMMON_FOODS_FRUIT = createCommon(
            "foods/fruit"
    );
    public static final TagKey<Item> COMMON_FOODS_BREAD = createCommon(
            "foods/bread"
    );
    public static final TagKey<Item> COMMON_FOODS_PIE = createCommon(
            "foods/pie"
    );
    public static final TagKey<Item> COMMON_FOODS_SOUP = createCommon(
            "foods/soup"
    );
    public static final TagKey<Item> COMMON_FOODS_COOKED_MEAT = createCommon(
            "foods/cooked_meat"
    );
    public static final TagKey<Item> COMMON_FOODS_COOKED_FISH = createCommon(
            "foods/cooked_fish"
    );

    private ModItemTags() {
    }

    private static TagKey<Item> create(String path) {
        return create(Vintner.MOD_ID, path);
    }

    private static TagKey<Item> createCommon(String path) {
        return create("c", path);
    }

    private static TagKey<Item> create(String namespace, String path) {
        return TagKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(
                        namespace,
                        path
                )
        );
    }
}
