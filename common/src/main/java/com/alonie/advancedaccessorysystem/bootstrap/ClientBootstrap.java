package com.alonie.advancedaccessorysystem.bootstrap;

import com.alonie.advancedaccessorysystem.client.config.AdvancedAccessorySystemConfigs;
import com.alonie.advancedaccessorysystem.bridge.PlatformLifecycle;

public final class ClientBootstrap {
    private ClientBootstrap() {
    }

    public static void register() {
        AdvancedAccessorySystemConfigs.init();
        PlatformLifecycle.registerClient();
        ArmorVisibilityBootstrap.registerClient();
        BoatPassengerBootstrap.registerClient();
        RideBootstrap.registerClient();
    }
}
