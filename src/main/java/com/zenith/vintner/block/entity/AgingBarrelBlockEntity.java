package com.zenith.vintner.block.entity;

import com.zenith.vintner.block.AgingBarrelBlock;
import com.zenith.vintner.registry.ModBlockEntities;
import com.zenith.vintner.registry.ModItems;
import com.zenith.vintner.wine.WineMetadata;
import com.zenith.vintner.wine.WineQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class AgingBarrelBlockEntity extends BlockEntity {
    public static final int CAPACITY = 4;
    public static final int AGING_TIME = 20 * 90;

    private int wineType;
    private int bottleCount;
    private int agingProgress;
    private boolean ready;
    private int vintage = 1;
    private WineQuality quality = WineQuality.COMMON;

    public AgingBarrelBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(ModBlockEntities.AGING_BARREL, pos, state);
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            AgingBarrelBlockEntity barrel
    ) {
        if (barrel.wineType == 0
                || barrel.bottleCount <= 0
                || barrel.ready) {
            return;
        }

        barrel.agingProgress++;

        if (barrel.agingProgress >= AGING_TIME) {
            barrel.agingProgress = AGING_TIME;
            barrel.ready = true;
            barrel.markChangedAndSync();
        } else if (barrel.agingProgress % 20 == 0) {
            barrel.setChanged();
        }
    }

    public boolean canInsert(ItemStack stack) {
        int offeredType = getWineType(stack);

        if (offeredType == 0 || ready) {
            return false;
        }

        if (bottleCount >= CAPACITY) {
            return false;
        }

        if (wineType == 0) {
            return true;
        }

        return wineType == offeredType
                && vintage == WineMetadata.vintage(stack)
                && quality == WineMetadata.quality(stack);
    }

    public boolean insertOne(ItemStack stack) {
        int offeredType = getWineType(stack);

        if (!canInsert(stack)) {
            return false;
        }

        WineMetadata.ensureDefaults(stack);

        if (wineType == 0) {
            wineType = offeredType;
            agingProgress = 0;
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

    public ItemStack takeOneAgedWine() {
        if (!isReady()) {
            return ItemStack.EMPTY;
        }

        Item agedWine = wineType == 1
                ? ModItems.AGED_RED_WINE
                : ModItems.AGED_WHITE_WINE;

        ItemStack result = new ItemStack(agedWine);

        WineMetadata.apply(
                result,
                vintage,
                quality.improved()
        );

        bottleCount--;

        if (bottleCount <= 0) {
            resetBatch();
        }

        markChangedAndSync();
        return result;
    }

    public ItemStack getStoredContentsCopy() {
        if (wineType == 0 || bottleCount <= 0) {
            return ItemStack.EMPTY;
        }

        Item storedItem;

        if (ready) {
            storedItem = wineType == 1
                    ? ModItems.AGED_RED_WINE
                    : ModItems.AGED_WHITE_WINE;
        } else {
            storedItem = wineType == 1
                    ? ModItems.RED_WINE
                    : ModItems.WHITE_WINE;
        }

        ItemStack result = new ItemStack(
                storedItem,
                bottleCount
        );

        WineMetadata.apply(
                result,
                vintage,
                ready ? quality.improved() : quality
        );

        return result;
    }

    private void resetBatch() {
        wineType = 0;
        bottleCount = 0;
        agingProgress = 0;
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

        if (!(state.getBlock() instanceof AgingBarrelBlock)) {
            return;
        }

        int status = wineType == 0
                ? 0
                : ready ? 2 : 1;

        BlockState updated = state
                .setValue(AgingBarrelBlock.STATUS, status)
                .setValue(AgingBarrelBlock.WINE_TYPE, wineType);

        if (!updated.equals(state)) {
            level.setBlock(
                    worldPosition,
                    updated,
                    Block.UPDATE_CLIENTS
            );
        }
    }

    private static int getWineType(ItemStack stack) {
        if (stack.is(ModItems.RED_WINE)) {
            return 1;
        }

        if (stack.is(ModItems.WHITE_WINE)) {
            return 2;
        }

        return 0;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        wineType = input.getIntOr("WineType", 0);
        bottleCount = input.getIntOr("BottleCount", 0);
        agingProgress = input.getIntOr("AgingProgress", 0);
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

        output.putInt("WineType", wineType);
        output.putInt("BottleCount", bottleCount);
        output.putInt("AgingProgress", agingProgress);
        output.putBoolean("Ready", ready);
        output.putInt("Vintage", vintage);
        output.putInt("Quality", quality.id());
    }
}
