package com.zenith.vintner.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zenith.vintner.block.EstateManagementDeskBlock;
import com.zenith.vintner.block.entity.EstateManagementDeskBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;

public final class EstateManagementDeskRenderer implements
        BlockEntityRenderer<
                EstateManagementDeskBlockEntity,
                EstateManagementDeskRenderState
                > {
    private static final float MAP_SCALE = 0.00275F;
    private final MapRenderer mapRenderer;

    public EstateManagementDeskRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        mapRenderer = Minecraft.getInstance().getMapRenderer();
    }

    @Override
    public EstateManagementDeskRenderState createRenderState() {
        return new EstateManagementDeskRenderState();
    }

    @Override
    public void extractRenderState(
            EstateManagementDeskBlockEntity desk,
            EstateManagementDeskRenderState state,
            float partialTick,
            Vec3 cameraPosition,
            ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(
                desk,
                state,
                partialTick,
                cameraPosition,
                breakProgress
        );
        state.facing = desk.getBlockState().getValue(
                EstateManagementDeskBlock.FACING
        );
        state.hasMap = false;

        ItemStack stack = desk.getMapCopy();
        MapId mapId = stack.get(DataComponents.MAP_ID);
        if (mapId == null || desk.getLevel() == null) {
            return;
        }
        MapItemSavedData mapData = desk.getLevel().getMapData(mapId);
        if (mapData == null) {
            return;
        }

        mapRenderer.extractRenderState(mapId, mapData, state.map);
        state.hasMap = true;
    }

    @Override
    public void submit(
            EstateManagementDeskRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraRenderState
    ) {
        if (!state.hasMap) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.754F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(
                state.facing.toYRot() + 180.0F
        ));

        // Match the multipart model convention (north = zero rotation) and
        // fill the framed left-hand document well. The ledger remains on the
        // other half of the blotter without sharing any surface area.
        poseStack.translate(-0.15625F, 0.004F, -0.109375F);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(MAP_SCALE, MAP_SCALE, MAP_SCALE);
        poseStack.translate(-64.0F, -64.0F, -1.0F);
        mapRenderer.render(
                state.map,
                poseStack,
                submitNodeCollector,
                true,
                state.lightCoords
        );
        poseStack.popPose();
    }
}
