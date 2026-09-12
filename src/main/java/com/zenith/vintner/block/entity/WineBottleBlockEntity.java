package com.zenith.vintner.block.entity;

import com.zenith.vintner.block.WineBottleBlock;
import com.zenith.vintner.registry.ModBlockEntities;
import com.zenith.vintner.wine.WineMetadata;
import com.zenith.vintner.wine.WineStyle;
import com.zenith.vintner.item.WineItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class WineBottleBlockEntity extends BlockEntity {
    private ItemStack bottle = ItemStack.EMPTY;

    public WineBottleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WINE_BOTTLE, pos, state);
    }

    public void setBottle(ItemStack stack) {
        bottle = stack.copyWithCount(1);
        setChanged();
        syncVisualState();
        syncToClient();
    }

    public ItemStack getBottleCopy() {
        return bottle.isEmpty() ? ItemStack.EMPTY : bottle.copy();
    }

    public ItemStack takeBottle() {
        if (bottle.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = bottle;
        bottle = ItemStack.EMPTY;
        setChanged();
        return result;
    }

    public int servings() {
        if (bottle.isEmpty() || bottle.is(Items.GLASS_BOTTLE)) {
            return 0;
        }

        return WineMetadata.servings(bottle);
    }

    private void syncVisualState() {
        if (level == null
                || bottle.isEmpty()
                || !getBlockState().hasProperty(
                WineBottleBlock.SERVINGS
        )
                || !getBlockState().hasProperty(
                WineBottleBlock.WHITE_WINE
        )) {
            return;
        }

        int servings = servings();
        BlockState state = getBlockState();
        boolean whiteWine = state.getValue(WineBottleBlock.WHITE_WINE);

        if (bottle.getItem() instanceof WineItem) {
            whiteWine = WineMetadata.wineStyle(bottle) == WineStyle.WHITE;
        }

        BlockState updated = state
                .setValue(WineBottleBlock.SERVINGS, servings)
                .setValue(WineBottleBlock.WHITE_WINE, whiteWine);

        if (updated == state) {
            return;
        }

        level.setBlock(
                worldPosition,
                updated,
                Block.UPDATE_ALL
        );
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide()) {
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
        bottle = input.read("Bottle", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
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
    public net.minecraft.nbt.CompoundTag getUpdateTag(
            HolderLookup.Provider provider
    ) {
        return saveWithoutMetadata(provider);
    }
}
