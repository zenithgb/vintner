package com.zenith.vintner.vineyard;

import net.minecraft.network.chat.Component;

import java.util.Locale;

/** A persistent pruning strategy for one grapevine root system. */
public enum VineYieldMode {
    HIGH_YIELD(2, 2),
    BALANCED(0, 5),
    QUALITY_FOCUS(-1, 7);

    private final int harvestAdjustment;
    private final int qualityPoints;

    VineYieldMode(int harvestAdjustment, int qualityPoints) {
        this.harvestAdjustment = harvestAdjustment;
        this.qualityPoints = qualityPoints;
    }

    public int harvestAdjustment() {
        return harvestAdjustment;
    }

    public int qualityPoints() {
        return qualityPoints;
    }

    public VineYieldMode next() {
        return switch (this) {
            case HIGH_YIELD -> BALANCED;
            case BALANCED -> QUALITY_FOCUS;
            case QUALITY_FOCUS -> HIGH_YIELD;
        };
    }

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public Component displayName() {
        return Component.translatable(
                "vine_yield_mode.vintner." + serializedName()
        );
    }

    public static VineYieldMode fromName(String name) {
        for (VineYieldMode mode : values()) {
            if (mode.serializedName().equals(name)) {
                return mode;
            }
        }
        return BALANCED;
    }
}
