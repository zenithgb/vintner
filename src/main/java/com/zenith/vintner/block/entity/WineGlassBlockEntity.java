package com.zenith.vintner.block.entity;

import com.zenith.vintner.registry.ModBlockEntities;
import com.zenith.vintner.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores the exact metadata-bearing glass stacks in one tabletop setting. */
public final class WineGlassBlockEntity extends BlockEntity {
    public static final int CAPACITY = 4;

    private final List<ItemStack> glasses = new ArrayList<>(CAPACITY);

    public WineGlassBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WINE_GLASSES, pos, state);
    }

    public boolean addGlass(ItemStack stack) {
        if (glasses.size() >= CAPACITY || !isGlass(stack)) {
            return false;
        }

        glasses.add(stack.copyWithCount(1));
        changedAndSync();
        return true;
    }

    public ItemStack takeGlass(int index) {
        if (index < 0 || index >= glasses.size()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = glasses.remove(index);
        changedAndSync();
        return result;
    }

    public List<ItemStack> getGlasses() {
        List<ItemStack> result = new ArrayList<>(glasses.size());

        for (ItemStack glass : glasses) {
            result.add(glass.copy());
        }

        return Collections.unmodifiableList(result);
    }

    public List<ItemStack> takeAll() {
        List<ItemStack> result = new ArrayList<>(glasses.size());

        for (ItemStack glass : glasses) {
            result.add(glass);
        }

        glasses.clear();
        changedAndSync();
        return result;
    }

    public int size() {
        return glasses.size();
    }

    public boolean isFull() {
        return glasses.size() >= CAPACITY;
    }

    private void changedAndSync() {
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

    private static boolean isGlass(ItemStack stack) {
        return stack.is(ModItems.WINE_GLASS)
                || stack.is(ModItems.FILLED_WINE_GLASS);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        glasses.clear();

        for (int index = 0; index < CAPACITY; index++) {
            ItemStack stack = input.read(
                    "Glass" + index,
                    ItemStack.CODEC
            ).orElse(ItemStack.EMPTY);

            if (!stack.isEmpty() && isGlass(stack)) {
                glasses.add(stack.copyWithCount(1));
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        for (int index = 0; index < glasses.size(); index++) {
            output.store(
                    "Glass" + index,
                    ItemStack.CODEC,
                    glasses.get(index)
            );
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveWithoutMetadata(provider);
    }
}
