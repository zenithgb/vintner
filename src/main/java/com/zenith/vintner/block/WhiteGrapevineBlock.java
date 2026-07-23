package com.zenith.vintner.block;

import com.mojang.serialization.MapCodec;
import com.zenith.vintner.registry.ModItems;
import com.zenith.vintner.vineyard.GrapeVariety;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class WhiteGrapevineBlock extends GrapevineBlock {
    public static final MapCodec<WhiteGrapevineBlock> CODEC =
            simpleCodec(WhiteGrapevineBlock::new);

    public WhiteGrapevineBlock(BlockBehaviour.Properties properties) {
        super(GrapeVariety.WHITE, properties);
    }

    @Override
    public MapCodec<WhiteGrapevineBlock> codec() {
        return CODEC;
    }

    @Override
    protected Item getGrapeItem() {
        return ModItems.WHITE_GRAPES;
    }

    @Override
    protected Item getCuttingItem() {
        return ModItems.WHITE_GRAPE_CUTTING;
    }
}
