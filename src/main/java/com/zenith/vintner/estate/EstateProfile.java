package com.zenith.vintner.estate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zenith.vintner.vineyard.SeasonalContext;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;

import java.util.Locale;

/** Persistent identity for one player-founded wine estate. */
public record EstateProfile(
        String ownerId,
        String ownerName,
        String estateName,
        String vineyardName,
        String bottleLabel,
        int foundingYear,
        String homeRegion,
        String homeDimension,
        int homeX,
        int homeY,
        int homeZ,
        String crestColor
) {
    public static final int MAX_NAME_LENGTH = 48;
    public static final Codec<EstateProfile> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("owner_id")
                            .forGetter(EstateProfile::ownerId),
                    Codec.STRING.fieldOf("owner_name")
                            .forGetter(EstateProfile::ownerName),
                    Codec.STRING.fieldOf("estate_name")
                            .forGetter(EstateProfile::estateName),
                    Codec.STRING.fieldOf("vineyard_name")
                            .forGetter(EstateProfile::vineyardName),
                    Codec.STRING.fieldOf("bottle_label")
                            .forGetter(EstateProfile::bottleLabel),
                    Codec.INT.fieldOf("founding_year")
                            .forGetter(EstateProfile::foundingYear),
                    Codec.STRING.fieldOf("home_region")
                            .forGetter(EstateProfile::homeRegion),
                    Codec.STRING.fieldOf("home_dimension")
                            .forGetter(EstateProfile::homeDimension),
                    Codec.INT.fieldOf("home_x")
                            .forGetter(EstateProfile::homeX),
                    Codec.INT.fieldOf("home_y")
                            .forGetter(EstateProfile::homeY),
                    Codec.INT.fieldOf("home_z")
                            .forGetter(EstateProfile::homeZ),
                    Codec.STRING.fieldOf("crest_color")
                            .forGetter(EstateProfile::crestColor)
            ).apply(instance, EstateProfile::new));

    public EstateProfile {
        ownerId = safe(ownerId, "");
        ownerName = safe(ownerName, "Unknown Vintner");
        estateName = sanitizeName(estateName);
        vineyardName = safe(
                vineyardName,
                estateName + " Vineyard"
        );
        bottleLabel = safe(bottleLabel, estateName);
        foundingYear = Math.max(1, foundingYear);
        homeRegion = safe(homeRegion, "minecraft:plains");
        homeDimension = safe(
                homeDimension,
                "minecraft:overworld"
        );
        crestColor = safe(
                crestColor,
                DyeColor.PURPLE.getName()
        );
    }

    public static EstateProfile found(
            ServerPlayer owner,
            ServerLevel level,
            BlockPos archivePos,
            String requestedName,
            DyeColor crest
    ) {
        String name = sanitizeName(requestedName);
        String region = level.getBiome(archivePos)
                .unwrapKey()
                .map(key -> key.identifier().toString())
                .orElse("minecraft:plains");

        return new EstateProfile(
                owner.getUUID().toString(),
                owner.getGameProfile().name(),
                name,
                name + " Vineyard",
                name,
                SeasonalContext.current(level).year(),
                region,
                level.dimension().identifier().toString(),
                archivePos.getX(),
                archivePos.getY(),
                archivePos.getZ(),
                crest == null
                        ? DyeColor.PURPLE.getName()
                        : crest.getName()
        );
    }

    public EstateProfile renamed(String requestedName, DyeColor crest) {
        String name = sanitizeName(requestedName);
        return new EstateProfile(
                ownerId,
                ownerName,
                name,
                name + " Vineyard",
                name,
                foundingYear,
                homeRegion,
                homeDimension,
                homeX,
                homeY,
                homeZ,
                crest == null ? crestColor : crest.getName()
        );
    }

    public String homeRegionDisplayName() {
        Identifier identifier = Identifier.tryParse(homeRegion);
        String path = identifier == null
                ? homeRegion
                : identifier.getPath();
        String[] words = path.replace('/', '_').split("_");
        StringBuilder display = new StringBuilder();

        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            if (!display.isEmpty()) {
                display.append(' ');
            }
            display.append(word.substring(0, 1).toUpperCase(Locale.ROOT));
            display.append(word.substring(1).toLowerCase(Locale.ROOT));
        }
        return display.isEmpty() ? homeRegion : display.toString();
    }

    public static String sanitizeName(String requestedName) {
        String normalized = requestedName == null
                ? ""
                : requestedName.trim().replaceAll("\\s+", " ");

        if (normalized.isBlank()) {
            return "Independent Estate";
        }
        return normalized.length() <= MAX_NAME_LENGTH
                ? normalized
                : normalized.substring(0, MAX_NAME_LENGTH).trim();
    }

    private static String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
