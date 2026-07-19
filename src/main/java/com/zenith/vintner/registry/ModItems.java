package com.zenith.vintner.registry;

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

public final class ModItems {
    public static final Item GRAPES = register(
            "grapes",
            properties -> new GrapeItem(properties)
    );

    private ModItems() {
    }

    private static Item register(
            String name,
            java.util.function.Function<Item.Properties, Item> factory
    ) {
        Identifier id = Identifier.fromNamespaceAndPath(Vintner.MOD_ID, name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);

        Item item = factory.apply(new Item.Properties().setId(key));

        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    public static void initialize() {
        CreativeModeTabEvents
                .modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS)
                .register(output -> output.accept(GRAPES));
    }
}
