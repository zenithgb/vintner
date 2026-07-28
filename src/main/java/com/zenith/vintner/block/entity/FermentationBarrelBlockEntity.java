package com.zenith.vintner.block.entity;

import com.zenith.vintner.block.FermentationBarrelBlock;
import com.zenith.vintner.registry.ModBlockEntities;
import com.zenith.vintner.registry.ModItems;
import com.zenith.vintner.wine.WinemakingEffects;
import com.zenith.vintner.wine.WineMetadata;
import com.zenith.vintner.wine.WineQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
    private long batchId;
    private int lastComparatorSignal = -1;

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
        barrel.updateComparatorSignal();

        if (barrel.batchType == 0
                || barrel.bottleCount <= 0
                || barrel.ready) {
            return;
        }

        barrel.fermentationProgress++;

        if (level instanceof ServerLevel serverLevel
                && shouldEmit(
                        barrel.fermentationProgress,
                        pos,
                        80
                )) {
            WinemakingEffects.fermentationActive(
                    serverLevel,
                    pos
            );
        }

        if (barrel.fermentationProgress
                >= FERMENTATION_TIME) {
            barrel.fermentationProgress =
                    FERMENTATION_TIME;
            barrel.ready = true;
            barrel.markChangedAndSync();

            if (level instanceof ServerLevel serverLevel) {
                WinemakingEffects.fermentationComplete(
                        serverLevel,
                        pos
                );
            }
        } else if (barrel.fermentationProgress % 20 == 0) {
            /*
             * Persist periodic progress without forcing a visual
             * block-state update every tick.
             */
            barrel.setChanged();
        }

        barrel.updateComparatorSignal();
    }

    private static boolean shouldEmit(
            int progress,
            BlockPos pos,
            int interval
    ) {
        int phase = Math.floorMod(
                pos.getX() * 31
                        + pos.getY() * 17
                        + pos.getZ(),
                interval
        );

        return progress % interval == phase;
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
                && quality == WineMetadata.quality(stack)
                && batchMatches(stack);
    }

    public boolean insertOne(ItemStack stack) {
        WineMetadata.ensureDefaults(stack);
        WineMetadata.ensureBatchIdentity(
                stack,
                WineMetadata.createBatchId(
                        level == null ? 0L : level.getGameTime(),
                        worldPosition
                )
        );

        if (!canInsert(stack)) {
            return false;
        }

        int offeredType = getMustType(stack);

        if (batchType == 0) {
            batchType = offeredType;
            fermentationProgress = 0;
            vintage = WineMetadata.vintage(stack);
            quality = WineMetadata.quality(stack);
            batchId = WineMetadata.batchId(stack);
        }

        bottleCount++;
        markChangedAndSync();
        return true;
    }

    public boolean isReady() {
        return ready && bottleCount > 0;
    }

    public int getBatchType() {
        return batchType;
    }

    public int getBottleCount() {
        return bottleCount;
    }

    public int getProgressPercent() {
        if (isReady()) {
            return 100;
        }

        return Math.min(
                100,
                fermentationProgress * 100 / FERMENTATION_TIME
        );
    }

    public int getComparatorSignal() {
        if (batchType == 0 || bottleCount <= 0) {
            return 0;
        }

        if (isReady()) {
            return 15;
        }

        return 1 + Math.min(
                13,
                fermentationProgress * 14 / FERMENTATION_TIME
        );
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
        WineMetadata.ensureBatchIdentity(result, batchId);
        WineMetadata.markBottled(
                result,
                level == null ? 0L : level.getGameTime()
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
        WineMetadata.ensureBatchIdentity(result, batchId);

        return result;
    }

    private boolean batchMatches(ItemStack stack) {
        long offeredBatch = WineMetadata.batchId(stack);

        return batchId == 0L
                || offeredBatch == 0L
                || batchId == offeredBatch;
    }

    private void resetBatch() {
        batchType = 0;
        bottleCount = 0;
        fermentationProgress = 0;
        ready = false;
        vintage = 1;
        quality = WineQuality.COMMON;
        batchId = 0L;
    }

    private void markChangedAndSync() {
        setChanged();
        syncVisualState();
        updateComparatorSignal();
    }

    private void updateComparatorSignal() {
        if (level == null || level.isClientSide()) {
            return;
        }

        int signal = getComparatorSignal();

        if (signal == lastComparatorSignal) {
            return;
        }

        lastComparatorSignal = signal;
        level.updateNeighbourForOutputSignal(
                worldPosition,
                getBlockState().getBlock()
        );
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
        batchId = input.getLongOr("BatchId", 0L);
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
        output.putLong("BatchId", batchId);
    }
}
