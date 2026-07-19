package com.zenith.vintner;

import com.zenith.vintner.registry.ModBlocks;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Vintner implements ModInitializer {
    public static final String MOD_ID = "vintner";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModBlocks.initialize();
        LOGGER.info("Vintner initialized.");
    }
}
