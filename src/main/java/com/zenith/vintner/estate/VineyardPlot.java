package com.zenith.vintner.estate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** A named, player-owned rectangular vineyard boundary. */
public record VineyardPlot(
        String ownerId,
        String name,
        String dimension,
        int minX,
        int minZ,
        int maxX,
        int maxZ,
        int anchorY,
        long createdDay
) {
    public static final int MAX_SIDE = 32;
    public static final Codec<VineyardPlot> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("owner_id")
                            .forGetter(VineyardPlot::ownerId),
                    Codec.STRING.fieldOf("name")
                            .forGetter(VineyardPlot::name),
                    Codec.STRING.fieldOf("dimension")
                            .forGetter(VineyardPlot::dimension),
                    Codec.INT.fieldOf("min_x")
                            .forGetter(VineyardPlot::minX),
                    Codec.INT.fieldOf("min_z")
                            .forGetter(VineyardPlot::minZ),
                    Codec.INT.fieldOf("max_x")
                            .forGetter(VineyardPlot::maxX),
                    Codec.INT.fieldOf("max_z")
                            .forGetter(VineyardPlot::maxZ),
                    Codec.INT.fieldOf("anchor_y")
                            .forGetter(VineyardPlot::anchorY),
                    Codec.LONG.fieldOf("created_day")
                            .forGetter(VineyardPlot::createdDay)
            ).apply(instance, VineyardPlot::new));

    public VineyardPlot {
        ownerId = ownerId == null ? "" : ownerId;
        name = EstateProfile.sanitizeName(name);
        dimension = dimension == null
                ? "minecraft:overworld"
                : dimension;
        int normalizedMinX = Math.min(minX, maxX);
        int normalizedMinZ = Math.min(minZ, maxZ);
        int normalizedMaxX = Math.max(minX, maxX);
        int normalizedMaxZ = Math.max(minZ, maxZ);
        minX = normalizedMinX;
        minZ = normalizedMinZ;
        maxX = normalizedMaxX;
        maxZ = normalizedMaxZ;
        createdDay = Math.max(0L, createdDay);
    }

    public static VineyardPlot create(
            ServerPlayer owner,
            ServerLevel level,
            BlockPos first,
            BlockPos second,
            String name
    ) {
        return new VineyardPlot(
                owner.getUUID().toString(),
                name,
                level.dimension().identifier().toString(),
                first.getX(),
                first.getZ(),
                second.getX(),
                second.getZ(),
                Math.floorDiv(first.getY() + second.getY(), 2),
                Math.floorDiv(level.getOverworldClockTime(), 24000L)
        );
    }

    public int width() {
        return maxX - minX + 1;
    }

    public int depth() {
        return maxZ - minZ + 1;
    }

    public int area() {
        return width() * depth();
    }

    public boolean validSize() {
        return width() <= MAX_SIDE && depth() <= MAX_SIDE;
    }

    public boolean contains(String targetDimension, BlockPos pos) {
        return dimension.equals(targetDimension)
                && pos.getX() >= minX
                && pos.getX() <= maxX
                && pos.getZ() >= minZ
                && pos.getZ() <= maxZ;
    }

    public BlockPos center() {
        return new BlockPos(
                Math.floorDiv(minX + maxX, 2),
                anchorY,
                Math.floorDiv(minZ + maxZ, 2)
        );
    }
}
