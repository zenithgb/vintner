package com.zenith.vintner.registry;

import com.zenith.vintner.Vintner;
import com.zenith.vintner.block.WoodVariant;
import com.zenith.vintner.item.GrapeBowlItem;
import com.zenith.vintner.vineyard.GrapeCultivar;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                            .displayItems((parameters, output) ->
                                    creativeGroups().forEach(group ->
                                            output.acceptAll(group.entries())
                                    )
                            )
                            .build()
            );

    private ModCreativeTabs() {
    }

    public static List<CreativeGroup> creativeGroups() {
        return List.of(
                new CreativeGroup(
                        "grapes_and_propagation",
                        grapesAndPropagation()
                ),
                new CreativeGroup("vineyard", vineyard()),
                new CreativeGroup(
                        "pressing_and_fermentation",
                        pressingAndFermentation()
                ),
                new CreativeGroup(
                        "ageing_and_cooperage",
                        ageingAndCooperage()
                ),
                new CreativeGroup(
                        "cellar_storage",
                        cellarStorage()
                ),
                new CreativeGroup(
                        "serving_and_table",
                        servingAndTable()
                ),
                new CreativeGroup(
                        "estate_and_surveying",
                        estateAndSurveying()
                )
        );
    }

    private static List<ItemStack> grapesAndPropagation() {
        List<ItemStack> entries = new ArrayList<>();
        for (GrapeCultivar cultivar : GrapeCultivar.activeValues()) {
            entries.add(ModItems.cultivarGrapes(cultivar));
            entries.add(ModItems.cultivarCutting(cultivar));
        }
        add(entries, ModItems.ROOTSTOCK_CUTTING);
        add(entries, ModItems.RESISTANT_ROOTSTOCK_CUTTING);
        add(entries, ModBlocks.NURSERY_BED);
        add(entries, ModItems.GRAFTING_KNIFE);
        return List.copyOf(entries);
    }

    private static List<ItemStack> vineyard() {
        List<ItemStack> entries = new ArrayList<>();
        add(entries, ModBlocks.VINEYARD_SOIL);
        add(entries, ModItems.COMPOST);
        addWoodFamily(entries, ModBlocks.TRELLISES);
        add(entries, ModItems.VINEYARD_NETTING);
        add(entries, ModItems.SOIL_PROBE);
        return List.copyOf(entries);
    }

    private static List<ItemStack> pressingAndFermentation() {
        List<ItemStack> entries = new ArrayList<>();
        addWoodFamily(entries, ModBlocks.GRAPE_PRESSES);
        add(entries, ModItems.RED_MUST);
        add(entries, ModItems.WHITE_MUST);
        addWoodFamily(entries, ModBlocks.FERMENTATION_BARRELS);
        add(entries, ModItems.RED_WINE);
        add(entries, ModItems.WHITE_WINE);
        return List.copyOf(entries);
    }

    private static List<ItemStack> ageingAndCooperage() {
        List<ItemStack> entries = new ArrayList<>();
        addWoodFamily(entries, ModBlocks.AGING_BARRELS);
        addWoodFamily(entries, ModBlocks.BARREL_STANDS);
        add(entries, ModItems.COOPERS_MALLET);
        add(entries, ModItems.TOASTING_KIT);
        add(entries, ModItems.SEASONING_KIT);
        add(entries, ModItems.CASK_CONVERSION_KIT);
        add(entries, ModItems.AGED_RED_WINE);
        add(entries, ModItems.AGED_WHITE_WINE);
        return List.copyOf(entries);
    }

    private static List<ItemStack> cellarStorage() {
        List<ItemStack> entries = new ArrayList<>();
        addWoodFamily(entries, ModBlocks.WINE_RACKS);
        addWoodFamily(entries, ModBlocks.WINE_CRATES);
        addWoodFamily(entries, ModBlocks.LABELLED_CELLAR_SHELVES);
        addWoodFamily(entries, ModBlocks.TASTING_CABINETS);
        addWoodFamily(entries, ModBlocks.VINTAGE_ARCHIVES);
        return List.copyOf(entries);
    }

    private static List<ItemStack> servingAndTable() {
        List<ItemStack> entries = new ArrayList<>();
        addWoodFamily(entries, ModBlocks.TASTING_SERVICES);
        addWoodFamily(entries, ModBlocks.WINE_BASKETS);
        for (GrapeCultivar cultivar : GrapeCultivar.activeValues()) {
            entries.add(GrapeBowlItem.create(cultivar, 4));
        }
        return List.copyOf(entries);
    }

    private static List<ItemStack> estateAndSurveying() {
        List<ItemStack> entries = new ArrayList<>();
        addWoodFamily(entries, ModBlocks.ESTATE_MANAGEMENT_DESKS);
        addWoodFamily(entries, ModBlocks.SURVEYORS_MAP_TABLES);
        add(entries, ModItems.VINTNER_ALMANAC);
        return List.copyOf(entries);
    }

    private static void addWoodFamily(
            List<ItemStack> entries,
            Map<WoodVariant, Block> family
    ) {
        for (WoodVariant wood : WoodVariant.values()) {
            add(entries, family.get(wood));
        }
    }

    private static void add(
            List<ItemStack> entries,
            ItemLike item
    ) {
        entries.add(new ItemStack(item));
    }

    public record CreativeGroup(String id, List<ItemStack> entries) {
    }

    public static void initialize() {
        // Loading the class performs the registry registration above.
    }
}
