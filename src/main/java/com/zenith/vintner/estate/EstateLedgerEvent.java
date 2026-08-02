package com.zenith.vintner.estate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** One persistent, player-caused event in an estate's operational history. */
public record EstateLedgerEvent(
        String ownerId,
        String type,
        long day,
        String detail,
        int amount,
        long batchId,
        int quality
) {
    public static final Codec<EstateLedgerEvent> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("owner_id")
                            .forGetter(EstateLedgerEvent::ownerId),
                    Codec.STRING.fieldOf("type")
                            .forGetter(EstateLedgerEvent::type),
                    Codec.LONG.fieldOf("day")
                            .forGetter(EstateLedgerEvent::day),
                    Codec.STRING.fieldOf("detail")
                            .forGetter(EstateLedgerEvent::detail),
                    Codec.INT.fieldOf("amount")
                            .forGetter(EstateLedgerEvent::amount),
                    Codec.LONG.optionalFieldOf("batch_id", 0L)
                            .forGetter(EstateLedgerEvent::batchId),
                    Codec.INT.optionalFieldOf("quality", 0)
                            .forGetter(EstateLedgerEvent::quality)
            ).apply(instance, EstateLedgerEvent::new));

    public EstateLedgerEvent {
        ownerId = ownerId == null ? "" : ownerId;
        type = type == null ? LedgerEventType.FOUNDING.serializedName() : type;
        detail = detail == null ? "" : detail;
        day = Math.max(0L, day);
        amount = Math.max(1, amount);
        quality = Math.clamp(quality, 0, 100);
    }

    public EstateLedgerEvent withAdditionalAmount(int addition, int score) {
        int safeAddition = Math.max(1, addition);
        int combinedAmount = amount + safeAddition;
        int weightedQuality = (quality * amount
                + Math.clamp(score, 0, 100) * safeAddition)
                / combinedAmount;
        return new EstateLedgerEvent(
                ownerId,
                type,
                day,
                detail,
                combinedAmount,
                batchId,
                weightedQuality
        );
    }

    public LedgerEventType eventType() {
        return LedgerEventType.fromName(type);
    }
}
