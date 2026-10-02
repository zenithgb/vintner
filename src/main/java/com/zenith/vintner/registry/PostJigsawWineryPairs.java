package com.zenith.vintner.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Adds a culture's two templates only after vanilla finishes its village layout. */
public final class PostJigsawWineryPairs {
    private static final int MAX_STREETS = 64;
    private static final int MAX_CANDIDATES_PER_PIECE = MAX_STREETS * 4;

    private PostJigsawWineryPairs() {
    }

    public static void append(
            String culture,
            StructurePiecesBuilder builder,
            ChunkPos startChunk,
            ChunkGenerator generator,
            RandomState randomState,
            StructureTemplateManager templates,
            LevelHeightAccessor heightAccessor
    ) {
        PairConfig config = PairConfig.forCulture(culture);
        if (config == null) {
            return;
        }
        List<StructurePiece> existing = builder.build().pieces();
        List<PoolElementStructurePiece> streets = existing.stream()
                .filter(piece -> piece instanceof PoolElementStructurePiece)
                .map(piece -> (PoolElementStructurePiece) piece)
                .filter(piece -> isStreet(piece, config))
                .sorted(Comparator
                        .comparingInt((PoolElementStructurePiece piece) ->
                                piece.getBoundingBox().minX())
                        .thenComparingInt(piece -> piece.getBoundingBox().minZ()))
                .limit(MAX_STREETS)
                .toList();
        if (streets.isEmpty()) {
            return;
        }

        StructurePoolElement wineryElement = StructurePoolElement
                .single(config.wineryId)
                .apply(StructureTemplatePool.Projection.RIGID);
        StructurePoolElement fieldElement = StructurePoolElement
                .single(config.fieldId)
                .apply(StructureTemplatePool.Projection.RIGID);
        Search winerySearch = findCandidate(
                wineryElement, config.wineryRoadEdge, true, config,
                streets, existing,
                startChunk, generator, randomState, templates, heightAccessor
        );
        if (winerySearch.piece == null) {
            return;
        }
        List<StructurePiece> withWinery = new ArrayList<>(existing);
        withWinery.add(winerySearch.piece);
        Search fieldSearch = findCandidate(
                fieldElement, config.fieldRoadEdge, false, config,
                streets, withWinery,
                startChunk, generator, randomState, templates, heightAccessor
        );
        if (fieldSearch.piece == null) {
            return;
        }

        // The only two builder mutations happen after both independent pieces
        // have passed. Each is a normal, serializable pool-element piece.
        builder.addPiece(winerySearch.piece);
        builder.addPiece(fieldSearch.piece);
    }

    private static boolean isStreet(
            PoolElementStructurePiece piece, PairConfig config
    ) {
        return piece.getElement() instanceof SinglePoolElement single
                && single.getTemplateLocation().toString()
                .startsWith(config.streetPrefix);
    }

    private static Search findCandidate(
            StructurePoolElement element,
            BlockPos authoredRoadEdge,
            boolean winery,
            PairConfig config,
            List<PoolElementStructurePiece> streets,
            List<StructurePiece> occupied,
            ChunkPos startChunk,
            ChunkGenerator generator,
            RandomState randomState,
            StructureTemplateManager templates,
            LevelHeightAccessor heightAccessor
    ) {
        Search result = new Search();
        for (PoolElementStructurePiece street : streets) {
            BoundingBox road = street.getBoundingBox();
            for (StreetSide side : StreetSide.values()) {
                if (result.considered >= MAX_CANDIDATES_PER_PIECE) {
                    return result;
                }
                result.considered++;
                Rotation rotation = winery
                        ? config.wineryRotation(side)
                        : config.fieldRotation(side);
                BlockPos transformedRoadEdge = StructureTemplate.transform(
                        authoredRoadEdge, Mirror.NONE, rotation, BlockPos.ZERO
                );
                int portX = side == StreetSide.WEST ? road.minX() - 1
                        : side == StreetSide.EAST ? road.maxX() + 1
                        : road.getCenter().getX();
                int portZ = side == StreetSide.NORTH ? road.minZ() - 1
                        : side == StreetSide.SOUTH ? road.maxZ() + 1
                        : road.getCenter().getZ();
                int roadSurface = surface(
                        generator, randomState, heightAccessor,
                        road.getCenter().getX(), road.getCenter().getZ()
                );
                int portSurface = surface(
                        generator, randomState, heightAccessor, portX, portZ
                );
                if (Math.abs(roadSurface - portSurface) > 2
                        || !dry(generator, randomState, heightAccessor,
                        portX, portZ)) {
                    continue;
                }
                // The authored surface at local Y=0 must meet the road-side
                // terrain rather than sit on top of it. Taiga's winery has
                // an additional two-block cellar/foundation below its
                // walkable entrance; its field does not.
                int terrainContact = portSurface - 2
                        - (winery && config.taiga ? 1 : 0);
                BlockPos origin = new BlockPos(
                        portX - transformedRoadEdge.getX(),
                        terrainContact - transformedRoadEdge.getY(),
                        portZ - transformedRoadEdge.getZ()
                );
                BoundingBox box = element.getBoundingBox(
                        templates, origin, rotation
                );
                if (!withinStructureReferenceRange(box, startChunk)) {
                    continue;
                }
                if (occupied.stream().anyMatch(piece ->
                        overlapsXZ(box, piece.getBoundingBox()))) {
                    continue;
                }
                if (!terrainFits(generator, randomState, heightAccessor,
                        box, portSurface)) {
                    continue;
                }
                result.piece = new PoolElementStructurePiece(
                        templates, element, origin, 0, rotation, box,
                        LiquidSettings.APPLY_WATERLOGGING
                );
                return result;
            }
        }
        return result;
    }

