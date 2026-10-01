package com.zenith.vintner.mixin;

import com.zenith.vintner.block.GrapevineBlock;
import com.zenith.vintner.block.TrellisBlock;
import com.zenith.vintner.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LakeFeature.class)
public abstract class LakeFeatureVineyardProtectionMixin {
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
