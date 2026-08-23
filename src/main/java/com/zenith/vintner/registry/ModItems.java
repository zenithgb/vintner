package com.zenith.vintner.registry;

import com.zenith.vintner.item.CompostItem;
import com.zenith.vintner.item.CoopersMalletItem;
import com.zenith.vintner.item.FilledGobletItem;
import com.zenith.vintner.item.GrapeCuttingItem;
import com.zenith.vintner.item.FilledWineGlassItem;
import com.zenith.vintner.item.GobletItem;
import com.zenith.vintner.item.GobletMaterial;
import com.zenith.vintner.item.WineGlassItem;
import com.zenith.vintner.item.MustItem;
import com.zenith.vintner.item.VintnerAlmanacItem;

import com.zenith.vintner.item.WineConsumables;
import com.zenith.vintner.item.WineEffectProfile;
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
    public static final Item COMPOST = register(
            "compost",
            CompostItem::new
    );

    public static final Item RED_GRAPES = register(
            "red_grapes",
            GrapeItem::new
    );

    public static final Item WHITE_GRAPES = register(
            "white_grapes",
            GrapeItem::new
    );

    public static final Item RED_GRAPE_CUTTING = register(
            "red_grape_cutting",
            properties -> new GrapeCuttingItem(
                    ModBlocks::redGrapevine,
                    properties
            )
    );

    public static final Item WHITE_GRAPE_CUTTING = register(
            "white_grape_cutting",
            properties -> new GrapeCuttingItem(
                    ModBlocks::whiteGrapevine,
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
                    WineEffectProfile.RED,
                    properties
                            .stacksTo(16)
                            .component(
                                    DataComponents.CONSUMABLE,
                                    WineConsumables.WINE
                            )
                            .usingConvertsTo(Items.GLASS_BOTTLE)
                            .useCooldown(12.0F)
            )
    );

    public static final Item WHITE_WINE = register(
            "white_wine",
            properties -> new WineItem(
                    WineEffectProfile.WHITE,
                    properties
                            .stacksTo(16)
                            .component(
                                    DataComponents.CONSUMABLE,
                                    WineConsumables.WINE
                            )
                            .usingConvertsTo(Items.GLASS_BOTTLE)
                            .useCooldown(12.0F)
            )
    );

    public static final Item AGED_RED_WINE = register(
            "aged_red_wine",
            properties -> new WineItem(
                    WineEffectProfile.AGED_RED,
                    properties
                            .stacksTo(16)
                            .component(
                                    DataComponents.CONSUMABLE,
                                    WineConsumables.WINE
                            )
                            .usingConvertsTo(Items.GLASS_BOTTLE)
                            .useCooldown(18.0F)
            )
    );

    public static final Item AGED_WHITE_WINE = register(
            "aged_white_wine",
            properties -> new WineItem(
                    WineEffectProfile.AGED_WHITE,
                    properties
                            .stacksTo(16)
                            .component(
                                    DataComponents.CONSUMABLE,
                                    WineConsumables.WINE
                            )
                            .usingConvertsTo(Items.GLASS_BOTTLE)
                            .useCooldown(18.0F)
            )
    );

    public static final Item WINE_GLASS = register(
            "wine_glass",
            WineGlassItem::new
    );

    public static final Item FILLED_WINE_GLASS = register(
            "filled_wine_glass",
            properties -> new FilledWineGlassItem(
                    properties
                            .stacksTo(1)
                            .component(
                                    DataComponents.CONSUMABLE,
                                    WineConsumables.WINE
                            )
                            .usingConvertsTo(WINE_GLASS)
            )
    );

    public static final Item PEWTER_GOBLET = register(
            "pewter_goblet",
            properties -> new GobletItem(
                    GobletMaterial.PEWTER,
                    properties.stacksTo(16)
            )
    );

    public static final Item COPPER_GOBLET = register(
            "copper_goblet",
            properties -> new GobletItem(
                    GobletMaterial.COPPER,
                    properties.stacksTo(16)
            )
    );

    public static final Item GOLDEN_GOBLET = register(
            "golden_goblet",
            properties -> new GobletItem(
                    GobletMaterial.GOLD,
                    properties.stacksTo(16)
            )
    );

    public static final Item FILLED_PEWTER_GOBLET = register(
            "filled_pewter_goblet",
            properties -> new FilledGobletItem(
                    GobletMaterial.PEWTER,
                    properties
                            .stacksTo(1)
                            .component(
                                    DataComponents.CONSUMABLE,
                                    WineConsumables.WINE
                            )
                            .usingConvertsTo(PEWTER_GOBLET)
            )
    );

    public static final Item FILLED_COPPER_GOBLET = register(
            "filled_copper_goblet",
            properties -> new FilledGobletItem(
                    GobletMaterial.COPPER,
                    properties
                            .stacksTo(1)
                            .component(
                                    DataComponents.CONSUMABLE,
                                    WineConsumables.WINE
                            )
                            .usingConvertsTo(COPPER_GOBLET)
            )
    );

    public static final Item FILLED_GOLDEN_GOBLET = register(
            "filled_golden_goblet",
            properties -> new FilledGobletItem(
                    GobletMaterial.GOLD,
                    properties
                            .stacksTo(1)
                            .component(
                                    DataComponents.CONSUMABLE,
                                    WineConsumables.WINE
                            )
                            .usingConvertsTo(GOLDEN_GOBLET)
            )
    );

    public static final Item VINTNER_ALMANAC = register(
            "vintner_almanac",
            VintnerAlmanacItem::new
    );

    public static final Item COOPERS_MALLET = register(
            "coopers_mallet",
            properties -> new CoopersMalletItem(
                    properties.durability(
                            CoopersMalletItem.MAX_DAMAGE
                    )
            )
    );

    public static final Item TOASTING_KIT = register(
            "toasting_kit",
            Item::new
    );

    public static final Item SEASONING_KIT = register(
            "seasoning_kit",
            Item::new
    );

    public static final Item CASK_CONVERSION_KIT = register(
            "cask_conversion_kit",
            Item::new
    );

    private ModItems() {
    }

    public static Item filledGoblet(GobletMaterial material) {
        return switch (material) {
            case COPPER -> FILLED_COPPER_GOBLET;
            case GOLD -> FILLED_GOLDEN_GOBLET;
            default -> FILLED_PEWTER_GOBLET;
        };
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
        CoopersMalletItem.initialize();

        CreativeModeTabEvents
                .modifyOutputEvent(
                        CreativeModeTabs.INGREDIENTS
                )
                .register(output -> {
                    output.accept(COMPOST);
                    output.accept(RED_GRAPE_CUTTING);
                    output.accept(WHITE_GRAPE_CUTTING);
                    output.accept(COOPERS_MALLET);
                    output.accept(TOASTING_KIT);
                    output.accept(SEASONING_KIT);
                    output.accept(CASK_CONVERSION_KIT);
                    output.accept(VINTNER_ALMANAC);
                });

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
                    output.accept(WINE_GLASS);
                    output.accept(FILLED_WINE_GLASS);
                    output.accept(PEWTER_GOBLET);
                    output.accept(COPPER_GOBLET);
                    output.accept(GOLDEN_GOBLET);
                    output.accept(FILLED_PEWTER_GOBLET);
                    output.accept(FILLED_COPPER_GOBLET);
                    output.accept(FILLED_GOLDEN_GOBLET);
                });
    }
}
