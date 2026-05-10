package com.alonie.advancedaccessorysystem.feature.armorvisibility.client.state;

import com.alonie.advancedaccessorysystem.client.config.AdvancedAccessorySystemConfigs;
import com.alonie.advancedaccessorysystem.feature.armorvisibility.ArmorVisibilityMask;
import net.minecraft.entity.EquipmentSlot;

public final class ArmorSlotVisibilityState {
    private ArmorSlotVisibilityState() {
    }

    public static boolean isSupported(EquipmentSlot slot) {
        return ArmorVisibilityMask.isSupported(slot);
    }

    public static int getLocalMask() {
        int visibilityMask = 0;

        if (AdvancedAccessorySystemConfigs.isArmorHidden(EquipmentSlot.HEAD)) {
            visibilityMask |= ArmorVisibilityMask.HEAD;
        }
        if (AdvancedAccessorySystemConfigs.isArmorHidden(EquipmentSlot.CHEST)) {
            visibilityMask |= ArmorVisibilityMask.CHEST;
        }
        if (AdvancedAccessorySystemConfigs.isArmorHidden(EquipmentSlot.LEGS)) {
            visibilityMask |= ArmorVisibilityMask.LEGS;
        }
        if (AdvancedAccessorySystemConfigs.isArmorHidden(EquipmentSlot.FEET)) {
            visibilityMask |= ArmorVisibilityMask.FEET;
        }

        return visibilityMask;
    }

    public static boolean isHidden(EquipmentSlot slot) {
        return isHidden(getLocalMask(), slot);
    }

    public static boolean isHidden(int visibilityMask, EquipmentSlot slot) {
        return ArmorVisibilityMask.isHidden(visibilityMask, slot);
    }
}
