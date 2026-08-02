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
        List<Section> sections
) implements CustomPacketPayload {
    private static final int MAX_SECTIONS = 8;
    private static final int MAX_LINES = 32;

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
            return new EstateDeskPayload(
                    estateName,
                    subtitle,
                    sections
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
        }
    };

    public EstateDeskPayload {
        sections = List.copyOf(sections).stream()
                .limit(MAX_SECTIONS)
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
}
