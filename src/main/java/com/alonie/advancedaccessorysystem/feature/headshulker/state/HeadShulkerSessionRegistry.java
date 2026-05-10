package com.alonie.advancedaccessorysystem.feature.headshulker.state;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Runtime session registry for shared head-shulker viewing sessions.
 */
public final class HeadShulkerSessionRegistry {
    private static final Map<String, HeadShulkerSessionHandle> SESSIONS = new ConcurrentHashMap<>();

    private HeadShulkerSessionRegistry() {
    }

    public static String vanillaKey(UUID targetUuid) {
        return "vanilla:" + targetUuid;
    }

    public static String cosmeticKey(UUID targetUuid, int slotIndex) {
        return "cosmetic:" + targetUuid + ":" + slotIndex;
    }

    public static <T extends HeadShulkerSessionHandle> T getOrCreate(String key, Supplier<T> factory, Class<T> type) {
        return type.cast(SESSIONS.computeIfAbsent(key, ignored -> factory.get()));
    }

    public static Iterable<HeadShulkerSessionHandle> snapshot() {
        return new ArrayList<>(SESSIONS.values());
    }

    public static void remove(String key, HeadShulkerSessionHandle session) {
        SESSIONS.remove(key, session);
    }

    public static void reset() {
        SESSIONS.clear();
    }
}
