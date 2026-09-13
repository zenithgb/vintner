package com.zenith.vintner.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

public final class WineBasketRenderState extends BlockEntityRenderState {
    public final ItemStackRenderState bottle = new ItemStackRenderState();
    public Direction facing = Direction.NORTH;
}
