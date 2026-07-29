package com.zenith.vintner.wine;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class WineMetadata {
    private static final String VINTAGE_KEY = "VintnerVintage";
    private static final String QUALITY_KEY = "VintnerQuality";
    private static final String BATCH_ID_KEY = "VintnerBatchId";
    private static final String PROFILE_SEED_KEY =
            "VintnerProfileSeed";
    private static final String BOTTLED_AT_KEY = "VintnerBottledAt";
    private static final String BOTTLE_AGE_KEY = "VintnerBottleAge";
    private static final String STORAGE_DAMAGE_KEY =
            "VintnerStorageDamage";
    private static final String CELLAR_RATING_KEY =
            "VintnerCellarRating";
    private static final String BOTTLE_NUMBER_KEY =
            "VintnerBottleNumber";
    private static final String BATCH_BOTTLE_COUNT_KEY =
            "VintnerBatchBottleCount";
    private static final String STORAGE_POOR_TICKS_KEY =
            "VintnerStoragePoorTicks";
    private static final String STORAGE_BASIC_TICKS_KEY =
            "VintnerStorageBasicTicks";
    private static final String STORAGE_GOOD_TICKS_KEY =
            "VintnerStorageGoodTicks";
    private static final String STORAGE_IDEAL_TICKS_KEY =
            "VintnerStorageIdealTicks";

    /*
     * One Minecraft year is currently treated as 96 in-game days.
     * This is intentionally isolated here so later seasons or calendar
     * compatibility can replace the calculation cleanly.
     */
    private static final long DAYS_PER_YEAR = 96L;

    private WineMetadata() {
    }

    public static int vintageFromGameTime(long gameTime) {
        long day = Math.max(0L, gameTime / 24000L);
        return Math.toIntExact((day / DAYS_PER_YEAR) + 1L);
    }

    public static void apply(
            ItemStack stack,
            int vintage,
            WineQuality quality
    ) {
        CompoundTag tag = getTagCopy(stack);
        tag.putInt(VINTAGE_KEY, Math.max(1, vintage));
        tag.putInt(QUALITY_KEY, quality.id());

        stack.set(
                DataComponents.CUSTOM_DATA,
                CustomData.of(tag)
        );
    }

    public static void ensureDefaults(ItemStack stack) {
        if (!hasMetadata(stack)) {
            apply(stack, 1, WineQuality.COMMON);
        }
    }

    public static void ensureBatchIdentity(
            ItemStack stack,
            long suggestedBatchId
    ) {
        ensureDefaults(stack);

        CompoundTag tag = getTagCopy(stack);

        if (!tag.contains(BATCH_ID_KEY)) {
            long batchId = suggestedBatchId == 0L
                    ? 1L
                    : suggestedBatchId;
            tag.putLong(BATCH_ID_KEY, batchId);
            tag.putInt(
                    PROFILE_SEED_KEY,
                    profileSeed(batchId, vintage(stack), quality(stack))
            );
            setTag(stack, tag);
        }
    }

    public static long createBatchId(
            long gameTime,
            BlockPos position
    ) {
        long value = gameTime
                ^ Long.rotateLeft(position.asLong(), 21)
                ^ 0x6A09E667F3BCC909L;
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        value ^= value >>> 31;
        return value == 0L ? 1L : value;
    }

    public static boolean hasMetadata(ItemStack stack) {
        CompoundTag tag = getTagCopy(stack);

        return tag.contains(VINTAGE_KEY)
                && tag.contains(QUALITY_KEY);
    }

    public static int vintage(ItemStack stack) {
        return getTagCopy(stack).getIntOr(VINTAGE_KEY, 1);
    }

    public static WineQuality quality(ItemStack stack) {
        int qualityId = getTagCopy(stack).getIntOr(
                QUALITY_KEY,
                WineQuality.COMMON.id()
        );

        return WineQuality.byId(qualityId);
    }

    public static long batchId(ItemStack stack) {
        return getTagCopy(stack).getLongOr(BATCH_ID_KEY, 0L);
    }

    public static int tastingProfileSeed(ItemStack stack) {
        CompoundTag tag = getTagCopy(stack);
        long batchId = tag.getLongOr(BATCH_ID_KEY, 0L);

        return tag.getIntOr(
                PROFILE_SEED_KEY,
                profileSeed(
                        batchId,
                        vintage(stack),
                        quality(stack)
                )
        );
    }

    public static void copyBatchMetadata(
            ItemStack source,
            ItemStack target
    ) {
        target.set(
                DataComponents.CUSTOM_DATA,
                CustomData.of(getTagCopy(source))
        );
    }

    public static void markBottled(
            ItemStack stack,
            long gameTime
    ) {
        CompoundTag tag = getTagCopy(stack);
        tag.putLong(BOTTLED_AT_KEY, Math.max(0L, gameTime));
        tag.putLong(BOTTLE_AGE_KEY, 0L);
        tag.putInt(STORAGE_DAMAGE_KEY, 0);
        tag.putInt(
                CELLAR_RATING_KEY,
                CellarRating.BASIC.id()
        );
        for (CellarRating rating : CellarRating.values()) {
            tag.putLong(storageTicksKey(rating), 0L);
        }
        setTag(stack, tag);
    }

    public static void assignBottleNumber(
            ItemStack stack,
            int bottleNumber,
            int batchBottleCount
    ) {
        CompoundTag tag = getTagCopy(stack);
        int safeBatchCount = Math.max(1, batchBottleCount);
        int safeBottleNumber = Math.clamp(
                bottleNumber,
                1,
                safeBatchCount
        );
        tag.putInt(BOTTLE_NUMBER_KEY, safeBottleNumber);
        tag.putInt(BATCH_BOTTLE_COUNT_KEY, safeBatchCount);
        setTag(stack, tag);
    }

    public static int bottleNumber(ItemStack stack) {
        return getTagCopy(stack).getIntOr(BOTTLE_NUMBER_KEY, 0);
    }

    public static int batchBottleCount(ItemStack stack) {
        return getTagCopy(stack).getIntOr(
                BATCH_BOTTLE_COUNT_KEY,
                0
        );
    }

    public static long bottledAt(ItemStack stack) {
        return getTagCopy(stack).getLongOr(BOTTLED_AT_KEY, 0L);
    }

    public static long bottleAge(ItemStack stack) {
        return getTagCopy(stack).getLongOr(BOTTLE_AGE_KEY, 0L);
    }

    public static long bottleAgeDays(ItemStack stack) {
        return bottleAge(stack) / 24000L;
    }

    public static int storageDamage(ItemStack stack) {
        return getTagCopy(stack).getIntOr(
                STORAGE_DAMAGE_KEY,
                0
        );
    }

    public static CellarRating lastCellarRating(ItemStack stack) {
        return CellarRating.byId(
                getTagCopy(stack).getIntOr(
                        CELLAR_RATING_KEY,
                        CellarRating.BASIC.id()
                )
        );
    }

    public static long storageTicks(
            ItemStack stack,
            CellarRating rating
    ) {
        return getTagCopy(stack).getLongOr(
                storageTicksKey(rating),
                0L
        );
    }

    public static long totalStorageTicks(ItemStack stack) {
        long total = 0L;

        for (CellarRating rating : CellarRating.values()) {
            total = saturatedAdd(total, storageTicks(stack, rating));
        }

        return total;
    }

    public static long totalStorageDays(ItemStack stack) {
        return totalStorageTicks(stack) / 24000L;
    }

    public static CellarRating dominantCellarRating(
            ItemStack stack
    ) {
        CellarRating lastRating = lastCellarRating(stack);
        CellarRating dominant = lastRating;
        long longestDuration = -1L;

        for (CellarRating rating : CellarRating.values()) {
            long duration = storageTicks(stack, rating);

            if (duration > longestDuration
                    || (duration == longestDuration
                    && rating == lastRating)) {
                dominant = rating;
                longestDuration = duration;
            }
        }

        return dominant;
    }

    public static void ageBottle(
            ItemStack stack,
            long elapsedTicks,
            CellarRating rating
    ) {
        if (elapsedTicks <= 0) {
            return;
        }

        CompoundTag tag = getTagCopy(stack);
        long age = tag.getLongOr(BOTTLE_AGE_KEY, 0L);
        int damage = tag.getIntOr(STORAGE_DAMAGE_KEY, 0);

        age = saturatedAdd(
                age,
                Math.max(
                        1L,
                        Math.round(elapsedTicks * rating.ageRate())
                )
        );
        long accumulatedDamage = (long) damage
                + rating.storageDamage(elapsedTicks);
        damage = (int) Math.min(
                Integer.MAX_VALUE,
                accumulatedDamage
        );

        tag.putLong(BOTTLE_AGE_KEY, age);
        tag.putInt(STORAGE_DAMAGE_KEY, damage);
        tag.putInt(CELLAR_RATING_KEY, rating.id());
        String historyKey = storageTicksKey(rating);
        tag.putLong(
                historyKey,
                saturatedAdd(
                        tag.getLongOr(historyKey, 0L),
                        elapsedTicks
                )
        );
        setTag(stack, tag);
    }

    public static WineAgeStage ageStage(ItemStack stack) {
        return WineAgeStage.from(
                bottleAge(stack),
                storageDamage(stack),
                quality(stack)
        );
    }

    public static void improveQuality(ItemStack stack) {
        apply(
                stack,
                vintage(stack),
                quality(stack).improved()
        );
    }

    public static boolean matchesBatch(
            ItemStack first,
            ItemStack second
    ) {
        if (vintage(first) != vintage(second)
                || quality(first) != quality(second)) {
            return false;
        }

        long firstBatch = batchId(first);
        long secondBatch = batchId(second);

        /*
         * Grapes and legacy stacks do not necessarily have a batch yet.
         * Once both stacks have an identity, never blend distinct batches.
         */
        return firstBatch == 0L
                || secondBatch == 0L
                || firstBatch == secondBatch;
    }

    public static Component vintageTooltip(ItemStack stack) {
        return Component.translatable(
                "tooltip.vintner.vintage",
                vintage(stack)
        );
    }

    public static Component qualityTooltip(ItemStack stack) {
        return Component.translatable(
                "tooltip.vintner.quality",
                quality(stack).displayName()
        );
    }

    public static Component batchTooltip(ItemStack stack) {
        return Component.translatable(
                "tooltip.vintner.batch",
                batchCode(stack)
        );
    }

    public static String batchCode(ItemStack stack) {
        long batchId = batchId(stack);

        if (batchId == 0L) {
            return "Legacy";
        }

        String encoded = Long.toUnsignedString(batchId, 36)
                .toUpperCase(java.util.Locale.ROOT);
        int start = Math.max(0, encoded.length() - 6);
        return encoded.substring(start);
    }

    private static int profileSeed(
            long batchId,
            int vintage,
            WineQuality quality
    ) {
        long mixed = batchId
                ^ ((long) vintage << 32)
                ^ quality.id() * 0x9E3779B97F4A7C15L;
        return Long.hashCode(mixed);
    }

    private static String storageTicksKey(CellarRating rating) {
        return switch (rating) {
            case POOR -> STORAGE_POOR_TICKS_KEY;
            case BASIC -> STORAGE_BASIC_TICKS_KEY;
            case GOOD -> STORAGE_GOOD_TICKS_KEY;
            case IDEAL -> STORAGE_IDEAL_TICKS_KEY;
        };
    }

    private static long saturatedAdd(long first, long second) {
        if (second > 0L && first > Long.MAX_VALUE - second) {
            return Long.MAX_VALUE;
        }
        return first + second;
    }

    private static CompoundTag getTagCopy(ItemStack stack) {
        CustomData data = stack.getOrDefault(
                DataComponents.CUSTOM_DATA,
                CustomData.EMPTY
        );

        return data.copyTag();
    }

    private static void setTag(
            ItemStack stack,
            CompoundTag tag
    ) {
        stack.set(
                DataComponents.CUSTOM_DATA,
                CustomData.of(tag)
        );
    }
}
