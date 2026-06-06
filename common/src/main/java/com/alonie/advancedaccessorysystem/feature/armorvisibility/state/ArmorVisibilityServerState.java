package com.alonie.advancedaccessorysystem.feature.armorvisibility.state;

import com.alonie.advancedaccessorysystem.feature.armorvisibility.ArmorVisibilityMask;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Server-session runtime state for armor visibility syncing.
 */
public final class ArmorVisibilityServerState {
    private static final Map<UUID, Integer> PLAYER_MASKS = new LinkedHashMap<>();
    private static final Set<UUID> PENDING_FULL_SYNCS = new HashSet<>();
    private static long serverTickCounter = 0L;

    private ArmorVisibilityServerState() {
    }

    public static long advanceTick() {
        return ++serverTickCounter;
    }

    public static void markPendingFullSync(UUID playerUuid) {
        PENDING_FULL_SYNCS.add(playerUuid);
    }

    public static boolean hasPendingFullSyncs() {
        return !PENDING_FULL_SYNCS.isEmpty();
    }

    public static boolean isPendingFullSync(UUID playerUuid) {
        return PENDING_FULL_SYNCS.contains(playerUuid);
    }

    public static void clearPendingFullSync(UUID playerUuid) {
        PENDING_FULL_SYNCS.remove(playerUuid);
    }

    public static void setMask(UUID playerUuid, int visibilityMask) {
        int sanitizedMask = ArmorVisibilityMask.sanitize(visibilityMask);
        if (sanitizedMask == 0) {
            PLAYER_MASKS.remove(playerUuid);
        } else {
            PLAYER_MASKS.put(playerUuid, sanitizedMask);
        }
    }

    public static void clearPlayer(UUID playerUuid) {
        PLAYER_MASKS.remove(playerUuid);
        PENDING_FULL_SYNCS.remove(playerUuid);
    }

    public static Iterable<Map.Entry<UUID, Integer>> masks() {
        return PLAYER_MASKS.entrySet();
    }

    public static void reset() {
        PLAYER_MASKS.clear();
        PENDING_FULL_SYNCS.clear();
        serverTickCounter = 0L;
    }
}
