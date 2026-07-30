package com.zenith.vintner.registry;

import com.zenith.vintner.block.VineyardSoilBlock;

import com.zenith.vintner.Vintner;
import com.zenith.vintner.block.AgingBarrelBlock;
import com.zenith.vintner.block.FermentationBarrelBlock;
import com.zenith.vintner.block.GrapePressBlock;
import com.zenith.vintner.block.RedGrapevineBlock;
import com.zenith.vintner.block.TrellisBlock;
import com.zenith.vintner.block.VintageArchiveBlock;
import com.zenith.vintner.block.WhiteGrapevineBlock;
import com.zenith.vintner.block.WineRackBlock;
import com.zenith.vintner.block.WoodVariant;
import com.zenith.vintner.vineyard.GrapeVariety;
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

    public static final Block VINEYARD_SOIL =
            registerWithItem(
                    "vineyard_soil",
                    VineyardSoilBlock::new,
                    BlockBehaviour.Properties.of()
                            .strength(0.6F)
                            .sound(SoundType.ROOTED_DIRT)
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

    public static Block vintageArchive(
            WoodVariant woodVariant
    ) {
        return VINTAGE_ARCHIVES.get(woodVariant);
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

    public static Block[] fermentationBarrelBlocks() {
        return orderedBlocks(FERMENTATION_BARRELS);
    }

    public static Block[] agingBarrelBlocks() {
        return orderedBlocks(AGING_BARRELS);
    }

    public static Block[] wineRackBlocks() {
        return orderedBlocks(WINE_RACKS);
    }

    public static Block[] vintageArchiveBlocks() {
        return orderedBlocks(VINTAGE_ARCHIVES);
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
                    TRELLISES.values().forEach(output::accept);
                    GRAPE_PRESSES.values().forEach(output::accept);
                    FERMENTATION_BARRELS.values()
                            .forEach(output::accept);
                    AGING_BARRELS.values().forEach(output::accept);
                    WINE_RACKS.values().forEach(output::accept);
                    VINTAGE_ARCHIVES.values()
                            .forEach(output::accept);
                });
    }
}
