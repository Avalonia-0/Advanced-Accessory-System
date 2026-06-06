package com.alonie.advancedaccessorysystem.feature.ride.sync;

import com.alonie.advancedaccessorysystem.bridge.PlatformNetworking;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.config.BoatPassengerConfigHelper;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.state.BoatPassengerSettingsState;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.sync.BoatPassengerSettingsSyncManager;
import com.alonie.advancedaccessorysystem.feature.ride.network.s2c.sync.RideStateSyncPayload;
import com.alonie.advancedaccessorysystem.feature.ride.rules.RideAccessoryHelper;
import com.alonie.advancedaccessorysystem.feature.ride.state.RideRuntimeSessionState;
import com.alonie.advancedaccessorysystem.feature.ride.state.RideSnapshot;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

public final class PlayerRideSyncManager {
    private static final int RIDE_ACCESSORY_EJECT_CHECK_INTERVAL = 5;
    private static final long HOTKEY_DISMOUNT_AUTO_RIDE_COOLDOWN_TICKS = 40L;
    private static final long PASSENGER_SELF_DISMOUNT_AUTO_RIDE_COOLDOWN_TICKS = 60L;
    private static final int MAX_PASSENGERS = 1;

    private PlayerRideSyncManager() {
    }

    public static void onServerTick(MinecraftServer server) {
        long currentTick = RideRuntimeSessionState.advanceTick();

        if (currentTick % RIDE_ACCESSORY_EJECT_CHECK_INTERVAL == 0L) {
            enforceRideAccessoryRequirement(server);
            autoMountConfiguredBoatPassengers(server);
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            RideSnapshot current = RideSnapshot.capture(player);
            RideSnapshot previous = RideRuntimeSessionState.getLastState(player.getUUID());

            boolean changed = previous == null || !previous.sameAs(current);
            if (changed) {
                maybeApplyPassengerSelfDismountCooldown(player, previous, current);
                sendVehicleClientSync(player, previous, current);
            }

            RideRuntimeSessionState.putLastState(player.getUUID(), current);
        }
    }


    private static void enforceRideAccessoryRequirement(MinecraftServer server) {
        for (ServerPlayer vehiclePlayer : server.getPlayerList().getPlayers()) {
            if (vehiclePlayer.getPassengers().isEmpty()) {
                continue;
            }

            if (RideAccessoryHelper.supportsPassengers(vehiclePlayer)) {
                continue;
            }

            forceDismountPassengers(vehiclePlayer, "missing_support_accessory");
        }
    }

