package com.alonie.advancedaccessorysystem.bootstrap;

import com.alonie.advancedaccessorysystem.feature.boatpassenger.client.sync.BoatPassengerSettingsSyncClient;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.sync.BoatPassengerSettingsSyncManager;

public final class BoatPassengerBootstrap {
    private BoatPassengerBootstrap() {
    }

    public static void registerCommon() {
        BoatPassengerSettingsSyncManager.register();
    }

    public static void registerClient() {
        BoatPassengerSettingsSyncClient.register();
    }
}
