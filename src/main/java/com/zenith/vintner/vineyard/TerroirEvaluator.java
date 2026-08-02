package com.zenith.vintner.vineyard;

import com.zenith.vintner.block.GrapevineBlock;
import com.zenith.vintner.block.TrellisBlock;
import com.zenith.vintner.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumMap;
import java.util.Map;

public final class TerroirEvaluator {
    private static final int SOIL_RADIUS = 3;
    private static final int TERRAIN_RADIUS = 4;
    private static final int WATER_RADIUS = 12;

    private TerroirEvaluator() {
    }

    public static TerroirReport inspect(
            Level level,
            BlockPos requestedPos
    ) {
        BlockPos sitePos = resolveSitePosition(level, requestedPos);
        TerrainSample terrainSample = sampleTerrain(level, sitePos);
        int waterDistance = findWaterDistance(level, sitePos);
        boolean nearbyWater = waterDistance <= WATER_RADIUS;
        SoilType soilType = detectSoil(level, sitePos, nearbyWater);
        SoilProfile soil = SoilProfile.of(soilType);

        TerrainProfile terrain = TerrainProfile.evaluate(
                sitePos.getY(),
                terrainSample.heightRange(),
                terrainSample.aspect(),
                level.canSeeSky(sitePos.above(2)),
                waterDistance,
                terrainSample.windExposure(),
                terrainSample.frostPocket(),
                terrainSample.terraced()
        );
        ClimateProfile climate = ClimateProfile.evaluate(
                level.getBiome(sitePos).value().getBaseTemperature(),
                level.getBiome(sitePos).value().hasPrecipitation(),
                nearbyWater,
                sitePos.getY(),
                terrain.frostPocket()
        );
        int siteScore = (
                climate.suitability() * 40
                        + soil.suitability() * 35
                        + terrain.suitability() * 25
        ) / 100;

        return new TerroirReport(
                climate,
                soil,
                terrain,
                siteScore
        );
    }

    public static BlockPos resolveSitePosition(
            Level level,
            BlockPos requestedPos
    ) {
        BlockState state = level.getBlockState(requestedPos);

        if (state.getBlock() instanceof GrapevineBlock
                && state.getValue(GrapevineBlock.UPPER)) {
            return requestedPos.below();
        }

        if (state.getBlock() instanceof TrellisBlock) {
            BlockPos cursor = requestedPos;
            for (int step = 0; step < 8; step++) {
                if (!(level.getBlockState(cursor.below()).getBlock()
                        instanceof TrellisBlock)) {
                    break;
                }
                cursor = cursor.below();
            }
            return cursor;
        }

        return requestedPos.above();
    }

    public static SoilType detectSoil(
            Level level,
            BlockPos sitePos,
            boolean nearbyWater
    ) {
        Map<SoilType, Integer> counts = new EnumMap<>(SoilType.class);

        for (int x = -SOIL_RADIUS; x <= SOIL_RADIUS; x++) {
            for (int z = -SOIL_RADIUS; z <= SOIL_RADIUS; z++) {
                SoilType sample = findSoilInColumn(
                        level,
                        sitePos.offset(x, 0, z)
                );
                if (sample != null) {
                    counts.merge(sample, 1, Integer::sum);
                }
            }
        }

        SoilType dominant = SoilType.LOAM;
        int dominantCount = -1;
        for (SoilType type : SoilType.values()) {
            int count = counts.getOrDefault(type, 0);
            if (count > dominantCount) {
                dominant = type;
                dominantCount = count;
            }
        }

        int sampledColumns = counts.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        int alluvialMaterial = counts.getOrDefault(SoilType.LOAM, 0)
                + counts.getOrDefault(SoilType.CLAY, 0)
                + counts.getOrDefault(SoilType.SAND, 0);
        boolean sedimentarySurface = dominant == SoilType.LOAM
                || dominant == SoilType.CLAY
                || dominant == SoilType.SAND
                || dominant == SoilType.ALLUVIAL;
        if (nearbyWater
                && sedimentarySurface
                && alluvialMaterial >= Math.max(4, sampledColumns / 2)) {
            return SoilType.ALLUVIAL;
        }

        return dominant;
    }

