package com.zenith.vintner.registry;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.CarverOutput;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

/** Predicts the vanilla 26.3 carver mask before a Savanna field is committed. */
final class SavannaCarverSuitability {
    private static final int ORIGIN_RADIUS = 8;

    private final ChunkGenerator generator;
    private final RandomState randomState;
    private final WorldGenerationContext context;
    private final BiomeResolver biomes;
    private final Map<ChunkPos, BitSet> masks = new HashMap<>();
    private int replayedCarvers;

    SavannaCarverSuitability(
            ChunkGenerator generator, RandomState randomState,
            LevelHeightAccessor heightAccessor
    ) {
        this.generator = generator;
        this.randomState = randomState;
        this.context = new WorldGenerationContext(generator, heightAccessor);
        this.biomes = generator.getBiomeSource()
                .createUncachedResolver(randomState);
    }

    /** Every authored agricultural ground cell needs immediate natural support. */
    boolean carvesFieldSupport(BlockPos ground) {
        int supportY = ground.getY() - 1;
        int minY = context.getMinGenY() + 1;
        int maxY = context.getMinGenY() + context.getGenDepth() - 8;
        if (supportY < minY || supportY > maxY) return false;
        ChunkPos target = new ChunkPos(ground.getX() >> 4, ground.getZ() >> 4);
        return masks.computeIfAbsent(target, this::replayTarget)
                .get(index(ground.getX() & 15, supportY,
                        ground.getZ() & 15, minY));
    }

    int replayedCarvers() {
        return replayedCarvers;
    }

    int targetChunks() {
        return masks.size();
    }

    private BitSet replayTarget(ChunkPos target) {
        int minY = context.getMinGenY() + 1;
        int maxY = context.getMinGenY() + context.getGenDepth() - 8;
        BitSet mask = new BitSet((maxY - minY + 1) * 256);
        CarverOutput output = new CarverOutput() {
            @Override
            public void carve(int x, int y, int z) {
                if (y >= minY && y <= maxY) {
                    mask.set(index(x, y, z, minY));
                }
            }

            @Override public int minY() { return minY; }
            @Override public int maxY() { return maxY; }
        };
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0));
        for (int ox = target.x() - ORIGIN_RADIUS;
                ox <= target.x() + ORIGIN_RADIUS; ox++) {
            for (int oz = target.z() - ORIGIN_RADIUS;
                    oz <= target.z() + ORIGIN_RADIUS; oz++) {
                ChunkPos origin = new ChunkPos(ox, oz);
                var biome = biomes.getNoiseBiome(
                        QuartPos.fromBlock(origin.getMinBlockX()), 0,
                        QuartPos.fromBlock(origin.getMinBlockZ()));
                int index = 0;
                for (var holder : generator.getBiomeGenerationSettings(biome)
                        .getCarvers()) {
                    WorldCarver carver = holder.value();
                    random.setLargeFeatureSeed(
                            randomState.seed() + index++, ox, oz);
                    if (carver.isStartChunk(random)) {
                        replayedCarvers++;
                        carver.carve(context, random, target, origin, output);
                    }
                }
            }
        }
        return mask;
    }

    private static int index(int x, int y, int z, int minY) {
        return ((y - minY) << 8) | (z << 4) | x;
    }
}
