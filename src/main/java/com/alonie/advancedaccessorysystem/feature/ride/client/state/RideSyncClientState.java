package com.alonie.advancedaccessorysystem.feature.ride.client.state;

import com.alonie.advancedaccessorysystem.feature.ride.network.s2c.sync.RideStateSyncPayload;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Client-side pending queue for ride syncs that arrive before entities are available.
 */
public final class RideSyncClientState {
    private static final Map<String, PendingRideSync> PENDING = new LinkedHashMap<>();

    private RideSyncClientState() {
    }

    public static void enqueue(RideStateSyncPayload payload, int ttlTicks) {
        PENDING.put(key(payload), new PendingRideSync(payload, ttlTicks));
    }

    public static boolean isEmpty() {
        return PENDING.isEmpty();
    }

    public static Iterator<Map.Entry<String, PendingRideSync>> iterator() {
        return PENDING.entrySet().iterator();
    }

    public static void clear() {
        PENDING.clear();
    }

    private static String key(RideStateSyncPayload payload) {
        return payload.vehicleId() + ":" + payload.passengerId() + ":" + payload.mounted();
    }

    public static final class PendingRideSync {
        private final RideStateSyncPayload payload;
        private int ttlTicks;

        public PendingRideSync(RideStateSyncPayload payload, int ttlTicks) {
            this.payload = Objects.requireNonNull(payload);
            this.ttlTicks = ttlTicks;
        }

        public RideStateSyncPayload payload() {
            return payload;
        }

        public int ttlTicks() {
            return ttlTicks;
        }

        public void setTtlTicks(int ttlTicks) {
            this.ttlTicks = ttlTicks;
        }
    }
}
