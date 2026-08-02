package com.zenith.vintner.mixin;

import com.zenith.vintner.registry.ModVillageStructures;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(
        targets = "net.minecraft.world.level.levelgen.structure.pools."
                + "JigsawPlacement$Placer"
)
abstract class JigsawPlacementPlacerMixin {
    @Unique
    private boolean vintner$vineyardPlaced;

    @Redirect(
            method = "tryPlacingChildren",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/"
                            + "structure/pools/StructureTemplatePool;"
                            + "getShuffledTemplates(Lnet/minecraft/util/"
                            + "RandomSource;)Ljava/util/List;"
            )
    )
    private List<StructurePoolElement> vintner$limitVineyardCandidates(
            StructureTemplatePool pool,
            RandomSource random
    ) {
        List<StructurePoolElement> candidates =
                pool.getShuffledTemplates(random);

        if (!vintner$vineyardPlaced) {
            return candidates;
        }

        return candidates.stream()
                .filter(element ->
                        !ModVillageStructures.isVineyardElement(element)
                )
                .toList();
    }

    @Redirect(
            method = "tryPlacingChildren",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
            )
    )
    private boolean vintner$recordPlacedVineyard(
            List<Object> pieces,
            Object value
    ) {
        boolean added = pieces.add(value);

        if (added
                && value instanceof PoolElementStructurePiece piece
                && ModVillageStructures.isVineyardElement(
                        piece.getElement()
                )) {
            vintner$vineyardPlaced = true;
        }

        return added;
    }
}
