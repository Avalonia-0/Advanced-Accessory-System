package com.alonie.advancedaccessorysystem.feature.accessory.slot;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * Wraps the vanilla {@link EquipmentSlot#HEAD}.
 * Always available on every platform.
 * Provides rendering on the player model.
 */
public final class VanillaHeadSlotProvider implements AccessorySlotProvider {
    public static final String NAME = "vanilla_head";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public ItemStack getStack(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.HEAD);
    }

    @Override
    public boolean setStack(LivingEntity entity, ItemStack stack) {
        entity.setItemSlot(EquipmentSlot.HEAD, stack == null ? ItemStack.EMPTY : stack);
        return true;
    }

    @Override
    public boolean clearIf(LivingEntity entity, Predicate<ItemStack> predicate) {
        ItemStack current = getStack(entity);
        if (!current.isEmpty() && predicate.test(current)) {
            entity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            return true;
        }
        return false;
    }

    @Override
    public boolean providesRender() {
        return true;
    }
}
