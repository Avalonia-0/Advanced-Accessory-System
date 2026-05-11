package com.alonie.advancedaccessorysystem.feature.ride.rules;

import com.alonie.advancedaccessorysystem.feature.accessory.capability.AccessoryCapability;
import com.alonie.advancedaccessorysystem.feature.accessory.capability.HeadAccessoryCapabilities;
import com.alonie.advancedaccessorysystem.compat.CompatServices;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public final class RideAccessoryHelper {
    private RideAccessoryHelper() {
    }

    public static boolean hasRideAccessory(PlayerEntity player) {
        ItemStack vanillaHead = player.getEquippedStack(EquipmentSlot.HEAD);
        ItemStack cosmeticHead = CompatServices.headAccessoryBridge().getCosmeticHeadStack(player);
        return isRideAccessory(vanillaHead) || isRideAccessory(cosmeticHead);
    }

    public static boolean hasBoatAccessory(PlayerEntity player) {
        ItemStack vanillaHead = player.getEquippedStack(EquipmentSlot.HEAD);
        ItemStack cosmeticHead = CompatServices.headAccessoryBridge().getCosmeticHeadStack(player);
        return isBoatAccessory(vanillaHead) || isBoatAccessory(cosmeticHead);
    }

    public static boolean supportsPassengers(PlayerEntity player) {
        return hasRideAccessory(player) || hasBoatAccessory(player);
    }

    public static boolean isPassengerSupportAccessory(ItemStack stack) {
        return HeadAccessoryCapabilities.supportsPassengerHandling(stack);
    }

    public static boolean destroyBoatAccessory(PlayerEntity player) {
        if (player == null) {
            return false;
        }

        ItemStack vanillaHead = player.getEquippedStack(EquipmentSlot.HEAD);
        if (isBoatAccessory(vanillaHead)) {
            player.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
            return true;
        }

        return CompatServices.headAccessoryBridge().clearCosmeticHeadStack(player, RideAccessoryHelper::isBoatAccessory);
    }

    public static boolean destroyPassengerSupportAccessory(PlayerEntity player) {
        if (player == null) {
            return false;
        }

        ItemStack vanillaHead = player.getEquippedStack(EquipmentSlot.HEAD);
        if (isPassengerSupportAccessory(vanillaHead)) {
            player.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
            return true;
        }

        return CompatServices.headAccessoryBridge().clearCosmeticHeadStack(player, RideAccessoryHelper::isPassengerSupportAccessory);
    }

    public static boolean isRideAccessory(ItemStack stack) {
        return HeadAccessoryCapabilities.supports(stack, AccessoryCapability.PLAYER_RIDE);
    }

    public static boolean isBoatAccessory(ItemStack stack) {
        return HeadAccessoryCapabilities.supports(stack, AccessoryCapability.BOAT_PASSENGER);
    }
}