    public static SoilType classifySoil(BlockState state) {
        if (state.is(ModBlocks.VINEYARD_SOIL)) {
            return null;
        }
        if (state.is(Blocks.CLAY)) {
            return SoilType.CLAY;
        }
        if (state.is(Blocks.DRIPSTONE_BLOCK)) {
            return SoilType.LIMESTONE;
        }
        if (state.is(Blocks.CALCITE)) {
            return SoilType.CHALK;
        }
        if (state.is(Blocks.GRAVEL)) {
            return SoilType.GRAVEL;
        }
        if (state.is(Blocks.SAND)
                || state.is(Blocks.RED_SAND)
                || state.is(Blocks.SANDSTONE)
                || state.is(Blocks.RED_SANDSTONE)) {
            return SoilType.SAND;
        }
        if (state.is(Blocks.BASALT)
                || state.is(Blocks.SMOOTH_BASALT)
                || state.is(Blocks.BLACKSTONE)
                || state.is(Blocks.TUFF)
                || state.is(Blocks.MAGMA_BLOCK)) {
            return SoilType.VOLCANIC;
        }
        if (state.is(Blocks.MUD)
                || state.is(Blocks.PACKED_MUD)) {
            return SoilType.ALLUVIAL;
        }
        if (state.is(Blocks.DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.FARMLAND)
                || state.is(Blocks.DIRT_PATH)) {
            return SoilType.LOAM;
        }
        return null;
    }

    public static boolean canProbe(BlockState state) {
        return state.is(ModBlocks.VINEYARD_SOIL)
                || classifySoil(state) != null;
    }

    private static SoilType findSoilInColumn(
            Level level,
            BlockPos column
    ) {
        for (int y = column.getY() + 3;
             y >= column.getY() - 7;
             y--) {
            SoilType type = classifySoil(
                    level.getBlockState(
                            new BlockPos(column.getX(), y, column.getZ())
                    )
            );
            if (type != null) {
                return type;
            }
        }
        return null;
    }

    private static TerrainSample sampleTerrain(
            Level level,
            BlockPos sitePos
    ) {
        int north = surfaceY(level, sitePos.north(TERRAIN_RADIUS));
        int east = surfaceY(level, sitePos.east(TERRAIN_RADIUS));
        int south = surfaceY(level, sitePos.south(TERRAIN_RADIUS));
        int west = surfaceY(level, sitePos.west(TERRAIN_RADIUS));
        int center = surfaceY(level, sitePos);

        int min = Math.min(
                center,
                Math.min(Math.min(north, east), Math.min(south, west))
        );
        int max = Math.max(
                center,
                Math.max(Math.max(north, east), Math.max(south, west))
        );
        Direction aspect = lowestDirection(north, east, south, west);
        int exposedSides = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos check = sitePos.relative(direction, 3).above(2);
            if (level.canSeeSky(check)) {
                exposedSides++;
            }
        }
        int windExposure = Math.clamp(
                20 + exposedSides * 18 + Math.max(0, sitePos.getY() - 80),
                0,
                100
        );
        int surroundingAverage = (north + east + south + west) / 4;
        boolean frostPocket = center + 1 < surroundingAverage;
        int oneBlockSteps = 0;
        for (int height : new int[]{north, east, south, west}) {
            if (Math.abs(height - center) == 1) {
                oneBlockSteps++;
            }
        }
        boolean terraced = oneBlockSteps >= 2 && max - min <= 3;

        return new TerrainSample(
                max - min,
                aspect,
                windExposure,
                frostPocket,
                terraced
        );
    }

    private static int surfaceY(Level level, BlockPos around) {
        for (int y = around.getY() + 4;
             y >= around.getY() - 8;
             y--) {
            BlockState state = level.getBlockState(
                    new BlockPos(around.getX(), y, around.getZ())
            );
            if (classifySoil(state) != null) {
                return y + 1;
            }
        }
        return around.getY();
    }

    private static Direction lowestDirection(
            int north,
            int east,
            int south,
            int west
    ) {
        Direction direction = Direction.NORTH;
        int lowest = north;
        if (east < lowest) {
            direction = Direction.EAST;
            lowest = east;
        }
        if (south < lowest) {
            direction = Direction.SOUTH;
            lowest = south;
        }
        if (west < lowest) {
            direction = Direction.WEST;
        }
        return direction;
    }

    private static int findWaterDistance(
            Level level,
            BlockPos sitePos
    ) {
        int closest = WATER_RADIUS + 1;
        for (int x = -WATER_RADIUS; x <= WATER_RADIUS; x++) {
            for (int z = -WATER_RADIUS; z <= WATER_RADIUS; z++) {
                int distance = Math.abs(x) + Math.abs(z);
                if (distance >= closest || distance > WATER_RADIUS) {
                    continue;
                }
                for (int y = -3; y <= 2; y++) {
                    if (level.getBlockState(sitePos.offset(x, y, z))
                            .is(Blocks.WATER)) {
                        closest = distance;
                        break;
                    }
                }
            }
        }
        return closest;
    }

    private record TerrainSample(
            int heightRange,
            Direction aspect,
            int windExposure,
            boolean frostPocket,
            boolean terraced
    ) {
    }
}
