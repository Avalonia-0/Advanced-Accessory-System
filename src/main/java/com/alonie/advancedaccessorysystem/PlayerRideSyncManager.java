package com.alonie.advancedaccessorysystem;

import com.alonie.advancedaccessorysystem.compat.CosmeticArmorCompat;
import com.alonie.advancedaccessorysystem.network.RideStateSyncPayload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerRideSyncManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("AdvancedAccessorySystem/RideSync");
    private static final int TRACE_TICKS_AFTER_CHANGE = 80;
    private static final int SADDLE_EJECT_CHECK_INTERVAL = 5;

    private static final Map<UUID, RideState> LAST_STATES = new HashMap<>();
    private static long tickCounter = 0L;

    private PlayerRideSyncManager() {
    }

    public static void register() {
        LOGGER.info("Registering player ride sync manager.");
        ServerTickEvents.END_SERVER_TICK.register(PlayerRideSyncManager::onEndServerTick);
    }

    private static void onEndServerTick(MinecraftServer server) {
        tickCounter++;

        if (tickCounter % SADDLE_EJECT_CHECK_INTERVAL == 0L) {
            enforceSaddleRequirement(server);
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            RideState current = RideState.capture(player);
            RideState previous = LAST_STATES.get(player.getUuid());

            boolean changed = previous == null || !previous.sameAs(current);
            int traceTicksRemaining = changed
                    ? TRACE_TICKS_AFTER_CHANGE
                    : Math.max(0, previous == null ? 0 : previous.traceTicksRemaining - 1);

            if (changed) {
                LOGGER.warn(
                        "RIDE_STATE_CHANGED tick={}, player={}, before={}, after={}",
                        tickCounter,
                        describePlayer(player),
                        previous == null ? "<none>" : previous,
                        current
                );
                sendVehicleClientSync(player, previous, current);
            }

            if (current.hasVehicle || current.hasPassengers || traceTicksRemaining > 0) {
                LOGGER.info(
                        "RIDE_STATE_TICK tick={}, player={}, hasVehicle={}, vehicle={}, passengerCount={}, passengers={}, traceTicksRemaining={}",
                        tickCounter,
                        describePlayer(player),
                        current.hasVehicle,
                        current.vehicleSummary,
                        current.passengerCount,
                        current.passengerSummary,
                        traceTicksRemaining
                );
            }

            LAST_STATES.put(player.getUuid(), current.withTraceTicksRemaining(traceTicksRemaining));
        }
    }


    private static void enforceSaddleRequirement(MinecraftServer server) {
        for (ServerPlayerEntity vehiclePlayer : server.getPlayerManager().getPlayerList()) {
            if (vehiclePlayer.getPassengerList().isEmpty()) {
                continue;
            }

            if (isRideableBySaddle(vehiclePlayer)) {
                continue;
            }

            LOGGER.info(
                    "FORCED_DISMOUNT no saddle: tick={}, vehicle={}, passengers={}",
                    tickCounter,
                    describePlayer(vehiclePlayer),
                    describePassengers(vehiclePlayer)
            );

            for (Entity passenger : java.util.List.copyOf(vehiclePlayer.getPassengerList())) {
                if (passenger instanceof ServerPlayerEntity serverPassenger) {
                    LOGGER.info(
                            "FORCED_DISMOUNT passenger stopRiding: tick={}, rider={}, vehicle={}",
                            tickCounter,
                            describePlayer(serverPassenger),
                            describePlayer(vehiclePlayer)
                    );
                    serverPassenger.stopRiding();
                }
            }
        }
    }

    private static boolean isRideableBySaddle(ServerPlayerEntity player) {
        ItemStack vanillaHead = player.getEquippedStack(EquipmentSlot.HEAD);
        ItemStack cosmeticHead = CosmeticArmorCompat.getCosmeticHeadStack(player);
        boolean vanilla = isSaddle(vanillaHead);
        boolean cosmetic = isSaddle(cosmeticHead);

        LOGGER.info(
                "RIDE_SYNC saddle check for {}: vanillaHead={}, cosmeticHead={}, vanillaMatch={}, cosmeticMatch={}",
                describePlayer(player),
                describeStack(vanillaHead),
                describeStack(cosmeticHead),
                vanilla,
                cosmetic
        );

        return vanilla || cosmetic;
    }

    private static boolean isSaddle(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.isOf(Items.SADDLE);
    }

    private static String describeStack(ItemStack stack) {
        if (stack == null) {
            return "null";
        }
        if (stack.isEmpty()) {
            return "empty";
        }
        return stack.getItem().toString() + " x" + stack.getCount();
    }

    private static void sendVehicleClientSync(ServerPlayerEntity vehiclePlayer, RideState previous, RideState current) {
        int[] previousIds = previous == null ? new int[0] : previous.passengerIds;
        int[] currentIds = current.passengerIds;

        for (int passengerId : currentIds) {
            if (!contains(previousIds, passengerId)) {
                ServerPlayNetworking.send(vehiclePlayer, new RideStateSyncPayload(vehiclePlayer.getId(), passengerId, true));
                LOGGER.info(
                        "RIDE_SYNC_PAYLOAD mount tick={}, vehicle={}, passengerId={}",
                        tickCounter,
                        describePlayer(vehiclePlayer),
                        passengerId
                );
            }
        }

        for (int passengerId : previousIds) {
            if (!contains(currentIds, passengerId)) {
                ServerPlayNetworking.send(vehiclePlayer, new RideStateSyncPayload(vehiclePlayer.getId(), passengerId, false));
                LOGGER.info(
                        "RIDE_SYNC_PAYLOAD dismount tick={}, vehicle={}, passengerId={}",
                        tickCounter,
                        describePlayer(vehiclePlayer),
                        passengerId
                );
            }
        }
    }

    private static boolean contains(int[] ids, int id) {
        for (int value : ids) {
            if (value == id) {
                return true;
            }
        }
        return false;
    }

    private static String describePlayer(ServerPlayerEntity player) {
        return player.getName().getString() + "[id=" + player.getId() + ",uuid=" + player.getUuidAsString() + "]";
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

    private record RideState(boolean hasVehicle,
                             String vehicleSummary,
                             boolean hasPassengers,
                             int passengerCount,
                             String passengerSummary,
                             int[] passengerIds,
                             int traceTicksRemaining) {
        static RideState capture(ServerPlayerEntity player) {
            Entity vehicle = player.getVehicle();
            return new RideState(
                    vehicle != null,
                    describeEntity(vehicle),
                    !player.getPassengerList().isEmpty(),
                    player.getPassengerList().size(),
                    describePassengers(player),
                    collectPassengerIds(player),
                    0
            );
        }

        RideState withTraceTicksRemaining(int ticks) {
            return new RideState(hasVehicle, vehicleSummary, hasPassengers, passengerCount, passengerSummary, passengerIds, ticks);
        }

        boolean sameAs(RideState other) {
            return other != null
                    && hasVehicle == other.hasVehicle
                    && vehicleSummary.equals(other.vehicleSummary)
                    && hasPassengers == other.hasPassengers
                    && passengerCount == other.passengerCount
                    && passengerSummary.equals(other.passengerSummary)
                    && Arrays.equals(passengerIds, other.passengerIds);
        }
    }
}
