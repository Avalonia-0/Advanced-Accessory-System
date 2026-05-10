package com.alonie.advancedaccessorysystem.compat;

import com.alonie.advancedaccessorysystem.compat.api.HeadAccessoryCompatBridge;
import com.alonie.advancedaccessorysystem.compat.impl.cosmeticarmor.CosmeticArmorHeadAccessoryBridge;

public final class CompatServices {
    private static final HeadAccessoryCompatBridge HEAD_ACCESSORY_BRIDGE = new CosmeticArmorHeadAccessoryBridge();

    private CompatServices() {
    }

    public static HeadAccessoryCompatBridge headAccessoryBridge() {
        return HEAD_ACCESSORY_BRIDGE;
    }
}
