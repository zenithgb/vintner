package com.zenith.vintner.block.entity;

import com.zenith.vintner.block.FermentationBarrelBlock;
import com.zenith.vintner.registry.ModBlockEntities;
import com.zenith.vintner.registry.ModItems;
import com.zenith.vintner.wine.WineMetadata;
import com.zenith.vintner.wine.WineQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class FermentationBarrelBlockEntity
        extends BlockEntity {
    public static final int CAPACITY = 4;

    /*
     * One minute keeps the foundation convenient to test.
     * This can become configurable or recipe-dependent later.
     */
    public static final int FERMENTATION_TIME = 20 * 60;

    private int batchType;
    private int bottleCount;
    private int fermentationProgress;
    private boolean ready;
    private int vintage = 1;
    private WineQuality quality = WineQuality.COMMON;

    public FermentationBarrelBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(ModBlockEntities.FERMENTATION_BARREL, pos, state);
    }

    public static void serverTick(
            net.minecraft.world.level.Level level,
            BlockPos pos,
            BlockState state,
            FermentationBarrelBlockEntity barrel
    ) {
        if (barrel.batchType == 0
                || barrel.bottleCount <= 0
                || barrel.ready) {
            return;
        }

        barrel.fermentationProgress++;

        if (barrel.fermentationProgress
                >= FERMENTATION_TIME) {
            barrel.fermentationProgress =
                    FERMENTATION_TIME;
            barrel.ready = true;
            barrel.markChangedAndSync();
        } else if (barrel.fermentationProgress % 20 == 0) {
            /*
             * Persist periodic progress without forcing a visual
             * block-state update every tick.
             */
            barrel.setChanged();
        }
    }

    public boolean canInsert(ItemStack stack) {
        int offeredType = getMustType(stack);

        if (offeredType == 0 || ready) {
            return false;
        }

        if (bottleCount >= CAPACITY) {
            return false;
        }

        if (batchType == 0) {
            return true;
        }

        return batchType == offeredType
                && vintage == WineMetadata.vintage(stack)
                && quality == WineMetadata.quality(stack);
    }

    public boolean insertOne(ItemStack stack) {
        int offeredType = getMustType(stack);

        if (!canInsert(stack)) {
            return false;
        }

        WineMetadata.ensureDefaults(stack);

        if (batchType == 0) {
            batchType = offeredType;
            fermentationProgress = 0;
            vintage = WineMetadata.vintage(stack);
            quality = WineMetadata.quality(stack);
        }

        bottleCount++;
        markChangedAndSync();
        return true;
    }

    public boolean isReady() {
        return ready && bottleCount > 0;
    }

    public ItemStack takeOneWine() {
        if (!isReady()) {
            return ItemStack.EMPTY;
        }

        Item wine = batchType == 1
                ? ModItems.RED_WINE
                : ModItems.WHITE_WINE;

        ItemStack result = new ItemStack(wine);

        WineMetadata.apply(
                result,
                vintage,
                quality
        );

        bottleCount--;

        if (bottleCount <= 0) {
            resetBatch();
        }

        markChangedAndSync();
        return result;
    }

    public ItemStack getStoredContentsCopy() {
        if (batchType == 0 || bottleCount <= 0) {
            return ItemStack.EMPTY;
        }

        Item storedItem;

        if (ready) {
            storedItem = batchType == 1
                    ? ModItems.RED_WINE
                    : ModItems.WHITE_WINE;
        } else {
            storedItem = batchType == 1
                    ? ModItems.RED_MUST
                    : ModItems.WHITE_MUST;
        }

        ItemStack result = new ItemStack(
                storedItem,
                bottleCount
        );

        WineMetadata.apply(
                result,
                vintage,
                quality
        );

        return result;
    }

    private void resetBatch() {
        batchType = 0;
        bottleCount = 0;
        fermentationProgress = 0;
        ready = false;
        vintage = 1;
        quality = WineQuality.COMMON;
    }

    private void markChangedAndSync() {
        setChanged();
        syncVisualState();
    }

    private void syncVisualState() {
        if (level == null) {
            return;
        }

        BlockState state = getBlockState();

        if (!(state.getBlock()
                instanceof FermentationBarrelBlock)) {
            return;
        }

        int status = batchType == 0
                ? 0
                : ready ? 2 : 1;

        BlockState updated = state
                .setValue(
                        FermentationBarrelBlock.STATUS,
                        status
                )
                .setValue(
                        FermentationBarrelBlock.WINE_TYPE,
                        batchType
                );

        if (!updated.equals(state)) {
            level.setBlock(
                    worldPosition,
                    updated,
                    Block.UPDATE_CLIENTS
            );
        }
    }

    private static int getMustType(ItemStack stack) {
        if (stack.is(ModItems.RED_MUST)) {
            return 1;
        }

        if (stack.is(ModItems.WHITE_MUST)) {
            return 2;
        }

        return 0;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        batchType = input.getIntOr("BatchType", 0);
        bottleCount = input.getIntOr("BottleCount", 0);
        fermentationProgress =
                input.getIntOr("FermentationProgress", 0);
        ready = input.getBooleanOr("Ready", false);
        vintage = input.getIntOr("Vintage", 1);
        quality = WineQuality.byId(
                input.getIntOr(
                        "Quality",
                        WineQuality.COMMON.id()
                )
        );
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putInt("BatchType", batchType);
        output.putInt("BottleCount", bottleCount);
        output.putInt(
                "FermentationProgress",
                fermentationProgress
        );
        output.putBoolean("Ready", ready);
        output.putInt("Vintage", vintage);
        output.putInt("Quality", quality.id());
    }
}
