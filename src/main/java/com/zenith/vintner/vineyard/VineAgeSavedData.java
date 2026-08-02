package com.zenith.vintner.vineyard;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zenith.vintner.Vintner;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Persistent planting dates for grapevine root blocks in one dimension. */
public final class VineAgeSavedData extends SavedData {
    private record Entry(long position, long plantedDay) {
        private static final Codec<Entry> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Codec.LONG.fieldOf("position")
                                .forGetter(Entry::position),
                        Codec.LONG.fieldOf("planted_day")
                                .forGetter(Entry::plantedDay)
                ).apply(instance, Entry::new)
        );
    }

    private static final Codec<VineAgeSavedData> CODEC =
            Entry.CODEC.listOf()
                    .optionalFieldOf("vines", List.of())
                    .xmap(VineAgeSavedData::new, VineAgeSavedData::entries)
                    .codec();

    public static final SavedDataType<VineAgeSavedData> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(
                            Vintner.MOD_ID,
                            "vine_ages"
                    ),
                    VineAgeSavedData::new,
                    CODEC,
                    DataFixTypes.SAVED_DATA_COMMAND_STORAGE
            );

    private final Map<Long, Long> plantedDays = new HashMap<>();

    public VineAgeSavedData() {
    }

    private VineAgeSavedData(List<Entry> entries) {
        for (Entry entry : entries) {
            plantedDays.put(
                    entry.position(),
                    Math.max(0L, entry.plantedDay())
            );
        }
    }

    public static VineAgeSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void plant(BlockPos rootPos, long plantedDay) {
        long key = rootPos.asLong();
        long safeDay = Math.max(0L, plantedDay);

        if (!plantedDays.containsKey(key)) {
            plantedDays.put(key, safeDay);
            setDirty();
        }
    }

    public long plantedDay(BlockPos rootPos, long currentDay) {
        long key = rootPos.asLong();
        long safeCurrentDay = Math.max(0L, currentDay);
        Long existing = plantedDays.get(key);

        if (existing != null) {
            return existing;
        }

        plantedDays.put(key, safeCurrentDay);
        setDirty();
        return safeCurrentDay;
    }

    public long ageDays(BlockPos rootPos, long currentDay) {
        long safeCurrentDay = Math.max(0L, currentDay);
        return Math.max(
                0L,
                safeCurrentDay - plantedDay(rootPos, safeCurrentDay)
        );
    }

    public VineAgeStage stage(BlockPos rootPos, long currentDay) {
        return VineAgeStage.atDays(ageDays(rootPos, currentDay));
    }

    public void remove(BlockPos rootPos) {
        if (plantedDays.remove(rootPos.asLong()) != null) {
            setDirty();
        }
    }

    private List<Entry> entries() {
        List<Entry> entries = new ArrayList<>(plantedDays.size());
        plantedDays.forEach((position, plantedDay) ->
                entries.add(new Entry(position, plantedDay))
        );
        return entries;
    }
}
