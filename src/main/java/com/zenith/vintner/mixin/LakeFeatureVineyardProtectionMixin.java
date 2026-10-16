package com.zenith.vintner.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zenith.vintner.block.GrapevineBlock;
import com.zenith.vintner.block.TrellisBlock;
import com.zenith.vintner.registry.ModBlocks;
import com.zenith.vintner.registry.PlainsLakeEnvelope;
import net.minecraft.core.registries.Registries;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LakeFeature.class)
public abstract class LakeFeatureVineyardProtectionMixin {
    @ModifyVariable(method = "place", at = @At("STORE"), ordinal = 0)
    private boolean[] vintner$captureLakeShape(
            boolean[] shape, @Share("lakeShape") LocalRef<boolean[]> shapeRef
    ) {
        shapeRef.set(shape);
        return shape;
    }

    // The random lake shape is complete, but no lake block has been written.
    @Inject(method = "place", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/feature/stateproviders/BlockStateProvider;"
                    + "getState(Lnet/minecraft/world/level/LevelAccessor;"
                    + "Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)"
                    + "Lnet/minecraft/world/level/block/state/BlockState;",
            ordinal = 0), cancellable = true)
    private void vintner$rejectLavaLakeOverProtectedLot(
            WorldGenLevel level,
            net.minecraft.world.level.chunk.ChunkGenerator generator,
            net.minecraft.util.RandomSource random,
            BlockPos origin,
            CallbackInfoReturnable<Boolean> cir,
            @Share("lakeShape") LocalRef<boolean[]> shapeRef
    ) {
        if (!(level instanceof WorldGenRegion region)
                || !(((LakeFeature) (Object) this).fluid().value()
                instanceof SimpleStateProvider fluid)
                || !fluid.state().is(Blocks.LAVA)) {
            return;
        }

        // LakeFeature has already replaced its position argument with the
        // lower corner of the generated 16 x 8 x 16 shape.
        BlockPos corner = origin;
        boolean[] lakeShape = shapeRef.get();
        Set<StructureStart> examined = new HashSet<>();
        Set<ChunkPos> surveyedChunks = new HashSet<>();
        var structures = region.registryAccess()
                .lookupOrThrow(Registries.STRUCTURE);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (!PlainsLakeEnvelope.hasCandidateInColumn(
                        lakeShape, x, z)) continue;
                int worldX = corner.getX() + x;
                int worldZ = corner.getZ() + z;
                if (!surveyedChunks.add(new ChunkPos(worldX >> 4, worldZ >> 4))) {
                    continue;
                }
                var candidateChunk = region.getChunk(worldX >> 4, worldZ >> 4,
                        ChunkStatus.STRUCTURE_REFERENCES, false);
                if (candidateChunk == null) {
                    cir.setReturnValue(false);
                    return;
                }
                for (var reference : candidateChunk.getAllReferences().entrySet()) {
                    if (!PlainsLakeEnvelope.protectsVillage(
                            structures.getKey(reference.getKey()))) {
                        continue;
                    }
                    for (long packed : reference.getValue()) {
                        ChunkPos startPos = ChunkPos.unpack(packed);
                        // FEATURES guarantees STRUCTURE_STARTS only within eight
                        // chunks of its centre. Vanilla's generic reference
                        // lookup can follow a farther village start and crash.
                        // Refuse this one lake when its protected lot cannot
                        // safely be inspected; never force another chunk.
                        if (PlainsLakeEnvelope.unavailableVillageStart(
                                region.getCenter(), startPos,
                                region.hasChunk(startPos.x(), startPos.z()))) {
                            cir.setReturnValue(false);
                            return;
                        }
                        var startChunk = region.getChunk(startPos.x(),
                                startPos.z(), ChunkStatus.STRUCTURE_STARTS, false);
                        if (startChunk == null) {
                            cir.setReturnValue(false);
                            return;
                        }
                        StructureStart start = startChunk.getStartForStructure(
                                reference.getKey());
                        if (start == null || !start.isValid()
                                || !examined.add(start)) continue;
                        for (var piece : start.getPieces()) {
                            if (!(piece instanceof PoolElementStructurePiece pool)
                                    || !(pool.getElement() instanceof SinglePoolElement single)) {
                                continue;
                            }
                            if (PlainsLakeEnvelope.rejectsLake(lakeShape,
                                    corner, single.getTemplateLocation(),
                                    piece.getBoundingBox())) {
                                cir.setReturnValue(false);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @Redirect(
            method = "place",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/WorldGenLevel;"
                            + "setBlock(Lnet/minecraft/core/BlockPos;"
                            + "Lnet/minecraft/world/level/block/state/"
                            + "BlockState;I)Z")
    )
    private boolean vintner$protectVineyardFromLakeWrite(
            WorldGenLevel level,
            BlockPos pos,
            BlockState replacement,
            int flags
    ) {
        Block current = level.getBlockState(pos).getBlock();
        if (current == ModBlocks.VINEYARD_SOIL
                || current instanceof GrapevineBlock
                || current instanceof TrellisBlock) {
            return false;
        }
        return level.setBlock(pos, replacement, flags);
    }
}
