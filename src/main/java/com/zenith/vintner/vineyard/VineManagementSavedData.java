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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/** Persistent yield strategy for grapevine root blocks in one dimension. */
public final class VineManagementSavedData extends SavedData {
    private record Entry(
            long position,
            String mode,
            String rootstock
    ) {
        private static final Codec<Entry> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Codec.LONG.fieldOf("position")
                                .forGetter(Entry::position),
                        Codec.STRING.fieldOf("mode")
                                .forGetter(Entry::mode),
                        Codec.STRING.optionalFieldOf(
                                "rootstock",
                                VineRootstock.OWN_ROOTS.serializedName()
                        ).forGetter(Entry::rootstock)
                ).apply(instance, Entry::new)
        );
    }

    private static final Codec<VineManagementSavedData> CODEC =
            Entry.CODEC.listOf()
                    .optionalFieldOf("vines", List.of())
                    .xmap(
                            VineManagementSavedData::new,
                            VineManagementSavedData::entries
                    )
                    .codec();

    public static final SavedDataType<VineManagementSavedData> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(
                            Vintner.MOD_ID,
                            "vine_management"
                    ),
                    VineManagementSavedData::new,
                    CODEC,
                    DataFixTypes.SAVED_DATA_COMMAND_STORAGE
            );

    private final Map<Long, VineYieldMode> modes = new HashMap<>();
    private final Map<Long, VineRootstock> rootstocks = new HashMap<>();

    public VineManagementSavedData() {
    }

    private VineManagementSavedData(List<Entry> entries) {
        for (Entry entry : entries) {
            VineYieldMode mode = VineYieldMode.fromName(entry.mode());
            if (mode != VineYieldMode.BALANCED) {
                modes.put(entry.position(), mode);
            }
            VineRootstock rootstock = VineRootstock.fromName(
                    entry.rootstock()
            );
            if (rootstock != VineRootstock.OWN_ROOTS) {
                rootstocks.put(entry.position(), rootstock);
            }
        }
    }

    public static VineManagementSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public VineYieldMode mode(BlockPos rootPos) {
        return modes.getOrDefault(
                rootPos.asLong(),
                VineYieldMode.BALANCED
        );
    }

    public VineRootstock rootstock(BlockPos rootPos) {
        return rootstocks.getOrDefault(
                rootPos.asLong(),
                VineRootstock.OWN_ROOTS
        );
    }

    public void setRootstock(
            BlockPos rootPos,
            VineRootstock rootstock
    ) {
        long key = rootPos.asLong();
        VineRootstock previous;

        if (rootstock == VineRootstock.OWN_ROOTS) {
            previous = rootstocks.remove(key);
        } else {
            previous = rootstocks.put(key, rootstock);
        }

        if (previous != rootstock) {
            setDirty();
        }
    }

    public void setMode(BlockPos rootPos, VineYieldMode mode) {
        long key = rootPos.asLong();
        VineYieldMode previous;

        if (mode == VineYieldMode.BALANCED) {
            previous = modes.remove(key);
        } else {
            previous = modes.put(key, mode);
        }

        if (previous != mode) {
            setDirty();
        }
    }

    public VineYieldMode cycle(BlockPos rootPos) {
        VineYieldMode next = mode(rootPos).next();
        setMode(rootPos, next);
        return next;
    }

    public void remove(BlockPos rootPos) {
        long key = rootPos.asLong();
        if (modes.remove(key) != null
                | rootstocks.remove(key) != null) {
            setDirty();
        }
    }

    private List<Entry> entries() {
        HashSet<Long> positions = new HashSet<>(modes.keySet());
        positions.addAll(rootstocks.keySet());
        List<Entry> entries = new ArrayList<>(positions.size());
        positions.forEach(position -> entries.add(new Entry(
                position,
                modes.getOrDefault(
                        position,
                        VineYieldMode.BALANCED
                ).serializedName(),
                rootstocks.getOrDefault(
                        position,
                        VineRootstock.OWN_ROOTS
                ).serializedName()
        )));
        return entries;
    }
}
