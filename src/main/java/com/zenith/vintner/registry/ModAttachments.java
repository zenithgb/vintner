package com.zenith.vintner.registry;

import com.zenith.vintner.Vintner;
import com.zenith.vintner.wine.WineConsumptionState;
import com.zenith.vintner.wine.WineFeastState;
import com.zenith.vintner.wine.WinePairingState;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

public final class ModAttachments {
    public static final AttachmentType<WineConsumptionState>
            WINE_CONSUMPTION = AttachmentRegistry.createPersistent(
                    Identifier.fromNamespaceAndPath(
                            Vintner.MOD_ID,
                            "wine_consumption"
                    ),
                    WineConsumptionState.CODEC
            );

    public static final AttachmentType<WinePairingState>
            WINE_PAIRING = AttachmentRegistry.createPersistent(
                    Identifier.fromNamespaceAndPath(
                            Vintner.MOD_ID,
                            "wine_pairing"
                    ),
                    WinePairingState.CODEC
            );

    public static final AttachmentType<WineFeastState>
            WINE_FEAST = AttachmentRegistry.createPersistent(
                    Identifier.fromNamespaceAndPath(
                            Vintner.MOD_ID,
                            "wine_feast"
                    ),
                    WineFeastState.CODEC
            );

    private ModAttachments() {
    }

    public static void initialize() {
    }
}
