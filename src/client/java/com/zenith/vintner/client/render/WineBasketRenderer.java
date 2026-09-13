package com.zenith.vintner.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zenith.vintner.block.WineBasketBlock;
import com.zenith.vintner.block.WineBottleBlock;
import com.zenith.vintner.block.WineDisplayAge;
import com.zenith.vintner.block.entity.WineBasketBlockEntity;
import com.zenith.vintner.registry.ModBlocks;
import com.zenith.vintner.wine.WineMetadata;
import com.zenith.vintner.wine.WineStyle;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Reuses the placed bottle models, including their fill and aged finishes. */
public final class WineBasketRenderer implements
        BlockEntityRenderer<WineBasketBlockEntity, WineBasketRenderState> {
    private static final BlockDisplayContext DISPLAY_CONTEXT =
            BlockDisplayContext.create();
    private final BlockModelResolver blockModelResolver;

    public WineBasketRenderer(BlockEntityRendererProvider.Context context) {
        blockModelResolver = context.blockModelResolver();
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
        // The actual stored stack remains authoritative. Select the same
        // three-dimensional appearance used by placed wine bottles; no new
        // decorative wine identities or duplicate bottle meshes are needed.
        BlockState bottleState = ModBlocks.WINE_BOTTLE.defaultBlockState()
                .setValue(WineBottleBlock.FACING, Direction.NORTH)
                .setValue(WineBottleBlock.SERVINGS, WineMetadata.servings(bottle))
                .setValue(WineBottleBlock.WHITE_WINE,
                        WineMetadata.wineStyle(bottle) == WineStyle.WHITE)
                .setValue(WineBottleBlock.AGED_WINE,
                        WineDisplayAge.isAged(bottle));
        blockModelResolver.update(state.bottle, bottleState, DISPLAY_CONTEXT);
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
        poseStack.translate(0.5F, 0.0F, 0.5F);
        // Vanilla blockstate y rotations run opposite PoseStack's Y axis.
        poseStack.mulPose(Axis.YP.rotationDegrees(
                180.0F - state.facing.toYRot()
        ));
        // North-facing basket: base at (8, 2.65, 11) pixels, neck raised
        // toward north. The body rests just above the 1.3-pixel woven floor.
        poseStack.translate(0.0F, 2.65F / 16.0F, 3.0F / 16.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-62.0F));
        // Turn the canonical front label toward the open top of the cradle.
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(-0.5F, 0.0F, -0.5F);
        state.bottle.submit(poseStack, collector, state.lightCoords,
                OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
