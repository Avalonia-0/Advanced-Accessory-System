package com.alonie.advancedaccessorysystem.compat.trinkets;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * Bridge to check for head-slot items in the Trinkets hat slot.
 * On Fabric with Trinkets installed, the actual implementation queries
 * the Trinkets API. On NeoForge the bridge is never registered and
 * TrinketsHatBridgeImpl.isAvailable() returns false.
 */
public interface TrinketsHatBridge {
    /**
     * @return the item stack in the Trinkets hat slot (head/hat), or EMPTY if none
     */
    ItemStack getHatStack(LivingEntity entity);

    /**
     * Removes the first stack matching the predicate from the Trinkets hat slot.
     * @return true if a stack was removed
     */
    boolean clearHatStack(LivingEntity entity, Predicate<ItemStack> predicate);

    /**
     * Sets the stack at index 0 of the Trinkets hat slot.
     */
    void setHatStack(LivingEntity entity, ItemStack stack);
}
