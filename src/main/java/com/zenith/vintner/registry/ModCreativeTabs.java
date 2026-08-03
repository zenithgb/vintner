package com.zenith.vintner.registry;

import com.zenith.vintner.Vintner;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import com.zenith.vintner.vineyard.GrapeCultivar;

public final class ModCreativeTabs {
    private static final ResourceKey<CreativeModeTab> VINTNER_TAB_KEY =
            ResourceKey.create(
                    Registries.CREATIVE_MODE_TAB,
                    Identifier.fromNamespaceAndPath(
                            Vintner.MOD_ID,
                            "main"
                    )
            );

    public static final CreativeModeTab VINTNER_TAB =
            Registry.register(
                    BuiltInRegistries.CREATIVE_MODE_TAB,
                    VINTNER_TAB_KEY,
                    FabricCreativeModeTab.builder()
                            .title(Component.translatable(
                                    "itemGroup.vintner.main"
                            ))
                            .icon(() -> new ItemStack(
                                    ModItems.RED_GRAPES
                            ))
                            .displayItems((parameters, output) -> {
                                output.accept(ModItems.COMPOST);
                                output.accept(ModBlocks.VINEYARD_SOIL);
                                ModBlocks.TRELLISES.values()
                                        .forEach(output::accept);
                                for (GrapeCultivar cultivar
                                        : GrapeCultivar.values()) {
                                    output.accept(
                                            ModItems.cultivarCutting(cultivar)
                                    );
                                }
                                output.accept(ModItems.ROOTSTOCK_CUTTING);
                                output.accept(
                                        ModItems.RESISTANT_ROOTSTOCK_CUTTING
                                );
                                output.accept(ModItems.GRAFTING_KNIFE);
                                output.accept(ModItems.VINEYARD_NETTING);
                                output.accept(ModBlocks.NURSERY_BED);
                                output.accept(ModItems.RED_GRAPES);
                                output.accept(ModItems.WHITE_GRAPES);
                                output.accept(ModItems.POMACE);
                                output.accept(ModItems.GRAPE_SEEDS);
                                output.accept(ModItems.GRAPE_SEED_OIL);
                                output.accept(ModItems.RAISINS);
                                output.accept(ModItems.VINEYARD_BREAD);
                                output.accept(ModItems.GRAPE_TART);
                                output.accept(ModItems.RED_WINE_STEW);
                                output.accept(ModItems.WHITE_WINE_FISH);
                                output.accept(ModItems.POACHED_FRUIT);
                                output.accept(ModItems.BRAISED_MEAT);
                                output.accept(ModItems.VINEYARD_SOUP);
                                ModBlocks.GRAPE_PRESSES.values()
                                        .forEach(output::accept);
                                output.accept(ModItems.RED_MUST);
                                output.accept(ModItems.WHITE_MUST);
                                ModBlocks.FERMENTATION_BARRELS.values()
                                        .forEach(output::accept);
                                output.accept(ModItems.RED_WINE);
                                output.accept(ModItems.WHITE_WINE);
                                ModBlocks.AGING_BARRELS.values()
                                        .forEach(output::accept);
                                output.accept(ModItems.COOPERS_MALLET);
                                output.accept(ModItems.TOASTING_KIT);
                                output.accept(ModItems.SEASONING_KIT);
                                output.accept(
                                        ModItems.CASK_CONVERSION_KIT
                                );
                                output.accept(ModItems.AGED_RED_WINE);
                                output.accept(ModItems.AGED_WHITE_WINE);
                                ModBlocks.WINE_RACKS.values()
                                        .forEach(output::accept);
                                ModBlocks.WINE_CRATES.values()
                                        .forEach(output::accept);
                                ModBlocks.VINTAGE_ARCHIVES.values()
                                        .forEach(output::accept);
                                ModBlocks.BARREL_STANDS.values()
                                        .forEach(output::accept);
                                ModBlocks.LABELLED_CELLAR_SHELVES.values()
                                        .forEach(output::accept);
                                ModBlocks.TASTING_CABINETS.values()
                                        .forEach(output::accept);
                                ModBlocks.ESTATE_MANAGEMENT_DESKS.values()
                                        .forEach(output::accept);
                                ModBlocks.SURVEYORS_MAP_TABLES.values()
                                        .forEach(output::accept);
                                ModBlocks.TRADE_CORRESPONDENCE_BOARDS.values()
                                        .forEach(output::accept);
                                output.accept(
                                        ModBlocks.VILLAGE_CONTRACT_BOARD
                                );
                                output.accept(ModItems.SOIL_PROBE);
                                output.accept(
                                        ModItems.VINTNER_ALMANAC
                                );
                            })
                            .build()
            );

    private ModCreativeTabs() {
    }

    public static void initialize() {
        // Loading the class performs the registry registration above.
    }
}
