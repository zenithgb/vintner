package com.zenith.vintner.registry;

import com.zenith.vintner.item.MustItem;

import com.zenith.vintner.item.WineConsumables;
import com.zenith.vintner.item.WineItem;
import net.minecraft.core.component.DataComponents;
import com.zenith.vintner.Vintner;
import com.zenith.vintner.item.GrapeItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.function.Function;

public final class ModItems {
    public static final Item RED_GRAPES = register(
            "red_grapes",
            properties -> new GrapeItem(
                    () -> ModBlocks.RED_GRAPEVINE,
                    properties
            )
    );

    public static final Item WHITE_GRAPES = register(
            "white_grapes",
            properties -> new GrapeItem(
                    () -> ModBlocks.WHITE_GRAPEVINE,
                    properties
            )
    );

    public static final Item RED_MUST = register(
            "red_must",
            MustItem::new
    );

    public static final Item WHITE_MUST = register(
            "white_must",
            MustItem::new
    );

    public static final Item RED_WINE = register(
            "red_wine",
            properties -> new WineItem(
                    properties
                            .stacksTo(16)
                            .component(
                                    DataComponents.CONSUMABLE,
                                    WineConsumables.RED_WINE
                            )
                            .usingConvertsTo(Items.GLASS_BOTTLE)
                            .useCooldown(12.0F)
            )
    );

    public static final Item WHITE_WINE = register(
            "white_wine",
            properties -> new WineItem(
                    properties
                            .stacksTo(16)
                            .component(
                                    DataComponents.CONSUMABLE,
                                    WineConsumables.WHITE_WINE
                            )
                            .usingConvertsTo(Items.GLASS_BOTTLE)
                            .useCooldown(12.0F)
            )
    );

    public static final Item AGED_RED_WINE = register(
            "aged_red_wine",
            properties -> new WineItem(
                    properties
                            .stacksTo(16)
                            .component(
                                    DataComponents.CONSUMABLE,
                                    WineConsumables.AGED_RED_WINE
                            )
                            .usingConvertsTo(Items.GLASS_BOTTLE)
                            .useCooldown(18.0F)
            )
    );

    public static final Item AGED_WHITE_WINE = register(
            "aged_white_wine",
            properties -> new WineItem(
                    properties
                            .stacksTo(16)
                            .component(
                                    DataComponents.CONSUMABLE,
                                    WineConsumables.AGED_WHITE_WINE
                            )
                            .usingConvertsTo(Items.GLASS_BOTTLE)
                            .useCooldown(18.0F)
            )
    );

    private ModItems() {
    }

    private static Item register(
            String name,
            Function<Item.Properties, Item> factory
    ) {
        Identifier id = Identifier.fromNamespaceAndPath(
                Vintner.MOD_ID,
                name
        );

        ResourceKey<Item> key = ResourceKey.create(
                Registries.ITEM,
                id
        );

        Item item = factory.apply(
                new Item.Properties().setId(key)
        );

        return Registry.register(
                BuiltInRegistries.ITEM,
                key,
                item
        );
    }

    public static void initialize() {
        CreativeModeTabEvents
                .modifyOutputEvent(
                        CreativeModeTabs.FOOD_AND_DRINKS
                )
                .register(output -> {
                    output.accept(RED_GRAPES);
                    output.accept(WHITE_GRAPES);
                    output.accept(RED_MUST);
                    output.accept(WHITE_MUST);
                    output.accept(RED_WINE);
                    output.accept(WHITE_WINE);
                    output.accept(AGED_RED_WINE);
                    output.accept(AGED_WHITE_WINE);
                });
    }
}
