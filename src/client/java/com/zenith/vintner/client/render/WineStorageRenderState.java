package com.zenith.vintner.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

public final class WineStorageRenderState extends BlockEntityRenderState {
    public static final int MAX_BOTTLES = 16;

    public final ItemStackRenderState[] bottles = createBottleStates();
    public Direction facing = Direction.NORTH;
    public WineStorageRenderer.Layout layout =
            WineStorageRenderer.Layout.RACK;

    private static ItemStackRenderState[] createBottleStates() {
        ItemStackRenderState[] states =
                new ItemStackRenderState[MAX_BOTTLES];
        for (int slot = 0; slot < states.length; slot++) {
            states[slot] = new ItemStackRenderState();
        }
        return states;
    }
}
