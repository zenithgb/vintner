package com.zenith.vintner.registry;

import com.zenith.vintner.block.VineyardSoilBlock;

import com.zenith.vintner.Vintner;
import com.zenith.vintner.block.AgingBarrelBlock;
import com.zenith.vintner.block.FermentationBarrelBlock;
import com.zenith.vintner.block.GrapePressBlock;
import com.zenith.vintner.block.RedGrapevineBlock;
import com.zenith.vintner.block.TrellisBlock;
import com.zenith.vintner.block.WhiteGrapevineBlock;
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

import java.util.function.Function;

public final class ModBlocks {
    public static final Block OAK_TRELLIS = registerWithItem(
            "oak_trellis",
            TrellisBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(1.5F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );

    public static final Block AGING_BARREL =
            registerWithItem(
                    "aging_barrel",
                    AgingBarrelBlock::new,
                    BlockBehaviour.Properties.of()
                            .strength(2.5F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
            );

    public static final Block FERMENTATION_BARREL =
            registerWithItem(
                    "fermentation_barrel",
                    FermentationBarrelBlock::new,
                    BlockBehaviour.Properties.of()
                            .strength(2.5F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
            );

    public static final Block GRAPE_PRESS = registerWithItem(
            "grape_press",
            GrapePressBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(2.5F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
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
            RedGrapevineBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.VINE)
                    .noOcclusion()
                    .randomTicks()
    );

    public static final Block WHITE_GRAPEVINE = registerWithoutItem(
            "white_grapevine",
            WhiteGrapevineBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.VINE)
                    .noOcclusion()
                    .randomTicks()
    );

    private ModBlocks() {
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
                    output.accept(OAK_TRELLIS);
                    output.accept(GRAPE_PRESS);
                    output.accept(FERMENTATION_BARREL);
                    output.accept(AGING_BARREL);
                });
    }
}
