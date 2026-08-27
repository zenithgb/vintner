package com.zenith.vintner.block.entity;

import com.zenith.vintner.block.TastingServiceBlock;
import com.zenith.vintner.item.WineItem;
import com.zenith.vintner.registry.ModBlockEntities;
import com.zenith.vintner.wine.WineMetadata;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class TastingServiceBlockEntity extends BlockEntity {
    private ItemStack bottle = ItemStack.EMPTY;
    private boolean whiteWine;

    public TastingServiceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TASTING_SERVICE, pos, state);
    }

    public boolean insertBottle(ItemStack stack) {
        if (!bottle.isEmpty()
                || !(stack.getItem() instanceof WineItem wine)
                || WineMetadata.servings(stack) <= 0) {
            return false;
        }

        bottle = stack.copyWithCount(1);
        WineMetadata.ensureDefaults(bottle);
        WineMetadata.setEffectProfile(
                bottle,
                wine.effectProfile().id()
        );
        whiteWine = wine.effectProfile().id().contains("white");
        changedAndSync();
        return true;
    }

    public synchronized ItemStack pourServing() {
        if (bottle.isEmpty()
                || bottle.is(Items.GLASS_BOTTLE)
                || WineMetadata.servings(bottle) <= 0) {
            return ItemStack.EMPTY;
        }

        ItemStack serving = bottle.copyWithCount(1);
        WineMetadata.setServings(serving, 1);
        int remaining = WineMetadata.servings(bottle) - 1;

        if (remaining <= 0) {
            bottle = new ItemStack(Items.GLASS_BOTTLE);
        } else {
            WineMetadata.setServings(bottle, remaining);
        }

        changedAndSync();
        return serving;
    }

    public ItemStack removeBottle() {
        if (bottle.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = bottle;
        bottle = ItemStack.EMPTY;
        whiteWine = false;
        changedAndSync();
        return result;
    }

    public ItemStack getBottleCopy() {
        return bottle.isEmpty() ? ItemStack.EMPTY : bottle.copy();
    }

    public boolean hasEmptyBottle() {
        return bottle.is(Items.GLASS_BOTTLE);
    }

    public int servings() {
        if (bottle.isEmpty() || bottle.is(Items.GLASS_BOTTLE)) {
            return 0;
        }

        return WineMetadata.servings(bottle);
    }

    private void changedAndSync() {
        setChanged();

        if (level == null) {
            return;
        }

        BlockState state = getBlockState();
        BlockState updated = state
                .setValue(
                        TastingServiceBlock.HAS_BOTTLE,
                        !bottle.isEmpty()
                )
                .setValue(
                        TastingServiceBlock.WHITE_WINE,
                        whiteWine
                )
                .setValue(
                        TastingServiceBlock.SERVINGS,
                        servings()
                );

        if (!updated.equals(state)) {
            level.setBlock(worldPosition, updated, Block.UPDATE_ALL);
        } else {
            level.sendBlockUpdated(
                    worldPosition,
                    state,
                    state,
                    Block.UPDATE_CLIENTS
            );
        }

        if (!level.isClientSide()) {
            level.updateNeighbourForOutputSignal(
                    worldPosition,
                    state.getBlock()
            );
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        bottle = input.read("Bottle", ItemStack.CODEC)
                .orElse(ItemStack.EMPTY);
        whiteWine = input.getBooleanOr("WhiteWine", false);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        if (!bottle.isEmpty()) {
            output.store("Bottle", ItemStack.CODEC, bottle);
        }

        output.putBoolean("WhiteWine", whiteWine);
    }
}
