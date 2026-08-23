package com.zenith.vintner.client.render;

import com.zenith.vintner.item.GobletMaterial;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public final class WineGlassRenderState extends BlockEntityRenderState {
    public final boolean[] filled = new boolean[4];
    public final boolean[] whiteWine = new boolean[4];
    public final GobletMaterial[] materials = {
            GobletMaterial.PEWTER,
            GobletMaterial.PEWTER,
            GobletMaterial.PEWTER,
            GobletMaterial.PEWTER
    };
    public int count;
    public Direction facing = Direction.NORTH;
}
