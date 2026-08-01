package com.zenith.vintner.registry;

import com.google.common.collect.ImmutableSet;
import com.zenith.vintner.Vintner;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PoiHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.TradeSet;

import java.util.function.Predicate;

public final class ModVillagers {
    private static final Identifier WINEMAKER_ID = id("winemaker");
    private static final Identifier COOPER_ID = id("cooper");

    public static final ResourceKey<PoiType> WINEMAKER_POI_KEY =
            ResourceKey.create(
                    Registries.POINT_OF_INTEREST_TYPE,
                    WINEMAKER_ID
            );
    public static final ResourceKey<PoiType> COOPER_POI_KEY =
            ResourceKey.create(
                    Registries.POINT_OF_INTEREST_TYPE,
                    COOPER_ID
            );

    public static final PoiType WINEMAKER_POI = PoiHelper.register(
            WINEMAKER_ID,
            1,
            2,
            ModBlocks.grapePressBlocks()
    );
    public static final PoiType COOPER_POI = PoiHelper.register(
            COOPER_ID,
            1,
            1,
            ModBlocks.barrelStandBlocks()
    );

    public static final ResourceKey<VillagerProfession> WINEMAKER =
            ResourceKey.create(
                    Registries.VILLAGER_PROFESSION,
                    WINEMAKER_ID
            );
    public static final ResourceKey<VillagerProfession> COOPER =
            ResourceKey.create(
                    Registries.VILLAGER_PROFESSION,
                    COOPER_ID
            );

    public static final VillagerProfession WINEMAKER_PROFESSION =
            registerProfession(
                    WINEMAKER,
                    WINEMAKER_POI_KEY,
                    "entity.vintner.villager.winemaker",
                    SoundEvents.VILLAGER_WORK_FARMER
            );
    public static final VillagerProfession COOPER_PROFESSION =
            registerProfession(
                    COOPER,
                    COOPER_POI_KEY,
                    "entity.vintner.villager.cooper",
                    SoundEvents.VILLAGER_WORK_TOOLSMITH
            );

    private ModVillagers() {
    }

    public static void initialize() {
        // Triggers static registration in a predictable initializer phase.
    }

    private static VillagerProfession registerProfession(
            ResourceKey<VillagerProfession> key,
            ResourceKey<PoiType> poiKey,
            String translationKey,
            SoundEvent workSound
    ) {
        Predicate<Holder<PoiType>> jobSite = holder ->
                holder.is(poiKey);

        VillagerProfession profession = new VillagerProfession(
                Component.translatable(translationKey),
                jobSite,
                jobSite,
                ImmutableSet.of(),
                ImmutableSet.of(),
                workSound,
                new Int2ObjectOpenHashMap<
                        ResourceKey<TradeSet>
                >()
        );

        return Registry.register(
                BuiltInRegistries.VILLAGER_PROFESSION,
                key,
                profession
        );
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(
                Vintner.MOD_ID,
                path
        );
    }
}
