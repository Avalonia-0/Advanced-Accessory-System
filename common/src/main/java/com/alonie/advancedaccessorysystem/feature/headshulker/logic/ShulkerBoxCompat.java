package com.alonie.advancedaccessorysystem.feature.headshulker.logic;

import com.alonie.advancedaccessorysystem.feature.accessory.capability.AccessoryCapability;
import com.alonie.advancedaccessorysystem.feature.accessory.capability.HeadAccessoryCapabilities;
import com.alonie.advancedaccessorysystem.feature.accessory.slot.AccessorySlotRegistry;
import com.alonie.advancedaccessorysystem.feature.accessory.slot.AccessorySlotRegistry.SlotQueryResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;

public final class ShulkerBoxCompat {
    private ShulkerBoxCompat() {
    }

    public static boolean isModAvailable() {
        return true;
    }

    public static ItemStack getEquippedHeadShulker(ServerPlayer player) {
        return AccessorySlotRegistry.findFirst(player, ShulkerBoxCompat::isShulkerBox);
    }

    public static boolean isShulkerBox(ItemStack stack) {
        return HeadAccessoryCapabilities.supports(stack, AccessoryCapability.HEAD_SHULKER_STORAGE);
    }

    public static boolean openHeadEquippedShulker(ServerPlayer player) {
        return openTargetHeadShulker(player, player);
    }

    public static boolean targetHasHeadShulker(ServerPlayer target) {
        if (target == null) {
            return false;
        }
        return AccessorySlotRegistry.anyMatch(target, ShulkerBoxCompat::isShulkerBox);
    }

    public static boolean openTargetHeadShulker(ServerPlayer viewer, ServerPlayer target) {
        if (viewer == null || target == null) {
            return false;
        }

        var result = AccessorySlotRegistry.findFirstWithSlot(target, ShulkerBoxCompat::isShulkerBox);
        if (result.isEmpty()) {
            return false;
        }

        SlotQueryResult match = result.get();
        return CosmeticHeadShulkerBridge.openVanilla(viewer, target, match.stack(), match.provider());
    }

    public static boolean open(ServerPlayer player, ItemStack stack) {
        if (player == null || !isShulkerBox(stack)) {
            return false;
        }
        return CosmeticHeadShulkerBridge.openVanilla(player, player, stack);
    }
}
