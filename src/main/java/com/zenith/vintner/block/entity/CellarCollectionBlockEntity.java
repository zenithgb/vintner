package com.zenith.vintner.block.entity;

import com.zenith.vintner.block.CellarCollectionBlock;
import com.zenith.vintner.block.CellarFixtureKind;
import com.zenith.vintner.item.WineItem;
import com.zenith.vintner.registry.ModBlockEntities;
import com.zenith.vintner.wine.CellarConditions;
import com.zenith.vintner.wine.CellarRating;
import com.zenith.vintner.wine.WineMetadata;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

public final class CellarCollectionBlockEntity extends BlockEntity {
    public static final int MAX_CAPACITY = 8;
    private static final int AGE_INTERVAL = 20;

    private final NonNullList<ItemStack> bottles =
            NonNullList.withSize(MAX_CAPACITY, ItemStack.EMPTY);
    private int tickCounter;
    private int selectionCursor = -1;
    private int lastComparatorSignal = -1;
    private CellarRating cellarRating = CellarRating.BASIC;
    private long lastAgingGameTime = -1L;

    public CellarCollectionBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CELLAR_COLLECTION, pos, state);
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            CellarCollectionBlockEntity collection
    ) {
        long currentTime = level.getGameTime();
        if (collection.lastAgingGameTime < 0L) {
            collection.lastAgingGameTime = currentTime;
            collection.setChanged();
        }
        collection.updateComparatorSignal();
        if (++collection.tickCounter < AGE_INTERVAL) {
            return;
        }
        collection.tickCounter = 0;
        long elapsed = currentTime - collection.lastAgingGameTime;
        collection.lastAgingGameTime = currentTime;
        if (elapsed <= 0L) {
            return;
        }

        collection.cellarRating =
                CellarConditions.evaluate(level, pos).rating();
        boolean changed = false;
        for (ItemStack bottle : collection.bottles) {
            if (!bottle.isEmpty()) {
                WineMetadata.ageBottle(
                        bottle,
                        elapsed,
                        collection.cellarRating
                );
                changed = true;
            }
        }
        if (changed) {
            collection.setChanged();
        }
    }

    public CellarFixtureKind getKind() {
        return getBlockState().getBlock()
                instanceof CellarCollectionBlock block
                ? block.kind()
                : CellarFixtureKind.LABELLED_SHELF;
    }

    public int getCapacity() {
        return getKind().capacity();
    }

    public boolean isFull() {
        return getBottleCount() >= getCapacity();
    }

    public boolean canInsert(ItemStack stack) {
        if (!(stack.getItem() instanceof WineItem) || isFull()) {
            return false;
        }
        if (!getKind().singleBatch()) {
            return true;
        }
        ItemStack first = firstBottle();
        return first.isEmpty() || WineMetadata.matchesBatch(first, stack);
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
        int slot = firstEmptySlot();
        if (slot < 0 || slot >= getCapacity()) {
            return false;
        }
        ItemStack bottle = stack.copy();
        bottle.setCount(1);
        if (WineMetadata.bottledAt(bottle) == 0L) {
            WineMetadata.markBottled(
                    bottle,
                    level == null ? 0L : level.getGameTime()
            );
        }
        bottles.set(slot, bottle);
        if (getBottleCount() == 1 && level != null) {
            lastAgingGameTime = level.getGameTime();
        }
        markChangedAndSync();
        return true;
    }

    public ItemStack takeLastBottle() {
        for (int slot = getCapacity() - 1; slot >= 0; slot--) {
            ItemStack bottle = bottles.get(slot);
            if (!bottle.isEmpty()) {
                bottles.set(slot, ItemStack.EMPTY);
                selectionCursor = Math.min(selectionCursor, slot - 1);
                if (getBottleCount() == 0) {
                    lastAgingGameTime = -1L;
                }
                markChangedAndSync();
                return bottle;
            }
        }
        return ItemStack.EMPTY;
    }

    public ItemStack nextBottleCopy() {
        int count = getBottleCount();
        if (count == 0) {
            selectionCursor = -1;
            return ItemStack.EMPTY;
        }
        selectionCursor = Math.floorMod(selectionCursor + 1, count);
        int found = 0;
        for (ItemStack bottle : bottles) {
            if (bottle.isEmpty()) {
                continue;
            }
            if (found++ == selectionCursor) {
                setChanged();
                return bottle.copy();
            }
        }
        return ItemStack.EMPTY;
    }

    public int getBottleCount() {
        int count = 0;
        for (ItemStack bottle : bottles) {
            if (!bottle.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public int getComparatorSignal() {
        int count = getBottleCount();
        return count == 0 ? 0 : 1 + count * 14 / getCapacity();
    }

    public List<ItemStack> getStoredBottlesCopy() {
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack bottle : bottles) {
            if (!bottle.isEmpty()) {
                result.add(bottle.copy());
            }
        }
        return result;
    }

    public List<ItemStack> removeAllBottles() {
        List<ItemStack> result = getStoredBottlesCopy();
        bottles.clear();
        selectionCursor = -1;
        lastAgingGameTime = -1L;
        markChangedAndSync();
        return result;
    }

    private ItemStack firstBottle() {
        for (ItemStack bottle : bottles) {
            if (!bottle.isEmpty()) {
                return bottle;
            }
        }
        return ItemStack.EMPTY;
    }

    private int firstEmptySlot() {
        for (int slot = 0; slot < getCapacity(); slot++) {
            if (bottles.get(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    private void markChangedAndSync() {
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            if (state.getBlock() instanceof CellarCollectionBlock) {
                BlockState updated = state.setValue(
                        CellarCollectionBlock.BOTTLE_COUNT,
                        getBottleCount()
                );
                if (!updated.equals(state)) {
                    level.setBlock(worldPosition, updated, Block.UPDATE_CLIENTS);
                }
            }
        }
        updateComparatorSignal();
    }

    private void updateComparatorSignal() {
        if (level == null || level.isClientSide()) {
            return;
        }
        int signal = getComparatorSignal();
        if (signal != lastComparatorSignal) {
            lastComparatorSignal = signal;
            level.updateNeighbourForOutputSignal(
                    worldPosition,
                    getBlockState().getBlock()
            );
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        bottles.clear();
        ContainerHelper.loadAllItems(input, bottles);
        tickCounter = input.getIntOr("TickCounter", 0);
        selectionCursor = input.getIntOr("SelectionCursor", -1);
        cellarRating = CellarRating.byId(
                input.getIntOr("CellarRating", CellarRating.BASIC.id())
        );
        lastAgingGameTime = input.getLongOr("LastAgingGameTime", -1L);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, bottles);
        output.putInt("TickCounter", tickCounter);
        output.putInt("SelectionCursor", selectionCursor);
        output.putInt("CellarRating", cellarRating.id());
        output.putLong("LastAgingGameTime", lastAgingGameTime);
    }
}
