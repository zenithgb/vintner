package com.zenith.vintner.block.entity;

import com.zenith.vintner.block.AgingBarrelBlock;
import com.zenith.vintner.registry.ModBlockEntities;
import com.zenith.vintner.registry.ModItems;
import com.zenith.vintner.wine.WinemakingEffects;
import com.zenith.vintner.wine.WineMetadata;
import com.zenith.vintner.wine.WineProvenance;
import com.zenith.vintner.wine.WineQuality;
import com.zenith.vintner.wine.WineQualityProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
    private int bottlesTaken;
    private int agingProgress;
    private boolean ready;
    private int vintage = 1;
    private WineQualityProfile qualityProfile =
            WineQualityProfile.legacy(WineQuality.TABLE);
    private WineProvenance provenance = WineProvenance.legacy();
    private long batchId;
    private int lastComparatorSignal = -1;

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
        barrel.updateComparatorSignal();

        if (barrel.wineType == 0
                || barrel.bottleCount < CAPACITY
                || barrel.ready) {
            return;
        }

        barrel.agingProgress++;

        if (level instanceof ServerLevel serverLevel
                && shouldEmit(
                        barrel.agingProgress,
                        pos,
                        200
                )) {
            WinemakingEffects.agingActive(
                    serverLevel,
                    pos
            );
        }

        if (barrel.agingProgress >= AGING_TIME) {
            barrel.agingProgress = AGING_TIME;
            barrel.ready = true;
            barrel.markChangedAndSync();

            if (level instanceof ServerLevel serverLevel) {
                WinemakingEffects.agingComplete(
                        serverLevel,
                        pos
                );
            }
        } else if (barrel.agingProgress % 20 == 0) {
            barrel.setChanged();
        }

        barrel.updateComparatorSignal();
    }

    private static boolean shouldEmit(
            int progress,
            BlockPos pos,
            int interval
    ) {
        int phase = Math.floorMod(
                pos.getX() * 31
                        + pos.getY() * 17
                        + pos.getZ(),
                interval
        );

        return progress % interval == phase;
    }

    public boolean canInsert(ItemStack stack) {
        int offeredType = getWineType(stack);

        if (offeredType == 0 || ready) {
            return false;
        }

        if (bottleCount >= CAPACITY) {
            return false;
        }

        if (wineType == 0) {
            return true;
        }

        return wineType == offeredType
                && vintage == WineMetadata.vintage(stack)
                && qualityProfile.equals(
                        WineMetadata.qualityProfile(stack)
                )
                && batchMatches(stack);
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

        int offeredType = getWineType(stack);

        if (wineType == 0) {
            wineType = offeredType;
            agingProgress = 0;
            vintage = WineMetadata.vintage(stack);
            qualityProfile = WineMetadata.qualityProfile(stack);
            provenance = WineMetadata.provenance(stack);
            batchId = WineMetadata.batchId(stack);
        }

        bottleCount++;
        markChangedAndSync();
        return true;
    }

    public boolean isReady() {
        return ready && bottleCount > 0;
    }

    public int getWineType() {
        return wineType;
    }

    public int getBottleCount() {
        return bottleCount;
    }

    public int getProgressPercent() {
        if (isReady()) {
            return 100;
        }

        return Math.min(
                100,
                agingProgress * 100 / AGING_TIME
        );
    }

    public int getComparatorSignal() {
        if (wineType == 0 || bottleCount <= 0) {
            return 0;
        }

        if (isReady()) {
            return 15;
        }

        return 1 + Math.min(
                13,
                agingProgress * 14 / AGING_TIME
        );
    }

    public ItemStack takeOneAgedWine() {
        if (!isReady()) {
            return ItemStack.EMPTY;
        }

        Item agedWine = wineType == 1
                ? ModItems.AGED_RED_WINE
                : ModItems.AGED_WHITE_WINE;

        ItemStack result = new ItemStack(agedWine);

        WineMetadata.applyProfile(
                result,
                vintage,
                qualityProfile.withAgeing(10)
        );
        WineMetadata.applyProvenance(result, provenance);
        WineMetadata.ensureBatchIdentity(result, batchId);
        WineMetadata.markBottled(
                result,
                level == null ? 0L : level.getGameTime()
        );
        WineMetadata.assignBottleNumber(
                result,
                bottlesTaken + 1,
                bottlesTaken + bottleCount
        );

        bottlesTaken++;
        bottleCount--;

        if (bottleCount <= 0) {
            resetBatch();
        }

        markChangedAndSync();
        return result;
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

        ItemStack result = new ItemStack(
                storedItem,
                bottleCount
        );

        WineMetadata.applyProfile(
                result,
                vintage,
                ready
                        ? qualityProfile.withAgeing(10)
                        : qualityProfile
        );
        WineMetadata.applyProvenance(result, provenance);
        WineMetadata.ensureBatchIdentity(result, batchId);

        return result;
    }

    private boolean batchMatches(ItemStack stack) {
        long offeredBatch = WineMetadata.batchId(stack);

        return batchId == 0L
                || offeredBatch == 0L
                || batchId == offeredBatch;
    }

    private void resetBatch() {
        wineType = 0;
        bottleCount = 0;
        agingProgress = 0;
        ready = false;
        vintage = 1;
        qualityProfile =
                WineQualityProfile.legacy(WineQuality.TABLE);
        provenance = WineProvenance.legacy();
        batchId = 0L;
        bottlesTaken = 0;
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

        if (!(state.getBlock() instanceof AgingBarrelBlock)) {
            return;
        }

        int status = ready
                ? 2
                : bottleCount >= CAPACITY ? 1 : 0;

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
        vintage = input.getIntOr("Vintage", 1);
        WineQuality legacyQuality = WineQuality.byId(
                input.getIntOr(
                        "Quality",
                        WineQuality.TABLE.id()
                )
        );
        qualityProfile = WineQualityProfile.load(
                input,
                "Quality",
                legacyQuality
        );
        provenance = WineProvenance.load(
                input,
                "Provenance"
        );
        batchId = input.getLongOr("BatchId", 0L);
        bottlesTaken = input.getIntOr("BottlesTaken", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putInt("WineType", wineType);
        output.putInt("BottleCount", bottleCount);
        output.putInt("AgingProgress", agingProgress);
        output.putBoolean("Ready", ready);
        output.putInt("Vintage", vintage);
        output.putInt("Quality", qualityProfile.quality().id());
        qualityProfile.save(output, "Quality");
        provenance.save(output, "Provenance");
        output.putLong("BatchId", batchId);
        output.putInt("BottlesTaken", bottlesTaken);
    }
}
