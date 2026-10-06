package com.zenith.vintner.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayList;
import java.util.List;

/** Bounded row terraces below the authored Savanna vineyard-soil cells. */
public final class SavannaVineyardSupport {
    public static final Identifier TEMPLATE_ID = Identifier.parse(
            "vintner:village/savanna/vineyard_field_tier1"
    );
    public static final int MAX_SUPPORT_DEPTH = 5;
    private static final int MAX_WATER_DEPTH = 1;

    private SavannaVineyardSupport() {
    }

    public static Result terrace(
            StructureTemplateManager templates,
            WorldGenLevel level,
            Identifier templateId,
            BlockPos origin,
            Rotation rotation,
            BoundingBox chunkBox
    ) {
        if (!TEMPLATE_ID.equals(templateId)) {
            return Result.EMPTY;
        }
        StructureTemplate template = templates.getOrCreate(templateId);
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(rotation);
        int considered = 0;
        int completed = 0;
        int blocksPlaced = 0;
        int deepest = 0;
        int rejectedDeep = 0;
        int rejectedLiquid = 0;
        for (var info : template.filterBlocks(
                origin, settings, ModBlocks.VINEYARD_SOIL)) {
            BlockPos soil = info.pos();
            if (!chunkBox.isInside(soil)
                    || !level.getBlockState(soil).is(ModBlocks.VINEYARD_SOIL)) {
                continue;
            }
            considered++;
            List<BlockPos> replaceable = new ArrayList<>();
            int waterDepth = 0;
            boolean unsafeLiquid = false;
            boolean anchored = false;
            for (int depth = 1; depth <= MAX_SUPPORT_DEPTH + 1; depth++) {
                BlockPos pos = soil.below(depth);
                BlockState state = level.getBlockState(pos);
                if (isStableAnchor(state)) {
                    anchored = true;
                    break;
                }
                if (depth > MAX_SUPPORT_DEPTH) {
                    break;
                }
                if (!state.getFluidState().isEmpty()) {
                    if (state.getFluidState().is(FluidTags.LAVA)
                            || !state.getFluidState().is(FluidTags.WATER)
                            || !state.canBeReplaced()) {
                        unsafeLiquid = true;
                    } else {
                        waterDepth++;
                    }
                }
                replaceable.add(pos.immutable());
            }
            if (!anchored) {
                rejectedDeep++;
                continue;
            }
            if (unsafeLiquid || waterDepth > MAX_WATER_DEPTH) {
                rejectedLiquid++;
                continue;
            }
            for (BlockPos pos : replaceable) {
                level.setBlock(pos, Blocks.COARSE_DIRT.defaultBlockState(),
                        Block.UPDATE_CLIENTS);
            }
            completed++;
            blocksPlaced += replaceable.size();
            deepest = Math.max(deepest, replaceable.size());
        }
        return new Result(considered, completed, blocksPlaced, deepest,
                rejectedDeep, rejectedLiquid);
    }

    private static boolean isStableAnchor(BlockState state) {
        return !state.isAir()
                && state.getFluidState().isEmpty()
                && !state.canBeReplaced()
                && !state.is(BlockTags.LEAVES)
                && !(state.getBlock() instanceof FallingBlock);
    }

    /** Keeps later lake writes from cutting through a generated bank or anchor. */
    public static boolean isTerraceOrAnchor(
            WorldGenLevel level, BlockPos pos, BlockState current
    ) {
        boolean support = current.is(Blocks.COARSE_DIRT);
        if (!support && !isStableAnchor(current)) {
            return false;
        }
        int limit = support ? MAX_SUPPORT_DEPTH : MAX_SUPPORT_DEPTH + 1;
        for (int distance = 1; distance <= limit; distance++) {
            BlockState above = level.getBlockState(pos.above(distance));
            if (above.is(ModBlocks.VINEYARD_SOIL)) {
                return support || distance > 1;
            }
            if (!above.is(Blocks.COARSE_DIRT)) {
                return false;
            }
        }
        return false;
    }

    public record Result(
            int columnsConsidered,
            int columnsCompleted,
            int blocksPlaced,
            int maximumDepth,
            int rejectedDeep,
            int rejectedLiquid
    ) {
        private static final Result EMPTY = new Result(0, 0, 0, 0, 0, 0);

        public boolean unsuitable() {
            return rejectedDeep > 0 || rejectedLiquid > 0;
        }
    }
}
