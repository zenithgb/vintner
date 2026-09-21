package com.zenith.vintner.network;

import com.zenith.vintner.Vintner;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Requests a server-validated Creative pick of a displayed service bottle. */
public record TastingServicePickPayload(BlockPos pos)
        implements CustomPacketPayload {
    public static final Type<TastingServicePickPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(
                    Vintner.MOD_ID,
                    "tasting_service_pick"
            )
    );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            TastingServicePickPayload
            > CODEC = StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    TastingServicePickPayload::pos,
                    TastingServicePickPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
