package com.alonie.advancedaccessorysystem.compat.api;

import net.minecraft.item.ItemStack;

public record HeadAccessorySlotRef(Object inventory, int slotIndex, ItemStack stack) {
}
