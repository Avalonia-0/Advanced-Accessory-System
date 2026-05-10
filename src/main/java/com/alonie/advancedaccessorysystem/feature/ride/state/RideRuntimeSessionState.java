package com.alonie.advancedaccessorysystem.feature.ride.state;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-session runtime state for ride tracking and temporary auto-ride cooldowns.
 */
public final class RideRuntimeSessionState {
    private static final Map<UUID, RideSnapshot> LAST_STATES = new HashMap<>();
    private static final Map<UUID, Long> AUTO_RIDE_COOLDOWNS = new HashMap<>();
    private static long tickCounter = 0L;

    private RideRuntimeSessionState() {
    }

    public static long advanceTick() {
        return ++tickCounter;
    }

    public static RideSnapshot getLastState(UUID playerUuid) {
        return LAST_STATES.get(playerUuid);
    }

    public static void putLastState(UUID playerUuid, RideSnapshot snapshot) {
        LAST_STATES.put(playerUuid, snapshot);
    }

    public static boolean isAutoRideCooldownActive(UUID entityUuid) {
        Long blockedUntilTick = AUTO_RIDE_COOLDOWNS.get(entityUuid);
        if (blockedUntilTick == null) {
            return false;
        }

        if (tickCounter >= blockedUntilTick) {
            AUTO_RIDE_COOLDOWNS.remove(entityUuid);
            return false;
        }

        return true;
    }

    public static void applyAutoRideCooldown(UUID entityUuid, long durationTicks) {
        long blockedUntilTick = tickCounter + durationTicks;
        AUTO_RIDE_COOLDOWNS.merge(entityUuid, blockedUntilTick, Math::max);
    }

    public static void reset() {
        LAST_STATES.clear();
        AUTO_RIDE_COOLDOWNS.clear();
        tickCounter = 0L;
    }
}
