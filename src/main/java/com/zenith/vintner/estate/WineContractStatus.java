package com.zenith.vintner.estate;

import java.util.Locale;

public enum WineContractStatus {
    OFFERED,
    ACTIVE,
    COMPLETED,
    EXPIRED;

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static WineContractStatus byName(String value) {
        if (value == null) {
            return OFFERED;
        }
        try {
            return valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return OFFERED;
        }
    }
}
