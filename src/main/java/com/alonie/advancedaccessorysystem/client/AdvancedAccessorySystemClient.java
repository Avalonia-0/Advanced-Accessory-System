package com.alonie.advancedaccessorysystem.client;

import com.alonie.advancedaccessorysystem.compat.CosmeticArmorCompat;
import com.alonie.advancedaccessorysystem.client.network.RideStateSyncClient;
import net.fabricmc.api.ClientModInitializer;

public final class AdvancedAccessorySystemClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CosmeticArmorCompat.initClient();
        RideStateSyncClient.register();
    }
}
