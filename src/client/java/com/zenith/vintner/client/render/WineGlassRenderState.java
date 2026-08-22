package com.zenith.vintner.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public final class WineGlassRenderState extends BlockEntityRenderState {
    public final boolean[] filled = new boolean[4];
    public int count;
    public Direction facing = Direction.NORTH;
}
