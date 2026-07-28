package com.zenith.vintner.registry;

import com.zenith.vintner.Vintner;
import com.zenith.vintner.block.entity.AgingBarrelBlockEntity;
import com.zenith.vintner.block.entity.FermentationBarrelBlockEntity;
import com.zenith.vintner.block.entity.GrapePressBlockEntity;
import com.zenith.vintner.block.entity.WineRackBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    public static final BlockEntityType<GrapePressBlockEntity>
            GRAPE_PRESS = Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(
                            Vintner.MOD_ID,
                            "grape_press"
                    ),
                    FabricBlockEntityTypeBuilder.create(
                            GrapePressBlockEntity::new,
                            ModBlocks.grapePressBlocks()
                    ).build()
            );

    public static final BlockEntityType<
            AgingBarrelBlockEntity
            > AGING_BARREL = Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(
                            Vintner.MOD_ID,
                            "aging_barrel"
                    ),
                    FabricBlockEntityTypeBuilder.create(
                            AgingBarrelBlockEntity::new,
                            ModBlocks.agingBarrelBlocks()
                    ).build()
            );

    public static final BlockEntityType<
            FermentationBarrelBlockEntity
            > FERMENTATION_BARREL = Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(
                            Vintner.MOD_ID,
                            "fermentation_barrel"
                    ),
                    FabricBlockEntityTypeBuilder.create(
                            FermentationBarrelBlockEntity::new,
                            ModBlocks.fermentationBarrelBlocks()
                    ).build()
            );

    public static final BlockEntityType<WineRackBlockEntity>
            WINE_RACK = Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(
                            Vintner.MOD_ID,
                            "wine_rack"
                    ),
                    FabricBlockEntityTypeBuilder.create(
                            WineRackBlockEntity::new,
                            ModBlocks.wineRackBlocks()
                    ).build()
            );

    private ModBlockEntities() {
    }

    public static void initialize() {
    }
}
