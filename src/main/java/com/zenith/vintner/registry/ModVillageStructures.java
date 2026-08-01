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
    private static final int SPECIALIST_HOUSE_WEIGHT = 1;
    // Match the combined weight of each culture's vanilla farm pieces. This
    // makes vineyards a normal part of village agriculture without making
    // every generated building a Vintner structure.
    private static final int PLAINS_VINEYARD_WEIGHT = 8;
    private static final int DESERT_VINEYARD_WEIGHT = 19;
    private static final int SAVANNA_VINEYARD_WEIGHT = 14;
    private static final int SNOWY_VINEYARD_WEIGHT = 6;
    private static final int TAIGA_VINEYARD_WEIGHT = 13;

    private static final List<VillageStructureDefinition>
            VILLAGE_STRUCTURES = List.of(
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
            vineyard("plains", PLAINS_VINEYARD_WEIGHT),
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
            vineyard("desert", DESERT_VINEYARD_WEIGHT),
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
            vineyard("savanna", SAVANNA_VINEYARD_WEIGHT),
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
            vineyard("snowy", SNOWY_VINEYARD_WEIGHT),
            house(
                    "taiga",
                    "taiga_cartographer_house_1",
                    "winemaker_spruce"
            ),
            house(
                    "taiga",
                    "taiga_tool_smith_1",
                    "cooper_spruce"
            ),
            vineyard("taiga", TAIGA_VINEYARD_WEIGHT)
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
        return VILLAGE_STRUCTURES.stream()
                .map(VillageStructureDefinition::pool)
                .distinct()
                .toList();
    }

    public static List<Identifier> vineyardTemplates() {
        return VILLAGE_STRUCTURES.stream()
                .filter(definition -> definition.processor() == null)
                .map(VillageStructureDefinition::template)
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

        for (VillageStructureDefinition definition : VILLAGE_STRUCTURES) {
            StructureTemplatePool pool = pools.getValueOrThrow(
                    definition.pool()
            );

            if (INJECTED_POOLS.contains(pool)) {
                continue;
            }

            StructurePoolElement element;

            if (definition.processor() == null) {
                element = StructurePoolElement
                        .legacy(definition.template().toString())
                        .apply(StructureTemplatePool.Projection.RIGID);
            } else {
                Holder<StructureProcessorList> processor = processors
                        .getOrThrow(definition.processor());
                element = StructurePoolElement
                        .legacy(
                                definition.template().toString(),
                                processor
                        )
                        .apply(StructureTemplatePool.Projection.RIGID);
            }
            StructureTemplatePoolAccessor accessor =
                    (StructureTemplatePoolAccessor) pool;

            List<Pair<StructurePoolElement, Integer>> rawTemplates =
                    new ArrayList<>(accessor.vintner$getRawTemplates());
            rawTemplates.add(Pair.of(element, definition.weight()));
            accessor.vintner$setRawTemplates(List.copyOf(rawTemplates));

            for (int index = 0; index < definition.weight(); index++) {
                accessor.vintner$getTemplates().add(element);
            }
            accessor.vintner$setMaxSize(Integer.MIN_VALUE);

            additions++;
        }

        for (ResourceKey<StructureTemplatePool> poolKey : housePools()) {
            INJECTED_POOLS.add(pools.getValueOrThrow(poolKey));
        }

        Vintner.LOGGER.info(
                "Added {} Vintner village structures to {} cultures.",
                additions,
                housePools().size()
        );
    }

    private static VillageStructureDefinition house(
            String culture,
            String template,
            String processor
    ) {
        return new VillageStructureDefinition(
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
                ),
                SPECIALIST_HOUSE_WEIGHT
        );
    }

    private static VillageStructureDefinition vineyard(
            String culture,
            int weight
    ) {
        return new VillageStructureDefinition(
                ResourceKey.create(
                        Registries.TEMPLATE_POOL,
                        minecraft("village/" + culture + "/houses")
                ),
                vintner("village/" + culture + "/vineyard"),
                null,
                weight
        );
    }

    private static Identifier minecraft(String path) {
        return Identifier.fromNamespaceAndPath("minecraft", path);
    }

    private static Identifier vintner(String path) {
        return Identifier.fromNamespaceAndPath(Vintner.MOD_ID, path);
    }

    private record VillageStructureDefinition(
            ResourceKey<StructureTemplatePool> pool,
            Identifier template,
            ResourceKey<StructureProcessorList> processor,
            int weight
    ) {
    }
}
