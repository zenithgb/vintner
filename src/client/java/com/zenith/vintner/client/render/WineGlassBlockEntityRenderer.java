package com.zenith.vintner.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zenith.vintner.block.WineGlassBlock;
import com.zenith.vintner.block.entity.WineGlassBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Renders each stored glass with its real metadata-bearing item model. */
public final class WineGlassBlockEntityRenderer implements
        BlockEntityRenderer<WineGlassBlockEntity, WineGlassRenderState> {
    private final ItemModelResolver itemModelResolver;

    public WineGlassBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public WineGlassRenderState createRenderState() {
        return new WineGlassRenderState();
    }

    @Override
    public void extractRenderState(
            WineGlassBlockEntity blockEntity,
            WineGlassRenderState state,
            float partialTick,
            Vec3 cameraPosition,
            ModelFeatureRenderer.CrumblingOverlay breakingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(
                blockEntity,
                state,
                partialTick,
                cameraPosition,
                breakingOverlay
        );

        List<ItemStack> glasses = blockEntity.getGlasses();
        state.count = Math.min(
                glasses.size(),
                WineGlassBlockEntity.CAPACITY
        );
        state.facing = blockEntity.getBlockState().hasProperty(
                WineGlassBlock.FACING
        )
                ? blockEntity.getBlockState().getValue(WineGlassBlock.FACING)
                : Direction.NORTH;

        for (int index = 0; index < state.glasses.length; index++) {
            ItemStack stack = index < state.count
                    ? glasses.get(index)
                    : ItemStack.EMPTY;
            itemModelResolver.updateForTopItem(
                    state.glasses[index],
                    stack,
                    ItemDisplayContext.FIXED,
                    blockEntity.getLevel(),
                    null,
                    index
            );
        }
    }

    @Override
    public void submit(
            WineGlassRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraState
    ) {
        for (int index = 0; index < state.count; index++) {
            if (state.glasses[index].isEmpty()) {
                continue;
            }

            Vec3 position = WineGlassBlock.layoutPosition(
                    state.count,
                    index,
                    state.facing
            );
            poseStack.pushPose();
            // The crossed sprite is centred around the render origin. Raise it
            // just enough for the foot to rest on the supporting surface.
            poseStack.translate(position.x, 0.23, position.z);
            poseStack.scale(1.05F, 1.05F, 1.05F);
            state.glasses[index].submit(
                    poseStack,
                    collector,
                    state.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    0
            );
            poseStack.popPose();
        }
    }
}
