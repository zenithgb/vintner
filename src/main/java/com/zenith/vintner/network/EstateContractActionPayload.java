package com.zenith.vintner.network;

import com.zenith.vintner.Vintner;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** A bounded request to accept one server-authored estate contract. */
public record EstateContractActionPayload(
        int deskX,
        int deskY,
        int deskZ,
        String contractId
) implements CustomPacketPayload {
    public static final Type<EstateContractActionPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(
                    Vintner.MOD_ID,
                    "estate_contract_action"
            )
    );
    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            EstateContractActionPayload
            > CODEC = new StreamCodec<>() {
        @Override
        public EstateContractActionPayload decode(
                RegistryFriendlyByteBuf buffer
        ) {
            return new EstateContractActionPayload(
                    buffer.readInt(),
                    buffer.readInt(),
                    buffer.readInt(),
                    buffer.readUtf(32)
            );
        }

        @Override
        public void encode(
                RegistryFriendlyByteBuf buffer,
                EstateContractActionPayload payload
        ) {
            buffer.writeInt(payload.deskX());
            buffer.writeInt(payload.deskY());
            buffer.writeInt(payload.deskZ());
            buffer.writeUtf(payload.contractId(), 32);
        }
    };

    public EstateContractActionPayload {
        contractId = contractId == null ? "" : contractId;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
