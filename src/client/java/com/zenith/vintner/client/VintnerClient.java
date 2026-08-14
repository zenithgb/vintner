package com.zenith.vintner.client;

import com.zenith.vintner.client.render.WineGlassBlockEntityRenderer;
import com.zenith.vintner.registry.ModBlockEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

public class VintnerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(
                ModBlockEntities.WINE_GLASSES,
                WineGlassBlockEntityRenderer::new
        );
    }
}
