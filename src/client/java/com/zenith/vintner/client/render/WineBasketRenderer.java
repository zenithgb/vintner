package com.zenith.vintner.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zenith.vintner.block.WineBasketBlock;
import com.zenith.vintner.block.entity.WineBasketBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** Renders the exact stored bottle stack as one clean, angled display item. */
public final class WineBasketRenderer implements
        BlockEntityRenderer<WineBasketBlockEntity, WineBasketRenderState> {
    private final ItemModelResolver itemModelResolver;

    public WineBasketRenderer(BlockEntityRendererProvider.Context context) {
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public WineBasketRenderState createRenderState() {
        return new WineBasketRenderState();
    }

    @Override
    public void extractRenderState(
            WineBasketBlockEntity basket, WineBasketRenderState state,
            float partialTick, Vec3 cameraPosition,
            ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(
                basket, state, partialTick, cameraPosition, breakProgress
        );
        state.facing = basket.getBlockState().getValue(WineBasketBlock.FACING);
        ItemStack bottle = basket.getBottleCopy();
        state.bottle.clear();
        if (!WineBasketBlockEntity.isCompatibleBottle(bottle)) {
            return;
        }

        // The actual ItemStack remains authoritative. Rendering it as one
        // silhouette avoids exposing the internal caps of the upright block
        // mesh when that segmented model is laid nearly horizontal.
        itemModelResolver.updateForTopItem(
                state.bottle,
                bottle,
                ItemDisplayContext.NONE,
                basket.getLevel(),
                null,
                0
        );
    }

    @Override
    public void submit(
            WineBasketRenderState state, PoseStack poseStack,
            SubmitNodeCollector collector, CameraRenderState camera
    ) {
        if (state.bottle.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5F, 4.4F / 16.0F, 0.5F);
        // Vanilla blockstate rotations run opposite PoseStack's Y axis.
        poseStack.mulPose(Axis.YP.rotationDegrees(
                180.0F - state.facing.toYRot()
        ));
        // The bottle lies along the long cradle with its neck slightly raised.
        poseStack.mulPose(Axis.XP.rotationDegrees(-72.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.9F, 0.9F, 0.9F);
        state.bottle.submit(
                poseStack,
                collector,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0
        );
        poseStack.popPose();
    }
}
