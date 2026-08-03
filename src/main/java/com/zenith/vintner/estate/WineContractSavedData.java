package com.zenith.vintner.estate;

import com.mojang.serialization.Codec;
import com.zenith.vintner.Vintner;
import com.zenith.vintner.wine.WineAgeStage;
import com.zenith.vintner.wine.WineMarketRegion;
import com.zenith.vintner.wine.WineStyle;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Persistent offers and active deliveries for player-founded estates. */
public final class WineContractSavedData extends SavedData {
    public static final int MAX_CURRENT_CONTRACTS = 3;
    public static final long OFFER_PERIOD_DAYS = 8L;
    private static final int MAX_HISTORY_PER_ESTATE = 24;
    private static final Codec<WineContractSavedData> CODEC =
            WineContract.CODEC.listOf()
                    .optionalFieldOf("contracts", List.of())
                    .xmap(
                            WineContractSavedData::new,
                            WineContractSavedData::allContracts
                    )
                    .codec();

    public static final SavedDataType<WineContractSavedData> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(
                            Vintner.MOD_ID,
                            "wine_contracts"
                    ),
                    WineContractSavedData::new,
                    CODEC,
                    DataFixTypes.SAVED_DATA_COMMAND_STORAGE
            );

    private final List<WineContract> contracts = new ArrayList<>();

    public WineContractSavedData() {
    }

    private WineContractSavedData(List<WineContract> contracts) {
        this.contracts.addAll(contracts);
    }

    public static WineContractSavedData get(ServerLevel level) {
        return level.getServer()
                .overworld()
                .getDataStorage()
                .computeIfAbsent(TYPE);
    }

    public List<WineContract> currentContracts(
            ServerLevel level,
            UUID ownerId,
            WineMarketRegion region,
            int reputationScore,
            boolean generateOffers
    ) {
        long day = currentDay(level);
        boolean changed = expire(ownerId, day);
        long cycle = Math.floorDiv(day, OFFER_PERIOD_DAYS);

        if (generateOffers && !hasCycle(ownerId, cycle)) {
            contracts.addAll(generate(
                    ownerId,
                    region,
                    reputationScore,
                    cycle
            ));
            trim(ownerId);
            changed = true;
        }
        if (changed) {
            setDirty();
        }

        String owner = ownerId.toString();
        return contracts.stream()
                .filter(contract -> contract.ownerId().equals(owner))
                .filter(WineContract::isCurrent)
                .sorted(Comparator
                        .comparing((WineContract contract) ->
                                contract.contractStatus()
                                        != WineContractStatus.ACTIVE)
                        .thenComparing(WineContract::createdDay)
                        .thenComparing(WineContract::contractId))
                .limit(MAX_CURRENT_CONTRACTS)
                .toList();
    }

    public AcceptResult accept(
            ServerLevel level,
            UUID ownerId,
            String contractId
    ) {
        long day = currentDay(level);
        boolean changed = expire(ownerId, day);
        String owner = ownerId.toString();

        if (contracts.stream().anyMatch(contract ->
                contract.ownerId().equals(owner)
                        && contract.contractStatus()
                        == WineContractStatus.ACTIVE)) {
            if (changed) {
                setDirty();
            }
            return AcceptResult.ALREADY_ACTIVE;
        }

        for (int index = 0; index < contracts.size(); index++) {
            WineContract contract = contracts.get(index);
            if (!contract.ownerId().equals(owner)
                    || !contract.contractId().equals(contractId)) {
                continue;
            }
            if (contract.contractStatus() != WineContractStatus.OFFERED
                    || contract.expiresDay() <= day) {
                if (changed) {
                    setDirty();
                }
                return AcceptResult.UNAVAILABLE;
            }
            contracts.set(
                    index,
                    contract.withStatus(WineContractStatus.ACTIVE)
            );
            setDirty();
            return AcceptResult.ACCEPTED;
        }
        if (changed) {
            setDirty();
        }
        return AcceptResult.NOT_FOUND;
    }

    public Delivery deliver(
            ServerLevel level,
            UUID ownerId,
            ItemStack bottle
    ) {
        long day = currentDay(level);
        boolean changed = expire(ownerId, day);
        String owner = ownerId.toString();

        for (int index = 0; index < contracts.size(); index++) {
            WineContract contract = contracts.get(index);
            if (!contract.ownerId().equals(owner)
                    || contract.contractStatus()
                    != WineContractStatus.ACTIVE) {
                continue;
            }
            if (!contract.matches(bottle, owner)) {
                if (changed) {
                    setDirty();
                }
                return new Delivery(DeliveryResult.MISMATCH, contract);
            }
            WineContract updated = contract.withDeliveredBottle();
            contracts.set(index, updated);
            setDirty();
            return new Delivery(
                    updated.contractStatus()
                            == WineContractStatus.COMPLETED
                            ? DeliveryResult.COMPLETED
                            : DeliveryResult.ACCEPTED,
                    updated
            );
        }
        if (changed) {
            setDirty();
        }
        return new Delivery(DeliveryResult.NO_ACTIVE_CONTRACT, null);
    }

    public WineContract active(UUID ownerId) {
        String owner = ownerId.toString();
        return contracts.stream()
                .filter(contract -> contract.ownerId().equals(owner))
                .filter(contract -> contract.contractStatus()
                        == WineContractStatus.ACTIVE)
                .findFirst()
                .orElse(null);
    }

    private boolean expire(UUID ownerId, long day) {
        String owner = ownerId.toString();
        boolean changed = false;
        for (int index = 0; index < contracts.size(); index++) {
            WineContract contract = contracts.get(index);
            if (contract.ownerId().equals(owner)
                    && contract.isCurrent()
                    && contract.expiresDay() <= day) {
                contracts.set(
                        index,
                        contract.withStatus(WineContractStatus.EXPIRED)
                );
                changed = true;
            }
        }
        return changed;
    }

    private boolean hasCycle(UUID ownerId, long cycle) {
        String owner = ownerId.toString();
        long cycleStart = cycle * OFFER_PERIOD_DAYS;
        return contracts.stream().anyMatch(contract ->
                contract.ownerId().equals(owner)
                        && contract.createdDay() == cycleStart);
    }

    private static List<WineContract> generate(
            UUID ownerId,
            WineMarketRegion region,
            int reputationScore,
            long cycle
    ) {
        long created = cycle * OFFER_PERIOD_DAYS;
        long expires = created + OFFER_PERIOD_DAYS;
        TradePartner partner = TradePartner.forRegion(region);
        WineStyle localStyle = region == WineMarketRegion.COASTAL
                ? WineStyle.WHITE
                : WineStyle.RED;
        int reputationBonus = Math.min(4, Math.max(0, reputationScore / 60));

        List<WineContract> result = new ArrayList<>();
        result.add(createOffer(
                ownerId,
                partner,
                region,
                cycle,
                0,
                localStyle,
                30,
                WineAgeStage.YOUNG,
                4,
                10 + reputationBonus
        ));
        result.add(createOffer(
                ownerId,
                partner,
                region,
                cycle,
                1,
                localStyle == WineStyle.RED
                        ? WineStyle.WHITE
                        : WineStyle.RED,
                45,
                WineAgeStage.DEVELOPING,
                3,
                16 + reputationBonus
        ));
        result.add(createOffer(
                ownerId,
                partner,
                region,
                cycle,
                2,
                localStyle,
                60,
                WineAgeStage.MATURE,
                2,
                22 + reputationBonus
        ));
        return List.copyOf(result);
    }

    private static WineContract createOffer(
            UUID ownerId,
            TradePartner partner,
            WineMarketRegion region,
            long cycle,
            int offerIndex,
            WineStyle style,
            int minimumQuality,
            WineAgeStage minimumAge,
            int bottles,
            int reward
    ) {
        long mixed = ownerId.getMostSignificantBits()
                ^ Long.rotateLeft(ownerId.getLeastSignificantBits(), 17)
                ^ Long.rotateLeft(cycle, 29)
                ^ offerIndex * 0x9E3779B97F4A7C15L;
        String id = Long.toUnsignedString(mixed, 36).toUpperCase();
        if (id.length() > 8) {
            id = id.substring(0, 8);
        }
        return new WineContract(
                ownerId.toString(),
                id,
                partner.id(),
                region.id(),
                cycle * OFFER_PERIOD_DAYS,
                cycle * OFFER_PERIOD_DAYS + OFFER_PERIOD_DAYS,
                WineContractStatus.OFFERED.serializedName(),
                style.id(),
                minimumQuality,
                minimumAge.ordinal(),
                bottles,
                0,
                reward
        );
    }

    private void trim(UUID ownerId) {
        String owner = ownerId.toString();
        int count = (int) contracts.stream()
                .filter(contract -> contract.ownerId().equals(owner))
                .count();
        while (count > MAX_HISTORY_PER_ESTATE) {
            boolean removed = false;
            for (int index = 0; index < contracts.size(); index++) {
                WineContract contract = contracts.get(index);
                if (contract.ownerId().equals(owner)
                        && !contract.isCurrent()) {
                    contracts.remove(index);
                    count--;
                    removed = true;
                    break;
                }
            }
            if (!removed) {
                break;
            }
        }
    }

    private static long currentDay(ServerLevel level) {
        return Math.floorDiv(level.getOverworldClockTime(), 24000L);
    }

    private List<WineContract> allContracts() {
        return List.copyOf(contracts);
    }

    public enum AcceptResult {
        ACCEPTED,
        ALREADY_ACTIVE,
        UNAVAILABLE,
        NOT_FOUND
    }

    public enum DeliveryResult {
        ACCEPTED,
        COMPLETED,
        MISMATCH,
        NO_ACTIVE_CONTRACT
    }

    public record Delivery(DeliveryResult result, WineContract contract) {
    }
}
