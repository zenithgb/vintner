package com.zenith.vintner.registry;

import com.zenith.vintner.block.VillageContractBoardBlock;
import com.zenith.vintner.block.WoodVariant;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/** Places one persistent public contract board beside a loaded village bell. */
public final class VillageContractBoardPlacement {
    private static final int RETRY_TICKS = 20;
    private static final int MAX_ATTEMPTS = 30;
    private static final int EXISTING_BOARD_RADIUS = 24;
    private static final Map<ResourceKey<Level>, Map<BlockPos, PendingBell>>
            PENDING = new HashMap<>();

    private VillageContractBoardPlacement() {
    }

    public static void initialize() {
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register(
                (blockEntity, level) -> {
                    if (blockEntity instanceof BellBlockEntity) {
                        PENDING.computeIfAbsent(
                                level.dimension(),
                                ignored -> new LinkedHashMap<>()
                        ).putIfAbsent(
                                blockEntity.getBlockPos().immutable(),
                                new PendingBell(0, 0)
                        );
                    }
                }
        );
        ServerTickEvents.END_SERVER_TICK.register(
                VillageContractBoardPlacement::processPending
        );
        ServerLifecycleEvents.SERVER_STOPPED.register(
                ignored -> PENDING.clear()
        );
    }

    private static void processPending(MinecraftServer server) {
        for (var dimensionEntry : PENDING.entrySet()) {
            ServerLevel level = server.getLevel(dimensionEntry.getKey());
            if (level == null) {
                continue;
            }

            Iterator<Map.Entry<BlockPos, PendingBell>> iterator =
                    dimensionEntry.getValue().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<BlockPos, PendingBell> entry = iterator.next();
                BlockPos bellPos = entry.getKey();
                PendingBell pending = entry.getValue();

                if (server.getTickCount() < pending.nextTick()) {
                    continue;
                }
                if (!(level.getBlockEntity(bellPos)
                        instanceof BellBlockEntity)) {
                    iterator.remove();
                    continue;
                }
                if (!level.isVillage(bellPos)) {
                    retryOrRemove(server, entry, iterator, pending);
                    continue;
                }
                if (hasNearbyBoard(level, bellPos)) {
                    iterator.remove();
                    continue;
                }
                if (placeBoard(level, bellPos)) {
                    iterator.remove();
                    continue;
                }
                retryOrRemove(server, entry, iterator, pending);
            }
        }
        PENDING.values().removeIf(Map::isEmpty);
    }

    private static void retryOrRemove(
            MinecraftServer server,
            Map.Entry<BlockPos, PendingBell> entry,
            Iterator<Map.Entry<BlockPos, PendingBell>> iterator,
            PendingBell pending
    ) {
        int attempts = pending.attempts() + 1;
        if (attempts >= MAX_ATTEMPTS) {
            iterator.remove();
        } else {
            entry.setValue(new PendingBell(
                    attempts,
                    server.getTickCount() + RETRY_TICKS
            ));
        }
    }

    private static boolean hasNearbyBoard(
            ServerLevel level,
            BlockPos bellPos
    ) {
        for (BlockPos candidate : BlockPos.betweenClosed(
                bellPos.offset(
                        -EXISTING_BOARD_RADIUS,
                        -4,
                        -EXISTING_BOARD_RADIUS
                ),
                bellPos.offset(
                        EXISTING_BOARD_RADIUS,
                        4,
                        EXISTING_BOARD_RADIUS
                )
        )) {
            if (!level.hasChunkAt(candidate)) {
                continue;
            }
            if (level.getBlockState(candidate)
                    .is(ModBlocks.VILLAGE_CONTRACT_BOARD)) {
                return true;
            }
        }
        return false;
    }

    private static boolean placeBoard(
            ServerLevel level,
            BlockPos bellPos
    ) {
        for (int radius = 2; radius <= 5; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }
                    for (int yOffset : new int[]{0, -1, 1}) {
                        BlockPos candidate = bellPos.offset(
                                dx,
                                yOffset,
                                dz
                        );
                        if (!level.hasChunkAt(candidate)) {
                            continue;
                        }
                        if (!isSafePlacement(level, candidate)) {
                            continue;
                        }
                        Direction facing = directionToward(
                                candidate,
                                bellPos
                        );
                        BlockState board = ModBlocks.VILLAGE_CONTRACT_BOARD
                                .defaultBlockState()
                                .setValue(
                                        VillageContractBoardBlock.FACING,
                                        facing
                                )
                                .setValue(
                                        VillageContractBoardBlock.WOOD,
                                        woodFor(level, bellPos)
                                );
                        return level.setBlock(candidate, board, 3);
                    }
                }
            }
        }
        return false;
    }

    private static Direction directionToward(
            BlockPos from,
            BlockPos target
    ) {
        int dx = target.getX() - from.getX();
        int dz = target.getZ() - from.getZ();
        if (Math.abs(dx) >= Math.abs(dz)) {
            return dx >= 0 ? Direction.EAST : Direction.WEST;
        }
        return dz >= 0 ? Direction.SOUTH : Direction.NORTH;
    }

    private static boolean isSafePlacement(
            ServerLevel level,
            BlockPos candidate
    ) {
        BlockState existing = level.getBlockState(candidate);
        BlockPos groundPos = candidate.below();
        BlockState ground = level.getBlockState(groundPos);
        return existing.canBeReplaced()
                && existing.getFluidState().isEmpty()
                && ground.isFaceSturdy(
                        level,
                        groundPos,
                        Direction.UP
                );
    }

    private static WoodVariant woodFor(
            ServerLevel level,
            BlockPos pos
    ) {
        String biome = level.getBiome(pos).unwrapKey()
                .map(key -> key.identifier().getPath())
                .orElse("");
        if (biome.contains("desert")
                || biome.contains("badlands")
                || biome.contains("savanna")) {
            return WoodVariant.ACACIA;
        }
        if (biome.contains("snow")
                || biome.contains("frozen")
                || biome.contains("taiga")) {
            return WoodVariant.SPRUCE;
        }
        return WoodVariant.OAK;
    }

    private record PendingBell(int attempts, int nextTick) {
    }
}
