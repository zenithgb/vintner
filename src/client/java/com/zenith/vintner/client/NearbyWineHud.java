package com.zenith.vintner.client;

import com.zenith.vintner.block.entity.CellarCollectionBlockEntity;
import com.zenith.vintner.block.entity.WineBottleBlockEntity;
import com.zenith.vintner.block.entity.WineBasketBlockEntity;
import com.zenith.vintner.block.entity.WineCrateBlockEntity;
import com.zenith.vintner.block.entity.WineRackBlockEntity;
import com.zenith.vintner.wine.VisibleWineSlotTargeting;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Draws stored wine names while the player closely targets a display. */
public final class NearbyWineHud implements HudElement {
    private static final double MAX_DISTANCE_SQUARED = 2.25 * 2.25;
    private static final int TEXT_COLOR = 0xE6FFFFFF;

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            DeltaTracker deltaTracker
    ) {
        Minecraft client = Minecraft.getInstance();
        List<Component> labels = targetedWineNames(client);

        if (labels.isEmpty()) {
            return;
        }

        int lineHeight = client.font.lineHeight + 1;
        int firstLine = Math.min(
                graphics.guiHeight() / 2 + 18,
                graphics.guiHeight() - labels.size() * lineHeight - 4
        );
        for (int line = 0; line < labels.size(); line++) {
            graphics.centeredText(
                    client.font,
                    labels.get(line),
                    graphics.guiWidth() / 2,
                    firstLine + line * lineHeight,
                    TEXT_COLOR
            );
        }
    }

    static List<Component> targetedWineNames(Minecraft client) {
        if (client.player == null
                || client.level == null
                || client.gui.screen() != null
                || !(client.hitResult instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK
                || hit.distanceTo(client.player) > MAX_DISTANCE_SQUARED
        ) {
            return List.of();
        }

        Vec3 rayStart = client.player.getEyePosition(1.0F);
        Vec3 rayEnd = hit.getLocation().add(
                client.player.getViewVector(1.0F).scale(2.0)
        );
        var state = client.level.getBlockState(hit.getBlockPos());
        int slot = VisibleWineSlotTargeting.targetedSlot(
                state,
                hit.getBlockPos(),
                rayStart,
                rayEnd
        );
        if (slot == VisibleWineSlotTargeting.NO_SLOT) {
            return List.of();
        }

        var blockEntity = client.level.getBlockEntity(hit.getBlockPos());
        ItemStack bottle = ItemStack.EMPTY;
        if (blockEntity instanceof WineBottleBlockEntity bottleEntity) {
            bottle = bottleEntity.getBottleCopy();
        } else if (blockEntity instanceof WineBasketBlockEntity basketEntity) {
            bottle = basketEntity.getBottleCopy();
        } else if (blockEntity instanceof WineRackBlockEntity rackEntity) {
            bottle = rackEntity.getBottleCopy(slot);
        } else if (blockEntity instanceof WineCrateBlockEntity crateEntity) {
            bottle = crateEntity.getBottleCopy(slot);
        } else if (blockEntity
                instanceof CellarCollectionBlockEntity collectionEntity) {
            bottle = collectionEntity.getBottleCopy(slot);
        }
        return bottle.isEmpty()
                ? List.of()
                : List.of(bottle.getHoverName());
    }
}
