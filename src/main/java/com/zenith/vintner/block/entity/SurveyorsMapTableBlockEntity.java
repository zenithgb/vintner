package com.zenith.vintner.block.entity;

import com.zenith.vintner.block.SurveyorsMapTableBlock;
import com.zenith.vintner.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Stores the explored maps that make up an estate atlas. */
public final class SurveyorsMapTableBlockEntity extends BlockEntity {
    public static final int CAPACITY = 9;

    private final NonNullList<ItemStack> maps =
            NonNullList.withSize(CAPACITY, ItemStack.EMPTY);

    public SurveyorsMapTableBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(ModBlockEntities.SURVEYORS_MAP_TABLE, pos, state);
    }

    public AddResult addMap(ItemStack stack, ServerLevel level) {
        MapId candidateId = stack.get(DataComponents.MAP_ID);
        if (candidateId == null) {
            return AddResult.INVALID;
        }
        MapItemSavedData candidate = level.getMapData(candidateId);
        if (candidate == null) {
            return AddResult.INVALID;
        }
        if (getMapCount() >= CAPACITY) {
            return AddResult.FULL;
        }

        for (ItemStack stored : maps) {
            if (stored.isEmpty()) {
                continue;
            }
            MapId storedId = stored.get(DataComponents.MAP_ID);
            if (candidateId.equals(storedId)) {
                return AddResult.DUPLICATE;
            }
            if (storedId == null) {
                continue;
            }
            MapItemSavedData existing = level.getMapData(storedId);
            if (existing == null) {
                continue;
            }
            if (!candidate.dimension.equals(existing.dimension)) {
                return AddResult.WRONG_DIMENSION;
            }
            if (candidate.scale != existing.scale) {
                return AddResult.WRONG_SCALE;
            }
            if (candidate.centerX == existing.centerX
                    && candidate.centerZ == existing.centerZ) {
                return AddResult.DUPLICATE_COVERAGE;
            }
        }

        int slot = firstEmptySlot();
        if (slot < 0) {
            return AddResult.FULL;
        }
        maps.set(slot, stack.copyWithCount(1));
        markChangedAndSync();
        return AddResult.ADDED;
    }

    public ItemStack takeLastMap() {
        for (int slot = CAPACITY - 1; slot >= 0; slot--) {
            ItemStack map = maps.get(slot);
            if (map.isEmpty()) {
                continue;
            }
            maps.set(slot, ItemStack.EMPTY);
            markChangedAndSync();
            return map;
        }
        return ItemStack.EMPTY;
    }

    public List<ItemStack> getMapCopies() {
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack map : maps) {
            if (!map.isEmpty()) {
                result.add(map.copy());
            }
        }
        return List.copyOf(result);
    }

    public int getMapCount() {
        int count = 0;
        for (ItemStack map : maps) {
            if (!map.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private int firstEmptySlot() {
        for (int slot = 0; slot < CAPACITY; slot++) {
            if (maps.get(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    private void markChangedAndSync() {
        setChanged();
        if (level == null) {
            return;
        }
        BlockState state = getBlockState();
        if (state.hasProperty(SurveyorsMapTableBlock.HAS_MAPS)) {
            BlockState updated = state.setValue(
                    SurveyorsMapTableBlock.HAS_MAPS,
                    getMapCount() > 0
            );
            if (!updated.equals(state)) {
                level.setBlock(worldPosition, updated, Block.UPDATE_CLIENTS);
                state = updated;
            }
        }
        level.sendBlockUpdated(
                worldPosition,
                state,
                state,
                Block.UPDATE_CLIENTS
        );
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        maps.clear();
        ContainerHelper.loadAllItems(input, maps);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, maps);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public enum AddResult {
        ADDED,
        FULL,
        DUPLICATE,
        DUPLICATE_COVERAGE,
        WRONG_DIMENSION,
        WRONG_SCALE,
        INVALID
    }
}
