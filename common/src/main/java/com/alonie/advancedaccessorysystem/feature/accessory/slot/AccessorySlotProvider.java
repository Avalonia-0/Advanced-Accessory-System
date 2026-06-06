package com.alonie.advancedaccessorysystem.feature.accessory.slot;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * A single logical slot that can hold an accessory item.
 *
 * <p>Implementations wrap a concrete slot location (e.g. vanilla
 * {@code EquipmentSlot.HEAD}, a Trinkets {@code head/hat} slot).
 *
 * <p>Each registered provider is queried in registration order by the
 * aggregate methods in {@link AccessorySlotRegistry}.
 */
public interface AccessorySlotProvider {

    /** Human-readable key for registration and debug (e.g. "vanilla_head", "trinkets_hat"). */
    String name();

    /** Read the current stack in this slot. Never null. */
    ItemStack getStack(LivingEntity entity);

    /** Replace the stack in this slot. Returns true if the write succeeded. */
    boolean setStack(LivingEntity entity, ItemStack stack);

    /**
     * Clear the slot if the current stack matches the predicate.
     * Returns true if the slot was cleared.
     */
    boolean clearIf(LivingEntity entity, Predicate<ItemStack> predicate);

    /**
     * Whether this slot contributes a visible model on the player.
     * Vanilla HEAD returns true; third-party slots typically return false
     * (they have their own render pipeline).
     */
    default boolean providesRender() {
        return false;
    }
}
