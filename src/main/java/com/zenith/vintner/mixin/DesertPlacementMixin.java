package com.zenith.vintner.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Moves the entire rigid Desert template to the adjacent street's placement-time grade. */
@Mixin(PoolElementStructurePiece.class)
abstract class DesertPlacementMixin {
    private static final Identifier VINTNER_DESERT_WINERY =
            Identifier.parse("vintner:village/desert/vineyard");
    private static final BlockPos VINTNER_DESERT_CONNECTOR = new BlockPos(6, 7, 0);
    private static final int VINTNER_DESERT_DOOR_Y = 7;

    @Redirect(method = "place", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/structure/pools/StructurePoolElement;place("
                    + "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;"
                    + "Lnet/minecraft/world/level/WorldGenLevel;"
                    + "Lnet/minecraft/world/level/StructureManager;"
                    + "Lnet/minecraft/world/level/chunk/ChunkGenerator;"
                    + "Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;"
                    + "Lnet/minecraft/world/level/block/Rotation;"
                    + "Lnet/minecraft/world/level/levelgen/structure/BoundingBox;"
                    + "Lnet/minecraft/util/RandomSource;"
                    + "Lnet/minecraft/world/level/levelgen/structure/templatesystem/LiquidSettings;Z)Z"))
    private boolean vintner$placeDesertAtStreetGrade(StructurePoolElement element,
            StructureTemplateManager templates, WorldGenLevel level,
            StructureManager structures, ChunkGenerator generator, BlockPos original,
            BlockPos pivot, Rotation rotation, BoundingBox chunkBox, RandomSource random,
            LiquidSettings liquid, boolean keepJigsaws) {
        BlockPos placement = original;
        if (element instanceof SinglePoolElement single
                && single.getTemplateLocation().equals(VINTNER_DESERT_WINERY)) {
            BlockPos connector = original.offset(StructureTemplate.transform(
                    VINTNER_DESERT_CONNECTOR, Mirror.NONE, rotation, BlockPos.ZERO));
            Direction outward = switch (rotation) {
                case NONE -> Direction.SOUTH;
                case CLOCKWISE_90 -> Direction.WEST;
                case CLOCKWISE_180 -> Direction.NORTH;
                case COUNTERCLOCKWISE_90 -> Direction.EAST;
            };
            BlockPos streetAnchor = connector.relative(outward.getOpposite());
            int streetFeetY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG,
                    streetAnchor.getX(), streetAnchor.getZ());
            placement = original.atY(streetFeetY - VINTNER_DESERT_DOOR_Y);
        }
        return element.place(templates, level, structures, generator, placement, pivot,
                rotation, chunkBox, random, liquid, keepJigsaws);
    }
}
