package com.zenith.vintner.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class ModNetworking {
    private ModNetworking() {
    }

    public static void initialize() {
        PayloadTypeRegistry.clientboundPlay().register(
                AlmanacReportPayload.TYPE,
                AlmanacReportPayload.CODEC
        );
    }
}
