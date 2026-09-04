package com.zenith.vintner.estate;

import com.mojang.serialization.Codec;
import com.zenith.vintner.Vintner;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/** World-wide registry of the named plots belonging to each estate. */
public final class VineyardPlotSavedData extends SavedData {
    public static final int MAX_PLOTS_PER_ESTATE = 16;
    private static final Codec<VineyardPlotSavedData> CODEC =
            VineyardPlot.CODEC.listOf()
                    .optionalFieldOf("plots", List.of())
                    .xmap(
                            VineyardPlotSavedData::new,
                            VineyardPlotSavedData::allPlots
                    )
                    .codec();

    public static final SavedDataType<VineyardPlotSavedData> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(
                            Vintner.MOD_ID,
                            "vineyard_plots"
                    ),
                    VineyardPlotSavedData::new,
                    CODEC,
                    DataFixTypes.SAVED_DATA_COMMAND_STORAGE
            );

    private final List<VineyardPlot> plots = new ArrayList<>();

    public VineyardPlotSavedData() {
    }

    private VineyardPlotSavedData(List<VineyardPlot> plots) {
        this.plots.addAll(plots);
    }

    public static VineyardPlotSavedData get(ServerLevel level) {
        return level.getServer()
                .overworld()
                .getDataStorage()
                .computeIfAbsent(TYPE);
    }

    public List<VineyardPlot> plots(UUID ownerId) {
        String key = ownerId.toString();
        return plots.stream()
                .filter(plot -> plot.ownerId().equals(key))
                .toList();
    }

    public Optional<VineyardPlot> findContaining(
            UUID ownerId,
            ServerLevel level,
            BlockPos pos
    ) {
        String key = ownerId.toString();
        String dimension = level.dimension().identifier().toString();
        return plots.stream()
                .filter(plot -> plot.ownerId().equals(key))
                .filter(plot -> plot.contains(dimension, pos))
                .findFirst();
    }

    public Registration register(
            ServerPlayer owner,
            ServerLevel level,
            BlockPos first,
            BlockPos second,
            String name
    ) {
        VineyardPlot candidate = VineyardPlot.create(
                owner,
                level,
                first,
                second,
                name
        );

        if (!candidate.validSize()) {
            return new Registration(Status.TOO_LARGE, candidate);
        }

        String ownerKey = owner.getUUID().toString();
        String nameKey = candidate.name().toLowerCase(Locale.ROOT);
        int existingIndex = -1;
        int ownedPlots = 0;

        for (int index = 0; index < plots.size(); index++) {
            VineyardPlot plot = plots.get(index);
            if (!plot.ownerId().equals(ownerKey)) {
                continue;
            }
            ownedPlots++;
            if (plot.name().toLowerCase(Locale.ROOT).equals(nameKey)) {
                existingIndex = index;
            }
        }

        for (int index = 0; index < plots.size(); index++) {
            VineyardPlot plot = plots.get(index);
            if (index != existingIndex
                    && plot.ownerId().equals(ownerKey)
                    && candidate.overlaps(plot)) {
                return new Registration(Status.OVERLAPPING, candidate);
            }
        }

        if (existingIndex >= 0) {
            VineyardPlot existing = plots.get(existingIndex);
            VineyardPlot updated = new VineyardPlot(
                    candidate.ownerId(),
                    candidate.name(),
                    candidate.dimension(),
                    candidate.minX(),
                    candidate.minZ(),
                    candidate.maxX(),
                    candidate.maxZ(),
                    candidate.anchorY(),
                    existing.createdDay()
            );
            plots.set(existingIndex, updated);
            setDirty();
            return new Registration(Status.UPDATED, updated);
        }

        if (ownedPlots >= MAX_PLOTS_PER_ESTATE) {
            return new Registration(Status.FULL, candidate);
        }

        plots.add(candidate);
        setDirty();
        return new Registration(Status.CREATED, candidate);
    }

    private List<VineyardPlot> allPlots() {
        return List.copyOf(plots);
    }

    public record Registration(Status status, VineyardPlot plot) {
        public boolean successful() {
            return status == Status.CREATED || status == Status.UPDATED;
        }
    }

    public enum Status {
        CREATED,
        UPDATED,
        TOO_LARGE,
        OVERLAPPING,
        FULL
    }
}
