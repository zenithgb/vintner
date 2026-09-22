package com.zenith.vintner.block;

import com.zenith.vintner.registry.ModItems;
import com.zenith.vintner.vineyard.GrapeVariety;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class RedGrapevineBlock extends GrapevineBlock {
    public RedGrapevineBlock(BlockBehaviour.Properties properties) {
        this(WoodVariant.OAK, properties);
    }

    public RedGrapevineBlock(
            WoodVariant woodVariant,
            BlockBehaviour.Properties properties
    ) {
        super(GrapeVariety.RED, woodVariant, properties);
    }

    @Override
    protected Item getGrapeItem() {
        return ModItems.RED_GRAPES;
    }

    @Override
    protected Item getCuttingItem() {
        return ModItems.RED_GRAPE_CUTTING;
    }
}
