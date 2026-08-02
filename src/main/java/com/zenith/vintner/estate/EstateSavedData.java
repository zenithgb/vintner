package com.zenith.vintner.estate;

import com.mojang.serialization.Codec;
import com.zenith.vintner.Vintner;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** World-wide registry of player-founded estates. */
public final class EstateSavedData extends SavedData {
    private static final Codec<EstateSavedData> CODEC =
            EstateProfile.CODEC.listOf()
                    .optionalFieldOf("estates", List.of())
                    .xmap(EstateSavedData::new, EstateSavedData::profiles)
                    .codec();

    public static final SavedDataType<EstateSavedData> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(
                            Vintner.MOD_ID,
                            "estate_profiles"
                    ),
                    EstateSavedData::new,
                    CODEC,
                    DataFixTypes.SAVED_DATA_COMMAND_STORAGE
            );

    private final Map<String, EstateProfile> estates = new HashMap<>();

    public EstateSavedData() {
    }

    private EstateSavedData(List<EstateProfile> profiles) {
        for (EstateProfile profile : profiles) {
            estates.put(profile.ownerId(), profile);
        }
    }

    public static EstateSavedData get(ServerLevel level) {
        return level.getServer()
                .overworld()
                .getDataStorage()
                .computeIfAbsent(TYPE);
    }

    public Optional<EstateProfile> find(UUID ownerId) {
        return Optional.ofNullable(estates.get(ownerId.toString()));
    }

    public EstateProfile register(
            ServerPlayer owner,
            ServerLevel level,
            BlockPos archivePos,
            String requestedName,
            DyeColor crest
    ) {
        String ownerId = owner.getUUID().toString();
        EstateProfile existing = estates.get(ownerId);
        EstateProfile profile = existing == null
                ? EstateProfile.found(
                        owner,
                        level,
                        archivePos,
                        requestedName,
                        crest
                )
                : existing.renamed(requestedName, crest);

        estates.put(ownerId, profile);
        setDirty();
        return profile;
    }

    private List<EstateProfile> profiles() {
        return new ArrayList<>(estates.values());
    }
}
