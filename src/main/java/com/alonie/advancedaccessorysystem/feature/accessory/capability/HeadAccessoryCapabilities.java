package com.alonie.advancedaccessorysystem.feature.accessory.capability;

import com.alonie.advancedaccessorysystem.feature.accessory.state.AccessoryPatternRuntimeState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.EnumSet;

/**
 * Central resolver for head-accessory behavior tags.
 * The goal is to keep "what item is this?" separate from
 * "which systems should react to it?".
 */
public final class HeadAccessoryCapabilities {
    private HeadAccessoryCapabilities() {
    }

    public static EnumSet<AccessoryCapability> resolve(ItemStack stack) {
        EnumSet<AccessoryCapability> capabilities = EnumSet.noneOf(AccessoryCapability.class);
        if (stack == null || stack.isEmpty()) {
            return capabilities;
        }

        Identifier itemId = Registries.ITEM.getId(stack.getItem());
        if (AccessoryPatternRuntimeState.matchesSaddle(itemId)) {
            capabilities.add(AccessoryCapability.PLAYER_RIDE);
        }
        if (AccessoryPatternRuntimeState.matchesBoat(itemId)) {
            capabilities.add(AccessoryCapability.BOAT_PASSENGER);
        }
        if (isHeadShulkerStorage(stack)) {
            capabilities.add(AccessoryCapability.HEAD_SHULKER_STORAGE);
        }
        return capabilities;
    }

    public static boolean supports(ItemStack stack, AccessoryCapability capability) {
        return resolve(stack).contains(capability);
    }

    public static boolean supportsPassengerHandling(ItemStack stack) {
        EnumSet<AccessoryCapability> capabilities = resolve(stack);
        return capabilities.contains(AccessoryCapability.PLAYER_RIDE)
                || capabilities.contains(AccessoryCapability.BOAT_PASSENGER);
    }

    private static boolean isHeadShulkerStorage(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        return blockItem.getBlock() instanceof ShulkerBoxBlock;
    }
}
