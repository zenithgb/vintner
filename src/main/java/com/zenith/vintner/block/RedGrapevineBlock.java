package com.zenith.vintner.block;

import com.mojang.serialization.MapCodec;
import com.zenith.vintner.registry.ModItems;
import com.zenith.vintner.vineyard.GrapeVariety;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class RedGrapevineBlock extends GrapevineBlock {
    public static final MapCodec<RedGrapevineBlock> CODEC =
            simpleCodec(RedGrapevineBlock::new);

    public RedGrapevineBlock(BlockBehaviour.Properties properties) {
        super(GrapeVariety.RED, properties);
    }

    @Override
    public MapCodec<RedGrapevineBlock> codec() {
        return CODEC;
    }

    @Override
    protected Item getGrapeItem() {
        return ModItems.RED_GRAPES;
    }
}
