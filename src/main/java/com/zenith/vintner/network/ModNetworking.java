package com.zenith.vintner.network;

import com.zenith.vintner.estate.EstateDeskReport;
import com.zenith.vintner.estate.WineContractSavedData;
import com.zenith.vintner.registry.ModBlocks;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

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
                EstateContractActionPayload.TYPE,
                EstateContractActionPayload.CODEC
        );
        ServerPlayNetworking.registerGlobalReceiver(
                EstateContractActionPayload.TYPE,
                (payload, context) -> context.server().execute(() -> {
                    var player = context.player();
                    if (!(player.level() instanceof ServerLevel level)) {
                        return;
                    }
                    BlockPos deskPos = new BlockPos(
                            payload.deskX(),
                            payload.deskY(),
                            payload.deskZ()
                    );
                    if (player.blockPosition().distSqr(deskPos) > 64.0D
                            || !ModBlocks.ESTATE_MANAGEMENT_DESKS
                                    .containsValue(level.getBlockState(
                                            deskPos
                                    ).getBlock())
                            || !EstateDeskReport
                                    .hasNearbyCorrespondenceBoard(
                                            level,
                                            deskPos
                                    )) {
                        player.sendSystemMessage(Component.translatable(
                                "screen.vintner.estate_desk.contract.invalid"
                        ).withStyle(ChatFormatting.RED));
                        return;
                    }

                    var result = WineContractSavedData.get(level).accept(
                            level,
                            player.getUUID(),
                            payload.contractId()
                    );
                    String key = switch (result) {
                        case ACCEPTED -> "accepted";
                        case ALREADY_ACTIVE -> "already_active";
                        case UNAVAILABLE, NOT_FOUND -> "unavailable";
                    };
                    player.sendSystemMessage(Component.translatable(
                            "screen.vintner.estate_desk.contract." + key
                    ).withStyle(result
                            == WineContractSavedData.AcceptResult.ACCEPTED
                            ? ChatFormatting.GREEN
                            : ChatFormatting.RED));
                    EstateDeskReport.open(level, deskPos, player);
                })
        );
    }
}
