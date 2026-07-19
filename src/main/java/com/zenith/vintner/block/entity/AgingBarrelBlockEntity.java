package com.zenith.vintner.block.entity;

import com.zenith.vintner.block.AgingBarrelBlock;
import com.zenith.vintner.registry.ModBlockEntities;
import com.zenith.vintner.registry.ModItems;
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

        return bottleCount < CAPACITY
                && (wineType == 0 || wineType == offeredType);
    }

    public boolean insertOne(ItemStack stack) {
        int offeredType = getWineType(stack);

        if (!canInsert(stack)) {
            return false;
        }

        if (wineType == 0) {
            wineType = offeredType;
            agingProgress = 0;
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

        bottleCount--;

        if (bottleCount <= 0) {
            resetBatch();
        }

        markChangedAndSync();
        return new ItemStack(agedWine);
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

        return new ItemStack(storedItem, bottleCount);
    }

    private void resetBatch() {
        wineType = 0;
        bottleCount = 0;
        agingProgress = 0;
        ready = false;
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
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putInt("WineType", wineType);
        output.putInt("BottleCount", bottleCount);
        output.putInt("AgingProgress", agingProgress);
        output.putBoolean("Ready", ready);
    }
}
