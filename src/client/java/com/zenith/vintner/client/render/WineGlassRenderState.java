package com.zenith.vintner.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

public final class WineGlassRenderState extends BlockEntityRenderState {
    public final ItemStackRenderState[] glasses = {
            new ItemStackRenderState(),
            new ItemStackRenderState(),
            new ItemStackRenderState(),
            new ItemStackRenderState()
    };
    public int count;
    public Direction facing = Direction.NORTH;
}
