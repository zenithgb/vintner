package com.zenith.vintner.mixin;

import com.zenith.vintner.registry.DesertWineryFoundationSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PoolElementStructurePiece.class)
public abstract class DesertWineryFoundationMixin {
    private static final Identifier DESERT_WINERY =
            Identifier.fromNamespaceAndPath(
                    "vintner", "village/desert/vineyard"
            );

    @Inject(method = "postProcess", at = @At("TAIL"))
    private void vintner$supportDesertWineryBase(
            WorldGenLevel level,
            StructureManager structures,
            ChunkGenerator generator,
            RandomSource random,
            BoundingBox chunkBox,
            ChunkPos chunkPos,
            BlockPos pivot,
            CallbackInfo ci
    ) {
        PoolElementStructurePiece piece =
                (PoolElementStructurePiece) (Object) this;
        if (piece.getElement() instanceof SinglePoolElement single
                && single.getTemplateLocation().equals(DESERT_WINERY)) {
            DesertWineryFoundationSupport.fill(
                    level, piece.getBoundingBox(), chunkBox
            );
        }
    }
}
