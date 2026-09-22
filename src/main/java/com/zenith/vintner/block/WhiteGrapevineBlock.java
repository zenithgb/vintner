package com.zenith.vintner.block;

import com.zenith.vintner.registry.ModItems;
import com.zenith.vintner.vineyard.GrapeVariety;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class WhiteGrapevineBlock extends GrapevineBlock {
    public WhiteGrapevineBlock(BlockBehaviour.Properties properties) {
        this(WoodVariant.OAK, properties);
    }

    public WhiteGrapevineBlock(
            WoodVariant woodVariant,
            BlockBehaviour.Properties properties
    ) {
        super(GrapeVariety.WHITE, woodVariant, properties);
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
