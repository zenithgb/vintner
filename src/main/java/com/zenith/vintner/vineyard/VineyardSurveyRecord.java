package com.zenith.vintner.vineyard;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.Locale;
import java.util.Optional;

/** A compact, persistent field bookmark stored inside an Almanac. */
public record VineyardSurveyRecord(
        String dimension,
        BlockPos position,
        String climate,
        String soil,
        int siteScore,
        String rating
) {
    private static final String PRESENT = "VintnerSurveyPresent";
    private static final String DIMENSION = "VintnerSurveyDimension";
    private static final String X = "VintnerSurveyX";
    private static final String Y = "VintnerSurveyY";
    private static final String Z = "VintnerSurveyZ";
    private static final String CLIMATE = "VintnerSurveyClimate";
    private static final String SOIL = "VintnerSurveySoil";
    private static final String SCORE = "VintnerSurveyScore";
    private static final String RATING = "VintnerSurveyRating";

    public VineyardSurveyRecord {
        dimension = dimension == null ? "minecraft:overworld" : dimension;
        siteScore = Math.clamp(siteScore, 0, 100);
    }

    public static VineyardSurveyRecord capture(
            Level level,
            BlockPos requestedPos,
            TerroirReport report
    ) {
        return new VineyardSurveyRecord(
                level.dimension().identifier().toString(),
                TerroirEvaluator.resolveSitePosition(level, requestedPos),
                report.climate().band().name().toLowerCase(Locale.ROOT),
                report.soil().type().name().toLowerCase(Locale.ROOT),
                report.siteScore(),
                report.siteRating().name().toLowerCase(Locale.ROOT)
        );
    }

    public void save(ItemStack almanac) {
        CompoundTag tag = almanac.getOrDefault(
                DataComponents.CUSTOM_DATA,
                CustomData.EMPTY
        ).copyTag();
        tag.putBoolean(PRESENT, true);
        tag.putString(DIMENSION, dimension);
        tag.putInt(X, position.getX());
        tag.putInt(Y, position.getY());
        tag.putInt(Z, position.getZ());
        tag.putString(CLIMATE, climate);
        tag.putString(SOIL, soil);
        tag.putInt(SCORE, siteScore);
        tag.putString(RATING, rating);
        almanac.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static Optional<VineyardSurveyRecord> read(ItemStack almanac) {
        CompoundTag tag = almanac.getOrDefault(
                DataComponents.CUSTOM_DATA,
                CustomData.EMPTY
        ).copyTag();

        if (!tag.getBooleanOr(PRESENT, false)) {
            return Optional.empty();
        }

        return Optional.of(new VineyardSurveyRecord(
                tag.getStringOr(DIMENSION, "minecraft:overworld"),
                new BlockPos(
                        tag.getIntOr(X, 0),
                        tag.getIntOr(Y, 0),
                        tag.getIntOr(Z, 0)
                ),
                tag.getStringOr(CLIMATE, "temperate"),
                tag.getStringOr(SOIL, "loam"),
                tag.getIntOr(SCORE, 0),
                tag.getStringOr(RATING, "very_low")
        ));
    }
}
