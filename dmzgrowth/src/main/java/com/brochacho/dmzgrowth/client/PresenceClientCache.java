package com.brochacho.dmzgrowth.client;

import com.brochacho.dmzgrowth.net.PresenceSyncS2C;

import java.util.List;

public final class PresenceClientCache {
    private static volatile List<PresenceSyncS2C.Entry> entries = List.of();
    private static volatile long lastUpdateMs;

    private PresenceClientCache() {}

    public static void set(List<PresenceSyncS2C.Entry> list) {
        entries = List.copyOf(list);
        lastUpdateMs = System.currentTimeMillis();
    }

    /** Datos con mas de 10 s se consideran obsoletos (por ejemplo, ya no lleva scouter). */
    public static List<PresenceSyncS2C.Entry> get() {
        return System.currentTimeMillis() - lastUpdateMs > 10_000 ? List.of() : entries;
    }
}
