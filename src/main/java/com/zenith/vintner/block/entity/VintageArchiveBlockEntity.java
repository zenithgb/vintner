package com.zenith.vintner.block.entity;

import com.zenith.vintner.item.WineItem;
import com.zenith.vintner.registry.ModBlockEntities;
import com.zenith.vintner.wine.WineMetadata;
import com.zenith.vintner.wine.WineProvenance;
import com.zenith.vintner.wine.WineReadiness;
import com.zenith.vintner.wine.WineTastingProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class VintageArchiveBlockEntity extends BlockEntity {
    public static final int CAPACITY = 16;

    private final NonNullList<ItemStack> records =
            NonNullList.withSize(CAPACITY, ItemStack.EMPTY);
    private int selectedIndex;

    public VintageArchiveBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(ModBlockEntities.VINTAGE_ARCHIVE, pos, state);
    }

    public RecordResult record(ItemStack bottle) {
        if (!(bottle.getItem() instanceof WineItem)) {
            return RecordResult.FULL;
        }

        ItemStack snapshot = bottle.copy();
        snapshot.setCount(1);
        long batchId = WineMetadata.batchId(snapshot);
        int existing = findBatch(batchId);

        if (existing >= 0) {
            records.set(existing, snapshot);
            setChanged();
            return RecordResult.UPDATED;
        }

        int empty = firstEmptySlot();

        if (empty < 0) {
            return RecordResult.FULL;
        }

        records.set(empty, snapshot);
        selectedIndex = empty;
        setChanged();
        updateComparatorSignal();
        return RecordResult.ADDED;
    }

    public void reportNext(Player player) {
        int count = getRecordCount();

        if (count == 0) {
            player.sendSystemMessage(
                    Component.translatable(
                            "message.vintner.vintage_archive.empty"
                    ).withStyle(ChatFormatting.GRAY)
            );
            return;
        }

        int slot = nextOccupiedSlot(selectedIndex);
        ItemStack bottle = records.get(slot);
        int ordinal = occupiedOrdinal(slot);

        player.sendSystemMessage(
                Component.translatable(
                        "message.vintner.vintage_archive.entry",
                        ordinal,
                        count,
                        bottle.getHoverName(),
                        WineMetadata.batchCode(bottle)
                ).withStyle(ChatFormatting.GOLD)
        );
        player.sendSystemMessage(
                WineTastingProfile.from(bottle)
                        .description()
                        .copy()
                        .withStyle(ChatFormatting.GRAY)
        );

        WineProvenance provenance =
                WineMetadata.provenance(bottle);

        if (provenance.known()) {
            player.sendSystemMessage(
                    Component.translatable(
                            "message.vintner.vintage_archive.provenance",
                            provenance.varietyDisplayName(),
                            provenance.harvestDay(),
                            provenance.producerDisplayName()
                    ).withStyle(ChatFormatting.DARK_GRAY)
            );
        } else {
            player.sendSystemMessage(
                    Component.translatable(
                            "message.vintner.almanac.provenance_legacy"
                    ).withStyle(ChatFormatting.DARK_GRAY)
            );
        }

        player.sendSystemMessage(
                Component.translatable(
                        "message.vintner.vintage_archive.condition",
                        WineMetadata.qualityScore(bottle),
                        WineMetadata.ageStage(bottle).displayName(),
                        WineReadiness.from(bottle).displayName()
                ).withStyle(ChatFormatting.DARK_GRAY)
        );

        selectedIndex = nextOccupiedSlot((slot + 1) % CAPACITY);
        setChanged();
    }

    public int getRecordCount() {
        int count = 0;

        for (ItemStack record : records) {
            if (!record.isEmpty()) {
                count++;
            }
        }

        return count;
    }

    public int getComparatorSignal() {
        int count = getRecordCount();
        return count == 0 ? 0 : 1 + count * 14 / CAPACITY;
    }

    public ItemStack getRecordCopy(int slot) {
        if (slot < 0 || slot >= CAPACITY) {
            return ItemStack.EMPTY;
        }

        ItemStack record = records.get(slot);
        return record.isEmpty() ? ItemStack.EMPTY : record.copy();
    }

    private int findBatch(long batchId) {
        if (batchId == 0L) {
            return -1;
        }

        for (int slot = 0; slot < CAPACITY; slot++) {
            ItemStack record = records.get(slot);

            if (!record.isEmpty()
                    && WineMetadata.batchId(record) == batchId) {
                return slot;
            }
        }

        return -1;
    }

    private int firstEmptySlot() {
        for (int slot = 0; slot < CAPACITY; slot++) {
            if (records.get(slot).isEmpty()) {
                return slot;
            }
        }

        return -1;
    }

    private int nextOccupiedSlot(int start) {
        for (int offset = 0; offset < CAPACITY; offset++) {
            int slot = Math.floorMod(start + offset, CAPACITY);

            if (!records.get(slot).isEmpty()) {
                return slot;
            }
        }

        return 0;
    }

    private int occupiedOrdinal(int targetSlot) {
        int ordinal = 0;

        for (int slot = 0; slot <= targetSlot; slot++) {
            if (!records.get(slot).isEmpty()) {
                ordinal++;
            }
        }

        return ordinal;
    }

    private void updateComparatorSignal() {
        if (level != null && !level.isClientSide()) {
            level.updateNeighbourForOutputSignal(
                    worldPosition,
                    getBlockState().getBlock()
            );
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        records.clear();
        ContainerHelper.loadAllItems(input, records);
        selectedIndex = Math.clamp(
                input.getIntOr("SelectedIndex", 0),
                0,
                CAPACITY - 1
        );
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, records);
        output.putInt("SelectedIndex", selectedIndex);
    }

    public enum RecordResult {
        ADDED("message.vintner.vintage_archive.added"),
        UPDATED("message.vintner.vintage_archive.updated"),
        FULL("message.vintner.vintage_archive.full");

        private final String translationKey;

        RecordResult(String translationKey) {
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return translationKey;
        }
    }
}
