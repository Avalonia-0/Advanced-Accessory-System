package com.alonie.advancedaccessorysystem.feature.ride.client.sync;

import com.alonie.advancedaccessorysystem.feature.ride.client.state.RideSyncClientState;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import com.alonie.advancedaccessorysystem.feature.ride.network.s2c.sync.RideStateSyncPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

import java.util.Iterator;
import java.util.Map;

public final class RideStateSyncClient {
    private static final int PENDING_TTL_TICKS = 40;

    private RideStateSyncClient() {
    }

    public static void register() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> RideSyncClientState.clear());
        ClientTickEvents.END_CLIENT_TICK.register(RideStateSyncClient::onEndClientTick);
    }

    public static void onClientWorldLoad() {
        RideSyncClientState.clear();
    }

    public static void handleSync(RideStateSyncPayload payload) {
        enqueue(payload);
        applyOrQueue(MinecraftClient.getInstance(), payload, false);
    }

    private static void onEndClientTick(MinecraftClient client) {
        if (client.world == null || RideSyncClientState.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<String, RideSyncClientState.PendingRideSync>> iterator = RideSyncClientState.iterator();
        while (iterator.hasNext()) {
            RideSyncClientState.PendingRideSync pending = iterator.next().getValue();
            if (applyOrQueue(client, pending.payload(), true)) {
                iterator.remove();
                continue;
            }

            int remaining = pending.ttlTicks() - 1;
            if (remaining <= 0) {
                iterator.remove();
            } else {
                pending.setTtlTicks(remaining);
            }
        }
    }

    private static void enqueue(RideStateSyncPayload payload) {
        RideSyncClientState.enqueue(payload, PENDING_TTL_TICKS);
    }

    private static boolean applyOrQueue(MinecraftClient client, RideStateSyncPayload payload, boolean pendingSource) {
        if (client.world == null) {
            return false;
        }

        Entity vehicle = client.world.getEntityById(payload.vehicleId());
        Entity passenger = client.world.getEntityById(payload.passengerId());

        if (payload.mounted()) {
            if (vehicle == null || passenger == null) {
                return false;
            }

            if (passenger.getVehicle() != vehicle) {
                passenger.startRiding(vehicle, true, true);
            }
            return passenger.getVehicle() == vehicle;
        }

        Entity attached = findAttachedPassenger(vehicle, payload.passengerId());
        if (attached == null) {
            attached = passenger;
        }

        if (attached == null) {
            return false;
        }

        if (attached.getVehicle() == vehicle) {
            attached.stopRiding();
        }
        return attached.getVehicle() != vehicle;
    }

    private static Entity findAttachedPassenger(Entity vehicle, int passengerId) {
        if (vehicle == null) {
            return null;
        }
        for (Entity passenger : vehicle.getPassengerList()) {
            if (passenger.getId() == passengerId) {
                return passenger;
            }
        }
        return null;
    }

}
