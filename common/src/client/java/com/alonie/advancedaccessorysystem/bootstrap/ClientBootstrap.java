package com.alonie.advancedaccessorysystem.bootstrap;

import com.alonie.advancedaccessorysystem.bridge.PlatformLifecycleClient;
import com.alonie.advancedaccessorysystem.bridge.PlatformNetworkingClient;
import com.alonie.advancedaccessorysystem.client.config.AdvancedAccessorySystemConfigs;
import com.alonie.advancedaccessorysystem.feature.boatpassenger.client.sync.BoatPassengerSettingsSyncClient;

public final class ClientBootstrap {
    private ClientBootstrap() {
    }

    public static void register() {
        // Client-side config and networking
        AdvancedAccessorySystemConfigs.init();
        PlatformNetworkingClient.registerS2C();

        // Client lifecycle events (ticks, world load, keybinds)
        PlatformLifecycleClient.registerClient();

        // Client feature init
        BoatPassengerSettingsSyncClient.init();
    }
}
