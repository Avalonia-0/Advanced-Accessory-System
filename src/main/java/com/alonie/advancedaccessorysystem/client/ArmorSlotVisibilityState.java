package com.alonie.advancedaccessorysystem.client;

import net.minecraft.entity.EquipmentSlot;

import java.util.EnumMap;
import java.util.Map;

public final class ArmorSlotVisibilityState {
    private static final Map<EquipmentSlot, Boolean> HIDDEN_SLOTS = new EnumMap<>(EquipmentSlot.class);

    private ArmorSlotVisibilityState() {
    }

    public static boolean isSupported(EquipmentSlot slot) {
        return slot == EquipmentSlot.HEAD
                || slot == EquipmentSlot.CHEST
                || slot == EquipmentSlot.LEGS
                || slot == EquipmentSlot.FEET;
    }

    public static boolean isHidden(EquipmentSlot slot) {
        if (!isSupported(slot)) {
            return false;
        }
        return Boolean.TRUE.equals(HIDDEN_SLOTS.get(slot));
    }

    public static boolean toggle(EquipmentSlot slot) {
        if (!isSupported(slot)) {
            return false;
        }

        boolean next = !isHidden(slot);
        HIDDEN_SLOTS.put(slot, next);
        return next;
    }

    public static void setHidden(EquipmentSlot slot, boolean hidden) {
        if (!isSupported(slot)) {
            return;
        }

        if (hidden) {
            HIDDEN_SLOTS.put(slot, true);
        } else {
            HIDDEN_SLOTS.remove(slot);
        }
    }
}
