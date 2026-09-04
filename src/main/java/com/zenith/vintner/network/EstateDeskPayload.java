package com.zenith.vintner.network;

import com.zenith.vintner.Vintner;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/** A bounded, server-authored snapshot for the Estate Management Desk. */
public record EstateDeskPayload(
        Component estateName,
        Component subtitle,
        List<Section> sections,
        List<MapInfo> maps,
        boolean atlasConnected,
        List<PlotSummary> plots
) implements CustomPacketPayload {
    private static final int MAX_SECTIONS = 8;
    private static final int MAX_LINES = 32;
    private static final int MAX_PLOTS = 32;
    private static final int MAX_MAPS = 9;

    public static final Type<EstateDeskPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(
                    Vintner.MOD_ID,
                    "estate_desk"
            )
    );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            EstateDeskPayload
            > CODEC = new StreamCodec<>() {
        @Override
        public EstateDeskPayload decode(RegistryFriendlyByteBuf buffer) {
            Component estateName = ComponentSerialization.STREAM_CODEC
                    .decode(buffer);
            Component subtitle = ComponentSerialization.STREAM_CODEC
                    .decode(buffer);
            int sectionCount = readBoundedCount(
                    buffer,
                    MAX_SECTIONS,
                    "sections"
            );
            List<Section> sections = new ArrayList<>(sectionCount);
            for (int sectionIndex = 0;
                    sectionIndex < sectionCount;
                    sectionIndex++) {
                Component title = ComponentSerialization.STREAM_CODEC
                        .decode(buffer);
                int lineCount = readBoundedCount(
                        buffer,
                        MAX_LINES,
                        "section lines"
                );
                List<Component> lines = new ArrayList<>(lineCount);
                for (int lineIndex = 0;
                        lineIndex < lineCount;
                        lineIndex++) {
                    lines.add(ComponentSerialization.STREAM_CODEC
                            .decode(buffer));
                }
                sections.add(new Section(title, lines));
            }
            int mapCount = readBoundedCount(buffer, MAX_MAPS, "maps");
            List<MapInfo> maps = new ArrayList<>(mapCount);
            for (int index = 0; index < mapCount; index++) {
                maps.add(new MapInfo(
                        buffer.readVarInt(),
                        buffer.readInt(),
                        buffer.readInt(),
                        buffer.readByte(),
                        buffer.readUtf(128)
                ));
            }
            boolean atlasConnected = buffer.readBoolean();
            int plotCount = readBoundedCount(
                    buffer,
                    MAX_PLOTS,
                    "plots"
            );
            List<PlotSummary> plots = new ArrayList<>(plotCount);
            for (int index = 0; index < plotCount; index++) {
                plots.add(new PlotSummary(
                        buffer.readUtf(64),
                        buffer.readUtf(128),
                        buffer.readBoolean(),
                        buffer.readInt(),
                        buffer.readInt(),
                        buffer.readInt(),
                        buffer.readInt(),
                        buffer.readVarInt(),
                        buffer.readVarInt(),
                        buffer.readUtf(32),
                        buffer.readVarInt(),
                        buffer.readVarInt(),
                        buffer.readVarInt(),
                        buffer.readVarInt()
                ));
            }
            return new EstateDeskPayload(
                    estateName,
                    subtitle,
                    sections,
                    maps,
                    atlasConnected,
                    plots
            );
        }

        @Override
        public void encode(
                RegistryFriendlyByteBuf buffer,
                EstateDeskPayload payload
        ) {
            ComponentSerialization.STREAM_CODEC.encode(
                    buffer,
                    payload.estateName()
            );
            ComponentSerialization.STREAM_CODEC.encode(
                    buffer,
                    payload.subtitle()
            );
            buffer.writeVarInt(payload.sections().size());
            for (Section section : payload.sections()) {
                ComponentSerialization.STREAM_CODEC.encode(
                        buffer,
                        section.title()
                );
                buffer.writeVarInt(section.lines().size());
                for (Component line : section.lines()) {
                    ComponentSerialization.STREAM_CODEC.encode(buffer, line);
                }
            }
            buffer.writeVarInt(payload.maps().size());
            for (MapInfo map : payload.maps()) {
                buffer.writeVarInt(map.id());
                buffer.writeInt(map.centerX());
                buffer.writeInt(map.centerZ());
                buffer.writeByte(map.scale());
                buffer.writeUtf(map.dimension(), 128);
            }
            buffer.writeBoolean(payload.atlasConnected());
            buffer.writeVarInt(payload.plots().size());
            for (PlotSummary plot : payload.plots()) {
                buffer.writeUtf(plot.name(), 64);
                buffer.writeUtf(plot.dimension(), 128);
                buffer.writeBoolean(plot.loaded());
                buffer.writeInt(plot.minX());
                buffer.writeInt(plot.minZ());
                buffer.writeInt(plot.maxX());
                buffer.writeInt(plot.maxZ());
                buffer.writeVarInt(plot.area());
                buffer.writeVarInt(plot.vineCount());
                buffer.writeUtf(plot.variety(), 32);
                buffer.writeVarInt(plot.health());
                buffer.writeVarInt(plot.projectedYield());
                buffer.writeVarInt(plot.projectedQuality());
                buffer.writeVarInt(plot.irrigation());
            }
        }
    };

    public EstateDeskPayload {
        sections = List.copyOf(sections).stream()
                .limit(MAX_SECTIONS)
                .toList();
        maps = List.copyOf(maps == null ? List.of() : maps).stream()
                .limit(MAX_MAPS)
                .toList();
        plots = List.copyOf(plots).stream()
                .limit(MAX_PLOTS)
                .toList();
    }

    private static int readBoundedCount(
            RegistryFriendlyByteBuf buffer,
            int maximum,
            String label
    ) {
        int count = buffer.readVarInt();
        if (count < 0 || count > maximum) {
            throw new IllegalArgumentException(
                    "Estate desk payload has invalid "
                            + label
                            + " count: "
                            + count
            );
        }
        return count;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record Section(Component title, List<Component> lines) {
        public Section {
            lines = List.copyOf(lines).stream()
                    .limit(MAX_LINES)
                    .toList();
        }
    }

    public record MapInfo(
            int id,
            int centerX,
            int centerZ,
            byte scale,
            String dimension
    ) {
        public MapInfo {
            scale = (byte) Math.clamp(scale, 0, 4);
            dimension = dimension == null
                    ? "minecraft:overworld"
                    : dimension;
        }
    }

    /** Compact, read-only operational data used by the map workspace. */
    public record PlotSummary(
            String name,
            String dimension,
            boolean loaded,
            int minX,
            int minZ,
            int maxX,
            int maxZ,
            int area,
            int vineCount,
            String variety,
            int health,
            int projectedYield,
            int projectedQuality,
            int irrigation
    ) {
        public PlotSummary {
            name = name == null ? "Vineyard" : name;
            dimension = dimension == null
                    ? "minecraft:overworld"
                    : dimension;
            variety = variety == null ? "Unplanted" : variety;
            area = Math.max(0, area);
            vineCount = Math.max(0, vineCount);
            health = Math.clamp(health, 0, 100);
            projectedYield = Math.max(0, projectedYield);
            projectedQuality = Math.clamp(projectedQuality, 0, 100);
            irrigation = Math.clamp(irrigation, 0, 100);
        }

        public int width() {
            return maxX - minX + 1;
        }

        public int depth() {
            return maxZ - minZ + 1;
        }
    }
}
