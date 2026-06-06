package com.alonie.advancedaccessorysystem.feature.armorvisibility.client.state;

import com.alonie.advancedaccessorysystem.feature.armorvisibility.ArmorVisibilityMask;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Client-side cache for synced armor visibility masks and local resend tracking.
 */
public final class ArmorVisibilityClientCache {
    private static final ConcurrentMap<UUID, Integer> SYNCED_MASKS = new ConcurrentHashMap<>();
    private static int lastSentMask = -1;
    private static boolean localStateDirty = true;
    private static long clientTickCounter = 0L;
    private static long lastSentTick = Long.MIN_VALUE;

    private ArmorVisibilityClientCache() {
    }

    public static int getSyncedMask(UUID playerUuid) {
        return SYNCED_MASKS.getOrDefault(playerUuid, 0);
    }

    public static void setSyncedMask(UUID playerUuid, int visibilityMask) {
        int sanitizedMask = ArmorVisibilityMask.sanitize(visibilityMask);
        if (sanitizedMask == 0) {
            SYNCED_MASKS.remove(playerUuid);
        } else {
            SYNCED_MASKS.put(playerUuid, sanitizedMask);
        }
    }

    public static void markLocalStateDirty() {
        localStateDirty = true;
    }

    public static boolean isLocalStateDirty() {
        return localStateDirty;
    }

    public static void clearLocalStateDirty() {
        localStateDirty = false;
    }

    public static int lastSentMask() {
        return lastSentMask;
    }

    public static void setLastSentMask(int visibilityMask) {
        lastSentMask = ArmorVisibilityMask.sanitize(visibilityMask);
    }

    public static long advanceClientTick() {
        return ++clientTickCounter;
    }

    public static long currentClientTick() {
        return clientTickCounter;
    }

    public static long lastSentTick() {
        return lastSentTick;
    }

    public static void setLastSentTick(long tick) {
        lastSentTick = tick;
    }

    public static void reset() {
        SYNCED_MASKS.clear();
        lastSentMask = -1;
        localStateDirty = true;
        clientTickCounter = 0L;
        lastSentTick = Long.MIN_VALUE;
    }
}
