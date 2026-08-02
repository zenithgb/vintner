package com.zenith.vintner.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.core.Direction;

public final class EstateManagementDeskRenderState
        extends BlockEntityRenderState {
    public final MapRenderState map = new MapRenderState();
    public Direction facing = Direction.NORTH;
    public boolean hasMap;
}
