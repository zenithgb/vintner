package com.zenith.vintner.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zenith.vintner.Vintner;
import com.zenith.vintner.block.WineBasketBlock;
import com.zenith.vintner.block.WineDisplayAge;
import com.zenith.vintner.block.entity.WineBasketBlockEntity;
import com.zenith.vintner.wine.WineMetadata;
import com.zenith.vintner.wine.WineStyle;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
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

        // Keep the exact stored stack authoritative, but select a dedicated
        // compact 3D bottle model instead of enlarging its flat inventory icon.
        ItemStack displayBottle = bottle.copy();
        boolean white = WineMetadata.wineStyle(bottle) == WineStyle.WHITE;
        boolean aged = WineDisplayAge.isAged(bottle);
        String style = aged ? (white ? "aged_white" : "aged_red")
                : (white ? "white" : "red");
        displayBottle.set(
                DataComponents.ITEM_MODEL,
                Identifier.fromNamespaceAndPath(
                        Vintner.MOD_ID,
                        "wine_basket_bottle_" + style
                )
        );
        itemModelResolver.updateForTopItem(
                state.bottle,
                displayBottle,
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
        poseStack.translate(0.5F, 3.31243F / 16.0F, 0.5F);
        // Vanilla blockstate rotations run opposite PoseStack's Y axis.
        poseStack.mulPose(Axis.YP.rotationDegrees(
                180.0F - state.facing.toYRot()
        ));
        // User-approved Blockbench pose. Free-format cubes use ZYX Euler
        // order; the last Y rotation maps the authored bottle's long Z axis
        // to the preview's X axis. Keep this whole pose local to the basket
        // so rotating a wood variant preserves the approved placement.
        poseStack.translate(-0.03865F / 16.0F, 0.0F, 2.0F / 16.0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-82.66555F));
        poseStack.mulPose(Axis.YP.rotationDegrees(59.62449F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-80.07501F));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.scale(0.62F, 0.62F, 0.62F);
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
