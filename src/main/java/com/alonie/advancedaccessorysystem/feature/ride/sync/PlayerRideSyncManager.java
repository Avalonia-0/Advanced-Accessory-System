package com.alonie.advancedaccessorysystem.feature.ride.sync;

import com.alonie.advancedaccessorysystem.collective.CollectiveNetworkingBridge;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.BoatPassengerConfigHelper;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.state.BoatPassengerSettingsState;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.sync.BoatPassengerSettingsSyncManager;
import com.alonie.advancedaccessorysystem.feature.ride.network.s2c.sync.RideStateSyncPayload;
import com.alonie.advancedaccessorysystem.feature.ride.rules.RideAccessoryHelper;
import com.alonie.advancedaccessorysystem.feature.ride.state.RideRuntimeSessionState;
import com.alonie.advancedaccessorysystem.feature.ride.state.RideSnapshot;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;

public final class PlayerRideSyncManager {
    private static final int RIDE_ACCESSORY_EJECT_CHECK_INTERVAL = 5;
    private static final long HOTKEY_DISMOUNT_AUTO_RIDE_COOLDOWN_TICKS = 40L;
    private static final long PASSENGER_SELF_DISMOUNT_AUTO_RIDE_COOLDOWN_TICKS = 60L;
    private static final int MAX_PASSENGERS = 1;

