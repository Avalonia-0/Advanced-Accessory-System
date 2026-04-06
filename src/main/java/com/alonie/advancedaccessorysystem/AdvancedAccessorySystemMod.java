package com.alonie.advancedaccessorysystem;

import com.alonie.advancedaccessorysystem.compat.CosmeticArmorCompat;
import com.alonie.advancedaccessorysystem.network.RideStateSyncPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class AdvancedAccessorySystemMod implements ModInitializer {
    public static final String MOD_ID = "advanced-accessory-system";

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(RideStateSyncPayload.ID, RideStateSyncPayload.CODEC);
        AllItemsHeadEquippablePatch.register();
        CosmeticArmorCompat.initCommon();
        PlayerRideInteractionHandler.register();
        PlayerRideSyncManager.register();
    }
}