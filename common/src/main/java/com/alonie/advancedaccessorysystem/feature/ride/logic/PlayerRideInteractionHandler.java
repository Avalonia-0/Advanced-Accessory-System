package com.alonie.advancedaccessorysystem.feature.ride.logic;

import com.alonie.advancedaccessorysystem.feature.accessory.slot.AccessorySlotRegistry;
import com.alonie.advancedaccessorysystem.feature.headshulker.logic.ShulkerBoxCompat;
import com.alonie.advancedaccessorysystem.feature.headslot.rule.AllItemsHeadEquippablePatch;
import com.alonie.advancedaccessorysystem.feature.ride.rules.RideAccessoryHelper;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

public final class PlayerRideInteractionHandler {
    private static final double MAX_RIDE_DISTANCE_SQUARED = 25.0D;
    private static final int MAX_PASSENGERS = 1;
    private static final double MAX_SHULKER_DISTANCE_SQUARED = 25.0D;

    private PlayerRideInteractionHandler() {
    }

    public static void register() {
        InteractionEvent.INTERACT_ENTITY.register((player, entity, hand) -> {
            if (player.level().isClientSide()) {
                return EventResult.pass();
            }
            if (hand != InteractionHand.MAIN_HAND) {
                return EventResult.pass();
            }
            if (!(player instanceof ServerPlayer rider)) {
                return EventResult.pass();
            }
            if (!(entity instanceof ServerPlayer target)) {
                return EventResult.pass();
            }

            // Shift+right-click auto-equip: give the held item to target's head slot
            if (rider.isCrouching()) {
                ItemStack held = rider.getMainHandItem();
                if (!held.isEmpty() && isHeadEquippable(held)) {
                    if (AccessorySlotRegistry.tryEquip(target, held)) {
                        held.shrink(1);
                        return EventResult.interruptTrue();
                    }
                }
            }

            if (canRide(rider, target)) {
                boolean mounted = executeRideCommand(rider, target);
                return mounted ? EventResult.interruptTrue() : EventResult.pass();
            }

            if (canOpenTargetHeadShulker(rider, target)) {
                boolean opened = ShulkerBoxCompat.openTargetHeadShulker(rider, target);
                return opened ? EventResult.interruptTrue() : EventResult.pass();
            }

            return EventResult.pass();
        });
    }

    private static boolean executeRideCommand(ServerPlayer rider, ServerPlayer target) {
        CommandSourceStack baseSource = rider.createCommandSourceStack();
        if (baseSource == null) {
            return false;
        }

        var server = baseSource.getServer();
        if (server == null) {
            return false;
        }

        String command = "ride " + rider.getStringUUID() + " mount " + target.getStringUUID();
        CommandSourceStack source = baseSource.withSuppressedOutput();

        try {
            server.getCommands().performPrefixedCommand(source, command);
            return rider.getVehicle() == target;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean canRide(ServerPlayer rider, ServerPlayer target) {
        if (rider == target) {
            return false;
        }
        if (rider.isCrouching()) {
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
        if (rider.isPassenger()) {
            return false;
        }
        int passengerCount = target.getPassengers().size();
        if (passengerCount >= MAX_PASSENGERS) {
            return false;
        }
        double distanceSquared = rider.distanceToSqr(target);
        if (distanceSquared > MAX_RIDE_DISTANCE_SQUARED) {
            return false;
        }
        if (!isRideableByRideAccessory(target)) {
            return false;
        }

        return true;
    }


    private static boolean canOpenTargetHeadShulker(ServerPlayer viewer, ServerPlayer target) {
        if (viewer == target) {
            return false;
        }
        if (viewer.isCrouching()) {
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
        double distanceSquared = viewer.distanceToSqr(target);
        if (distanceSquared > MAX_SHULKER_DISTANCE_SQUARED) {
            return false;
        }
        if (isRideableByRideAccessory(target)) {
            return false;
        }
        return ShulkerBoxCompat.targetHasHeadShulker(target);
    }

    private static boolean isRideableByRideAccessory(Player player) {
        return RideAccessoryHelper.supportsPassengers(player);
    }

    /**
     * Checks if an item can be worn on the head — either because it has
     * a natural {@code EQUIPPABLE} component for the HEAD slot (helmet,
     * pumpkin, skull) or because it is recognised by the mod's head slot
     * rules (boat, saddle, shulker box).
     */
    private static boolean isHeadEquippable(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        // Natural head equipment via data component
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.slot() == EquipmentSlot.HEAD) {
            return true;
        }
        // Custom accessory allowed by our head-slot mixin
        return AllItemsHeadEquippablePatch.shouldAllowManualHeadInsert(stack.getItem());
    }
}
