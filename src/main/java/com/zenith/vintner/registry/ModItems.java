package com.zenith.vintner.registry;

import com.zenith.vintner.item.CompostItem;
import com.zenith.vintner.item.CoopersMalletItem;
import com.zenith.vintner.item.GrapeCuttingItem;
import com.zenith.vintner.item.GraftingKnifeItem;
import com.zenith.vintner.item.MustItem;
import com.zenith.vintner.item.SoilProbeItem;
import com.zenith.vintner.item.VintnerAlmanacItem;
import com.zenith.vintner.item.VineyardNettingItem;

import com.zenith.vintner.item.WineConsumables;
import com.zenith.vintner.item.WineEffectProfile;
import com.zenith.vintner.item.WineItem;
import com.zenith.vintner.vineyard.GrapeCultivar;
import com.zenith.vintner.vineyard.GrapeVariety;
import com.zenith.vintner.vineyard.GraftedCuttingData;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ComposterBlock;

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

    public static final Item POMACE = register(
            "pomace",
            Item::new
    );

    public static final Item GRAPE_SEEDS = register(
            "grape_seeds",
            Item::new
    );

    public static final Item RED_GRAPE_CUTTING = register(
            "red_grape_cutting",
            properties -> new GrapeCuttingItem(
                    ModBlocks::redGrapevine,
                    GrapeVariety.RED,
                    properties
            )
    );

    public static final Item WHITE_GRAPE_CUTTING = register(
            "white_grape_cutting",
            properties -> new GrapeCuttingItem(
                    ModBlocks::whiteGrapevine,
                    GrapeVariety.WHITE,
                    properties
            )
    );

    public static final Item ROOTSTOCK_CUTTING = register(
            "rootstock_cutting",
            Item::new
    );

    public static final Item RESISTANT_ROOTSTOCK_CUTTING = register(
            "resistant_rootstock_cutting",
            Item::new
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

    public static final Item VINTNER_ALMANAC = register(
            "vintner_almanac",
            VintnerAlmanacItem::new
    );

    public static final Item SOIL_PROBE = register(
            "soil_probe",
            properties -> new SoilProbeItem(
                    properties.durability(SoilProbeItem.MAX_DAMAGE)
            )
    );

    public static final Item GRAFTING_KNIFE = register(
            "grafting_knife",
            properties -> new GraftingKnifeItem(
                    properties.durability(GraftingKnifeItem.MAX_DAMAGE)
            )
    );

    public static final Item VINEYARD_NETTING = register(
            "vineyard_netting",
            properties -> new VineyardNettingItem(
                    properties.stacksTo(16)
            )
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

        ComposterBlock.COMPOSTABLES.put(POMACE, 0.85F);
        ComposterBlock.COMPOSTABLES.put(GRAPE_SEEDS, 0.50F);

        CreativeModeTabEvents
                .modifyOutputEvent(
                        CreativeModeTabs.INGREDIENTS
                )
                .register(output -> {
                    output.accept(COMPOST);
                    output.accept(POMACE);
                    output.accept(GRAPE_SEEDS);
                    for (GrapeCultivar cultivar : GrapeCultivar.values()) {
                        output.accept(cultivarCutting(cultivar));
                    }
                    output.accept(ROOTSTOCK_CUTTING);
                    output.accept(RESISTANT_ROOTSTOCK_CUTTING);
                    output.accept(COOPERS_MALLET);
                    output.accept(TOASTING_KIT);
                    output.accept(SEASONING_KIT);
                    output.accept(CASK_CONVERSION_KIT);
                    output.accept(SOIL_PROBE);
                    output.accept(GRAFTING_KNIFE);
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
                });
    }

    public static ItemStack cultivarCutting(GrapeCultivar cultivar) {
        ItemStack cutting = new ItemStack(
                cultivar.variety() == GrapeVariety.RED
                        ? RED_GRAPE_CUTTING
                        : WHITE_GRAPE_CUTTING
        );
        GraftedCuttingData.applyCultivar(cutting, cultivar);
        return cutting;
    }
}
