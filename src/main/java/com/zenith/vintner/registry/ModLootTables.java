package com.zenith.vintner.registry;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.Set;

public final class ModLootTables {
    private static final float GRAPE_CUTTING_CHANCE = 0.25F;

    private static final Set<ResourceKey<LootTable>>
            VILLAGE_HOUSE_CHESTS = Set.of(
                    BuiltInLootTables.VILLAGE_DESERT_HOUSE,
                    BuiltInLootTables.VILLAGE_PLAINS_HOUSE,
                    BuiltInLootTables.VILLAGE_SAVANNA_HOUSE,
                    BuiltInLootTables.VILLAGE_SNOWY_HOUSE,
                    BuiltInLootTables.VILLAGE_TAIGA_HOUSE
            );

    private ModLootTables() {
    }

    public static void initialize() {
        LootTableEvents.MODIFY.register(
                (key, builder, source, registries) -> {
                    if (!source.isBuiltin()
                            || !VILLAGE_HOUSE_CHESTS.contains(key)) {
                        return;
                    }

                    builder.withPool(
                            LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .when(
                                            LootItemRandomChanceCondition
                                                    .randomChance(
                                                            GRAPE_CUTTING_CHANCE
                                                    )
                                    )
                                    .add(
                                            LootItem.lootTableItem(
                                                    ModItems.RED_GRAPE_CUTTING
                                            ).apply(
                                                    SetItemCountFunction.setCount(
                                                            UniformGenerator.between(
                                                                    1,
                                                                    2
                                                            )
                                                    )
                                            )
                                    )
                                    .add(
                                            LootItem.lootTableItem(
                                                    ModItems.WHITE_GRAPE_CUTTING
                                            ).apply(
                                                    SetItemCountFunction.setCount(
                                                            UniformGenerator.between(
                                                                    1,
                                                                    2
                                                            )
                                                    )
                                            )
                                    )
                    );
                }
        );
    }
}
