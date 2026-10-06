package com.zenith.vintner.mixin;

import com.zenith.vintner.registry.PostJigsawWineryPairs;
import com.zenith.vintner.registry.DesertWineryCandidate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

@Mixin(Structure.class)
abstract class StructureGenerateMixin {
    @Redirect(
            method = "generate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/structure/"
                            + "pieces/StructurePiecesBuilder;build()"
                            + "Lnet/minecraft/world/level/levelgen/structure/"
                            + "pieces/PiecesContainer;"
            )
    )
    private PiecesContainer vintner$appendPostJigsawPair(
            StructurePiecesBuilder builder,
            Holder<Structure> structure,
            ResourceKey<Level> dimension,
            RegistryAccess registries,
            ChunkGenerator generator,
            BiomeSource biomeSource,
            Climate.Sampler sampler,
            RandomState randomState,
            StructureTemplateManager templates,
            long seed,
            ChunkPos chunkPos,
            int references,
            LevelHeightAccessor heightAccessor,
            Predicate<Holder<Biome>> validBiome
    ) {
        String culture = structure.unwrapKey()
                .map(key -> switch (key.identifier().toString()) {
                    case "minecraft:village_plains" -> "plains";
                    case "minecraft:village_desert" -> "desert";
                    case "minecraft:village_savanna" -> "savanna";
                    case "minecraft:village_snowy" -> "snowy";
                    case "minecraft:village_taiga" -> "taiga";
                    default -> "";
                })
                .orElse("");
        if ("desert".equals(culture)) {
            var start = new StructureStart(structure.value(), chunkPos, references, builder.build());
            var selection = DesertWineryCandidate.select(seed, start, templates);
            if (selection.selected() != null) {
                var candidate = selection.selected();
                // Only a persisted bounding-box hint. Placement resolves the final grade
                // from the street anchor's FEATURES-time WORLD_SURFACE_WG height.
                int boundingBoxHintFeetY = generator.getFirstFreeHeight(
                        candidate.pathX(), candidate.pathZ(), Heightmap.Types.WORLD_SURFACE_WG,
                        heightAccessor, randomState);
                var origin = new BlockPos(candidate.originX(), boundingBoxHintFeetY - 7, candidate.originZ());
                StructurePoolElement element = StructurePoolElement
                        .single("vintner:village/desert/vineyard")
                        .apply(StructureTemplatePool.Projection.RIGID);
                BoundingBox box = element.getBoundingBox(templates, origin, candidate.rotation());
                builder.addPiece(new PoolElementStructurePiece(templates, element, origin, 0,
                        candidate.rotation(), box, LiquidSettings.APPLY_WATERLOGGING));
            }
            return builder.build();
        }
        PostJigsawWineryPairs.append(
                culture, seed, builder, chunkPos, generator, randomState,
                templates, heightAccessor
        );
        return builder.build();
    }
}
