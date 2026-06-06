package com.alonie.advancedaccessorysystem.feature.ride.state;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;

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
    public static RideSnapshot capture(ServerPlayer player) {
        Entity vehicle = player.getVehicle();
        return new RideSnapshot(
                vehicle != null,
                vehicle instanceof ServerPlayer,
                vehicle == null ? null : vehicle.getUUID(),
                describeEntity(vehicle),
                !player.getPassengers().isEmpty(),
                player.getPassengers().size(),
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
        return entity.getDisplayName().getString() + "[id=" + entity.getId() + ",type=" + entity.getType() + ",uuid=" + entity.getStringUUID() + "]";
    }

    private static String describePassengers(ServerPlayer player) {
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (Entity passenger : player.getPassengers()) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append(describeEntity(passenger));
        }
        builder.append(']');
        return builder.toString();
    }

    private static int[] collectPassengerIds(ServerPlayer player) {
        int[] ids = new int[player.getPassengers().size()];
        for (int i = 0; i < player.getPassengers().size(); i++) {
            ids[i] = player.getPassengers().get(i).getId();
        }
        return ids;
    }
}
