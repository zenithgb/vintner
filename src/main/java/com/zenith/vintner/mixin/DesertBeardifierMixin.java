package com.zenith.vintner.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Exclude exact Vintner pieces whose provisional or authored terrain contact must be preserved. */
@Mixin(Beardifier.class)
abstract class DesertBeardifierMixin {
    private static final Identifier VINTNER_DESERT_WINERY =
            Identifier.parse("vintner:village/desert/vineyard");
    private static final Set<Identifier> EXCLUDED = Set.of(
            VINTNER_DESERT_WINERY,
            Identifier.parse("vintner:village/savanna/vineyard_section_a_tier1"),
            Identifier.parse("vintner:village/savanna/vineyard_section_b_tier1"),
            Identifier.parse("vintner:village/savanna/vineyard_section_c_tier1"));

    @Redirect(method = "forStructuresInChunk", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/structure/StructureStart;getPieces()Ljava/util/List;"))
    private static List<StructurePiece> vintner$excludeExactPieces(StructureStart start) {
        List<StructurePiece> pieces = start.getPieces();
        List<StructurePiece> filtered = null;
        for (int index = 0; index < pieces.size(); index++) {
            StructurePiece piece = pieces.get(index);
            if (piece instanceof PoolElementStructurePiece pool
                    && pool.getElement() instanceof SinglePoolElement single
                    && EXCLUDED.contains(single.getTemplateLocation())) {
                if (filtered == null) filtered = new ArrayList<>(pieces);
                filtered.remove(piece);
            }
        }
        return filtered == null ? pieces : filtered;
    }
}
