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

    private ModItemTags() {
    }

    private static TagKey<Item> create(String path) {
        return TagKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(
                        Vintner.MOD_ID,
                        path
                )
        );
    }
}
