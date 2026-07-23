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
                                output.accept(ModBlocks.OAK_TRELLIS);
                                output.accept(ModItems.RED_GRAPE_CUTTING);
                                output.accept(ModItems.WHITE_GRAPE_CUTTING);
                                output.accept(ModItems.RED_GRAPES);
                                output.accept(ModItems.WHITE_GRAPES);
                                output.accept(ModBlocks.GRAPE_PRESS);
                                output.accept(ModItems.RED_MUST);
                                output.accept(ModItems.WHITE_MUST);
                                output.accept(
                                        ModBlocks.FERMENTATION_BARREL
                                );
                                output.accept(ModItems.RED_WINE);
                                output.accept(ModItems.WHITE_WINE);
                                output.accept(ModBlocks.AGING_BARREL);
                                output.accept(ModItems.AGED_RED_WINE);
                                output.accept(ModItems.AGED_WHITE_WINE);
                            })
                            .build()
            );

    private ModCreativeTabs() {
    }

    public static void initialize() {
        // Loading the class performs the registry registration above.
    }
}
