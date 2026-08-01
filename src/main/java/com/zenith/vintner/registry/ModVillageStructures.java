package com.zenith.vintner.registry;

import com.mojang.datafixers.util.Pair;
import com.zenith.vintner.Vintner;
import com.zenith.vintner.mixin.StructureTemplatePoolAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

public final class ModVillageStructures {
    private static final int HOUSE_WEIGHT = 1;

    private static final List<HouseDefinition> HOUSES = List.of(
            house(
                    "plains",
                    "plains_cartographer_1",
                    "winemaker_oak"
            ),
            house(
                    "plains",
                    "plains_tool_smith_1",
                    "cooper_oak"
            ),
            house(
                    "desert",
                    "desert_cartographer_house_1",
                    "winemaker_acacia"
            ),
            house(
                    "desert",
                    "desert_tool_smith_1",
                    "cooper_acacia"
            ),
            house(
                    "savanna",
                    "savanna_cartographer_1",
                    "winemaker_acacia"
            ),
            house(
                    "savanna",
                    "savanna_tool_smith_1",
                    "cooper_acacia"
            ),
            house(
                    "snowy",
                    "snowy_cartographer_house_1",
                    "winemaker_spruce"
            ),
            house(
                    "snowy",
                    "snowy_tool_smith_1",
                    "cooper_spruce"
            ),
            house(
                    "taiga",
                    "taiga_cartographer_house_1",
                    "winemaker_spruce"
            ),
            house(
                    "taiga",
                    "taiga_tool_smith_1",
                    "cooper_spruce"
            )
    );

    private static final Set<StructureTemplatePool> INJECTED_POOLS =
            Collections.newSetFromMap(new WeakHashMap<>());

    private ModVillageStructures() {
    }

    public static void initialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(
                server -> inject(server.registryAccess())
        );
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(
                (server, resourceManager, success) -> {
                    if (success) {
                        inject(server.registryAccess());
                    }
                }
        );
    }

    public static List<ResourceKey<StructureTemplatePool>> housePools() {
        return HOUSES.stream()
                .map(HouseDefinition::pool)
                .distinct()
                .toList();
    }

    public static boolean isInjected(
            RegistryAccess registryAccess,
            ResourceKey<StructureTemplatePool> poolKey
    ) {
        StructureTemplatePool pool = registryAccess
                .lookupOrThrow(Registries.TEMPLATE_POOL)
                .getValueOrThrow(poolKey);

        return INJECTED_POOLS.contains(pool);
    }

    private static void inject(RegistryAccess registryAccess) {
        Registry<StructureTemplatePool> pools = registryAccess
                .lookupOrThrow(Registries.TEMPLATE_POOL);
        Registry<StructureProcessorList> processors = registryAccess
                .lookupOrThrow(Registries.PROCESSOR_LIST);
        int additions = 0;

        for (HouseDefinition house : HOUSES) {
            StructureTemplatePool pool = pools.getValueOrThrow(
                    house.pool()
            );

            if (INJECTED_POOLS.contains(pool)) {
                continue;
            }

            Holder<StructureProcessorList> processor = processors
                    .getOrThrow(house.processor());
            StructurePoolElement element = StructurePoolElement
                    .legacy(house.template().toString(), processor)
                    .apply(StructureTemplatePool.Projection.RIGID);
            StructureTemplatePoolAccessor accessor =
                    (StructureTemplatePoolAccessor) pool;

            List<Pair<StructurePoolElement, Integer>> rawTemplates =
                    new ArrayList<>(accessor.vintner$getRawTemplates());
            rawTemplates.add(Pair.of(element, HOUSE_WEIGHT));
            accessor.vintner$setRawTemplates(List.copyOf(rawTemplates));

            for (int index = 0; index < HOUSE_WEIGHT; index++) {
                accessor.vintner$getTemplates().add(element);
            }

            additions++;
        }

        for (ResourceKey<StructureTemplatePool> poolKey : housePools()) {
            INJECTED_POOLS.add(pools.getValueOrThrow(poolKey));
        }

        Vintner.LOGGER.info(
                "Added {} specialist houses to {} village cultures.",
                additions,
                housePools().size()
        );
    }

    private static HouseDefinition house(
            String culture,
            String template,
            String processor
    ) {
        return new HouseDefinition(
                ResourceKey.create(
                        Registries.TEMPLATE_POOL,
                        minecraft("village/" + culture + "/houses")
                ),
                minecraft(
                        "village/" + culture + "/houses/" + template
                ),
                ResourceKey.create(
                        Registries.PROCESSOR_LIST,
                        vintner("village/" + processor)
                )
        );
    }

    private static Identifier minecraft(String path) {
        return Identifier.fromNamespaceAndPath("minecraft", path);
    }

    private static Identifier vintner(String path) {
        return Identifier.fromNamespaceAndPath(Vintner.MOD_ID, path);
    }

    private record HouseDefinition(
            ResourceKey<StructureTemplatePool> pool,
            Identifier template,
            ResourceKey<StructureProcessorList> processor
    ) {
    }
}
