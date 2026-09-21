package com.zenith.vintner.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ModNetworking {
    private ModNetworking() {
    }

    public static void initialize() {
        PayloadTypeRegistry.clientboundPlay().register(
                AlmanacReportPayload.TYPE,
                AlmanacReportPayload.CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(
                EstateDeskPayload.TYPE,
                EstateDeskPayload.CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
                TastingServicePickPayload.TYPE,
                TastingServicePickPayload.CODEC
        );
        ServerPlayNetworking.registerGlobalReceiver(
                TastingServicePickPayload.TYPE,
                (payload, context) -> TastingServiceCreativePicker.tryPick(
                        context.player(),
                        payload.pos()
                )
        );
    }
}
