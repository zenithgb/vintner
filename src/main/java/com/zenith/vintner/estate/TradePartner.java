package com.zenith.vintner.estate;

import com.zenith.vintner.wine.WineBuyerType;
import com.zenith.vintner.wine.WineMarketRegion;
import net.minecraft.network.chat.Component;

/** Named, stable counterparties used by the first contract cycle. */
public enum TradePartner {
    GREENFIELD_EXCHANGE(
            "greenfield_exchange",
            "Greenfield Exchange",
            WineMarketRegion.AGRICULTURAL,
            WineBuyerType.VILLAGE_MERCHANT
    ),
    SALTWIND_GUILD(
            "saltwind_guild",
            "Saltwind Vintners' Guild",
            WineMarketRegion.COASTAL,
            WineBuyerType.COASTAL_SETTLEMENT
    ),
    NORTHWATCH_PROVISIONERS(
            "northwatch_provisioners",
            "Northwatch Provisioners",
            WineMarketRegion.COLD,
            WineBuyerType.COLD_REGION_SETTLEMENT
    ),
    STONEGATE_CELLARS(
            "stonegate_cellars",
            "Stonegate Cellars",
            WineMarketRegion.MINING,
            WineBuyerType.MINING_SETTLEMENT
    );

    private final String id;
    private final String fallbackName;
    private final WineMarketRegion region;
    private final WineBuyerType buyer;

    TradePartner(
            String id,
            String fallbackName,
            WineMarketRegion region,
            WineBuyerType buyer
    ) {
        this.id = id;
        this.fallbackName = fallbackName;
        this.region = region;
        this.buyer = buyer;
    }

    public String id() {
        return id;
    }

    public String fallbackName() {
        return fallbackName;
    }

    public WineMarketRegion region() {
        return region;
    }

    public WineBuyerType buyer() {
        return buyer;
    }

    public Component displayName() {
        return Component.translatable("trade_partner.vintner." + id);
    }

    public static TradePartner forRegion(WineMarketRegion region) {
        for (TradePartner partner : values()) {
            if (partner.region == region) {
                return partner;
            }
        }
        return GREENFIELD_EXCHANGE;
    }

    public static TradePartner byId(String id) {
        for (TradePartner partner : values()) {
            if (partner.id.equals(id)) {
                return partner;
            }
        }
        return GREENFIELD_EXCHANGE;
    }
}
