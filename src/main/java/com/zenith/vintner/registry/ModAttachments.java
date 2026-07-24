package com.zenith.vintner.registry;

import com.zenith.vintner.Vintner;
import com.zenith.vintner.wine.WineConsumptionState;
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

    private ModAttachments() {
    }

    public static void initialize() {
    }
}
