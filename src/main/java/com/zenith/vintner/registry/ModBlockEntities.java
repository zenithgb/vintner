package com.zenith.vintner.registry;

import com.zenith.vintner.Vintner;
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

    private ModBlockEntities() {
    }

    public static void initialize() {
    }
}
