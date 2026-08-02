package com.zenith.vintner.network;

import com.zenith.vintner.Vintner;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

/** Opens a server-authored Almanac report in the vanilla book screen. */
public record AlmanacReportPayload(List<Component> pages)
        implements CustomPacketPayload {
    public static final Type<AlmanacReportPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(
                    Vintner.MOD_ID,
                    "almanac_report"
            )
    );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            AlmanacReportPayload
            > CODEC = StreamCodec.composite(
                    ComponentSerialization.STREAM_CODEC.apply(
                            ByteBufCodecs.list(32)
                    ),
                    AlmanacReportPayload::pages,
                    AlmanacReportPayload::new
            );

    public AlmanacReportPayload {
        pages = List.copyOf(pages);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
