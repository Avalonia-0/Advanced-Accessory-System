package com.alonie.advancedaccessorysystem.feature.ride.rules;

import com.alonie.advancedaccessorysystem.feature.accessory.capability.AccessoryCapability;
import com.alonie.advancedaccessorysystem.feature.accessory.capability.HeadAccessoryCapabilities;
import com.alonie.advancedaccessorysystem.feature.accessory.slot.AccessorySlotRegistry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class RideAccessoryHelper {
    private RideAccessoryHelper() {
    }

    public static boolean hasRideAccessory(Player player) {
        return AccessorySlotRegistry.anyMatch(player, RideAccessoryHelper::isRideAccessory);
    }

    public static boolean hasBoatAccessory(Player player) {
        return AccessorySlotRegistry.anyMatch(player, RideAccessoryHelper::isBoatAccessory);
    }

    public static boolean supportsPassengers(Player player) {
        return hasRideAccessory(player) || hasBoatAccessory(player);
    }

    public static boolean isPassengerSupportAccessory(ItemStack stack) {
        return HeadAccessoryCapabilities.supportsPassengerHandling(stack);
    }

    public static boolean destroyBoatAccessory(Player player) {
        if (player == null) {
            return false;
        }
        return AccessorySlotRegistry.clearFirst(player, RideAccessoryHelper::isBoatAccessory);
    }

    public static boolean destroyPassengerSupportAccessory(Player player) {
        if (player == null) {
            return false;
        }
        return AccessorySlotRegistry.clearFirst(player, RideAccessoryHelper::isPassengerSupportAccessory);
    }

    public static boolean isRideAccessory(ItemStack stack) {
        return HeadAccessoryCapabilities.supports(stack, AccessoryCapability.PLAYER_RIDE);
    }

    public static boolean isBoatAccessory(ItemStack stack) {
        return HeadAccessoryCapabilities.supports(stack, AccessoryCapability.BOAT_PASSENGER);
    }
}
