package com.zenith.vintner.wine;

import com.zenith.vintner.vineyard.GrapeCultivar;
import com.zenith.vintner.vineyard.GrapeVariety;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class WineMetadata {
    private static final String VINTAGE_KEY = "VintnerVintage";
    private static final String QUALITY_KEY = "VintnerQuality";
    private static final String QUALITY_PROFILE_VERSION_KEY =
            "VintnerQualityProfileVersion";
    private static final String QUALITY_FOUNDATION_KEY =
            "VintnerQualityFoundation";
    private static final String QUALITY_VINEYARD_KEY =
            "VintnerQualityVineyard";
    private static final String QUALITY_PROCESSING_KEY =
            "VintnerQualityProcessing";
    private static final String QUALITY_FERMENTATION_KEY =
            "VintnerQualityFermentation";
    private static final String QUALITY_AGEING_KEY =
            "VintnerQualityAgeing";
    private static final String QUALITY_STORAGE_KEY =
            "VintnerQualityStorage";
    private static final String BATCH_ID_KEY = "VintnerBatchId";
    private static final String PROFILE_SEED_KEY =
            "VintnerProfileSeed";
    private static final String PROVENANCE_VERSION_KEY =
            "VintnerProvenanceVersion";
    private static final String VARIETY_KEY = "VintnerVariety";
    private static final String HARVESTED_AT_KEY =
            "VintnerHarvestedAt";
    private static final String ORIGIN_DIMENSION_KEY =
            "VintnerOriginDimension";
    private static final String ORIGIN_X_KEY = "VintnerOriginX";
    private static final String ORIGIN_Y_KEY = "VintnerOriginY";
    private static final String ORIGIN_Z_KEY = "VintnerOriginZ";
    private static final String PRODUCER_ID_KEY =
            "VintnerProducerId";
    private static final String PRODUCER_NAME_KEY =
            "VintnerProducerName";
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
    private static final String AGING_VESSEL_KEY =
            "VintnerAgingVessel";
    private static final String ESTATE_NAME_KEY =
            "VintnerEstateName";
    private static final String WINE_STYLE_KEY =
            "VintnerWineStyle";

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
        applyProfile(
                stack,
                vintage,
                WineQualityProfile.legacy(quality)
        );
    }

    public static void applyProfile(
            ItemStack stack,
            int vintage,
            WineQualityProfile profile
    ) {
        CompoundTag tag = getTagCopy(stack);
        tag.putInt(VINTAGE_KEY, Math.max(1, vintage));
        writeQualityProfile(tag, profile);

        stack.set(
                DataComponents.CUSTOM_DATA,
                CustomData.of(tag)
        );
    }

    public static void ensureDefaults(ItemStack stack) {
        if (!hasMetadata(stack)) {
            apply(stack, 1, WineQuality.TABLE);
        }
        ensureIdentityDetails(stack);
    }

    private static void ensureIdentityDetails(ItemStack stack) {
        CompoundTag tag = getTagCopy(stack);
        boolean changed = false;

        if (!tag.contains(WINE_STYLE_KEY)) {
            tag.putString(WINE_STYLE_KEY, WineStyle.from(stack).id());
            changed = true;
        }
        if (!tag.contains(ESTATE_NAME_KEY)) {
            tag.putString(ESTATE_NAME_KEY, defaultEstateName(tag));
            changed = true;
        }
        if (changed) {
            setTag(stack, tag);
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
        return qualityProfile(stack).quality();
    }

    public static int qualityScore(ItemStack stack) {
        return qualityProfile(stack).score();
    }

    public static WineQualityProfile qualityProfile(ItemStack stack) {
        return readQualityProfile(getTagCopy(stack));
    }

    public static void setQualityProfile(
            ItemStack stack,
            WineQualityProfile profile
    ) {
        CompoundTag tag = getTagCopy(stack);
        writeQualityProfile(tag, profile);
        setTag(stack, tag);
    }

    private static WineQuality legacyQuality(CompoundTag tag) {
        int qualityId = tag.getIntOr(
                QUALITY_KEY,
                WineQuality.TABLE.id()
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

    public static void applyProvenance(
            ItemStack stack,
            WineProvenance provenance
    ) {
        CompoundTag tag = getTagCopy(stack);
        tag.putInt(PROVENANCE_VERSION_KEY, 1);
        tag.putString(VARIETY_KEY, provenance.variety());
        tag.putLong(
                HARVESTED_AT_KEY,
                provenance.harvestedAt()
        );
        tag.putString(
                ORIGIN_DIMENSION_KEY,
                provenance.originDimension()
        );
        tag.putInt(ORIGIN_X_KEY, provenance.originX());
        tag.putInt(ORIGIN_Y_KEY, provenance.originY());
        tag.putInt(ORIGIN_Z_KEY, provenance.originZ());
        tag.putString(
                PRODUCER_ID_KEY,
                provenance.producerId()
        );
        tag.putString(
                PRODUCER_NAME_KEY,
                provenance.producerName()
        );
        provenance.vintageConditions().write(tag);
        if (!tag.contains(ESTATE_NAME_KEY)
                || tag.getStringOr(ESTATE_NAME_KEY, "").isBlank()
                || "Independent Vineyard".equals(
                tag.getStringOr(ESTATE_NAME_KEY, "")
        )) {
            tag.putString(
                    ESTATE_NAME_KEY,
                    provenance.producerName().isBlank()
                            ? "Independent Vineyard"
                            : provenance.producerName() + " Vineyard"
            );
        }
        setTag(stack, tag);
    }

    public static WineProvenance provenance(ItemStack stack) {
        CompoundTag tag = getTagCopy(stack);

        if (tag.getIntOr(PROVENANCE_VERSION_KEY, 0) <= 0) {
            return WineProvenance.legacy();
        }

        return new WineProvenance(
                tag.getStringOr(
                        VARIETY_KEY,
                        WineProvenance.UNKNOWN
                ),
                tag.getLongOr(HARVESTED_AT_KEY, 0L),
                tag.getStringOr(
                        ORIGIN_DIMENSION_KEY,
                        WineProvenance.UNKNOWN
                ),
                tag.getIntOr(ORIGIN_X_KEY, 0),
                tag.getIntOr(ORIGIN_Y_KEY, 0),
                tag.getIntOr(ORIGIN_Z_KEY, 0),
                tag.getStringOr(PRODUCER_ID_KEY, ""),
                tag.getStringOr(PRODUCER_NAME_KEY, ""),
                WineVintageConditions.read(tag)
        );
    }

    /** Records cultivar identity without beginning batch provenance. */
    public static void applyCultivar(
            ItemStack stack,
            GrapeCultivar cultivar
    ) {
        CompoundTag tag = getTagCopy(stack);
        tag.putString(VARIETY_KEY, cultivar.serializedName());
        setTag(stack, tag);
    }

    public static GrapeCultivar cultivar(
            ItemStack stack,
            GrapeVariety fallback
    ) {
        return GrapeCultivar.fromName(
                getTagCopy(stack).getStringOr(VARIETY_KEY, ""),
                fallback
        );
    }

    public static void applyVintageConditions(
            ItemStack stack,
            WineVintageConditions conditions
    ) {
        CompoundTag tag = getTagCopy(stack);
        conditions.write(tag);
        setTag(stack, tag);
    }

    public static WineVintageConditions vintageConditions(
            ItemStack stack
    ) {
        return WineVintageConditions.read(getTagCopy(stack));
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

    public static void setAgingVessel(
            ItemStack stack,
            AgingVessel vessel
    ) {
        CompoundTag tag = getTagCopy(stack);
        tag.putString(AGING_VESSEL_KEY, vessel.id());
        setTag(stack, tag);
    }

    public static AgingVessel agingVessel(ItemStack stack) {
        return AgingVessel.byId(
                getTagCopy(stack).getStringOr(
                        AGING_VESSEL_KEY,
                        AgingVessel.OAK.id()
                )
        );
    }

    public static WineStyle wineStyle(ItemStack stack) {
        CompoundTag tag = getTagCopy(stack);
        return tag.contains(WINE_STYLE_KEY)
                ? WineStyle.byId(tag.getStringOr(
                        WINE_STYLE_KEY,
                        WineStyle.from(stack).id()
                ))
                : WineStyle.from(stack);
    }

    public static void setEstateName(ItemStack stack, String estateName) {
        CompoundTag tag = getTagCopy(stack);
        tag.putString(
                ESTATE_NAME_KEY,
                estateName == null || estateName.isBlank()
                        ? "Independent Vineyard"
                        : estateName.trim()
        );
        setTag(stack, tag);
    }

    public static String estateName(ItemStack stack) {
        CompoundTag tag = getTagCopy(stack);
        return tag.getStringOr(
                ESTATE_NAME_KEY,
                defaultEstateName(tag)
        );
    }

    public static int estimatedTradeValue(ItemStack stack) {
        return WineAppraisal.independent(stack).totalValue();
    }

    public static int settlementPrestige(ItemStack stack) {
        return WineAppraisal.independent(stack).prestige();
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

    public static long bottledDay(ItemStack stack) {
        return bottledAt(stack) / 24000L;
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
        WineQualityProfile profile = readQualityProfile(tag)
                .withStorage(storageContribution(tag));
        writeQualityProfile(tag, profile);
        setTag(stack, tag);
    }

    public static WineAgeStage ageStage(ItemStack stack) {
        String variety = provenance(stack).variety();
        float cultivarPotential = GrapeCultivar.isCultivarName(variety)
                ? GrapeCultivar.fromName(
                        variety,
                        GrapeVariety.RED
                ).ageingMultiplier()
                : 1.0F;
        return WineAgeStage.from(
                bottleAge(stack),
                storageDamage(stack),
                quality(stack),
                cultivarPotential
        );
    }

    public static boolean matchesBatch(
            ItemStack first,
            ItemStack second
    ) {
        if (!matchesBatchIdentity(first, second)
                || !qualityProfile(first).equals(
                qualityProfile(second)
        )) {
            return false;
        }

        return true;
    }

    public static boolean matchesBatchIdentity(
            ItemStack first,
            ItemStack second
    ) {
        if (vintage(first) != vintage(second)) {
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

    private static String defaultEstateName(CompoundTag tag) {
        String producer = tag.getStringOr(PRODUCER_NAME_KEY, "");
        return producer.isBlank()
                ? "Independent Vineyard"
                : producer + " Vineyard";
    }

    private static WineQualityProfile readQualityProfile(
            CompoundTag tag
    ) {
        if (tag.getIntOr(QUALITY_PROFILE_VERSION_KEY, 0) <= 0) {
            return WineQualityProfile.legacy(legacyQuality(tag));
        }

        return new WineQualityProfile(
                tag.getIntOr(QUALITY_FOUNDATION_KEY, 0),
                tag.getIntOr(QUALITY_VINEYARD_KEY, 0),
                tag.getIntOr(QUALITY_PROCESSING_KEY, 0),
                tag.getIntOr(QUALITY_FERMENTATION_KEY, 0),
                tag.getIntOr(QUALITY_AGEING_KEY, 0),
                tag.getIntOr(QUALITY_STORAGE_KEY, 0)
        );
    }

    private static void writeQualityProfile(
            CompoundTag tag,
            WineQualityProfile profile
    ) {
        tag.putInt(
                QUALITY_PROFILE_VERSION_KEY,
                WineQualityProfile.VERSION
        );
        tag.putInt(QUALITY_FOUNDATION_KEY, profile.foundation());
        tag.putInt(QUALITY_VINEYARD_KEY, profile.vineyard());
        tag.putInt(QUALITY_PROCESSING_KEY, profile.processing());
        tag.putInt(
                QUALITY_FERMENTATION_KEY,
                profile.fermentation()
        );
        tag.putInt(QUALITY_AGEING_KEY, profile.ageing());
        tag.putInt(QUALITY_STORAGE_KEY, profile.storage());
        tag.putInt(QUALITY_KEY, profile.quality().id());
    }

    private static int storageContribution(CompoundTag tag) {
        long idealDays = tag.getLongOr(
                STORAGE_IDEAL_TICKS_KEY,
                0L
        ) / 24000L;
        long goodDays = tag.getLongOr(
                STORAGE_GOOD_TICKS_KEY,
                0L
        ) / 24000L;
        long poorDays = tag.getLongOr(
                STORAGE_POOR_TICKS_KEY,
                0L
        ) / 24000L;
        long damageDays = tag.getIntOr(
                STORAGE_DAMAGE_KEY,
                0
        ) / 24000L;
        long score = idealDays / 4L
                + goodDays / 8L
                - poorDays / 2L
                - damageDays;

        return (int) Math.clamp(score, -30L, 15L);
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
