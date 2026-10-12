package com.zenith.vintner.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Blocks;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Adds one logical winery after vanilla finishes its village layout. */
public final class PostJigsawWineryPairs {
    private static final int MAX_STREETS = 64;
    private static final int MAX_CANDIDATES_PER_PIECE = MAX_STREETS * 4;

    private PostJigsawWineryPairs() {
    }

    public static void append(
            String culture,
            long seed,
            StructurePiecesBuilder builder,
            ChunkPos startChunk,
            ChunkGenerator generator,
            RandomState randomState,
            StructureTemplateManager templates,
            LevelHeightAccessor heightAccessor
    ) {
        PairConfig config = PairConfig.forCulture(culture);
        if (config == null || !VillageWineryEligibility.eligible(
                seed, startChunk.getMiddleBlockX(),
                startChunk.getMiddleBlockZ(), culture)) {
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
        StructureTemplate wineryTemplate = templates.getOrCreate(
                Identifier.parse(config.wineryId));
        Search winerySearch = findCandidate(
                wineryElement, wineryTemplate, config.wineryRoadEdge,
                true, config,
                streets, existing,
                startChunk, generator, randomState, templates, heightAccessor
        );
        if (winerySearch.piece == null) {
            return;
        }
        if (config.fieldId == null) {
            builder.addPiece(winerySearch.piece);
            return;
        }
        StructurePoolElement fieldElement = StructurePoolElement
                .single(config.fieldId)
                .apply(StructureTemplatePool.Projection.RIGID);
        StructureTemplate fieldTemplate = templates.getOrCreate(
                Identifier.parse(config.fieldId));
        List<StructurePiece> withWinery = new ArrayList<>(existing);
        withWinery.add(winerySearch.piece);
        Search fieldSearch = findCandidate(
                fieldElement, fieldTemplate, config.fieldRoadEdge,
                false, config,
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
            StructureTemplate template,
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
                // Plains uses a one-block contact offset and two-block piece
                // ground-level delta to meet terrain without moving the
                // shaping reference. Taiga keeps its separate cellar offset.
                int terrainContact = portSurface - 2
                        + (winery ? config.wineryContactOffset : 0);
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
                        box, portSurface, !winery,
                        template, origin, rotation)) {
                    continue;
                }
                result.piece = new PoolElementStructurePiece(
                        templates, element, origin,
                        winery && (config.taiga || "vintner:village/plains/vineyard".equals(config.wineryId)) ? 2 : 0, rotation, box,
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
            int portSurface,
            boolean field,
            StructureTemplate template,
            BlockPos origin,
            Rotation rotation
    ) {
        int low = portSurface;
        int high = portSurface;
        List<StructureTemplate.StructureBlockInfo> authoredAir =
                template.filterBlocks(origin,
                        new StructurePlaceSettings().setRotation(rotation),
                        Blocks.AIR);
        Set<BlockPos> airAtFoundation = new HashSet<>();
        for (var block : authoredAir) {
            if (block.pos().getY() == box.minY()) {
                airAtFoundation.add(block.pos());
            }
        }
        // Every non-air Y=0 cell is authored contact. Only Snowy has air
        // margin on this plane; water there must not invalidate dry property.
        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int z = box.minZ(); z <= box.maxZ(); z++) {
                if (airAtFoundation.contains(
                        new BlockPos(x, box.minY(), z))) {
                    continue;
                }
                int surface = surface(generator, randomState,
                        heightAccessor, x, z);
                if (!dry(generator, randomState, heightAccessor, x, z)) {
                    return false;
                }
                if (field) {
                    var beneath = generator.getBaseColumn(
                            x, z, heightAccessor, randomState
                    ).getBlock(box.minY() - 1);
                    if (beneath.isAir() || !beneath.getFluidState().isEmpty()) {
                        return false;
                    }
                }
                low = Math.min(low, surface);
                high = Math.max(high, surface);
                if (high - low > 2) {
                    return false;
                }
            }
        }
        // Open authored space can flood when a rigid piece lowers nearby
        // terrain toward a lake. Check only lower exposed air at the boundary;
        // water behind solid authored walls or below the entrance is valid.
        for (var block : authoredAir) {
            BlockPos pos = block.pos();
            if (pos.getY() <= box.minY()
                    || pos.getY() > box.minY() + 4
                    || airAtFoundation.contains(new BlockPos(
                    pos.getX(), box.minY(), pos.getZ()))) {
                continue;
            }
            if (pos.getX() == box.minX()
                    && wetAt(generator, randomState, heightAccessor,
                    pos.west())) return false;
            if (pos.getX() == box.maxX()
                    && wetAt(generator, randomState, heightAccessor,
                    pos.east())) return false;
            if (pos.getZ() == box.minZ()
                    && wetAt(generator, randomState, heightAccessor,
                    pos.north())) return false;
            if (pos.getZ() == box.maxZ()
                    && wetAt(generator, randomState, heightAccessor,
                    pos.south())) return false;
        }
        return true;
    }

    private static boolean wetAt(
            ChunkGenerator generator,
            RandomState randomState,
            LevelHeightAccessor heightAccessor,
            BlockPos pos
    ) {
        return !generator.getBaseColumn(pos.getX(), pos.getZ(),
                heightAccessor, randomState).getBlock(pos.getY())
                .getFluidState().isEmpty();
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
        // A single water block raises WORLD_SURFACE_WG above OCEAN_FLOOR_WG.
        // Both paired templates have functional ground in every Y=0 column.
        return dryHeightmapContact(top, floor);
    }

    public static boolean dryHeightmapContact(int top, int floor) {
        return top == floor;
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
            int wineryContactOffset,
            boolean taiga,
            boolean southFacing
    ) {
        private static PairConfig forCulture(String culture) {
            return switch (culture) {
                case "plains" -> new PairConfig(
                        "minecraft:village/plains/streets/",
                        "vintner:village/plains/vineyard", null,
                        new BlockPos(8, 2, 0), null, 1, false, false
                );
                case "desert" -> new PairConfig(
                        "minecraft:village/desert/streets/",
                        "vintner:village/desert/vineyard", null,
                        new BlockPos(6, 7, 0), null, 0, false, false
                );
                case "savanna" -> new PairConfig(
                        "minecraft:village/savanna/streets/",
                        "vintner:village/savanna/winery_tier1",
                        "vintner:village/savanna/vineyard_field_tier1",
                        new BlockPos(5, 0, 17), new BlockPos(3, 0, 0),
                        0, false, true
                );
                case "snowy" -> new PairConfig(
                        "minecraft:village/snowy/streets/",
                        "vintner:village/snowy/vineyard", null,
                        new BlockPos(2, 1, 9), null, 0, false, true
                );
                case "taiga" -> new PairConfig(
                        "minecraft:village/taiga/streets/",
                        "vintner:village/taiga/winery_tier1",
                        "vintner:village/taiga/vineyard_field_tier1",
                        new BlockPos(0, 1, 6), new BlockPos(10, 0, 9),
                        -1, true, false
                );
                default -> null;
            };
        }

        private Rotation wineryRotation(StreetSide side) {
            if (!taiga) {
                return southFacing ? side.wineryRotation
                        : side.fieldRotation;
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
