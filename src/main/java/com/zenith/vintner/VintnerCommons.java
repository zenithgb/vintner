package com.zenith.vintner;

import com.zenithgb.library.module.ModuleRegistry;
import com.zenithgb.library.module.ZenithgbModule;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

/** Vintner's identity in Commons; all wine gameplay remains Vintner-owned. */
public final class VintnerCommons {
    private VintnerCommons() {
    }

    static void initialize() {
        String version = FabricLoader.getInstance().getModContainer(Vintner.MOD_ID)
                .orElseThrow().getMetadata().getVersion().getFriendlyString();
        ModuleRegistry.getInstance().register(new ZenithgbModule(
                Identifier.fromNamespaceAndPath(Vintner.MOD_ID, Vintner.MOD_ID), "Vintner", version));
    }
}
