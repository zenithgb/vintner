package com.zenith.vintner.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/** Candidate cells a 26.3 LakeFeature may replace from its generated mask. */
public final class PlainsLakeEnvelope {
    private static final Identifier PLAINS_VILLAGE =
            Identifier.parse("minecraft:village_plains");
    private static final Identifier SAVANNA_VILLAGE =
            Identifier.parse("minecraft:village_savanna");
    private static final Identifier TAIGA_VILLAGE =
            Identifier.parse("minecraft:village_taiga");
    private static final Identifier PLAINS_WINERY =
            Identifier.parse("vintner:village/plains/vineyard");
    private static final Identifier SAVANNA_FIELD =
            Identifier.parse("vintner:village/savanna/vineyard_field_tier1");
    private static final Identifier SAVANNA_SECTION_A =
            Identifier.parse("vintner:village/savanna/vineyard_section_a_tier1");
    private static final Identifier SAVANNA_SECTION_B =
            Identifier.parse("vintner:village/savanna/vineyard_section_b_tier1");
    private static final Identifier SAVANNA_SECTION_C =
            Identifier.parse("vintner:village/savanna/vineyard_section_c_tier1");
    private static final Identifier TAIGA_FIELD =
            Identifier.parse("vintner:village/taiga/vineyard_field_tier1");

    private PlainsLakeEnvelope() {
    }

    public static boolean protectsVillage(Identifier structureId) {
        return PLAINS_VILLAGE.equals(structureId)
                || SAVANNA_VILLAGE.equals(structureId)
                || TAIGA_VILLAGE.equals(structureId);
    }

    /** FEATURES guarantees start data only through radius eight around its centre. */
    public static boolean unavailableVillageStart(
            ChunkPos regionCenter, ChunkPos start, boolean inRegion
    ) {
        return regionCenter.getChessboardDistance(start.x(), start.z()) > 8
                || !inRegion;
    }

    public static boolean rejectsLake(
            boolean[] shape, BlockPos corner,
            Identifier templateId, BoundingBox pieceBox
    ) {
        if (PLAINS_WINERY.equals(templateId)) {
            return intersects(shape, corner, pieceBox);
        }
        if (SAVANNA_FIELD.equals(templateId)
                || SAVANNA_SECTION_A.equals(templateId)
                || SAVANNA_SECTION_B.equals(templateId)
                || SAVANNA_SECTION_C.equals(templateId)
                || TAIGA_FIELD.equals(templateId)) {
            // Saved paired fields and Savanna V2 sections require the natural
            // block immediately below their agricultural surface.
            return intersects(shape, corner, new BoundingBox(
                    pieceBox.minX(), pieceBox.minY() - 1, pieceBox.minZ(),
                    pieceBox.maxX(), pieceBox.maxY(), pieceBox.maxZ()));
        }
        return false;
    }

    public static boolean intersects(
            boolean[] shape, BlockPos corner, BoundingBox protectedBox
    ) {
        int minX = Math.max(0, protectedBox.minX() - corner.getX());
        int maxX = Math.min(15, protectedBox.maxX() - corner.getX());
        int minY = Math.max(0, protectedBox.minY() - corner.getY());
        int maxY = Math.min(7, protectedBox.maxY() - corner.getY());
        int minZ = Math.max(0, protectedBox.minZ() - corner.getZ());
        int maxZ = Math.min(15, protectedBox.maxZ() - corner.getZ());
        if (minX > maxX || minY > maxY || minZ > maxZ) return false;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    if (mayWrite(shape, x, y, z)) return true;
                }
            }
        }
        return false;
    }

    public static boolean hasCandidateInColumn(boolean[] shape, int x, int z) {
        for (int y = 0; y < 8; y++) {
            if (mayWrite(shape, x, y, z)) return true;
        }
        return false;
    }

    private static boolean mayWrite(boolean[] shape, int x, int y, int z) {
        // A true cell receives fluid below Y=4 or cave air above it.
        if (shape[index(x, y, z)]) return true;
        // Vanilla considers the six direct neighbours for its barrier shell.
        // Upper-shell stone is random, so this is a conservative candidate
        // envelope; it never depends on the current terrain or random state.
        return (x < 15 && shape[index(x + 1, y, z)])
                || (x > 0 && shape[index(x - 1, y, z)])
                || (z < 15 && shape[index(x, y, z + 1)])
                || (z > 0 && shape[index(x, y, z - 1)])
                || (y < 7 && shape[index(x, y + 1, z)])
                || (y > 0 && shape[index(x, y - 1, z)]);
    }

    private static int index(int x, int y, int z) {
        return (x * 16 + z) * 8 + y;
    }
}
