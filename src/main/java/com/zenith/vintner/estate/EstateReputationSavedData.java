package com.zenith.vintner.estate;

import com.mojang.serialization.Codec;
import com.zenith.vintner.Vintner;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Persistent reputation evidence for player-founded estates. */
public final class EstateReputationSavedData extends SavedData {
    private static final Codec<EstateReputationSavedData> CODEC =
            EstateReputationProfile.CODEC.listOf()
                    .optionalFieldOf("profiles", List.of())
                    .xmap(
                            EstateReputationSavedData::new,
                            EstateReputationSavedData::allProfiles
                    )
                    .codec();

    public static final SavedDataType<EstateReputationSavedData> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(
                            Vintner.MOD_ID,
                            "estate_reputation"
                    ),
                    EstateReputationSavedData::new,
                    CODEC,
                    DataFixTypes.SAVED_DATA_COMMAND_STORAGE
            );

    private final Map<String, EstateReputationProfile> profiles =
            new HashMap<>();

    public EstateReputationSavedData() {
    }

    private EstateReputationSavedData(
            List<EstateReputationProfile> profiles
    ) {
        for (EstateReputationProfile profile : profiles) {
            this.profiles.put(profile.ownerId(), profile);
        }
    }

    public static EstateReputationSavedData get(ServerLevel level) {
        return level.getServer()
                .overworld()
                .getDataStorage()
                .computeIfAbsent(TYPE);
    }

    public EstateReputationProfile profile(UUID ownerId) {
        return profiles.getOrDefault(
                ownerId.toString(),
                EstateReputationProfile.empty(ownerId.toString())
        );
    }

    public EstateReputationProfile syncFromLedger(
            UUID ownerId,
            List<EstateLedgerEvent> events
    ) {
        EstateReputationProfile current = profile(ownerId);
        EstateReputationProfile updated = current.sync(events);
        storeIfChanged(current, updated);
        return updated;
    }

    public EstateReputationProfile recordInfrastructure(
            UUID ownerId,
            EstateInfrastructureReport infrastructure
    ) {
        EstateReputationProfile current = profile(ownerId);
        EstateReputationProfile updated = current.withFacilities(
                infrastructure.facilityMask()
        );
        storeIfChanged(current, updated);
        return updated;
    }

    public EstateReputationProfile recordEvent(
            UUID ownerId,
            LedgerEventType type,
            int amount,
            long batchId,
            int quality,
            List<EstateLedgerEvent> legacyEntries
    ) {
        String key = ownerId.toString();
        EstateReputationProfile current = profiles.get(key);
        EstateReputationProfile updated = current == null
                ? EstateReputationProfile.empty(key).sync(legacyEntries)
                : current.record(type, amount, batchId, quality);
        storeIfChanged(
                current == null
                        ? EstateReputationProfile.empty(key)
                        : current,
                updated
        );
        return updated;
    }

    private void storeIfChanged(
            EstateReputationProfile current,
            EstateReputationProfile updated
    ) {
        if (!updated.equals(current)) {
            profiles.put(updated.ownerId(), updated);
            setDirty();
        }
    }

    private List<EstateReputationProfile> allProfiles() {
        return new ArrayList<>(profiles.values());
    }
}
