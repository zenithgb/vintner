package com.zenith.vintner.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zenith.vintner.block.WineGlassBlock;
import com.zenith.vintner.block.entity.WineGlassBlockEntity;
import com.zenith.vintner.item.FilledGobletItem;
import com.zenith.vintner.item.GobletMaterial;
import com.zenith.vintner.wine.WineMetadata;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Renders each metadata-bearing goblet as a continuous metal vessel. */
public final class WineGlassBlockEntityRenderer implements
        BlockEntityRenderer<WineGlassBlockEntity, WineGlassRenderState> {
    private static final int SIDES = 8;
    private static final Identifier PEWTER_TEXTURE =
            Identifier.withDefaultNamespace(
                    "textures/block/iron_block.png"
            );
    private static final Identifier COPPER_TEXTURE =
            Identifier.withDefaultNamespace(
                    "textures/block/copper_block.png"
            );
    private static final Identifier GOLD_TEXTURE =
            Identifier.withDefaultNamespace(
                    "textures/block/gold_block.png"
            );
    private static final Identifier RED_WINE_TEXTURE =
            Identifier.withDefaultNamespace(
                    "textures/block/red_stained_glass.png"
            );
    private static final Identifier WHITE_WINE_TEXTURE =
            Identifier.withDefaultNamespace(
                    "textures/block/yellow_stained_glass.png"
            );

    public WineGlassBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
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

        for (int index = 0; index < state.filled.length; index++) {
            ItemStack stack = index < state.count
                    ? glasses.get(index)
                    : ItemStack.EMPTY;
            state.materials[index] = GobletMaterial.from(stack);
            state.filled[index] = stack.getItem()
                    instanceof FilledGobletItem;
            state.whiteWine[index] = state.filled[index]
                    && WineMetadata
                    .effectProfile(stack)
                    .contains("white");
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
            Vec3 position = WineGlassBlock.layoutPosition(
                    state.count,
                    index,
                    state.facing
            );
            poseStack.pushPose();
            poseStack.translate(position.x, 0.0, position.z);
            collector.submitCustomGeometry(
                    poseStack,
                    RenderTypes.entityTranslucent(
                            metalTexture(state.materials[index])
                    ),
                    (pose, consumer) -> renderGoblet(
                            pose,
                            consumer,
                            state.lightCoords
                    )
            );
            if (state.filled[index]) {
                collector.order(1).submitCustomGeometry(
                        poseStack,
                        RenderTypes.entityTranslucent(
                                state.whiteWine[index]
                                        ? WHITE_WINE_TEXTURE
                                        : RED_WINE_TEXTURE
                        ),
                        (pose, consumer) -> renderWine(
                                pose,
                                consumer,
                                state.lightCoords
                        )
                );
            }
            poseStack.popPose();
        }
    }

    private static Identifier metalTexture(GobletMaterial material) {
        return switch (material) {
            case COPPER -> COPPER_TEXTURE;
            case GOLD -> GOLD_TEXTURE;
            default -> PEWTER_TEXTURE;
        };
    }

    private static void renderGoblet(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light
    ) {
        // A broad octagonal foot and short stem give the opaque vessel a
        // deliberate vanilla-scale goblet silhouette. Hidden joins overlap,
        // and the stem remains uncapped, to avoid coplanar flicker.
        prism(pose, consumer, light, 0.006F, 0.04F, 0.11F, 0.11F, true);
        prism(pose, consumer, light, 0.032F, 0.255F, 0.025F, 0.025F, false);

        // The bowl is one hollow octagonal shell rather than stacked cubes.
        float[] heights = {0.235F, 0.305F, 0.48F, 0.58F};
        float[] outer = {0.05F, 0.09F, 0.155F, 0.16F};
        float[] inner = {0.026F, 0.066F, 0.128F, 0.133F};

        for (int tier = 0; tier < heights.length - 1; tier++) {
            frustumSides(
                    pose,
                    consumer,
                    light,
                    heights[tier],
                    heights[tier + 1],
                    outer[tier],
                    outer[tier + 1],
                    false
            );
            frustumSides(
                    pose,
                    consumer,
                    light,
                    heights[tier],
                    heights[tier + 1],
                    inner[tier],
                    inner[tier + 1],
                    true
            );
        }

        annulus(
                pose,
                consumer,
                light,
                heights[heights.length - 1],
                inner[inner.length - 1],
                outer[outer.length - 1]
        );
        annulus(
                pose,
                consumer,
                light,
                heights[0],
                inner[0],
                outer[0]
        );
        disc(
                pose,
                consumer,
                light,
                heights[0] + 0.006F,
                inner[0],
                false
        );
    }

    private static void renderWine(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light
    ) {
        // The serving sits inside the bowl with a visible metal rim above it.
        frustumSides(
                pose,
                consumer,
                light,
                0.27F,
                0.49F,
                0.03F,
                0.12F,
                false
        );
        disc(pose, consumer, light, 0.49F, 0.12F, false);
        disc(pose, consumer, light, 0.27F, 0.03F, true);
    }

    private static void prism(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            float y0,
            float y1,
            float lowerRadius,
            float upperRadius,
            boolean capped
    ) {
        frustumSides(
                pose,
                consumer,
                light,
                y0,
                y1,
                lowerRadius,
                upperRadius,
                false
        );
        if (capped) {
            disc(pose, consumer, light, y0, lowerRadius, true);
            disc(pose, consumer, light, y1, upperRadius, false);
        }
    }

    private static void frustumSides(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            float y0,
            float y1,
            float lowerRadius,
            float upperRadius,
            boolean inward
    ) {
        for (int side = 0; side < SIDES; side++) {
            float angle0 = angle(side);
            float angle1 = angle(side + 1);
            float x00 = x(lowerRadius, angle0);
            float z00 = z(lowerRadius, angle0);
            float x01 = x(lowerRadius, angle1);
            float z01 = z(lowerRadius, angle1);
            float x10 = x(upperRadius, angle0);
            float z10 = z(upperRadius, angle0);
            float x11 = x(upperRadius, angle1);
            float z11 = z(upperRadius, angle1);
            float normalAngle = (angle0 + angle1) * 0.5F;
            float normalX = (float) Math.cos(normalAngle);
            float normalZ = (float) Math.sin(normalAngle);

            if (inward) {
                quad(
                        pose,
                        consumer,
                        light,
                        x00, y0, z00,
                        x10, y1, z10,
                        x11, y1, z11,
                        x01, y0, z01,
                        -normalX, 0.0F, -normalZ
                );
            } else {
                quad(
                        pose,
                        consumer,
                        light,
                        x01, y0, z01,
                        x11, y1, z11,
                        x10, y1, z10,
                        x00, y0, z00,
                        normalX, 0.0F, normalZ
                );
            }
        }
    }

    private static void annulus(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            float y,
            float innerRadius,
            float outerRadius
    ) {
        for (int side = 0; side < SIDES; side++) {
            float angle0 = angle(side);
            float angle1 = angle(side + 1);
            quad(
                    pose,
                    consumer,
                    light,
                    x(innerRadius, angle0), y, z(innerRadius, angle0),
                    x(innerRadius, angle1), y, z(innerRadius, angle1),
                    x(outerRadius, angle1), y, z(outerRadius, angle1),
                    x(outerRadius, angle0), y, z(outerRadius, angle0),
                    0.0F, 1.0F, 0.0F
            );
        }
    }

    private static void disc(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            float y,
            float radius,
            boolean downward
    ) {
        for (int side = 0; side < SIDES; side++) {
            float angle0 = angle(side);
            float angle1 = angle(side + 1);
            if (downward) {
                quad(
                        pose,
                        consumer,
                        light,
                        0.0F, y, 0.0F,
                        x(radius, angle1), y, z(radius, angle1),
                        x(radius, angle0), y, z(radius, angle0),
                        0.0F, y, 0.0F,
                        0.0F, -1.0F, 0.0F
                );
            } else {
                quad(
                        pose,
                        consumer,
                        light,
                        0.0F, y, 0.0F,
                        x(radius, angle0), y, z(radius, angle0),
                        x(radius, angle1), y, z(radius, angle1),
                        0.0F, y, 0.0F,
                        0.0F, 1.0F, 0.0F
                );
            }
        }
    }

    private static void quad(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float normalX, float normalY, float normalZ
    ) {
        vertex(pose, consumer, light, x0, y0, z0, 0.0F, 1.0F, normalX, normalY, normalZ);
        vertex(pose, consumer, light, x1, y1, z1, 0.0F, 0.0F, normalX, normalY, normalZ);
        vertex(pose, consumer, light, x2, y2, z2, 1.0F, 0.0F, normalX, normalY, normalZ);
        vertex(pose, consumer, light, x3, y3, z3, 1.0F, 1.0F, normalX, normalY, normalZ);
    }

    private static void vertex(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            float x,
            float y,
            float z,
            float u,
            float v,
            float normalX,
            float normalY,
            float normalZ
    ) {
        consumer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, normalX, normalY, normalZ);
    }

    private static float angle(int side) {
        return (float) (Math.PI * 2.0 * side / SIDES + Math.PI / 8.0);
    }

    private static float x(float radius, float angle) {
        return (float) Math.cos(angle) * radius;
    }

    private static float z(float radius, float angle) {
        return (float) Math.sin(angle) * radius;
    }
}
