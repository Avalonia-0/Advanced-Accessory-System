package com.alonie.advancedaccessorysystem.client.network;

import com.alonie.advancedaccessorysystem.network.RideStateSyncPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class RideStateSyncClient {
    private static final Logger LOGGER = LoggerFactory.getLogger("AdvancedAccessorySystem/RideSyncClient");
    private static final int PENDING_TTL_TICKS = 40;
    private static final Map<String, PendingRideSync> PENDING = new LinkedHashMap<>();

    private RideStateSyncClient() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(RideStateSyncPayload.ID, (payload, context) -> {
            enqueue(payload);
            applyOrQueue(context.client(), payload, false);
        });

        ClientTickEvents.END_CLIENT_TICK.register(RideStateSyncClient::onEndClientTick);
    }

    private static void onEndClientTick(MinecraftClient client) {
        if (client.world == null || PENDING.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<String, PendingRideSync>> iterator = PENDING.entrySet().iterator();
        while (iterator.hasNext()) {
            PendingRideSync pending = iterator.next().getValue();
            if (applyOrQueue(client, pending.payload(), true)) {
                iterator.remove();
                continue;
            }

            int remaining = pending.ttlTicks() - 1;
            if (remaining <= 0) {
                LOGGER.warn("RideStateSync pending payload expired: {}", pending.payload());
                iterator.remove();
            } else {
                pending.setTtlTicks(remaining);
            }
        }
    }

    private static void enqueue(RideStateSyncPayload payload) {
        PENDING.put(key(payload), new PendingRideSync(payload, PENDING_TTL_TICKS));
    }

    private static boolean applyOrQueue(MinecraftClient client, RideStateSyncPayload payload, boolean fromPending) {
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
                boolean mounted = passenger.startRiding(vehicle, true, true);
                LOGGER.info("RideStateSync apply mount: fromPending={}, vehicle={}, passenger={}, mountedNow={}, startRidingResult={}",
                        fromPending,
                        describe(vehicle),
                        describe(passenger),
                        passenger.getVehicle() == vehicle,
                        mounted);
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
            LOGGER.info("RideStateSync apply dismount: fromPending={}, vehicle={}, passenger={}, detachedNow={}",
                    fromPending,
                    describe(vehicle),
                    describe(attached),
                    attached.getVehicle() != vehicle);
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

    private static String key(RideStateSyncPayload payload) {
        return payload.vehicleId() + ":" + payload.passengerId() + ":" + payload.mounted();
    }

    private static String describe(Entity entity) {
        if (entity == null) {
            return "null";
        }
        return entity.getName().getString() + "[id=" + entity.getId() + ",uuid=" + entity.getUuidAsString() + ",type=" + entity.getType() + "]";
    }

    private static final class PendingRideSync {
        private final RideStateSyncPayload payload;
        private int ttlTicks;

        private PendingRideSync(RideStateSyncPayload payload, int ttlTicks) {
            this.payload = Objects.requireNonNull(payload);
            this.ttlTicks = ttlTicks;
        }

        RideStateSyncPayload payload() {
            return payload;
        }

        int ttlTicks() {
            return ttlTicks;
        }

        void setTtlTicks(int ttlTicks) {
            this.ttlTicks = ttlTicks;
        }
    }
}
