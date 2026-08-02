package com.zenith.vintner;

import com.zenith.vintner.registry.ModBlockEntities;
import com.zenith.vintner.registry.ModBlocks;
import com.zenith.vintner.registry.ModCreativeTabs;
import com.zenith.vintner.registry.ModAttachments;
import com.zenith.vintner.registry.ModItems;
import com.zenith.vintner.registry.ModGameRules;
import com.zenith.vintner.registry.ModLootTables;
import com.zenith.vintner.registry.ModMobEffects;
import com.zenith.vintner.registry.ModTrades;
import com.zenith.vintner.registry.ModVillageStructures;
import com.zenith.vintner.registry.ModVillagers;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Vintner implements ModInitializer {
    public static final String MOD_ID = "vintner";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModGameRules.initialize();
        ModMobEffects.initialize();
        ModAttachments.initialize();
        ModItems.initialize();
        ModBlocks.initialize();
        ModVillagers.initialize();
        ModVillageStructures.initialize();
        ModCreativeTabs.initialize();
        ModBlockEntities.initialize();
        ModLootTables.initialize();
        ModTrades.initialize();
        LOGGER.info("Vintner initialized.");
    }
}
