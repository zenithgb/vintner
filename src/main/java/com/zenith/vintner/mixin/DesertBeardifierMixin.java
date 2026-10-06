package com.zenith.vintner.mixin;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** The Desert piece's provisional bounding box must not reshape terrain before its Y is resolved. */
@Mixin(Beardifier.class)
abstract class DesertBeardifierMixin {
    private static final Identifier VINTNER_DESERT_WINERY =
            Identifier.parse("vintner:village/desert/vineyard");

    @Redirect(method = "forStructuresInChunk", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/structure/StructureStart;getPieces()Ljava/util/List;"))
    private static List<StructurePiece> vintner$excludeDesertOnly(StructureStart start) {
        List<StructurePiece> pieces = start.getPieces();
        List<StructurePiece> filtered = null;
        for (int index = 0; index < pieces.size(); index++) {
            StructurePiece piece = pieces.get(index);
            if (piece instanceof PoolElementStructurePiece pool
                    && pool.getElement() instanceof SinglePoolElement single
                    && single.getTemplateLocation().equals(VINTNER_DESERT_WINERY)) {
                if (filtered == null) filtered = new ArrayList<>(pieces);
                filtered.remove(piece);
            }
        }
        return filtered == null ? pieces : filtered;
    }
}
