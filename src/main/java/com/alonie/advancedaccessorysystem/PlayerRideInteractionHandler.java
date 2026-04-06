package com.alonie.advancedaccessorysystem;

import com.alonie.advancedaccessorysystem.compat.CosmeticArmorCompat;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PlayerRideInteractionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("AdvancedAccessorySystem/RideDebug");
    private static final double MAX_RIDE_DISTANCE_SQUARED = 25.0D;
    private static final int MAX_PASSENGERS = 1;

    private PlayerRideInteractionHandler() {
    }

    public static void register() {
        LOGGER.info("Registering player ride interaction handler.");
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            LOGGER.info(
                    "UseEntityCallback fired: worldIsClient={}, hand={}, player={}, targetEntityType={}, targetEntityClass={}",
                    world.isClient(),
                    hand,
                    describePlayer(player),
                    entity.getType(),
                    entity.getClass().getName()
            );

            if (world.isClient()) {
                LOGGER.info("Ride attempt ignored: client side.");
                return ActionResult.PASS;
            }
            if (hand != Hand.MAIN_HAND) {
                LOGGER.info("Ride attempt ignored: not main hand ({}).", hand);
                return ActionResult.PASS;
            }
            if (!(player instanceof ServerPlayerEntity rider)) {
                LOGGER.info("Ride attempt ignored: source player is not ServerPlayerEntity ({}).", player.getClass().getName());
                return ActionResult.PASS;
            }
            if (!(entity instanceof ServerPlayerEntity target)) {
                LOGGER.info("Ride attempt ignored: target is not ServerPlayerEntity ({}).", entity.getClass().getName());
                return ActionResult.PASS;
            }
            if (!canRide(rider, target)) {
                LOGGER.info("Ride attempt rejected by canRide(). rider={}, target={}", describePlayer(rider), describePlayer(target));
                return ActionResult.PASS;
            }

            boolean mounted = executeRideCommand(rider, target);
            LOGGER.info("Ride attempt executed. rider={}, target={}, mounted={}", describePlayer(rider), describePlayer(target), mounted);
            return mounted ? ActionResult.SUCCESS : ActionResult.PASS;
        });
    }

    private static boolean executeRideCommand(ServerPlayerEntity rider, ServerPlayerEntity target) {
        ServerCommandSource baseSource = rider.getCommandSource();
        if (baseSource == null) {
            LOGGER.info("Ride command skipped: rider command source is null. rider={}", describePlayer(rider));
            return false;
        }

        var server = baseSource.getServer();
        if (server == null) {
            LOGGER.info("Ride command skipped: command source server is null. rider={}", describePlayer(rider));
            return false;
        }

        String command = "ride " + rider.getUuidAsString() + " mount " + target.getUuidAsString();
        ServerCommandSource source = baseSource.withSilent();

        LOGGER.info("Executing ride command: {}", command);
        try {
            server.getCommandManager().parseAndExecute(source, command);
            boolean mounted = rider.getVehicle() == target;
            LOGGER.info(
                    "Ride command completed. rider={}, target={}, vehicleAfter={}, mounted={}",
                    describePlayer(rider),
                    describePlayer(target),
                    rider.getVehicle() == null ? "null" : rider.getVehicle().getType().toString(),
                    mounted
            );
            return mounted;
        } catch (Exception e) {
            LOGGER.error("Ride command failed. rider={}, target={}, command={}", describePlayer(rider), describePlayer(target), command, e);
            return false;
        }
    }

    private static boolean canRide(ServerPlayerEntity rider, ServerPlayerEntity target) {
        if (rider == target) {
            LOGGER.info("canRide=false: rider and target are the same player ({})", describePlayer(rider));
            return false;
        }
        if (rider.isSneaking()) {
            LOGGER.info("canRide=false: rider is sneaking ({})", describePlayer(rider));
            return false;
        }
        if (!rider.isAlive() || !target.isAlive()) {
            LOGGER.info("canRide=false: riderAlive={}, targetAlive={}", rider.isAlive(), target.isAlive());
            return false;
        }
        if (rider.isSpectator() || target.isSpectator()) {
            LOGGER.info("canRide=false: riderSpectator={}, targetSpectator={}", rider.isSpectator(), target.isSpectator());
            return false;
        }
        if (target.isSleeping()) {
            LOGGER.info("canRide=false: target is sleeping ({})", describePlayer(target));
            return false;
        }
        if (rider.hasVehicle()) {
            LOGGER.info("canRide=false: riderHasVehicle=true, targetHasVehicle={}", target.hasVehicle());
            return false;
        }
        int passengerCount = target.getPassengerList().size();
        if (passengerCount >= MAX_PASSENGERS) {
            LOGGER.info("canRide=false: target passenger count limit reached. passengers={}, max={}", passengerCount, MAX_PASSENGERS);
            return false;
        }
        double distanceSquared = rider.squaredDistanceTo(target);
        if (distanceSquared > MAX_RIDE_DISTANCE_SQUARED) {
            LOGGER.info("canRide=false: target too far. distanceSquared={}, maxDistanceSquared={}", distanceSquared, MAX_RIDE_DISTANCE_SQUARED);
            return false;
        }
        if (!isRideableBySaddle(target)) {
            LOGGER.info("canRide=false: target does not have a saddle in vanilla head slot or cosmetic head slot. target={}", describePlayer(target));
            return false;
        }

        LOGGER.info("canRide=true: rider={}, target={}", describePlayer(rider), describePlayer(target));
        return true;
    }

    private static boolean isRideableBySaddle(PlayerEntity player) {
        ItemStack vanillaHead = player.getEquippedStack(EquipmentSlot.HEAD);
        ItemStack cosmeticHead = CosmeticArmorCompat.getCosmeticHeadStack(player);
        boolean vanilla = isSaddle(vanillaHead);
        boolean cosmetic = isSaddle(cosmeticHead);

        LOGGER.info(
                "Saddle check for {}: vanillaHead={}, cosmeticHead={}, vanillaMatch={}, cosmeticMatch={}",
                describePlayer(player),
                describeStack(vanillaHead),
                describeStack(cosmeticHead),
                vanilla,
                cosmetic
        );

        return vanilla || cosmetic;
    }

    private static boolean isSaddle(ItemStack stack) {
        return !stack.isEmpty() && stack.isOf(Items.SADDLE);
    }

    private static String describePlayer(PlayerEntity player) {
        if (player == null) {
            return "null-player";
        }
        return player.getName().getString() + "[uuid=" + player.getUuidAsString() + "]";
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
}
