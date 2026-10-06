package com.zenith.vintner.registry;

/** Stateless, order-independent seven-in-ten decision for a village start. */
public final class VillageWineryEligibility {
    private VillageWineryEligibility() {
    }

    public static boolean eligible(long seed, int startX, int startZ,
                                   String culture) {
        long salt = switch (culture) {
            case "plains" -> 0x54c3abdf73477e2bL;
            case "desert" -> 0x7f8ab981c165b729L;
            case "savanna" -> 0x19e5fd934a785e31L;
            case "snowy" -> 0xc3d41f5e80cb62a7L;
            case "taiga" -> 0x8c7a41e2d519f6b3L;
            default -> throw new IllegalArgumentException(culture);
        };
        long coordinate = ((long) startX << 32)
                ^ Integer.toUnsignedLong(startZ);
        return Long.remainderUnsigned(mix64(mix64(seed ^ salt) ^ coordinate), 10)
                < 7;
    }

    private static long mix64(long value) {
        value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
        value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
        return value ^ (value >>> 31);
    }
}
