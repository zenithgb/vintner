package com.zenith.vintner.estate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/** Persistent, bounded evidence used to calculate one estate's reputation. */
public record EstateReputationProfile(
        String ownerId,
        int registeredPlots,
        int plantedVines,
        int harvestedGrapes,
        List<Long> bottledBatches,
        List<Long> archivedBatches,
        int bestQuality,
        int facilityMask
) {
    public static final int MAX_TRACKED_BATCHES = 32;
    public static final Codec<EstateReputationProfile> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("owner_id")
                            .forGetter(EstateReputationProfile::ownerId),
                    Codec.INT.optionalFieldOf("registered_plots", 0)
                            .forGetter(
                                    EstateReputationProfile::registeredPlots
                            ),
                    Codec.INT.optionalFieldOf("planted_vines", 0)
                            .forGetter(EstateReputationProfile::plantedVines),
                    Codec.INT.optionalFieldOf("harvested_grapes", 0)
                            .forGetter(
                                    EstateReputationProfile::harvestedGrapes
                            ),
                    Codec.LONG.listOf()
                            .optionalFieldOf("bottled_batches", List.of())
                            .forGetter(
                                    EstateReputationProfile::bottledBatches
                            ),
                    Codec.LONG.listOf()
                            .optionalFieldOf("archived_batches", List.of())
                            .forGetter(
                                    EstateReputationProfile::archivedBatches
                            ),
                    Codec.INT.optionalFieldOf("best_quality", 0)
                            .forGetter(EstateReputationProfile::bestQuality),
                    Codec.INT.optionalFieldOf("facility_mask", 0)
                            .forGetter(EstateReputationProfile::facilityMask)
            ).apply(instance, EstateReputationProfile::new));

    public EstateReputationProfile {
        ownerId = ownerId == null ? "" : ownerId;
        registeredPlots = Math.max(0, registeredPlots);
        plantedVines = Math.max(0, plantedVines);
        harvestedGrapes = Math.max(0, harvestedGrapes);
        bottledBatches = boundedDistinct(bottledBatches);
        archivedBatches = boundedDistinct(archivedBatches);
        bestQuality = Math.clamp(bestQuality, 0, 100);
        facilityMask = Math.max(0, facilityMask);
    }

    public static EstateReputationProfile empty(String ownerId) {
        return new EstateReputationProfile(
                ownerId,
                0,
                0,
                0,
                List.of(),
                List.of(),
                0,
                0
        );
    }

    public EstateReputationProfile sync(
            List<EstateLedgerEvent> events
    ) {
        int plots = 0;
        int vines = 0;
        int grapes = 0;
        int quality = bestQuality;
        LinkedHashSet<Long> bottled = new LinkedHashSet<>(bottledBatches);
        LinkedHashSet<Long> archived = new LinkedHashSet<>(archivedBatches);

        for (int index = events.size() - 1; index >= 0; index--) {
            EstateLedgerEvent event = events.get(index);
            switch (event.eventType()) {
                case PLOT_REGISTERED -> plots = saturatingAdd(plots, 1);
                case PLANTING -> vines = saturatingAdd(
                        vines,
                        event.amount()
                );
                case HARVEST -> grapes = saturatingAdd(
                        grapes,
                        event.amount()
                );
                case BOTTLING -> {
                    addBatch(bottled, event.batchId());
                    quality = Math.max(quality, event.quality());
                }
                case ARCHIVED -> {
                    addBatch(archived, event.batchId());
                    quality = Math.max(quality, event.quality());
                }
                default -> {
                }
            }
        }

        return new EstateReputationProfile(
                ownerId,
                Math.max(registeredPlots, plots),
                Math.max(plantedVines, vines),
                Math.max(harvestedGrapes, grapes),
                new ArrayList<>(bottled),
                new ArrayList<>(archived),
                quality,
                facilityMask
        );
    }

    public EstateReputationProfile withFacilities(int newFacilityMask) {
        return new EstateReputationProfile(
                ownerId,
                registeredPlots,
                plantedVines,
                harvestedGrapes,
                bottledBatches,
                archivedBatches,
                bestQuality,
                facilityMask | newFacilityMask
        );
    }

    public EstateReputationProfile record(
            LedgerEventType type,
            int amount,
            long batchId,
            int quality
    ) {
        int plots = registeredPlots;
        int vines = plantedVines;
        int grapes = harvestedGrapes;
        int best = bestQuality;
        LinkedHashSet<Long> bottled = new LinkedHashSet<>(bottledBatches);
        LinkedHashSet<Long> archived = new LinkedHashSet<>(archivedBatches);
        int safeAmount = Math.max(1, amount);

        switch (type) {
            case PLOT_REGISTERED -> plots = saturatingAdd(plots, 1);
            case PLANTING -> vines = saturatingAdd(vines, safeAmount);
            case HARVEST -> grapes = saturatingAdd(grapes, safeAmount);
            case BOTTLING -> {
                addBatch(bottled, batchId);
                best = Math.max(best, quality);
            }
            case ARCHIVED -> {
                addBatch(archived, batchId);
                best = Math.max(best, quality);
            }
            default -> {
            }
        }

        return new EstateReputationProfile(
                ownerId,
                plots,
                vines,
                grapes,
                new ArrayList<>(bottled),
                new ArrayList<>(archived),
                best,
                facilityMask
        );
    }

    public int score() {
        int plotPoints = Math.min(24, registeredPlots * 6);
        int plantingPoints = Math.min(20, plantedVines / 8);
        int harvestPoints = Math.min(20, harvestedGrapes / 16);
        int bottlingPoints = Math.min(30, bottledBatches.size() * 3);
        int archivePoints = Math.min(40, archivedBatches.size() * 5);
        int qualityPoints = bestQuality >= 90
                ? 35
                : bestQuality >= 80
                ? 20
                : bestQuality >= 70
                ? 10
                : bestQuality >= 60
                ? 5
                : 0;
        int facilityPoints = Integer.bitCount(facilityMask) * 10;
        return 5
                + plotPoints
                + plantingPoints
                + harvestPoints
                + bottlingPoints
                + archivePoints
                + qualityPoints
                + facilityPoints;
    }

    public EstateReputationTier tier() {
        return EstateReputationTier.forScore(score());
    }

    private static void addBatch(
            LinkedHashSet<Long> batches,
            long batchId
    ) {
        if (batchId != 0L) {
            batches.add(batchId);
        }
        while (batches.size() > MAX_TRACKED_BATCHES) {
            batches.remove(batches.getFirst());
        }
    }

    private static List<Long> boundedDistinct(List<Long> values) {
        LinkedHashSet<Long> distinct = new LinkedHashSet<>();
        if (values != null) {
            for (Long value : values) {
                if (value != null && value != 0L) {
                    distinct.add(value);
                }
            }
        }
        while (distinct.size() > MAX_TRACKED_BATCHES) {
            distinct.remove(distinct.getFirst());
        }
        return List.copyOf(distinct);
    }

    private static int saturatingAdd(int current, int amount) {
        long total = (long) current + Math.max(0, amount);
        return (int) Math.min(Integer.MAX_VALUE, total);
    }
}
