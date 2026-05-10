package com.alonie.advancedaccessorysystem.feature.headshulker.logic;

import com.alonie.advancedaccessorysystem.compat.CompatServices;
import com.alonie.advancedaccessorysystem.compat.api.HeadAccessorySlotRef;
import com.alonie.advancedaccessorysystem.feature.accessory.capability.AccessoryCapability;
import com.alonie.advancedaccessorysystem.feature.accessory.capability.HeadAccessoryCapabilities;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ShulkerBoxCompat {
    private ShulkerBoxCompat() {
    }

    public static boolean isModAvailable() {
        return true;
    }

    public static ItemStack getEquippedHeadShulker(ServerPlayerEntity player) {
        ItemStack vanillaHead = player.getEquippedStack(EquipmentSlot.HEAD);
        if (isShulkerBox(vanillaHead)) {
            return vanillaHead;
        }

        ItemStack cosmeticHead = CompatServices.headAccessoryBridge().getCosmeticHeadStack(player);
        return isShulkerBox(cosmeticHead) ? cosmeticHead : ItemStack.EMPTY;
    }

    public static ItemStack getAllowedOpenStack(ServerPlayerEntity player) {
        return ItemStack.EMPTY;
    }

    public static void clearAllowedOpenStack(ServerPlayerEntity player) {
    }

    public static boolean isShulkerBox(ItemStack stack) {
        return HeadAccessoryCapabilities.supports(stack, AccessoryCapability.HEAD_SHULKER_STORAGE);
    }

    public static boolean openHeadEquippedShulker(ServerPlayerEntity player) {
        return openTargetHeadShulker(player, player);
    }

    public static boolean targetHasHeadShulker(ServerPlayerEntity target) {
        if (target == null) {
            return false;
        }
        if (isShulkerBox(target.getEquippedStack(EquipmentSlot.HEAD))) {
            return true;
        }
        HeadAccessorySlotRef cosmeticRef = CompatServices.headAccessoryBridge().getCosmeticHeadSlotRef(target);
        ItemStack cosmeticHead = cosmeticRef == null ? ItemStack.EMPTY : cosmeticRef.stack();
        return isShulkerBox(cosmeticHead);
    }

    public static boolean openTargetHeadShulker(ServerPlayerEntity viewer, ServerPlayerEntity target) {
        if (viewer == null || target == null) {
            return false;
        }

        ItemStack vanillaHead = target.getEquippedStack(EquipmentSlot.HEAD);
        if (isShulkerBox(vanillaHead)) {
            return CosmeticHeadShulkerBridge.openVanilla(viewer, target, vanillaHead);
        }

        HeadAccessorySlotRef cosmeticRef = CompatServices.headAccessoryBridge().getCosmeticHeadSlotRef(target);
        ItemStack cosmeticHead = cosmeticRef == null ? ItemStack.EMPTY : cosmeticRef.stack();
        if (isShulkerBox(cosmeticHead)) {
            return CosmeticHeadShulkerBridge.open(viewer, target, cosmeticRef);
        }

        return false;
    }

    public static boolean open(ServerPlayerEntity player, ItemStack stack) {
        if (player == null || !isShulkerBox(stack)) {
            return false;
        }
        return CosmeticHeadShulkerBridge.openVanilla(player, player, stack);
    }
}
