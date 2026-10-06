package com.zenith.vintner.mixin;

import com.zenith.vintner.Vintner;
import com.zenith.vintner.registry.SavannaVineyardSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adds bounded support only after a Savanna field's owned chunk slice places. */
@Mixin(SinglePoolElement.class)
abstract class SavannaVineyardSupportMixin {
    @Inject(method = "place", at = @At("RETURN"))
    private void vintner$terraceSavannaVineyard(
            StructureTemplateManager templates,
            WorldGenLevel level,
            StructureManager structures,
            ChunkGenerator generator,
            BlockPos origin,
            BlockPos pivot,
            Rotation rotation,
            BoundingBox chunkBox,
            RandomSource random,
            LiquidSettings liquid,
            boolean keepJigsaws,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!cir.getReturnValue()) {
            return;
        }
        Identifier templateId = ((SinglePoolElement) (Object) this)
                .getTemplateLocation();
        var result = SavannaVineyardSupport.terrace(
                templates, level, templateId, origin, rotation, chunkBox
        );
        if (result.unsuitable()) {
            Vintner.LOGGER.warn(
                    "UNSUITABLE_SAVANNA_VINEYARD_SUPPORT template={} origin={} "
                            + "rotation={} chunkBox={} rejectedDeep={} "
                            + "rejectedLiquid={}",
                    templateId, origin, rotation, chunkBox,
                    result.rejectedDeep(), result.rejectedLiquid()
            );
        }
    }
}
