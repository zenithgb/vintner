package com.zenith.vintner.wine;

import com.zenith.vintner.vineyard.GrapeVariety;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Locale;
import java.util.UUID;

public record WineProvenance(
        String variety,
        long harvestedAt,
        String originDimension,
        int originX,
        int originY,
        int originZ,
        String producerId,
        String producerName
) {
    public static final String UNKNOWN = "unknown";
    private static final long TICKS_PER_DAY = 24000L;

    public WineProvenance {
        variety = normalizeVariety(variety);
        harvestedAt = Math.max(0L, harvestedAt);
        originDimension = safeText(originDimension, UNKNOWN);
        producerId = safeText(producerId, "");
        producerName = safeText(producerName, "");
    }

    public static WineProvenance legacy() {
        return new WineProvenance(
                UNKNOWN,
                0L,
                UNKNOWN,
                0,
                0,
                0,
                "",
                ""
        );
    }

    public static WineProvenance harvested(
            GrapeVariety variety,
            long gameTime,
            String dimension,
            BlockPos origin,
            UUID producerId,
            String producerName
    ) {
        return new WineProvenance(
                variety.name().toLowerCase(Locale.ROOT),
                gameTime,
                dimension,
                origin.getX(),
                origin.getY(),
                origin.getZ(),
                producerId.toString(),
                producerName
        );
    }

    public boolean known() {
        return !UNKNOWN.equals(variety);
    }

    public long harvestDay() {
        return harvestedAt / TICKS_PER_DAY;
    }

    public Component varietyDisplayName() {
        return Component.translatable(
                "wine_variety.vintner." + variety
        );
    }

    public Component originDisplayName() {
        if (UNKNOWN.equals(originDimension)) {
            return Component.translatable(
                    "wine_origin.vintner.unknown"
            );
        }

        return Component.translatable(
                "wine_origin.vintner.coordinates",
                originX,
                originY,
                originZ,
                dimensionName()
        );
    }

    public Component producerDisplayName() {
        if (producerName.isBlank()) {
            return Component.translatable(
                    "wine_producer.vintner.unknown"
            );
        }

        return Component.literal(producerName);
    }

    public void save(ValueOutput output, String prefix) {
        output.putString(prefix + "Variety", variety);
        output.putLong(prefix + "HarvestedAt", harvestedAt);
        output.putString(
                prefix + "OriginDimension",
                originDimension
        );
        output.putInt(prefix + "OriginX", originX);
        output.putInt(prefix + "OriginY", originY);
        output.putInt(prefix + "OriginZ", originZ);
        output.putString(prefix + "ProducerId", producerId);
        output.putString(prefix + "ProducerName", producerName);
    }

    public static WineProvenance load(
            ValueInput input,
            String prefix
    ) {
        return new WineProvenance(
                input.getStringOr(prefix + "Variety", UNKNOWN),
                input.getLongOr(prefix + "HarvestedAt", 0L),
                input.getStringOr(
                        prefix + "OriginDimension",
                        UNKNOWN
                ),
                input.getIntOr(prefix + "OriginX", 0),
                input.getIntOr(prefix + "OriginY", 0),
                input.getIntOr(prefix + "OriginZ", 0),
                input.getStringOr(prefix + "ProducerId", ""),
                input.getStringOr(prefix + "ProducerName", "")
        );
    }

    private String dimensionName() {
        int separator = originDimension.indexOf(':');
        String path = separator >= 0
                ? originDimension.substring(separator + 1)
                : originDimension;

        return path.replace('_', ' ');
    }

    private static String normalizeVariety(String variety) {
        String normalized = safeText(
                variety,
                UNKNOWN
        ).toLowerCase(Locale.ROOT);

        return normalized.equals("red")
                || normalized.equals("white")
                ? normalized
                : UNKNOWN;
    }

    private static String safeText(
            String value,
            String fallback
    ) {
        return value == null || value.isBlank()
                ? fallback
                : value;
    }
}
