package com.zenith.vintner.registry;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/** Horizontal selection only; the physical street grade is resolved at placement. */
public final class DesertWineryCandidate {
    private static final String STREET_PREFIX = "minecraft:village/desert/streets/";
    private static final Identifier WINERY_ID = Identifier.parse("vintner:village/desert/vineyard");
    private static final BlockPos CONNECTOR = new BlockPos(6, 7, 0);

    private DesertWineryCandidate() {}

    public static Selection select(long seed, StructureStart start, StructureTemplateManager templates) {
        if (start == null || !start.isValid()) return new Selection(false, 0, null);
        ChunkPos startChunk = start.getChunkPos();
        if (!VillageWineryEligibility.eligible(seed, startChunk.getMiddleBlockX(),
                startChunk.getMiddleBlockZ(), "desert")) return new Selection(false, 0, null);

        StructureTemplate winery = templates.getOrCreate(WINERY_ID);
        List<StructurePiece> pieces = start.getPieces();
        List<Candidate> candidates = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (StructurePiece piece : pieces) {
            if (!(piece instanceof PoolElementStructurePiece street)
                    || !(street.getElement() instanceof SinglePoolElement single)) continue;
            String templateId = single.getTemplateLocation().toString();
            if (!templateId.startsWith(STREET_PREFIX)) continue;
            for (StructureTemplate.JigsawBlockInfo port : street.getElement().getShuffledJigsawBlocks(
                    templates, street.getPosition(), street.getRotation(), RandomSource.create(0L))) {
                if (!port.name().toString().equals("minecraft:building_entrance")) continue;
                Direction outward = JigsawBlock.getFrontFacing(port.state());
                Rotation rotation = facingRotation(outward);
                if (rotation == null) continue;
                BlockPos transformed = StructureTemplate.transform(CONNECTOR, Mirror.NONE, rotation, BlockPos.ZERO);
                int originX = port.pos().getX() + outward.getStepX() - transformed.getX();
                int originZ = port.pos().getZ() + outward.getStepZ() - transformed.getZ();
                BoundingBox footprint = winery.getBoundingBox(
                        new StructurePlaceSettings().setRotation(rotation), new BlockPos(originX, 0, originZ));
                if (footprint.maxX() - footprint.minX() != 14 || footprint.maxZ() - footprint.minZ() != 14) continue;
                ChunkPos owner = ChunkPos.containing(port.pos());
                if (footprint.minX() >> 4 < owner.x() - 1 || footprint.maxX() >> 4 > owner.x() + 1
                        || footprint.minZ() >> 4 < owner.z() - 1 || footprint.maxZ() >> 4 > owner.z() + 1
                        || pieces.stream().anyMatch(other -> other != piece
                        && overlapsXZ(footprint, other.getBoundingBox()))) continue;
                String key = templateId + "|" + street.getPosition().getX() + ","
                        + street.getPosition().getZ() + "|" + street.getRotation() + "|"
                        + port.pos().getX() + "," + port.pos().getZ() + "|" + outward;
                if (!seen.add(key)) continue;
                candidates.add(new Candidate(key, templateId, port.pos().getX(), port.pos().getZ(),
                        outward, rotation, originX, originZ, footprint, owner));
            }
        }
        long salt = startChunk.pack() ^ seed ^ 0x7F8AB981C165B729L;
        candidates.sort((left, right) -> {
            int order = Long.compareUnsigned(rank(salt, left.key()), rank(salt, right.key()));
            return order != 0 ? order : left.key().compareTo(right.key());
        });
        return new Selection(true, candidates.size(), candidates.isEmpty() ? null : candidates.getFirst());
    }

    private static Rotation facingRotation(Direction outward) {
        return switch (outward) {
            case SOUTH -> Rotation.NONE;
            case NORTH -> Rotation.CLOCKWISE_180;
            case EAST -> Rotation.COUNTERCLOCKWISE_90;
            case WEST -> Rotation.CLOCKWISE_90;
            default -> null;
        };
    }

    private static boolean overlapsXZ(BoundingBox a, BoundingBox b) {
        return a.minX() <= b.maxX() && a.maxX() >= b.minX()
                && a.minZ() <= b.maxZ() && a.maxZ() >= b.minZ();
    }

    private static long rank(long salt, String key) {
        long value = salt ^ 0xCBF29CE484222325L;
        for (byte b : key.getBytes(StandardCharsets.UTF_8)) value = (value ^ (b & 0xFFL)) * 1099511628211L;
        value = (value ^ value >>> 30) * -4658895280553007687L;
        value = (value ^ value >>> 27) * -7723592293110705685L;
        return value ^ value >>> 31;
    }

    public record Selection(boolean eligible, int candidateCount, Candidate selected) {}
    public record Candidate(String key, String streetTemplate, int pathX, int pathZ,
            Direction outward, Rotation rotation, int originX, int originZ,
            BoundingBox footprint, ChunkPos owner) {}
}
