package com.zenith.vintner.block.entity;

import com.zenith.vintner.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class EstateManagementDeskBlockEntity
        extends BlockEntity {
    private ItemStack map = ItemStack.EMPTY;

    public EstateManagementDeskBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(ModBlockEntities.ESTATE_MANAGEMENT_DESK, pos, state);
    }

    public ItemStack getMapCopy() {
        return map.isEmpty() ? ItemStack.EMPTY : map.copy();
    }

    public void setMap(ItemStack stack) {
        map = stack.isEmpty()
                ? ItemStack.EMPTY
                : stack.copyWithCount(1);
        markChangedAndSync();
    }

    public ItemStack takeMap() {
        ItemStack result = getMapCopy();
        map = ItemStack.EMPTY;
        markChangedAndSync();
        return result;
    }

    private void markChangedAndSync() {
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(
                    worldPosition,
                    state,
                    state,
                    Block.UPDATE_CLIENTS
            );
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        map = input.read("Map", ItemStack.CODEC)
                .orElse(ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!map.isEmpty()) {
            output.store("Map", ItemStack.CODEC, map);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(
            HolderLookup.Provider provider
    ) {
        return saveWithoutMetadata(provider);
    }
}