    private PlayerRideSyncManager() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(PlayerRideSyncManager::onEndServerTick);
    }

    private static void onEndServerTick(MinecraftServer server) {
        long currentTick = RideRuntimeSessionState.advanceTick();

        if (currentTick % RIDE_ACCESSORY_EJECT_CHECK_INTERVAL == 0L) {
            enforceRideAccessoryRequirement(server);
            autoMountConfiguredBoatPassengers(server);
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            RideSnapshot current = RideSnapshot.capture(player);
            RideSnapshot previous = RideRuntimeSessionState.getLastState(player.getUuid());

            boolean changed = previous == null || !previous.sameAs(current);
            if (changed) {
                maybeApplyPassengerSelfDismountCooldown(player, previous, current);
                sendVehicleClientSync(player, previous, current);
            }

            RideRuntimeSessionState.putLastState(player.getUuid(), current);
        }
    }


    private static void enforceRideAccessoryRequirement(MinecraftServer server) {
        for (ServerPlayerEntity vehiclePlayer : server.getPlayerManager().getPlayerList()) {
            if (vehiclePlayer.getPassengerList().isEmpty()) {
                continue;
            }

            if (RideAccessoryHelper.supportsPassengers(vehiclePlayer)) {
                continue;
            }

            forceDismountPassengers(vehiclePlayer, "missing_support_accessory");
        }
    }

    private static void autoMountConfiguredBoatPassengers(MinecraftServer server) {
        for (ServerPlayerEntity vehiclePlayer : server.getPlayerManager().getPlayerList()) {
            if (!canAutoMountConfiguredBoatPassenger(vehiclePlayer)) {
                continue;
            }

            BoatPassengerSettingsState settings = BoatPassengerSettingsSyncManager.getGlobalSettings();
            if (settings.radius() <= 0 || settings.autoPickUpRules().getAllowedPatterns().isEmpty()) {
                continue;
            }

            Entity candidate = findNearestConfiguredBoatPassenger(vehiclePlayer, settings);
            if (candidate == null) {
                continue;
            }

            candidate.startRiding(vehiclePlayer, true, true);
        }
    }

    private static boolean canAutoMountConfiguredBoatPassenger(ServerPlayerEntity vehiclePlayer) {
        return vehiclePlayer.isAlive()
                && !vehiclePlayer.isSpectator()
                && !vehiclePlayer.isSleeping()
                && RideAccessoryHelper.hasBoatAccessory(vehiclePlayer)
                && vehiclePlayer.getPassengerList().size() < MAX_PASSENGERS;
    }

    private static Entity findNearestConfiguredBoatPassenger(
            ServerPlayerEntity vehiclePlayer,
            BoatPassengerSettingsState settings
    ) {
        double radius = settings.radius();
        double radiusSquared = radius * radius;

        return vehiclePlayer.getEntityWorld()
                .getOtherEntities(vehiclePlayer, vehiclePlayer.getBoundingBox().expand(radius),
                        entity -> isValidConfiguredBoatPassenger(vehiclePlayer, entity, settings, radiusSquared))
                .stream()
                .min(Comparator.comparingDouble(vehiclePlayer::squaredDistanceTo))
                .orElse(null);
    }

    private static boolean isValidConfiguredBoatPassenger(
            ServerPlayerEntity vehiclePlayer,
            Entity entity,
            BoatPassengerSettingsState settings,
            double radiusSquared
    ) {
        return entity.isAlive()
                && !entity.isRemoved()
                && !entity.hasVehicle()
                && !isAutoRideTemporarilyBlocked(entity)
                && settings.allowsAutoPickUp(entity)
                && vehiclePlayer.squaredDistanceTo(entity) <= radiusSquared;
    }

    public static boolean forceDismountPassengersFromHotkey(
            ServerPlayerEntity vehiclePlayer,
            boolean useChargedLaunch,
            double chargedLaunchSpeed
    ) {
        if (vehiclePlayer == null || vehiclePlayer.getPassengerList().isEmpty()) {
            return false;
        }

        BoatPassengerSettingsState settings = BoatPassengerSettingsSyncManager.getGlobalSettings();

        for (Entity passenger : java.util.List.copyOf(vehiclePlayer.getPassengerList())) {
            passenger.stopRiding();
            applyAutoRideCooldown(passenger, HOTKEY_DISMOUNT_AUTO_RIDE_COOLDOWN_TICKS);

            Vec3d launchVelocity = getHotkeyDismountLaunchVelocity(
                    vehiclePlayer,
                    settings,
                    passenger,
                    useChargedLaunch,
                    chargedLaunchSpeed
            );
            if (launchVelocity.lengthSquared() > 0.0D) {
                passenger.setVelocity(passenger.getVelocity().add(launchVelocity));
                passenger.velocityDirty = true;

                if (passenger instanceof ServerPlayerEntity serverPlayerPassenger) {
                    serverPlayerPassenger.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(serverPlayerPassenger));
                }
            }
        }

        return true;
    }

    public static boolean forceDismountPassengers(ServerPlayerEntity vehiclePlayer, String reason) {
        if (vehiclePlayer == null || vehiclePlayer.getPassengerList().isEmpty()) {
            return false;
        }

        for (Entity passenger : java.util.List.copyOf(vehiclePlayer.getPassengerList())) {
            passenger.stopRiding();
        }

        return true;
    }

    public static void onPassengerRequestedSelfDismount(ServerPlayerEntity passengerPlayer) {
        if (passengerPlayer == null || !(passengerPlayer.getVehicle() instanceof ServerPlayerEntity)) {
            return;
        }

        applyAutoRideCooldown(
                passengerPlayer,
                PASSENGER_SELF_DISMOUNT_AUTO_RIDE_COOLDOWN_TICKS
        );
    }

    private static boolean isAutoRideTemporarilyBlocked(Entity entity) {
        return RideRuntimeSessionState.isAutoRideCooldownActive(entity.getUuid());
    }

    private static void maybeApplyPassengerSelfDismountCooldown(
            ServerPlayerEntity player,
            RideSnapshot previous,
            RideSnapshot current
    ) {
        if (previous == null
                || !previous.hasVehicle()
                || !previous.vehicleIsPlayer()
                || current.hasVehicle()
                || !player.isSneaking()) {
            return;
        }

        applyAutoRideCooldown(player, PASSENGER_SELF_DISMOUNT_AUTO_RIDE_COOLDOWN_TICKS);
    }

    private static void applyAutoRideCooldown(Entity entity, long durationTicks) {
        RideRuntimeSessionState.applyAutoRideCooldown(entity.getUuid(), durationTicks);
    }

    private static Vec3d getHotkeyDismountLaunchVelocity(
            ServerPlayerEntity vehiclePlayer,
            BoatPassengerSettingsState settings,
            Entity passenger,
            boolean useChargedLaunch,
            double chargedLaunchSpeed
    ) {
        double speed = settings.dismountLaunchSpeed();
        if (useChargedLaunch && passenger != null && settings.allowsChargedLaunch(passenger)) {
            speed = BoatPassengerConfigHelper.sanitizeDismountLaunchSpeed(chargedLaunchSpeed);
        }
        if (speed <= 0.0D) {
            return Vec3d.ZERO;
        }

        return vehiclePlayer.getRotationVec(1.0F).multiply(speed);
    }

    private static void sendVehicleClientSync(ServerPlayerEntity vehiclePlayer, RideSnapshot previous, RideSnapshot current) {
        int[] previousIds = previous == null ? new int[0] : previous.passengerIds();
        int[] currentIds = current.passengerIds();

        for (int passengerId : currentIds) {
            if (!contains(previousIds, passengerId)) {
                CollectiveNetworkingBridge.sendToClient(new RideStateSyncPayload(vehiclePlayer.getId(), passengerId, true), vehiclePlayer);
            }
        }

        for (int passengerId : previousIds) {
            if (!contains(currentIds, passengerId)) {
                CollectiveNetworkingBridge.sendToClient(new RideStateSyncPayload(vehiclePlayer.getId(), passengerId, false), vehiclePlayer);
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

}
