package com.alonie.advancedaccessorysystem.feature.ride.state;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public record RideSnapshot(boolean hasVehicle,
                           boolean vehicleIsPlayer,
                           UUID vehicleUuid,
                           String vehicleSummary,
                           boolean hasPassengers,
                           int passengerCount,
                           String passengerSummary,
                           int[] passengerIds) {
    public static RideSnapshot capture(ServerPlayerEntity player) {
        Entity vehicle = player.getVehicle();
        return new RideSnapshot(
                vehicle != null,
                vehicle instanceof ServerPlayerEntity,
                vehicle == null ? null : vehicle.getUuid(),
                describeEntity(vehicle),
                !player.getPassengerList().isEmpty(),
                player.getPassengerList().size(),
                describePassengers(player),
                collectPassengerIds(player)
        );
    }

    public boolean sameAs(RideSnapshot other) {
        return other != null
                && hasVehicle == other.hasVehicle
                && vehicleIsPlayer == other.vehicleIsPlayer
                && Objects.equals(vehicleUuid, other.vehicleUuid)
                && vehicleSummary.equals(other.vehicleSummary)
                && hasPassengers == other.hasPassengers
                && passengerCount == other.passengerCount
                && passengerSummary.equals(other.passengerSummary)
                && Arrays.equals(passengerIds, other.passengerIds);
    }

    private static String describeEntity(Entity entity) {
        if (entity == null) {
            return "null";
        }
        return entity.getName().getString() + "[id=" + entity.getId() + ",type=" + entity.getType() + ",uuid=" + entity.getUuidAsString() + "]";
    }

    private static String describePassengers(ServerPlayerEntity player) {
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (Entity passenger : player.getPassengerList()) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append(describeEntity(passenger));
        }
        builder.append(']');
        return builder.toString();
    }

    private static int[] collectPassengerIds(ServerPlayerEntity player) {
        int[] ids = new int[player.getPassengerList().size()];
        for (int i = 0; i < player.getPassengerList().size(); i++) {
            ids[i] = player.getPassengerList().get(i).getId();
        }
        return ids;
    }
}
