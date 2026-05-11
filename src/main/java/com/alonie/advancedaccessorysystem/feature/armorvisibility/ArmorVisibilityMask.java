package com.alonie.advancedaccessorysystem.feature.armorvisibility;

import net.minecraft.entity.EquipmentSlot;

public final class ArmorVisibilityMask {
    public static final int HEAD = 1;
    public static final int CHEST = 1 << 1;
    public static final int LEGS = 1 << 2;
    public static final int FEET = 1 << 3;
    public static final int ALL = HEAD | CHEST | LEGS | FEET;

    private ArmorVisibilityMask() {
    }

    public static int getBit(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> HEAD;
            case CHEST -> CHEST;
            case LEGS -> LEGS;
            case FEET -> FEET;
            default -> 0;
        };
    }

    public static boolean isSupported(EquipmentSlot slot) {
        return getBit(slot) != 0;
    }

    public static int sanitize(int mask) {
        return mask & ALL;
    }

    public static boolean isHidden(int mask, EquipmentSlot slot) {
        int bit = getBit(slot);
        return bit != 0 && (sanitize(mask) & bit) != 0;
    }
}