    private static void autoMountConfiguredBoatPassengers(MinecraftServer server) {
        for (ServerPlayer vehiclePlayer : server.getPlayerList().getPlayers()) {
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

    private static boolean canAutoMountConfiguredBoatPassenger(ServerPlayer vehiclePlayer) {
        return vehiclePlayer.isAlive()
                && !vehiclePlayer.isSpectator()
                && !vehiclePlayer.isSleeping()
                && RideAccessoryHelper.hasBoatAccessory(vehiclePlayer)
                && vehiclePlayer.getPassengers().size() < MAX_PASSENGERS;
    }

    private static Entity findNearestConfiguredBoatPassenger(
            ServerPlayer vehiclePlayer,
            BoatPassengerSettingsState settings
    ) {
        double radius = settings.radius();
        double radiusSquared = radius * radius;

        return vehiclePlayer.level()
                .getEntities(vehiclePlayer, vehiclePlayer.getBoundingBox().inflate(radius),
                        entity -> isValidConfiguredBoatPassenger(vehiclePlayer, entity, settings, radiusSquared))
                .stream()
                .min(Comparator.comparingDouble(e -> vehiclePlayer.distanceToSqr(e)))
                .orElse(null);
    }

    private static boolean isValidConfiguredBoatPassenger(
            ServerPlayer vehiclePlayer,
            Entity entity,
            BoatPassengerSettingsState settings,
            double radiusSquared
    ) {
        return entity.isAlive()
                && !entity.isRemoved()
                && !entity.isPassenger()
                && !isAutoRideTemporarilyBlocked(entity)
                && settings.allowsAutoPickUp(entity)
                && vehiclePlayer.distanceToSqr(entity) <= radiusSquared;
    }

    public static boolean forceDismountPassengersFromHotkey(
            ServerPlayer vehiclePlayer,
            boolean useChargedLaunch,
            double chargedLaunchSpeed
    ) {
        if (vehiclePlayer == null || vehiclePlayer.getPassengers().isEmpty()) {
            return false;
        }

        BoatPassengerSettingsState settings = BoatPassengerSettingsSyncManager.getGlobalSettings();

        for (Entity passenger : java.util.List.copyOf(vehiclePlayer.getPassengers())) {
            passenger.stopRiding();
            applyAutoRideCooldown(passenger, HOTKEY_DISMOUNT_AUTO_RIDE_COOLDOWN_TICKS);

            Vec3 launchVelocity = getHotkeyDismountLaunchVelocity(
                    vehiclePlayer,
                    settings,
                    passenger,
                    useChargedLaunch,
                    chargedLaunchSpeed
            );
            if (launchVelocity.lengthSqr() > 0.0D) {
                passenger.setDeltaMovement(passenger.getDeltaMovement().add(launchVelocity));

                if (passenger instanceof ServerPlayer serverPlayerPassenger) {
                    serverPlayerPassenger.connection.send(new ClientboundSetEntityMotionPacket(serverPlayerPassenger));
                }
            }
        }

        return true;
    }

    public static boolean forceDismountPassengers(ServerPlayer vehiclePlayer, String reason) {
        if (vehiclePlayer == null || vehiclePlayer.getPassengers().isEmpty()) {
            return false;
        }

        for (Entity passenger : java.util.List.copyOf(vehiclePlayer.getPassengers())) {
            passenger.stopRiding();
        }

        return true;
    }

    public static void onPassengerRequestedSelfDismount(ServerPlayer passengerPlayer) {
        if (passengerPlayer == null || !(passengerPlayer.getVehicle() instanceof ServerPlayer)) {
            return;
        }

        applyAutoRideCooldown(
                passengerPlayer,
                PASSENGER_SELF_DISMOUNT_AUTO_RIDE_COOLDOWN_TICKS
        );
    }

    private static boolean isAutoRideTemporarilyBlocked(Entity entity) {
        return RideRuntimeSessionState.isAutoRideCooldownActive(entity.getUUID());
    }

    private static void maybeApplyPassengerSelfDismountCooldown(
            ServerPlayer player,
            RideSnapshot previous,
            RideSnapshot current
    ) {
        if (previous == null
                || !previous.hasVehicle()
                || !previous.vehicleIsPlayer()
                || current.hasVehicle()
                || !player.isCrouching()) {
            return;
        }

        applyAutoRideCooldown(player, PASSENGER_SELF_DISMOUNT_AUTO_RIDE_COOLDOWN_TICKS);
    }

    private static void applyAutoRideCooldown(Entity entity, long durationTicks) {
        RideRuntimeSessionState.applyAutoRideCooldown(entity.getUUID(), durationTicks);
    }

    private static Vec3 getHotkeyDismountLaunchVelocity(
            ServerPlayer vehiclePlayer,
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
            return Vec3.ZERO;
        }

        return vehiclePlayer.getViewVector(1.0F).scale(speed);
    }

    private static void sendVehicleClientSync(ServerPlayer vehiclePlayer, RideSnapshot previous, RideSnapshot current) {
        int[] previousIds = previous == null ? new int[0] : previous.passengerIds();
        int[] currentIds = current.passengerIds();

        var buf = PlatformNetworking.createBuffer(vehiclePlayer.registryAccess());
        for (int passengerId : currentIds) {
            if (!contains(previousIds, passengerId)) {
                buf.clear();
                new RideStateSyncPayload(vehiclePlayer.getId(), passengerId, true).write(buf);
                PlatformNetworking.sendToClient(RideStateSyncPayload.ID, buf, vehiclePlayer);
            }
        }

        for (int passengerId : previousIds) {
            if (!contains(currentIds, passengerId)) {
                buf.clear();
                new RideStateSyncPayload(vehiclePlayer.getId(), passengerId, false).write(buf);
                PlatformNetworking.sendToClient(RideStateSyncPayload.ID, buf, vehiclePlayer);
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
