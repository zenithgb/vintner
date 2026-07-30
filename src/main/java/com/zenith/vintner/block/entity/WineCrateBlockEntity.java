package com.zenith.vintner.block.entity;

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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

public final class WineCrateBlockEntity extends BlockEntity {
    public static final int CAPACITY = 16;
    private static final int AGE_INTERVAL = 20;

    private final NonNullList<ItemStack> bottles =
            NonNullList.withSize(CAPACITY, ItemStack.EMPTY);
    private int tickCounter;
    private int lastComparatorSignal = -1;
    private CellarRating cellarRating = CellarRating.BASIC;
    private long lastAgingGameTime = -1L;

    public WineCrateBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(ModBlockEntities.WINE_CRATE, pos, state);
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            WineCrateBlockEntity crate
    ) {
        long currentGameTime = level.getGameTime();

        if (crate.lastAgingGameTime == -1L) {
            crate.lastAgingGameTime = currentGameTime;
            crate.setChanged();
        }

        crate.updateComparatorSignal();
        crate.tickCounter++;

        if (crate.tickCounter < AGE_INTERVAL) {
            return;
        }

        crate.tickCounter = 0;
        long elapsedTicks =
                currentGameTime - crate.lastAgingGameTime;
        crate.lastAgingGameTime = currentGameTime;

        if (elapsedTicks <= 0L) {
            crate.setChanged();
            return;
        }

        CellarConditions conditions =
                CellarConditions.evaluate(level, pos);
        crate.cellarRating = conditions.rating();

        boolean changed = false;

        for (ItemStack bottle : crate.bottles) {
            if (bottle.isEmpty()) {
                continue;
            }

            WineMetadata.ageBottle(
                    bottle,
                    elapsedTicks,
                    crate.cellarRating
            );
            changed = true;
        }

        if (changed) {
            crate.setChanged();
        }
    }

    public boolean canInsert(ItemStack stack) {
        return stack.getItem() instanceof WineItem
                && getBottleCount() < CAPACITY;
    }

    public boolean insertOne(ItemStack stack) {
        if (!canInsert(stack)) {
            return false;
        }

        boolean wasEmpty = getBottleCount() == 0;
        int slot = firstEmptySlot();

        if (slot < 0) {
            return false;
        }

        ItemStack bottle = stack.copy();
        bottle.setCount(1);
        WineMetadata.ensureDefaults(bottle);
        WineMetadata.ensureBatchIdentity(
                bottle,
                WineMetadata.createBatchId(
                        level == null ? 0L : level.getGameTime(),
                        worldPosition
                )
        );

        if (WineMetadata.bottledAt(bottle) == 0L) {
            WineMetadata.markBottled(
                    bottle,
                    level == null ? 0L : level.getGameTime()
            );
        }

        bottles.set(slot, bottle);

        if (wasEmpty && level != null) {
            lastAgingGameTime = level.getGameTime();
        }

        markChangedAndSync();
        return true;
    }

    public ItemStack takeLastBottle() {
        for (int slot = CAPACITY - 1; slot >= 0; slot--) {
            ItemStack bottle = bottles.get(slot);

            if (bottle.isEmpty()) {
                continue;
            }

            bottles.set(slot, ItemStack.EMPTY);

            if (getBottleCount() == 0) {
                lastAgingGameTime = -1L;
            }

            markChangedAndSync();
            return bottle;
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
        return count == 0 ? 0 : 1 + count * 14 / CAPACITY;
    }

    public CellarRating getCellarRating() {
        return cellarRating;
    }

    public List<ItemStack> getStoredBottlesCopy() {
        List<ItemStack> copies = new ArrayList<>();

        for (ItemStack bottle : bottles) {
            if (!bottle.isEmpty()) {
                copies.add(bottle.copy());
            }
        }

        return copies;
    }

    public List<ItemStack> removeAllBottles() {
        List<ItemStack> removed = getStoredBottlesCopy();

        if (removed.isEmpty()) {
            return removed;
        }

        bottles.clear();
        lastAgingGameTime = -1L;
        markChangedAndSync();
        return removed;
    }

    public ItemStack getBottleCopy(int slot) {
        if (slot < 0 || slot >= CAPACITY) {
            return ItemStack.EMPTY;
        }

        ItemStack bottle = bottles.get(slot);
        return bottle.isEmpty() ? ItemStack.EMPTY : bottle.copy();
    }

    private int firstEmptySlot() {
        for (int slot = 0; slot < CAPACITY; slot++) {
            if (bottles.get(slot).isEmpty()) {
                return slot;
            }
        }

        return -1;
    }

    private void markChangedAndSync() {
        setChanged();
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

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        bottles.clear();
        ContainerHelper.loadAllItems(input, bottles);
        tickCounter = input.getIntOr("TickCounter", 0);
        cellarRating = CellarRating.byId(
                input.getIntOr(
                        "CellarRating",
                        CellarRating.BASIC.id()
                )
        );
        lastAgingGameTime = input.getLongOr(
                "LastAgingGameTime",
                -1L
        );
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, bottles);
        output.putInt("TickCounter", tickCounter);
        output.putInt("CellarRating", cellarRating.id());
        output.putLong(
                "LastAgingGameTime",
                lastAgingGameTime
        );
    }
}
