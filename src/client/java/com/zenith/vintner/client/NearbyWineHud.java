package com.zenith.vintner.client;

import com.zenith.vintner.block.entity.WineBottleBlockEntity;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Draws the canonical wine name while the player closely targets a bottle. */
public final class NearbyWineHud implements HudElement {
    private static final double MAX_DISTANCE_SQUARED = 2.25 * 2.25;
    private static final int TEXT_COLOR = 0xE6FFFFFF;

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            DeltaTracker deltaTracker
    ) {
        Minecraft client = Minecraft.getInstance();
        Component label = targetedWineName(client);

        if (label == null) {
            return;
        }

        graphics.centeredText(
                client.font,
                label,
                graphics.guiWidth() / 2,
                graphics.guiHeight() / 2 + 18,
                TEXT_COLOR
        );
    }

    static Component targetedWineName(Minecraft client) {
        if (client.player == null
                || client.level == null
                || client.gui.screen() != null
                || !(client.hitResult instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK
                || hit.distanceTo(client.player) > MAX_DISTANCE_SQUARED
                || !(client.level.getBlockEntity(hit.getBlockPos())
                instanceof WineBottleBlockEntity bottleEntity)) {
            return null;
        }

        ItemStack bottle = bottleEntity.getBottleCopy();
        return bottle.isEmpty() ? null : bottle.getHoverName();
    }
}
