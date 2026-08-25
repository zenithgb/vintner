package com.zenith.vintner.block.entity;

import com.zenith.vintner.block.WineBottleBlock;
import com.zenith.vintner.item.FilledWineGlassItem;
import com.zenith.vintner.registry.ModBlockEntities;
import com.zenith.vintner.wine.WineMetadata;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class WineBottleBlockEntity extends BlockEntity {
    private ItemStack bottle = ItemStack.EMPTY;

    public WineBottleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WINE_BOTTLE, pos, state);
    }

    public void setBottle(ItemStack stack) {
        bottle = stack.copyWithCount(1);
        setChanged();
        syncServings();
    }

    public ItemStack getBottleCopy() {
        return bottle.isEmpty() ? ItemStack.EMPTY : bottle.copy();
    }

    public ItemStack takeBottle() {
        if (bottle.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = bottle;
        bottle = ItemStack.EMPTY;
        setChanged();
        return result;
    }

    public synchronized ItemStack pourServing() {
        if (bottle.isEmpty()
                || bottle.is(Items.GLASS_BOTTLE)
                || WineMetadata.servings(bottle) <= 0) {
            return ItemStack.EMPTY;
        }

        ItemStack glass = FilledWineGlassItem.fromBottle(bottle);
        int remaining = WineMetadata.servings(bottle) - 1;

        if (remaining <= 0) {
            bottle = new ItemStack(Items.GLASS_BOTTLE);
        } else {
            WineMetadata.setServings(bottle, remaining);
        }

        setChanged();
        syncServings();
        return glass;
    }

    public int servings() {
        if (bottle.isEmpty() || bottle.is(Items.GLASS_BOTTLE)) {
            return 0;
        }

        return WineMetadata.servings(bottle);
    }

    private void syncServings() {
        if (level == null
                || !getBlockState().hasProperty(
                WineBottleBlock.SERVINGS
        )) {
            return;
        }

        int servings = servings();
        if (getBlockState().getValue(WineBottleBlock.SERVINGS)
                == servings) {
            return;
        }

        level.setBlock(
                worldPosition,
                getBlockState().setValue(
                        WineBottleBlock.SERVINGS,
                        servings
                ),
                Block.UPDATE_ALL
        );
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        bottle = input.read("Bottle", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        if (!bottle.isEmpty()) {
            output.store("Bottle", ItemStack.CODEC, bottle);
        }
    }
}
