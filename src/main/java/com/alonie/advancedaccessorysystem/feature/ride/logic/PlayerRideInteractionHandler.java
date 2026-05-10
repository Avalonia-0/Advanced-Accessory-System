package com.alonie.advancedaccessorysystem.feature.ride.logic;

import com.alonie.advancedaccessorysystem.feature.headshulker.logic.ShulkerBoxCompat;
import com.alonie.advancedaccessorysystem.feature.ride.rules.RideAccessoryHelper;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public final class PlayerRideInteractionHandler {
    private static final double MAX_RIDE_DISTANCE_SQUARED = 25.0D;
    private static final int MAX_PASSENGERS = 1;
    private static final double MAX_SHULKER_DISTANCE_SQUARED = 25.0D;

    private PlayerRideInteractionHandler() {
    }

    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient()) {
                return ActionResult.PASS;
            }
            if (hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }
            if (!(player instanceof ServerPlayerEntity rider)) {
                return ActionResult.PASS;
            }
            if (!(entity instanceof ServerPlayerEntity target)) {
                return ActionResult.PASS;
            }

            if (canRide(rider, target)) {
                boolean mounted = executeRideCommand(rider, target);
                return mounted ? ActionResult.SUCCESS : ActionResult.PASS;
            }

            if (canOpenTargetHeadShulker(rider, target)) {
                boolean opened = ShulkerBoxCompat.openTargetHeadShulker(rider, target);
                return opened ? ActionResult.SUCCESS : ActionResult.PASS;
            }

            return ActionResult.PASS;
        });
    }

    private static boolean executeRideCommand(ServerPlayerEntity rider, ServerPlayerEntity target) {
        ServerCommandSource baseSource = rider.getCommandSource();
        if (baseSource == null) {
            return false;
        }

        var server = baseSource.getServer();
        if (server == null) {
            return false;
        }

        String command = "ride " + rider.getUuidAsString() + " mount " + target.getUuidAsString();
        ServerCommandSource source = baseSource.withSilent();

        try {
            server.getCommandManager().parseAndExecute(source, command);
            return rider.getVehicle() == target;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean canRide(ServerPlayerEntity rider, ServerPlayerEntity target) {
        if (rider == target) {
            return false;
        }
        if (rider.isSneaking()) {
            return false;
        }
        if (!rider.isAlive() || !target.isAlive()) {
            return false;
        }
        if (rider.isSpectator() || target.isSpectator()) {
            return false;
        }
        if (target.isSleeping()) {
            return false;
        }
        if (rider.hasVehicle()) {
            return false;
        }
        int passengerCount = target.getPassengerList().size();
        if (passengerCount >= MAX_PASSENGERS) {
            return false;
        }
        double distanceSquared = rider.squaredDistanceTo(target);
        if (distanceSquared > MAX_RIDE_DISTANCE_SQUARED) {
            return false;
        }
        if (!isRideableByRideAccessory(target)) {
            return false;
        }

        return true;
    }


    private static boolean canOpenTargetHeadShulker(ServerPlayerEntity viewer, ServerPlayerEntity target) {
        if (viewer == target) {
            return false;
        }
        if (viewer.isSneaking()) {
            return false;
        }
        if (!viewer.isAlive() || !target.isAlive()) {
            return false;
        }
        if (viewer.isSpectator() || target.isSpectator()) {
            return false;
        }
        if (target.isSleeping()) {
            return false;
        }
        double distanceSquared = viewer.squaredDistanceTo(target);
        if (distanceSquared > MAX_SHULKER_DISTANCE_SQUARED) {
            return false;
        }
        if (isRideableByRideAccessory(target)) {
            return false;
        }
        return ShulkerBoxCompat.targetHasHeadShulker(target);
    }

    private static boolean isRideableByRideAccessory(PlayerEntity player) {
        return RideAccessoryHelper.hasRideAccessory(player);
    }
}
