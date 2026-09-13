package com.zenith.vintner.client;

import com.zenith.vintner.block.CellarFixtureKind;
import com.zenith.vintner.block.entity.CellarCollectionBlockEntity;
import com.zenith.vintner.block.entity.WineBottleBlockEntity;
import com.zenith.vintner.block.entity.WineBasketBlockEntity;
import com.zenith.vintner.block.entity.WineRackBlockEntity;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

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

        var blockEntity = client.level.getBlockEntity(hit.getBlockPos());
        if (blockEntity instanceof WineBottleBlockEntity bottleEntity) {
            return wineNames(List.of(bottleEntity.getBottleCopy()));
        }
        if (blockEntity instanceof WineBasketBlockEntity basketEntity) {
            return wineNames(List.of(basketEntity.getBottleCopy()));
        }
        if (blockEntity instanceof WineRackBlockEntity rackEntity) {
            return wineNames(rackEntity.getStoredBottlesCopy());
        }
        if (blockEntity instanceof CellarCollectionBlockEntity collectionEntity
                && collectionEntity.getKind()
                == CellarFixtureKind.TASTING_CABINET) {
            return wineNames(collectionEntity.getStoredBottlesCopy());
        }
        return List.of();
    }

    private static List<Component> wineNames(List<ItemStack> bottles) {
        return bottles.stream()
                .filter(bottle -> !bottle.isEmpty())
                .map(ItemStack::getHoverName)
                .toList();
    }
}
