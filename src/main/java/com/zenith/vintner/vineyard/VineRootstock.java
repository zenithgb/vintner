package com.zenith.vintner.vineyard;

import net.minecraft.network.chat.Component;

import java.util.Locale;

/** Persistent root system chosen independently from the grafted grape variety. */
public enum VineRootstock {
    OWN_ROOTS,
    ADAPTED,
    RESISTANT;

    public int healthBonus(VineyardThreat threat) {
        return switch (this) {
            case OWN_ROOTS -> 0;
            case ADAPTED -> switch (threat) {
                case NUTRIENT_IMBALANCE, DROUGHT_STRESS, HEAT_STRESS -> 2;
                default -> 0;
            };
            case RESISTANT -> switch (threat) {
                case MILDEW_RISK, ROT_RISK -> 2;
                case NUTRIENT_IMBALANCE -> 1;
                default -> 0;
            };
        };
    }

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public Component displayName() {
        return Component.translatable(
                "vine_rootstock.vintner." + serializedName()
        );
    }

    public static VineRootstock fromName(String name) {
        for (VineRootstock rootstock : values()) {
            if (rootstock.serializedName().equals(name)) {
                return rootstock;
            }
        }
        return OWN_ROOTS;
    }
}
