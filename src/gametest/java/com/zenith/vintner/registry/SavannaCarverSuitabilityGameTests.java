package com.zenith.vintner.registry;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

/** Exercises the same authored 126-cell field mask in every rotation. */
public final class SavannaCarverSuitabilityGameTests {
    @GameTest(maxTicks = 40, padding = 64)
    public void fieldReplayDoesNotDependOnQueryOrder(GameTestHelper helper) {
        var level = helper.getLevel();
        var generator = level.getChunkSource().getGenerator();
        var randomState = level.getChunkSource().randomState();
        var template = level.getServer().getStructureTemplateManager()
                .get(Identifier.parse(
                        "vintner:village/savanna/vineyard_field_tier1"))
                .orElseThrow();
        int rotationIndex = 0;
        for (Rotation rotation : Rotation.values()) {
            BlockPos origin = helper.absolutePos(new BlockPos(
                    8 + 24 * (rotationIndex % 2), 8,
                    8 + 24 * (rotationIndex / 2)));
            var settings = new StructurePlaceSettings()
                    .setRotation(rotation);
            List<BlockPos> ground = new ArrayList<>();
            template.filterBlocks(origin, settings,
                    ModBlocks.VINEYARD_SOIL).forEach(
                    info -> ground.add(info.pos()));
            template.filterBlocks(origin, settings,
                    Blocks.GRASS_BLOCK).forEach(
                    info -> ground.add(info.pos()));
            helper.assertValueEqual(ground.size(), 126,
                    rotation + " must include all 45 vines and 81 lanes");
            var forward = new SavannaCarverSuitability(
                    generator, randomState, level);
            var reverse = new SavannaCarverSuitability(
                    generator, randomState, level);
            boolean[] expected = new boolean[ground.size()];
            for (int i = 0; i < ground.size(); i++) {
                expected[i] = forward.carvesFieldSupport(ground.get(i));
            }
            for (int i = ground.size() - 1; i >= 0; i--) {
                helper.assertValueEqual(
                        reverse.carvesFieldSupport(ground.get(i)),
                        expected[i], rotation + " replay at " + ground.get(i));
            }
            rotationIndex++;
        }
        helper.succeed();
    }
}
