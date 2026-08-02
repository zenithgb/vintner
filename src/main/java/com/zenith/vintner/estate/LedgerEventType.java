package com.zenith.vintner.estate;

import java.util.Locale;

public enum LedgerEventType {
    FOUNDING,
    ESTATE_RENAMED,
    PLOT_REGISTERED,
    PLOT_UPDATED,
    PLANTING,
    HARVEST,
    VINEYARD_PROBLEM,
    BATCH_PRESSED,
    BOTTLING,
    STORAGE,
    ARCHIVED;

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "ledger_event.vintner." + serializedName();
    }

    public static LedgerEventType fromName(String value) {
        if (value == null) {
            return FOUNDING;
        }
        try {
            return valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return FOUNDING;
        }
    }
}