    private static boolean terrainFits(
            ChunkGenerator generator,
            RandomState randomState,
            LevelHeightAccessor heightAccessor,
            BoundingBox box,
            int portSurface
    ) {
        int low = portSurface;
        int high = portSurface;
        // A corner-and-centre sample can miss a one-block-wide ravine. These
        // player templates are at most 18 blocks across, so every footprint
        // column is still a small, deterministic generator-height query.
        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int z = box.minZ(); z <= box.maxZ(); z++) {
                int surface = surface(generator, randomState,
                        heightAccessor, x, z);
                if (!dry(generator, randomState, heightAccessor, x, z)) {
                    return false;
                }
                low = Math.min(low, surface);
                high = Math.max(high, surface);
                if (high - low > 2) {
                    return false;
                }
            }
        }
        return true;
    }

    private static int surface(
            ChunkGenerator generator,
            RandomState randomState,
            LevelHeightAccessor heightAccessor,
            int x,
            int z
    ) {
        return generator.getFirstFreeHeight(
                x, z, Heightmap.Types.WORLD_SURFACE_WG,
                heightAccessor, randomState
        );
    }

    private static boolean dry(
            ChunkGenerator generator,
            RandomState randomState,
            LevelHeightAccessor heightAccessor,
            int x,
            int z
    ) {
        int top = surface(generator, randomState, heightAccessor, x, z);
        int floor = generator.getFirstFreeHeight(
                x, z, Heightmap.Types.OCEAN_FLOOR_WG,
                heightAccessor, randomState
        );
        return top - floor <= 1;
    }

    private static boolean overlapsXZ(BoundingBox left, BoundingBox right) {
        return left.minX() <= right.maxX()
                && left.maxX() >= right.minX()
                && left.minZ() <= right.maxZ()
                && left.maxZ() >= right.minZ();
    }

    private static boolean withinStructureReferenceRange(
            BoundingBox box,
            ChunkPos startChunk
    ) {
        // ChunkGenerator.createReferences only looks eight chunks away for a
        // structure start. A piece outside this radius would serialize but
        // could never be placed in its distant chunks.
        return (box.minX() >> 4) >= startChunk.x() - 8
                && (box.maxX() >> 4) <= startChunk.x() + 8
                && (box.minZ() >> 4) >= startChunk.z() - 8
                && (box.maxZ() >> 4) <= startChunk.z() + 8;
    }

    private enum StreetSide {
        NORTH(Rotation.NONE, Rotation.CLOCKWISE_180),
        SOUTH(Rotation.CLOCKWISE_180, Rotation.NONE),
        WEST(Rotation.COUNTERCLOCKWISE_90, Rotation.CLOCKWISE_90),
        EAST(Rotation.CLOCKWISE_90, Rotation.COUNTERCLOCKWISE_90);

        private final Rotation wineryRotation;
        private final Rotation fieldRotation;

        StreetSide(Rotation wineryRotation, Rotation fieldRotation) {
            this.wineryRotation = wineryRotation;
            this.fieldRotation = fieldRotation;
        }
    }

    private record PairConfig(
            String streetPrefix,
            String wineryId,
            String fieldId,
            BlockPos wineryRoadEdge,
            BlockPos fieldRoadEdge,
            boolean taiga
    ) {
        private static PairConfig forCulture(String culture) {
            return switch (culture) {
                case "savanna" -> new PairConfig(
                        "minecraft:village/savanna/streets/",
                        "vintner:village/savanna/winery_tier1",
                        "vintner:village/savanna/vineyard_field_tier1",
                        new BlockPos(5, 0, 17), new BlockPos(3, 0, 0), false
                );
                case "taiga" -> new PairConfig(
                        "minecraft:village/taiga/streets/",
                        "vintner:village/taiga/winery_tier1",
                        "vintner:village/taiga/vineyard_field_tier1",
                        new BlockPos(0, 1, 6), new BlockPos(10, 0, 9), true
                );
                default -> null;
            };
        }

        private Rotation wineryRotation(StreetSide side) {
            if (!taiga) {
                return side.wineryRotation;
            }
            return switch (side) {
                case NORTH -> Rotation.COUNTERCLOCKWISE_90;
                case SOUTH -> Rotation.CLOCKWISE_90;
                case WEST -> Rotation.CLOCKWISE_180;
                case EAST -> Rotation.NONE;
            };
        }

        private Rotation fieldRotation(StreetSide side) {
            if (!taiga) {
                return side.fieldRotation;
            }
            return switch (side) {
                case NORTH -> Rotation.CLOCKWISE_90;
                case SOUTH -> Rotation.COUNTERCLOCKWISE_90;
                case WEST -> Rotation.NONE;
                case EAST -> Rotation.CLOCKWISE_180;
            };
        }
    }

    private static final class Search {
        private int considered;
        private PoolElementStructurePiece piece;
    }
}
