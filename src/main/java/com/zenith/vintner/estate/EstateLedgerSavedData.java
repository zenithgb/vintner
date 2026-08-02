package com.zenith.vintner.estate;

import com.mojang.serialization.Codec;
import com.zenith.vintner.Vintner;
import com.zenith.vintner.wine.WineMetadata;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Persistent, bounded operating history for registered estates. */
public final class EstateLedgerSavedData extends SavedData {
    public static final int MAX_EVENTS_PER_ESTATE = 128;
    private static final Codec<EstateLedgerSavedData> CODEC =
            EstateLedgerEvent.CODEC.listOf()
                    .optionalFieldOf("events", List.of())
                    .xmap(
                            EstateLedgerSavedData::new,
                            EstateLedgerSavedData::allEvents
                    )
                    .codec();

    public static final SavedDataType<EstateLedgerSavedData> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(
                            Vintner.MOD_ID,
                            "estate_ledger"
                    ),
                    EstateLedgerSavedData::new,
                    CODEC,
                    DataFixTypes.SAVED_DATA_COMMAND_STORAGE
            );

    private final List<EstateLedgerEvent> events = new ArrayList<>();

    public EstateLedgerSavedData() {
    }

    private EstateLedgerSavedData(List<EstateLedgerEvent> events) {
        this.events.addAll(events);
    }

    public static EstateLedgerSavedData get(ServerLevel level) {
        return level.getServer()
                .overworld()
                .getDataStorage()
                .computeIfAbsent(TYPE);
    }

    public void record(
            ServerPlayer owner,
            LedgerEventType type,
            String detail,
            int amount,
            long batchId,
            int quality
    ) {
        if (EstateSavedData.get((ServerLevel) owner.level())
                .find(owner.getUUID())
                .isEmpty()) {
            return;
        }

        String ownerId = owner.getUUID().toString();
        long day = Math.floorDiv(
                ((ServerLevel) owner.level()).getOverworldClockTime(),
                24000L
        );
        String safeDetail = detail == null ? "" : detail;

        for (int index = events.size() - 1; index >= 0; index--) {
            EstateLedgerEvent existing = events.get(index);
            if (!existing.ownerId().equals(ownerId)) {
                continue;
            }
            if (existing.day() == day
                    && existing.eventType() == type
                    && existing.batchId() == batchId
                    && existing.detail().equals(safeDetail)) {
                events.set(
                        index,
                        existing.withAdditionalAmount(amount, quality)
                );
                setDirty();
                return;
            }
            break;
        }

        events.add(new EstateLedgerEvent(
                ownerId,
                type.serializedName(),
                day,
                safeDetail,
                amount,
                batchId,
                quality
        ));
        trim(owner.getUUID());
        setDirty();
    }

    public void recordWine(
            ServerPlayer owner,
            LedgerEventType type,
            ItemStack wine,
            int amount
    ) {
        record(
                owner,
                type,
                WineMetadata.batchCode(wine),
                amount,
                WineMetadata.batchId(wine),
                WineMetadata.qualityScore(wine)
        );
    }

    public List<EstateLedgerEvent> entries(UUID ownerId) {
        String key = ownerId.toString();
        List<EstateLedgerEvent> result = new ArrayList<>();
        for (int index = events.size() - 1; index >= 0; index--) {
            EstateLedgerEvent event = events.get(index);
            if (event.ownerId().equals(key)) {
                result.add(event);
            }
        }
        return List.copyOf(result);
    }

    public EstateLedgerEvent bestVintage(UUID ownerId) {
        return entries(ownerId).stream()
                .filter(event -> event.eventType()
                        == LedgerEventType.BOTTLING
                        || event.eventType()
                        == LedgerEventType.ARCHIVED)
                .max(Comparator.comparingInt(
                        EstateLedgerEvent::quality
                ))
                .orElse(null);
    }

    private void trim(UUID ownerId) {
        String key = ownerId.toString();
        int owned = 0;
        for (EstateLedgerEvent event : events) {
            if (event.ownerId().equals(key)) {
                owned++;
            }
        }
        while (owned > MAX_EVENTS_PER_ESTATE) {
            for (int index = 0; index < events.size(); index++) {
                if (events.get(index).ownerId().equals(key)) {
                    events.remove(index);
                    owned--;
                    break;
                }
            }
        }
    }

    private List<EstateLedgerEvent> allEvents() {
        return List.copyOf(events);
    }
}
