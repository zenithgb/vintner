package com.zenith.vintner.wine;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Inspectable quality contributions carried by every modern wine batch.
 * Scores intentionally remain compact and data-driven so later roadmap
 * systems can refine a stage without replacing bottle metadata.
 */
public record WineQualityProfile(
        int foundation,
        int vineyard,
        int processing,
        int fermentation,
        int ageing,
        int storage
) {
    public static final int VERSION = 1;

    public WineQualityProfile {
        foundation = Math.clamp(foundation, 0, 100);
        vineyard = Math.clamp(vineyard, 0, 60);
        processing = Math.clamp(processing, -10, 10);
        fermentation = Math.clamp(fermentation, -10, 10);
        ageing = Math.clamp(ageing, -10, 15);
        storage = Math.clamp(storage, -30, 15);
    }

    public static WineQualityProfile vineyard(int score) {
        return new WineQualityProfile(0, score, 0, 0, 0, 0);
    }

    public static WineQualityProfile legacy(WineQuality quality) {
        return new WineQualityProfile(
                quality.baselineScore(),
                0,
                0,
                0,
                0,
                0
        );
    }

    public int score() {
        return Math.clamp(
                foundation
                        + vineyard
                        + processing
                        + fermentation
                        + ageing
                        + storage,
                0,
                100
        );
    }

    public WineQuality quality() {
        return WineQuality.fromScore(score());
    }

    public WineQualityProfile withProcessing(int score) {
        return new WineQualityProfile(
                foundation,
                vineyard,
                score,
                fermentation,
                ageing,
                storage
        );
    }

    public WineQualityProfile withFermentation(int score) {
        return new WineQualityProfile(
                foundation,
                vineyard,
                processing,
                score,
                ageing,
                storage
        );
    }

    public WineQualityProfile withAgeing(int score) {
        return new WineQualityProfile(
                foundation,
                vineyard,
                processing,
                fermentation,
                score,
                storage
        );
    }

    public WineQualityProfile withStorage(int score) {
        return new WineQualityProfile(
                foundation,
                vineyard,
                processing,
                fermentation,
                ageing,
                score
        );
    }

    public void save(ValueOutput output, String prefix) {
        output.putInt(prefix + "ProfileVersion", VERSION);
        output.putInt(prefix + "Foundation", foundation);
        output.putInt(prefix + "Vineyard", vineyard);
        output.putInt(prefix + "Processing", processing);
        output.putInt(prefix + "Fermentation", fermentation);
        output.putInt(prefix + "Ageing", ageing);
        output.putInt(prefix + "Storage", storage);
    }

    public static WineQualityProfile load(
            ValueInput input,
            String prefix,
            WineQuality legacyQuality
    ) {
        if (input.getIntOr(prefix + "ProfileVersion", 0) <= 0) {
            return legacy(legacyQuality);
        }

        return new WineQualityProfile(
                input.getIntOr(prefix + "Foundation", 0),
                input.getIntOr(prefix + "Vineyard", 0),
                input.getIntOr(prefix + "Processing", 0),
                input.getIntOr(prefix + "Fermentation", 0),
                input.getIntOr(prefix + "Ageing", 0),
                input.getIntOr(prefix + "Storage", 0)
        );
    }
}
