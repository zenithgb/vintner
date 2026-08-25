package com.zenith.vintner.client;

import net.fabricmc.api.ClientModInitializer;

public class VintnerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Tasting services use baked models; no client renderer is required.
    }
}
