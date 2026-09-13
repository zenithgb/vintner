package com.zenith.vintner.block.entity;

import com.zenith.vintner.block.WineBasketBlock;
import com.zenith.vintner.item.WineItem;
import com.zenith.vintner.registry.ModBlockEntities;
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

/** One exact bottle stack; display never normalizes or ages its contents. */
public final class WineBasketBlockEntity extends BlockEntity {
    private ItemStack bottle = ItemStack.EMPTY;

    public WineBasketBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WINE_BASKET, pos, state);
    }

    public synchronized boolean canInsert(ItemStack stack) {
        return bottle.isEmpty() && isCompatibleBottle(stack);
    }

    public static boolean isCompatibleBottle(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof WineItem;
    }

    public synchronized boolean insertBottle(ItemStack stack) {
        if ((level != null && level.isClientSide()) || !canInsert(stack)) {
            return false;
        }
        // ItemStack copying retains all components, including unknown future
        // components. Do not run WineMetadata.ensureDefaults on stored wine.
        bottle = stack.copyWithCount(1);
        changedAndSync();
        return true;
    }

    public synchronized ItemStack getBottleCopy() {
        return bottle.copy();
    }

    public synchronized ItemStack removeBottle() {
        if ((level != null && level.isClientSide()) || bottle.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = drainBottle();
        changedAndSync();
        return result;
    }

    public ItemStack takeBottle() {
        return removeBottle();
    }

    private ItemStack drainBottle() {
        ItemStack result = bottle;
        bottle = ItemStack.EMPTY;
        setChanged();
        return result;
    }

    private void changedAndSync() {
        setChanged();
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockState state = getBlockState();
        BlockState updated = state.setValue(
                WineBasketBlock.HAS_BOTTLE, !bottle.isEmpty()
        );
        if (!updated.equals(state)) {
            level.setBlock(worldPosition, updated, Block.UPDATE_ALL);
        }
        // A packet is also needed when occupancy is unchanged: the renderer
        // and nearby-wine identification consume the actual stack components.
        level.sendBlockUpdated(
                worldPosition, state, updated, Block.UPDATE_CLIENTS
        );
    }

    @Override
    public synchronized void preRemoveSideEffects(
            BlockPos pos, BlockState state
    ) {
        // Vanilla invokes this for survival, creative and explosion removal.
        // Drain here exactly once; the block's loot table drops only the basket.
        // Do not synchronize the old blockstate while it is being removed.
        if (level != null && !level.isClientSide() && !bottle.isEmpty()) {
            Block.popResource(level, pos, drainBottle());
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected synchronized void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ItemStack loaded = input.read("Bottle", ItemStack.CODEC)
                .orElse(ItemStack.EMPTY);
        bottle = isCompatibleBottle(loaded)
                ? loaded.copyWithCount(1) : ItemStack.EMPTY;
    }

    @Override
    protected synchronized void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!bottle.isEmpty()) {
            output.store("Bottle", ItemStack.CODEC, bottle);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveWithoutMetadata(provider);
    }
}
