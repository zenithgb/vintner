package com.zenith.vintner.estate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zenith.vintner.item.WineItem;
import com.zenith.vintner.wine.WineAgeStage;
import com.zenith.vintner.wine.WineMetadata;
import com.zenith.vintner.wine.WineStyle;
import net.minecraft.world.item.ItemStack;

/** One persisted wine order offered to a player-owned estate. */
public record WineContract(
        String ownerId,
        String contractId,
        String partnerId,
        String marketRegion,
        long createdDay,
        long expiresDay,
        String status,
        String style,
        int minimumQuality,
        int minimumAgeStage,
        int requiredBottles,
        int deliveredBottles,
        int rewardEmeralds
) {
    public static final Codec<WineContract> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("owner_id")
                            .forGetter(WineContract::ownerId),
                    Codec.STRING.fieldOf("contract_id")
                            .forGetter(WineContract::contractId),
                    Codec.STRING.fieldOf("partner_id")
                            .forGetter(WineContract::partnerId),
                    Codec.STRING.fieldOf("market_region")
                            .forGetter(WineContract::marketRegion),
                    Codec.LONG.fieldOf("created_day")
                            .forGetter(WineContract::createdDay),
                    Codec.LONG.fieldOf("expires_day")
                            .forGetter(WineContract::expiresDay),
                    Codec.STRING.fieldOf("status")
                            .forGetter(WineContract::status),
                    Codec.STRING.fieldOf("style")
                            .forGetter(WineContract::style),
                    Codec.INT.fieldOf("minimum_quality")
                            .forGetter(WineContract::minimumQuality),
                    Codec.INT.fieldOf("minimum_age_stage")
                            .forGetter(WineContract::minimumAgeStage),
                    Codec.INT.fieldOf("required_bottles")
                            .forGetter(WineContract::requiredBottles),
                    Codec.INT.fieldOf("delivered_bottles")
                            .forGetter(WineContract::deliveredBottles),
                    Codec.INT.fieldOf("reward_emeralds")
                            .forGetter(WineContract::rewardEmeralds)
            ).apply(instance, WineContract::new));

    public WineContract {
        ownerId = ownerId == null ? "" : ownerId;
        contractId = contractId == null ? "" : contractId;
        partnerId = partnerId == null
                ? TradePartner.GREENFIELD_EXCHANGE.id()
                : partnerId;
        marketRegion = marketRegion == null
                ? "agricultural"
                : marketRegion;
        createdDay = Math.max(0L, createdDay);
        expiresDay = Math.max(createdDay + 1L, expiresDay);
        status = WineContractStatus.byName(status).serializedName();
        style = WineStyle.byId(style).id();
        minimumQuality = Math.clamp(minimumQuality, 0, 100);
        minimumAgeStage = Math.clamp(
                minimumAgeStage,
                WineAgeStage.YOUNG.ordinal(),
                WineAgeStage.PEAK.ordinal()
        );
        requiredBottles = Math.clamp(requiredBottles, 1, 16);
        deliveredBottles = Math.clamp(
                deliveredBottles,
                0,
                requiredBottles
        );
        rewardEmeralds = Math.clamp(rewardEmeralds, 1, 64);
    }

    public TradePartner partner() {
        return TradePartner.byId(partnerId);
    }

    public WineContractStatus contractStatus() {
        return WineContractStatus.byName(status);
    }

    public WineStyle requiredStyle() {
        return WineStyle.byId(style);
    }

    public WineAgeStage requiredAge() {
        return WineAgeStage.values()[minimumAgeStage];
    }

    public boolean isCurrent() {
        WineContractStatus value = contractStatus();
        return value == WineContractStatus.OFFERED
                || value == WineContractStatus.ACTIVE;
    }

    public boolean matches(ItemStack bottle, String expectedOwnerId) {
        if (!(bottle.getItem() instanceof WineItem)
                || WineMetadata.wineStyle(bottle) != requiredStyle()
                || WineMetadata.qualityScore(bottle) < minimumQuality) {
            return false;
        }
        WineAgeStage age = WineMetadata.ageStage(bottle);
        if (age == WineAgeStage.SPOILED
                || age.ordinal() < minimumAgeStage) {
            return false;
        }
        String producer = WineMetadata.provenance(bottle).producerId();
        return producer.isBlank() || producer.equals(expectedOwnerId);
    }

    public WineContract withStatus(WineContractStatus newStatus) {
        return new WineContract(
                ownerId,
                contractId,
                partnerId,
                marketRegion,
                createdDay,
                expiresDay,
                newStatus.serializedName(),
                style,
                minimumQuality,
                minimumAgeStage,
                requiredBottles,
                deliveredBottles,
                rewardEmeralds
        );
    }

    public WineContract withDeliveredBottle() {
        int delivered = Math.min(requiredBottles, deliveredBottles + 1);
        WineContractStatus next = delivered >= requiredBottles
                ? WineContractStatus.COMPLETED
                : WineContractStatus.ACTIVE;
        return new WineContract(
                ownerId,
                contractId,
                partnerId,
                marketRegion,
                createdDay,
                expiresDay,
                next.serializedName(),
                style,
                minimumQuality,
                minimumAgeStage,
                requiredBottles,
                delivered,
                rewardEmeralds
        );
    }
}
