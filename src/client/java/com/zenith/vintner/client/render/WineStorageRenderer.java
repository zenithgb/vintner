package com.zenith.vintner.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zenith.vintner.Vintner;
import com.zenith.vintner.block.CellarFixtureKind;
import com.zenith.vintner.block.entity.CellarCollectionBlockEntity;
import com.zenith.vintner.block.entity.WineCrateBlockEntity;
import com.zenith.vintner.block.entity.WineRackBlockEntity;
import com.zenith.vintner.wine.WineBottleAppearance;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Shared per-slot renderer for wine storage furniture.
 *
 * <p>Furniture stays in the block model. Each visible bottle comes directly
 * from the synchronized block-entity slot, so mixed red/white and ordinary/
 * aged contents no longer get approximated by one aggregate block state.</p>
 */
public final class WineStorageRenderer<T extends BlockEntity> implements
        BlockEntityRenderer<T, WineStorageRenderState> {
    private final ItemModelResolver itemModelResolver;
    private final int capacity;
    private final BiFunction<T, Integer, ItemStack> bottleAt;
    private final Function<T, Layout> layoutFor;

    private WineStorageRenderer(
            BlockEntityRendererProvider.Context context,
            int capacity,
            BiFunction<T, Integer, ItemStack> bottleAt,
            Function<T, Layout> layoutFor
    ) {
        itemModelResolver = context.itemModelResolver();
        this.capacity = capacity;
        this.bottleAt = bottleAt;
        this.layoutFor = layoutFor;
    }

    public static WineStorageRenderer<WineRackBlockEntity> rack(
            BlockEntityRendererProvider.Context context
    ) {
        return new WineStorageRenderer<>(
                context,
                WineRackBlockEntity.CAPACITY,
                WineRackBlockEntity::getBottleCopy,
                ignored -> Layout.RACK
        );
    }

    public static WineStorageRenderer<WineCrateBlockEntity> crate(
            BlockEntityRendererProvider.Context context
    ) {
        return new WineStorageRenderer<>(
                context,
                WineCrateBlockEntity.CAPACITY,
                WineCrateBlockEntity::getBottleCopy,
                ignored -> Layout.CRATE
        );
    }

    public static WineStorageRenderer<CellarCollectionBlockEntity>
    cellarCollection(BlockEntityRendererProvider.Context context) {
        return new WineStorageRenderer<>(
                context,
                CellarCollectionBlockEntity.MAX_CAPACITY,
                CellarCollectionBlockEntity::getBottleCopy,
                collection -> collection.getKind()
                        == CellarFixtureKind.TASTING_CABINET
                        ? Layout.TASTING_CABINET
                        : Layout.LABELLED_SHELF
        );
    }

    @Override
    public WineStorageRenderState createRenderState() {
        return new WineStorageRenderState();
    }

    @Override
    public void extractRenderState(
            T storage,
            WineStorageRenderState state,
            float partialTick,
            Vec3 cameraPosition,
            ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(
                storage,
                state,
                partialTick,
                cameraPosition,
                breakProgress
        );
        state.facing = storage.getBlockState().getValue(
                BlockStateProperties.HORIZONTAL_FACING
        );
        state.layout = layoutFor.apply(storage);

        for (ItemStackRenderState bottleState : state.bottles) {
            bottleState.clear();
        }

        for (int slot = 0; slot < capacity; slot++) {
            ItemStack bottle = bottleAt.apply(storage, slot);
            if (bottle.isEmpty()) {
                continue;
            }

            ItemStack displayBottle = bottle.copy();
            WineBottleAppearance appearance =
                    WineBottleAppearance.from(bottle);
            displayBottle.set(
                    DataComponents.ITEM_MODEL,
                    Identifier.fromNamespaceAndPath(
                            Vintner.MOD_ID,
                            appearance.itemModelId()
                    )
            );
            itemModelResolver.updateForTopItem(
                    state.bottles[slot],
                    displayBottle,
                    ItemDisplayContext.NONE,
                    storage.getLevel(),
                    null,
                    slot
            );
        }
    }

    @Override
    public void submit(
            WineStorageRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera
    ) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(
                180.0F - state.facing.toYRot()
        ));

        for (int slot = 0; slot < state.bottles.length; slot++) {
            ItemStackRenderState bottle = state.bottles[slot];
            if (bottle.isEmpty() || slot >= state.layout.capacity) {
                continue;
            }
            state.layout.submit(slot, bottle, poseStack, collector, state);
        }
        poseStack.popPose();
    }

    enum Layout {
        RACK(4, 0.80F) {
            @Override
            Position position(int slot) {
                int column = slot % 2;
                int shelf = slot / 2;
                return new Position(
                        column == 0 ? 5.0F : 11.0F,
                        shelf == 0 ? 2.68F : 10.18F,
                        7.04F,
                        false
                );
            }
        },
        CRATE(16, 0.54F) {
            private static final float[] CENTRES = {
                    3.0F, 6.33F, 9.67F, 13.0F
            };

            @Override
            Position position(int slot) {
                return new Position(
                        CENTRES[slot % 4],
                        5.0F,
                        CENTRES[slot / 4],
                        true
                );
            }
        },
        LABELLED_SHELF(8, 0.80F) {
            @Override
            Position position(int slot) {
                return fixturePosition(slot, 3.18F, 10.18F);
            }
        },
        TASTING_CABINET(8, 0.80F) {
            @Override
            Position position(int slot) {
                return fixturePosition(slot, 2.68F, 10.18F);
            }
        };

        private static final float[] FIXTURE_CENTRES = {
                3.75F, 6.25F, 9.75F, 12.25F
        };

        private final int capacity;
        private final float scale;

        Layout(int capacity, float scale) {
            this.capacity = capacity;
            this.scale = scale;
        }

        abstract Position position(int slot);

        private static Position fixturePosition(
                int slot,
                float lowerY,
                float upperY
        ) {
            return new Position(
                    FIXTURE_CENTRES[slot % 4],
                    slot < 4 ? lowerY : upperY,
                    6.69F,
                    false
            );
        }

        private void submit(
                int slot,
                ItemStackRenderState bottle,
                PoseStack poseStack,
                SubmitNodeCollector collector,
                WineStorageRenderState state
        ) {
            Position position = position(slot);
            poseStack.pushPose();
            poseStack.translate(
                    position.x / 16.0F - 0.5F,
                    position.y / 16.0F,
                    position.z / 16.0F - 0.5F
            );
            if (position.vertical) {
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            }
            poseStack.scale(scale, scale, scale);
            bottle.submit(
                    poseStack,
                    collector,
                    state.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    0
            );
            poseStack.popPose();
        }
    }

    private record Position(
            float x,
            float y,
            float z,
            boolean vertical
    ) {
    }
}
