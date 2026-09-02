package com.zenith.vintner.registry;

import com.zenith.vintner.block.VineyardSoilBlock;

import com.zenith.vintner.Vintner;
import com.zenith.vintner.block.AgingBarrelBlock;
import com.zenith.vintner.block.BarrelStandBlock;
import com.zenith.vintner.block.CellarCollectionBlock;
import com.zenith.vintner.block.CellarFixtureKind;
import com.zenith.vintner.block.EstateManagementDeskBlock;
import com.zenith.vintner.block.FermentationBarrelBlock;
import com.zenith.vintner.block.GrapePressBlock;
import com.zenith.vintner.block.NurseryBedBlock;
import com.zenith.vintner.block.RedGrapevineBlock;
import com.zenith.vintner.block.SurveyorsMapTableBlock;
import com.zenith.vintner.block.TastingServiceBlock;
import com.zenith.vintner.block.TrellisBlock;
import com.zenith.vintner.block.VintageArchiveBlock;
import com.zenith.vintner.block.WhiteGrapevineBlock;
import com.zenith.vintner.block.WineCrateBlock;
import com.zenith.vintner.block.WineBottleBlock;
import com.zenith.vintner.block.WineRackBlock;
import com.zenith.vintner.block.WoodVariant;
import com.zenith.vintner.vineyard.GrapeVariety;
import com.zenith.vintner.wine.AgingVessel;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public final class ModBlocks {
    public static final Block OAK_TRELLIS = registerWithItem(
            "oak_trellis",
            properties -> new TrellisBlock(
                    WoodVariant.OAK,
                    properties
            ),
            trellisProperties()
    );

    public static final Map<WoodVariant, Block> TRELLISES =
            registerTrellises();

    public static final Block AGING_BARREL =
            registerWithItem(
                    "aging_barrel",
                    AgingBarrelBlock::new,
                    machineProperties()
            );

    public static final Map<WoodVariant, Block> AGING_BARRELS =
            registerMachineVariants(
                    WoodVariant.DARK_OAK,
                    AGING_BARREL,
                    WoodVariant::agingBarrelId,
                    AgingBarrelBlock::new
            );

    public static final Block CHESTNUT_AGING_BARREL =
            registerWithItem(
                    "chestnut_aging_barrel",
                    properties -> new AgingBarrelBlock(
                            AgingVessel.CHESTNUT,
                            properties
                    ),
                    machineProperties()
            );

    public static final Block NEUTRAL_AGING_BARREL =
            registerWithItem(
                    "neutral_aging_barrel",
                    properties -> new AgingBarrelBlock(
                            AgingVessel.NEUTRAL,
                            properties
                    ),
                    machineProperties()
            );

    public static final Block LARGE_CASK = registerWithItem(
            "large_cask",
            properties -> new AgingBarrelBlock(
                    AgingVessel.LARGE_CASK,
                    properties
            ),
            machineProperties()
    );

    public static final Block FERMENTATION_BARREL =
            registerWithItem(
                    "fermentation_barrel",
                    FermentationBarrelBlock::new,
                    machineProperties()
            );

    public static final Map<WoodVariant, Block>
            FERMENTATION_BARRELS = registerMachineVariants(
                    WoodVariant.OAK,
                    FERMENTATION_BARREL,
                    WoodVariant::fermentationBarrelId,
                    FermentationBarrelBlock::new
            );

    public static final Block GRAPE_PRESS = registerWithItem(
            "grape_press",
            GrapePressBlock::new,
            machineProperties()
    );

    public static final Map<WoodVariant, Block> GRAPE_PRESSES =
            registerMachineVariants(
                    WoodVariant.OAK,
                    GRAPE_PRESS,
                    WoodVariant::grapePressId,
                    GrapePressBlock::new
            );

    public static final Block WINE_RACK = registerWithItem(
            "wine_rack",
            WineRackBlock::new,
            machineProperties()
    );

    public static final Map<WoodVariant, Block> WINE_RACKS =
            registerMachineVariants(
                    WoodVariant.OAK,
                    WINE_RACK,
                    WoodVariant::wineRackId,
                    WineRackBlock::new
            );

    public static final Block WINE_CRATE = registerWithItem(
            "wine_crate",
            WineCrateBlock::new,
            machineProperties()
    );

    /**
     * A placed wine bottle has no standalone BlockItem. It is created by
     * using a WineItem on a block and returns that exact WineItem on removal.
     */
    public static final Block WINE_BOTTLE = registerWithoutItem(
            "wine_bottle",
            WineBottleBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(0.3F)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
    );

    public static final Block TASTING_SERVICE = registerWithItem(
            "tasting_service",
            TastingServiceBlock::new,
            tastingServiceProperties()
    );

    public static final Map<WoodVariant, Block> TASTING_SERVICES =
            registerTastingServices();

    public static final Map<WoodVariant, Block> WINE_CRATES =
            registerMachineVariants(
                    WoodVariant.OAK,
                    WINE_CRATE,
                    WoodVariant::wineCrateId,
                    WineCrateBlock::new
            );

    public static final Block VINTAGE_ARCHIVE = registerWithItem(
            "vintage_archive",
            VintageArchiveBlock::new,
            machineProperties()
    );

    public static final Map<WoodVariant, Block>
            VINTAGE_ARCHIVES = registerMachineVariants(
                    WoodVariant.OAK,
                    VINTAGE_ARCHIVE,
                    WoodVariant::vintageArchiveId,
                    VintageArchiveBlock::new
            );

    public static final Block BARREL_STAND = registerWithItem(
            "barrel_stand",
            BarrelStandBlock::new,
            machineProperties()
    );

    public static final Map<WoodVariant, Block> BARREL_STANDS =
            registerMachineVariants(
                    WoodVariant.OAK,
                    BARREL_STAND,
                    WoodVariant::barrelStandId,
                    BarrelStandBlock::new
            );

    public static final Block LABELLED_CELLAR_SHELF =
            registerWithItem(
                    "labelled_cellar_shelf",
                    properties -> new CellarCollectionBlock(
                            CellarFixtureKind.LABELLED_SHELF,
                            properties
                    ),
                    machineProperties()
            );

    public static final Map<WoodVariant, Block>
            LABELLED_CELLAR_SHELVES = registerMachineVariants(
                    WoodVariant.OAK,
                    LABELLED_CELLAR_SHELF,
                    WoodVariant::labelledCellarShelfId,
                    properties -> new CellarCollectionBlock(
                            CellarFixtureKind.LABELLED_SHELF,
                            properties
                    )
            );

    public static final Block TASTING_CABINET = registerWithItem(
            "tasting_cabinet",
            properties -> new CellarCollectionBlock(
                    CellarFixtureKind.TASTING_CABINET,
                    properties
            ),
            machineProperties()
    );

    public static final Map<WoodVariant, Block> TASTING_CABINETS =
            registerMachineVariants(
                    WoodVariant.OAK,
                    TASTING_CABINET,
                    WoodVariant::tastingCabinetId,
                    properties -> new CellarCollectionBlock(
                            CellarFixtureKind.TASTING_CABINET,
                            properties
                    )
            );

    public static final Block VINEYARD_SOIL =
            registerWithItem(
                    "vineyard_soil",
                    VineyardSoilBlock::new,
                    BlockBehaviour.Properties.of()
                            .strength(0.6F)
                            .sound(SoundType.ROOTED_DIRT)
            );

    public static final Block NURSERY_BED = registerWithItem(
            "nursery_bed",
            NurseryBedBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .randomTicks()
    );

    public static final Block ESTATE_MANAGEMENT_DESK = registerWithItem(
            "estate_management_desk",
            EstateManagementDeskBlock::new,
            machineProperties()
    );

    public static final Map<WoodVariant, Block>
            ESTATE_MANAGEMENT_DESKS = registerMachineVariants(
                    WoodVariant.OAK,
                    ESTATE_MANAGEMENT_DESK,
                    WoodVariant::estateManagementDeskId,
                    EstateManagementDeskBlock::new
            );

    public static final Block SURVEYORS_MAP_TABLE = registerWithItem(
            "surveyors_map_table",
            SurveyorsMapTableBlock::new,
            machineProperties()
    );

    public static final Map<WoodVariant, Block>
            SURVEYORS_MAP_TABLES = registerMachineVariants(
                    WoodVariant.OAK,
                    SURVEYORS_MAP_TABLE,
                    WoodVariant::surveyorsMapTableId,
                    SurveyorsMapTableBlock::new
            );

    public static final Block RED_GRAPEVINE = registerWithoutItem(
            "red_grapevine",
            properties -> new RedGrapevineBlock(
                    WoodVariant.OAK,
                    properties
            ),
            grapevineProperties()
    );

    public static final Map<WoodVariant, Block> RED_GRAPEVINES =
            registerGrapevines(true, RED_GRAPEVINE);

    public static final Block WHITE_GRAPEVINE = registerWithoutItem(
            "white_grapevine",
            properties -> new WhiteGrapevineBlock(
                    WoodVariant.OAK,
                    properties
            ),
            grapevineProperties()
    );

    public static final Map<WoodVariant, Block> WHITE_GRAPEVINES =
            registerGrapevines(false, WHITE_GRAPEVINE);

    private ModBlocks() {
    }

    public static Block trellis(WoodVariant woodVariant) {
        return TRELLISES.get(woodVariant);
    }

    public static Block grapePress(WoodVariant woodVariant) {
        return GRAPE_PRESSES.get(woodVariant);
    }

    public static Block fermentationBarrel(
            WoodVariant woodVariant
    ) {
        return FERMENTATION_BARRELS.get(woodVariant);
    }

    public static Block agingBarrel(WoodVariant woodVariant) {
        return AGING_BARRELS.get(woodVariant);
    }

    public static Block wineRack(WoodVariant woodVariant) {
        return WINE_RACKS.get(woodVariant);
    }

    public static Block wineCrate(WoodVariant woodVariant) {
        return WINE_CRATES.get(woodVariant);
    }

    public static Block vintageArchive(
            WoodVariant woodVariant
    ) {
        return VINTAGE_ARCHIVES.get(woodVariant);
    }

    public static Block barrelStand(WoodVariant woodVariant) {
        return BARREL_STANDS.get(woodVariant);
    }

    public static Block labelledCellarShelf(WoodVariant woodVariant) {
        return LABELLED_CELLAR_SHELVES.get(woodVariant);
    }

    public static Block tastingCabinet(WoodVariant woodVariant) {
        return TASTING_CABINETS.get(woodVariant);
    }

    public static Block estateManagementDesk(
            WoodVariant woodVariant
    ) {
        return ESTATE_MANAGEMENT_DESKS.get(woodVariant);
    }

    public static Block surveyorsMapTable(WoodVariant woodVariant) {
        return SURVEYORS_MAP_TABLES.get(woodVariant);
    }

    public static Block tastingService(WoodVariant woodVariant) {
        return TASTING_SERVICES.get(woodVariant);
    }

    public static Block redGrapevine(WoodVariant woodVariant) {
        return RED_GRAPEVINES.get(woodVariant);
    }

    public static Block whiteGrapevine(WoodVariant woodVariant) {
        return WHITE_GRAPEVINES.get(woodVariant);
    }

    public static Block grapevine(
            GrapeVariety variety,
            WoodVariant woodVariant
    ) {
        return variety == GrapeVariety.RED
                ? redGrapevine(woodVariant)
                : whiteGrapevine(woodVariant);
    }

    public static Block[] grapePressBlocks() {
        return orderedBlocks(GRAPE_PRESSES);
    }

    public static Block[] barrelStandBlocks() {
        return orderedBlocks(BARREL_STANDS);
    }

    public static Block[] fermentationBarrelBlocks() {
        return orderedBlocks(FERMENTATION_BARRELS);
    }

    public static Block[] agingBarrelBlocks() {
        Block[] cosmetic = orderedBlocks(AGING_BARRELS);
        Block[] result = new Block[cosmetic.length + 3];
        System.arraycopy(
                cosmetic,
                0,
                result,
                0,
                cosmetic.length
        );
        result[cosmetic.length] = CHESTNUT_AGING_BARREL;
        result[cosmetic.length + 1] = NEUTRAL_AGING_BARREL;
        result[cosmetic.length + 2] = LARGE_CASK;
        return result;
    }

    public static Block[] wineRackBlocks() {
        return orderedBlocks(WINE_RACKS);
    }

    public static Block[] wineCrateBlocks() {
        return orderedBlocks(WINE_CRATES);
    }

    public static Block[] vintageArchiveBlocks() {
        return orderedBlocks(VINTAGE_ARCHIVES);
    }

    public static Block[] tastingServiceBlocks() {
        return orderedBlocks(TASTING_SERVICES);
    }

    public static Block[] cellarCollectionBlocks() {
        Block[] shelves = orderedBlocks(LABELLED_CELLAR_SHELVES);
        Block[] cabinets = orderedBlocks(TASTING_CABINETS);
        Block[] result = new Block[shelves.length + cabinets.length];
        System.arraycopy(shelves, 0, result, 0, shelves.length);
        System.arraycopy(
                cabinets,
                0,
                result,
                shelves.length,
                cabinets.length
        );
        return result;
    }

    public static Block[] estateManagementDeskBlocks() {
        return orderedBlocks(ESTATE_MANAGEMENT_DESKS);
    }

    public static Block[] surveyorsMapTableBlocks() {
        return orderedBlocks(SURVEYORS_MAP_TABLES);
    }

    private static Map<WoodVariant, Block> registerTrellises() {
        EnumMap<WoodVariant, Block> blocks =
                new EnumMap<>(WoodVariant.class);
        blocks.put(WoodVariant.OAK, OAK_TRELLIS);

        for (WoodVariant woodVariant : WoodVariant.values()) {
            if (woodVariant == WoodVariant.OAK) {
                continue;
            }

            blocks.put(
                    woodVariant,
                    registerWithItem(
                            woodVariant.trellisId(),
                            properties -> new TrellisBlock(
                                    woodVariant,
                                    properties
                            ),
                            trellisProperties()
                    )
            );
        }

        return Collections.unmodifiableMap(blocks);
    }

    private static Map<WoodVariant, Block> registerTastingServices() {
        EnumMap<WoodVariant, Block> blocks =
                new EnumMap<>(WoodVariant.class);
        blocks.put(WoodVariant.OAK, TASTING_SERVICE);

        for (WoodVariant woodVariant : WoodVariant.values()) {
            if (woodVariant == WoodVariant.OAK) {
                continue;
            }

            blocks.put(
                    woodVariant,
                    registerWithItem(
                            woodVariant.tastingServiceId(),
                            TastingServiceBlock::new,
                            tastingServiceProperties()
                    )
            );
        }

        return Collections.unmodifiableMap(blocks);
    }

    private static Map<WoodVariant, Block> registerMachineVariants(
            WoodVariant existingVariant,
            Block existingBlock,
            Function<WoodVariant, String> idFactory,
            Function<BlockBehaviour.Properties, Block> factory
    ) {
        EnumMap<WoodVariant, Block> blocks =
                new EnumMap<>(WoodVariant.class);
        blocks.put(existingVariant, existingBlock);

        for (WoodVariant woodVariant : WoodVariant.values()) {
            if (woodVariant == existingVariant) {
                continue;
            }

            blocks.put(
                    woodVariant,
                    registerWithItem(
                            idFactory.apply(woodVariant),
                            factory,
                            machineProperties()
                    )
            );
        }

        return Collections.unmodifiableMap(blocks);
    }

    private static Map<WoodVariant, Block> registerGrapevines(
            boolean red,
            Block existingBlock
    ) {
        EnumMap<WoodVariant, Block> blocks =
                new EnumMap<>(WoodVariant.class);
        blocks.put(WoodVariant.OAK, existingBlock);

        for (WoodVariant woodVariant : WoodVariant.values()) {
            if (woodVariant == WoodVariant.OAK) {
                continue;
            }

            blocks.put(
                    woodVariant,
                    registerWithoutItem(
                            woodVariant.grapevineId(red),
                            properties -> red
                                    ? new RedGrapevineBlock(
                                            woodVariant,
                                            properties
                                    )
                                    : new WhiteGrapevineBlock(
                                            woodVariant,
                                            properties
                                    ),
                            grapevineProperties()
                    )
            );
        }

        return Collections.unmodifiableMap(blocks);
    }

    private static Block[] orderedBlocks(
            Map<WoodVariant, Block> blocks
    ) {
        WoodVariant[] variants = WoodVariant.values();
        Block[] result = new Block[variants.length];

        for (int index = 0; index < variants.length; index++) {
            result[index] = blocks.get(variants[index]);
        }

        return result;
    }

    private static BlockBehaviour.Properties trellisProperties() {
        return BlockBehaviour.Properties.of()
                .strength(1.5F)
                .sound(SoundType.WOOD)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.of()
                .strength(2.5F)
                .sound(SoundType.WOOD)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties tastingServiceProperties() {
        return BlockBehaviour.Properties.of()
                .strength(0.8F)
                .sound(SoundType.WOOD)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties grapevineProperties() {
        return BlockBehaviour.Properties.of()
                .strength(1.0F)
                .sound(SoundType.VINE)
                .noOcclusion()
                .randomTicks();
    }

    private static Block registerWithItem(
            String name,
            Function<BlockBehaviour.Properties, Block> factory,
            BlockBehaviour.Properties properties
    ) {
        Block block = registerWithoutItem(name, factory, properties);
        registerBlockItem(name, block);
        return block;
    }

    private static Block registerWithoutItem(
            String name,
            Function<BlockBehaviour.Properties, Block> factory,
            BlockBehaviour.Properties properties
    ) {
        Identifier id = Identifier.fromNamespaceAndPath(
                Vintner.MOD_ID,
                name
        );

        ResourceKey<Block> key = ResourceKey.create(
                Registries.BLOCK,
                id
        );

        Block block = factory.apply(properties.setId(key));

        return Registry.register(
                BuiltInRegistries.BLOCK,
                key,
                block
        );
    }

    private static void registerBlockItem(
            String name,
            Block block
    ) {
        Identifier id = Identifier.fromNamespaceAndPath(
                Vintner.MOD_ID,
                name
        );

        ResourceKey<Item> key = ResourceKey.create(
                Registries.ITEM,
                id
        );

        Item item = new BlockItem(
                block,
                new Item.Properties()
                        .setId(key)
                        .useBlockDescriptionPrefix()
        );

        Registry.register(
                BuiltInRegistries.ITEM,
                key,
                item
        );
    }

    public static void initialize() {
        CreativeModeTabEvents
                .modifyOutputEvent(
                        CreativeModeTabs.NATURAL_BLOCKS
                )
                .register(output -> output.accept(VINEYARD_SOIL));

        CreativeModeTabEvents
                .modifyOutputEvent(
                        CreativeModeTabs.FUNCTIONAL_BLOCKS
                )
                .register(output -> {
                    output.accept(NURSERY_BED);
                    ESTATE_MANAGEMENT_DESKS.values()
                            .forEach(output::accept);
                    SURVEYORS_MAP_TABLES.values()
                            .forEach(output::accept);
                    TRELLISES.values().forEach(output::accept);
                    GRAPE_PRESSES.values().forEach(output::accept);
                    FERMENTATION_BARRELS.values()
                            .forEach(output::accept);
                    AGING_BARRELS.values().forEach(output::accept);
                    WINE_RACKS.values().forEach(output::accept);
                    WINE_CRATES.values().forEach(output::accept);
                    VINTAGE_ARCHIVES.values()
                            .forEach(output::accept);
                    BARREL_STANDS.values().forEach(output::accept);
                    LABELLED_CELLAR_SHELVES.values()
                            .forEach(output::accept);
                    TASTING_CABINETS.values()
                            .forEach(output::accept);
                    TASTING_SERVICES.values().forEach(output::accept);
                });
    }
}
