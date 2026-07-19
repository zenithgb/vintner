package com.zenith.vintner.registry;

import com.zenith.vintner.Vintner;
import com.zenith.vintner.block.entity.AgingBarrelBlockEntity;
import com.zenith.vintner.block.entity.FermentationBarrelBlockEntity;
import com.zenith.vintner.block.entity.GrapePressBlockEntity;
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
                            ModBlocks.GRAPE_PRESS
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
                            ModBlocks.AGING_BARREL
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
                            ModBlocks.FERMENTATION_BARREL
                    ).build()
            );

    private ModBlockEntities() {
    }

    public static void initialize() {
    }
}
