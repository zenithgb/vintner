package com.zenith.vintner.client.render;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public final class WineBasketRenderState extends BlockEntityRenderState {
    public final BlockModelRenderState bottle = new BlockModelRenderState();
    public Direction facing = Direction.NORTH;
}
