package com.zenith.vintner.block.entity;

import com.zenith.vintner.wine.WineMetadata;
import com.zenith.vintner.wine.WineQuality;

import com.zenith.vintner.block.GrapePressBlock;
import com.zenith.vintner.registry.ModBlockEntities;
import com.zenith.vintner.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class GrapePressBlockEntity extends BlockEntity {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;

    public static final int CAPACITY = 8;
    public static final int GRAPES_PER_PRESS = 4;

    private final NonNullList<ItemStack> items =
            NonNullList.withSize(2, ItemStack.EMPTY);

    private int lastComparatorSignal = -1;

    public GrapePressBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(ModBlockEntities.GRAPE_PRESS, pos, state);
    }

    public ItemStack getInput() {
        return items.get(INPUT_SLOT);
    }

    public ItemStack getOutput() {
        return items.get(OUTPUT_SLOT);
    }

    public boolean canInsert(ItemStack offered) {
        if (!isGrape(offered)) {
            return false;
        }

        ItemStack input = getInput();

        if (input.isEmpty()) {
            return true;
        }

        return input.is(offered.getItem())
                && input.getCount() < CAPACITY
                && WineMetadata.matchesBatch(
                        input,
                        offered
                );
    }

    public int insert(ItemStack offered, int requestedAmount) {
        if (!canInsert(offered) || requestedAmount <= 0) {
            return 0;
        }

        ItemStack input = getInput();

        int currentCount = input.isEmpty()
                ? 0
                : input.getCount();

        int inserted = Math.min(
                requestedAmount,
                CAPACITY - currentCount
        );

        if (inserted <= 0) {
            return 0;
        }

        if (input.isEmpty()) {
            WineMetadata.ensureDefaults(offered);

            ItemStack insertedStack = offered.copy();
            insertedStack.setCount(inserted);

            items.set(
                    INPUT_SLOT,
                    insertedStack
            );
        } else {
            input.grow(inserted);
        }

        markChangedAndSync();
        return inserted;
    }

    public boolean canPress() {
        ItemStack input = getInput();

        if (input.getCount() < GRAPES_PER_PRESS) {
            return false;
        }

        Item mustItem = getMustFor(input);

        if (mustItem == null) {
            return false;
        }

        ItemStack output = getOutput();

        if (output.isEmpty()) {
            return true;
        }

        return output.is(mustItem)
                && output.getCount() < CAPACITY
                && WineMetadata.matchesBatch(
                        output,
                        input
                );
    }

    public boolean press() {
        if (!canPress()) {
            return false;
        }

        ItemStack input = getInput();
        Item mustItem = getMustFor(input);
        int vintage = WineMetadata.vintage(input);
        WineQuality quality = WineMetadata.quality(input);

        input.shrink(GRAPES_PER_PRESS);

        if (input.isEmpty()) {
            items.set(INPUT_SLOT, ItemStack.EMPTY);
        }

        ItemStack output = getOutput();

        if (output.isEmpty()) {
            ItemStack must = new ItemStack(mustItem);

            WineMetadata.apply(
                    must,
                    vintage,
                    quality
            );
            WineMetadata.ensureBatchIdentity(
                    must,
                    WineMetadata.createBatchId(
                            level == null ? 0L : level.getGameTime(),
                            worldPosition
                    )
            );

            items.set(
                    OUTPUT_SLOT,
                    must
            );
        } else {
            output.grow(1);
        }

        markChangedAndSync();
        return true;
    }

    public boolean hasMust() {
        return !getOutput().isEmpty();
    }

    public int getComparatorSignal() {
        int mustCount = getOutput().getCount();

        if (mustCount <= 0) {
            return 0;
        }

        return 1 + mustCount * 14 / CAPACITY;
    }

    public ItemStack bottleOneMust() {
        ItemStack output = getOutput();

        if (output.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack bottledMust =
                new ItemStack(output.getItem());

        WineMetadata.copyBatchMetadata(output, bottledMust);

        output.shrink(1);

        if (output.isEmpty()) {
            items.set(OUTPUT_SLOT, ItemStack.EMPTY);
        }

        markChangedAndSync();
        return bottledMust;
    }

    public ItemStack getStoredGrapesCopy() {
        ItemStack input = getInput();

        return input.isEmpty()
                ? ItemStack.EMPTY
                : input.copy();
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

        if (!(state.getBlock() instanceof GrapePressBlock)) {
            return;
        }

        BlockState updated = state
                .setValue(
                        GrapePressBlock.INPUT_LEVEL,
                        getInputVisualLevel()
                )
                .setValue(
                        GrapePressBlock.INPUT_TYPE,
                        getGrapeVisualType()
                )
                .setValue(
                        GrapePressBlock.OUTPUT_TYPE,
                        getMustVisualType()
                );

        if (!updated.equals(state)) {
            level.setBlock(
                    worldPosition,
                    updated,
                    Block.UPDATE_CLIENTS
            );
        }
    }

    private int getInputVisualLevel() {
        int count = getInput().getCount();

        if (count <= 0) {
            return 0;
        }

        return count < GRAPES_PER_PRESS ? 1 : 2;
    }

    private int getGrapeVisualType() {
        ItemStack input = getInput();

        if (input.is(ModItems.RED_GRAPES)) {
            return 1;
        }

        if (input.is(ModItems.WHITE_GRAPES)) {
            return 2;
        }

        return 0;
    }

    private int getMustVisualType() {
        ItemStack output = getOutput();

        if (output.is(ModItems.RED_MUST)) {
            return 1;
        }

        if (output.is(ModItems.WHITE_MUST)) {
            return 2;
        }

        return 0;
    }

    private static boolean isGrape(ItemStack stack) {
        return stack.is(ModItems.RED_GRAPES)
                || stack.is(ModItems.WHITE_GRAPES);
    }

    private static Item getMustFor(ItemStack grapes) {
        if (grapes.is(ModItems.RED_GRAPES)) {
            return ModItems.RED_MUST;
        }

        if (grapes.is(ModItems.WHITE_GRAPES)) {
            return ModItems.WHITE_MUST;
        }

        return null;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        items.clear();
        ContainerHelper.loadAllItems(input, items);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
    }
}
