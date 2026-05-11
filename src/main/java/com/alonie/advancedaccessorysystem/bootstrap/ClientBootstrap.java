package com.alonie.advancedaccessorysystem.bootstrap;

import com.alonie.advancedaccessorysystem.client.config.AdvancedAccessorySystemConfigs;
import com.alonie.advancedaccessorysystem.client.hotkeys.AdvancedAccessorySystemHotkeys;
import com.alonie.advancedaccessorysystem.collective.CollectiveLifecycleBridge;

public final class ClientBootstrap {
    private ClientBootstrap() {
    }

    public static void register() {
        AdvancedAccessorySystemConfigs.init();
        AdvancedAccessorySystemHotkeys.init();
        CollectiveLifecycleBridge.registerClient();
        CompatBootstrap.registerClient();
        ArmorVisibilityBootstrap.registerClient();
        BoatPassengerBootstrap.registerClient();
        RideBootstrap.registerClient();
    }
}
